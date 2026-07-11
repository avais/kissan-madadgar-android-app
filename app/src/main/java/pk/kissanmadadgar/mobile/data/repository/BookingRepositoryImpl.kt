package pk.kissanmadadgar.mobile.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pk.kissanmadadgar.mobile.core.security.SessionManager
import pk.kissanmadadgar.mobile.data.local.dao.BookingDao
import pk.kissanmadadgar.mobile.data.local.entity.BookingEntity
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.domain.repository.BookingRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed, per-account offline cache for bookings. Replaces the old process-lifetime-only
 * InMemoryBookingRepository so "My Bookings" survives app restarts/kills while offline.
 *
 * The one piece of real logic here is [reconcile]: every write that could be merging fresh
 * server data with what's already cached (setBookings — called both after a network fetch and
 * after an optimistic local start/complete) runs each incoming row through a monotonic
 * status-rank check so a stale/racing fetch can never revert a booking that has already
 * progressed further locally (e.g. STARTED_FROM_FARMER_SIDE reverting back to APPROVED).
 */
@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingDao: BookingDao,
    private val sessionManager: SessionManager
) : BookingRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun currentOwnerKey(): String {
        sessionManager.getUserId()?.takeIf { it.isNotBlank() }?.let { return it }
        val guestToken = sessionManager.getGuestToken()?.takeIf { it.isNotBlank() }
        return if (guestToken != null) "guest_$guestToken" else "guest"
    }

    override fun getBookingsForFarmer(farmerId: String): Flow<List<Booking>> {
        // farmerId here is always the caller's own logged-in user id (see
        // MainViewModel.loadRoleSpecificData), which is exactly the owner scoping key.
        return bookingDao.observeBookings(farmerId).map { entities -> entities.map(::toDomain) }
    }

    override fun getBookingsForProvider(providerId: String): Flow<List<Booking>> {
        return bookingDao.observeBookings(providerId).map { entities -> entities.map(::toDomain) }
    }

    override fun observeBookings(ownerKey: String): Flow<List<Booking>> {
        return bookingDao.observeBookings(ownerKey).map { entities -> entities.map(::toDomain) }
    }

    override fun getAllBookingsAdmin(): Flow<List<Booking>> {
        return bookingDao.observeAllBookings().map { entities -> entities.map(::toDomain) }
    }

    override suspend fun createBooking(booking: Booking): Result<Unit> {
        bookingDao.upsertOne(toEntity(booking, currentOwnerKey()))
        return Result.success(Unit)
    }

    override suspend fun updateBooking(booking: Booking): Result<Unit> {
        bookingDao.upsertOne(toEntity(booking, currentOwnerKey()))
        return Result.success(Unit)
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        val existing = bookingDao.getById(bookingId) ?: return Result.failure(Exception("بکنگ نہیں ملی"))
        bookingDao.updateStatusFields(
            bookingId = bookingId,
            status = status.name,
            rentalRequestStatus = existing.rentalRequestStatus,
            rentalRequestStatusUrdu = existing.rentalRequestStatusUrdu,
            cachedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override fun setBookings(bookings: List<Booking>) {
        val owner = currentOwnerKey()
        scope.launch {
            val reconciled = bookings.map { incoming ->
                val incomingEntity = toEntity(incoming, owner)
                reconcile(bookingDao.getById(incoming.id), incomingEntity)
            }
            bookingDao.replaceAllForOwner(owner, reconciled)
        }
    }

    override fun upsertBookings(bookings: List<Booking>) {
        val owner = currentOwnerKey()
        scope.launch {
            val reconciled = bookings.map { incoming ->
                val incomingEntity = toEntity(incoming, owner)
                reconcile(bookingDao.getById(incoming.id), incomingEntity)
            }
            bookingDao.upsertAll(reconciled)
        }
    }

    override fun clearPlaceholderBookings() {
        val owner = currentOwnerKey()
        scope.launch {
            bookingDao.deletePlaceholdersForOwner(owner)
        }
    }

    private fun rankOf(status: String): Int = when (status.uppercase()) {
        "PENDING" -> 0
        "APPROVED" -> 1
        "STARTED_FROM_FARMER_SIDE" -> 2
        "STARTED_FROM_SERVICE_PROVIDER_SIDE" -> 3
        "COMPLETED", "REJECTED" -> 4
        else -> 0
    }

    private fun reconcile(local: BookingEntity?, incoming: BookingEntity): BookingEntity {
        if (local == null) return incoming
        val localRank = rankOf(local.rentalRequestStatus)
        val incomingRank = rankOf(incoming.rentalRequestStatus)
        return when {
            incomingRank > localRank -> incoming
            incomingRank < localRank -> local
            local.rentalRequestStatus.equals(incoming.rentalRequestStatus, ignoreCase = true) -> incoming
            else -> local
        }
    }

    private fun toEntity(booking: Booking, ownerUserId: String): BookingEntity = BookingEntity(
        id = booking.id,
        ownerUserId = ownerUserId,
        farmerId = booking.farmerId,
        farmerName = booking.farmerName,
        farmerPhone = booking.farmerPhone,
        machineryId = booking.machineryId,
        machineryName = booking.machineryName,
        bookingDate = booking.bookingDate,
        durationHours = booking.durationHours,
        totalPrice = booking.totalPrice,
        status = booking.status.name,
        createdAt = booking.createdAt,
        locationUr = booking.locationUr,
        acres = booking.acres,
        rejectionReason = booking.rejectionReason,
        lifecyclePhotos = booking.lifecyclePhotos,
        providerName = booking.providerName,
        providerPhone = booking.providerPhone,
        machineryImageUrl = booking.machineryImageUrl,
        isApprovalAllowed = booking.isApprovalAllowed,
        rentalRequestStatus = booking.rentalRequestStatus,
        rentalRequestStatusUrdu = booking.rentalRequestStatusUrdu,
        serviceProviderId = booking.serviceProviderId,
        serviceTakerId = booking.serviceTakerId,
        isRatingDone = booking.isRatingDone,
        cachedAt = System.currentTimeMillis()
    )

    private fun toDomain(entity: BookingEntity): Booking = Booking(
        id = entity.id,
        farmerId = entity.farmerId,
        farmerName = entity.farmerName,
        farmerPhone = entity.farmerPhone,
        machineryId = entity.machineryId,
        machineryName = entity.machineryName,
        bookingDate = entity.bookingDate,
        durationHours = entity.durationHours,
        totalPrice = entity.totalPrice,
        status = runCatching { BookingStatus.valueOf(entity.status) }.getOrDefault(BookingStatus.PENDING),
        createdAt = entity.createdAt,
        locationUr = entity.locationUr,
        acres = entity.acres,
        rejectionReason = entity.rejectionReason,
        lifecyclePhotos = entity.lifecyclePhotos,
        providerName = entity.providerName,
        providerPhone = entity.providerPhone,
        machineryImageUrl = entity.machineryImageUrl,
        isApprovalAllowed = entity.isApprovalAllowed,
        rentalRequestStatus = entity.rentalRequestStatus,
        rentalRequestStatusUrdu = entity.rentalRequestStatusUrdu,
        serviceProviderId = entity.serviceProviderId,
        serviceTakerId = entity.serviceTakerId,
        isRatingDone = entity.isRatingDone
    )
}
