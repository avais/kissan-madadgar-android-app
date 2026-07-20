package pk.kissanmadadgar.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pk.kissanmadadgar.mobile.domain.model.*
import pk.kissanmadadgar.mobile.data.remote.dto.ImplementDto
import pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryRequest
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryResponse
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenRequest
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenResponse
import pk.kissanmadadgar.mobile.data.remote.dto.MobileProfileResponse
import pk.kissanmadadgar.mobile.data.remote.dto.SupportResponse
import pk.kissanmadadgar.mobile.data.remote.dto.MyMachinesResponseDto

interface AuthRepository {
    suspend fun login(phone: String, cnic: String?, latitude: Double, longitude: Double): Result<String>
    suspend fun verifyOtp(phoneNumber: String, otp: String, role: UserRole, guestToken: String? = null, type: String? = null): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getImplements(): Result<List<ImplementDto>>
    suspend fun getDistricts(): Result<List<DistrictDto>>
    suspend fun registerMachinery(payload: RegisterMachineryRequest): Result<RegisterMachineryResponse>
    suspend fun getGuestToken(request: GuestTokenRequest): Result<GuestTokenResponse>
    suspend fun getProfile(token: String): Result<MobileProfileResponse>
    suspend fun getSupport(module: String? = null): Result<SupportResponse>
    suspend fun getAvailableMachines(
        latitude: Double,
        longitude: Double,
        type: String = "home",
        page: Int = 0,
        size: Int = 5,
        districtId: Int? = null,
        keyword: String? = null
    ): Result<PaginatedMachinery>
    suspend fun getMyMachines(page: Int = 0, size: Int = 10): Result<MyMachinesResponseDto>
    suspend fun getRoute(originLat: Double, originLng: Double, destLat: Double, destLng: Double): Result<RouteInfo>
    suspend fun getHelperVideos(): Result<List<String>>
    suspend fun updateProfile(name: String, cnic: String, address: String, districtId: Long?, mobile: String): Result<Unit>
    suspend fun createRentalBooking(
        fleetId: Long?,
        acres: Double,
        date: String,
        numberOfHours: Double,
        latitude: Double,
        longitude: Double,
        farmingActivityAddress: String
    ): Result<Unit>
    suspend fun getRentalBookings(
        status: String? = null,
        page: Int? = null,
        size: Int? = null,
        id: String? = null,
        keyword: String? = null
    ): Result<pk.kissanmadadgar.mobile.domain.model.PaginatedBookings>
    suspend fun approveBooking(bookingId: String): Result<String>
    suspend fun rejectBooking(bookingId: String, reason: String?): Result<String>
    suspend fun submitBookingFeedback(bookingId: String, rating: Int, comment: String): Result<String>
}

interface MachineryRepository {
    fun getCategories(): Flow<List<Category>>
    fun getAvailableMachinery(): Flow<List<Machinery>>
    fun getMachineryByProvider(providerId: String): Flow<List<Machinery>>
    suspend fun getMachineryById(id: String): Machinery?
    suspend fun addMachinery(machinery: Machinery): Result<Unit>
    suspend fun updateMachineryStatus(id: String, status: MachineryStatus): Result<Unit>
    fun getAllMachineryAdmin(): Flow<List<Machinery>>
}

interface BookingRepository {
    fun getBookingsForFarmer(farmerId: String): Flow<List<Booking>>
    fun getBookingsForProvider(providerId: String): Flow<List<Booking>>
    suspend fun createBooking(booking: Booking): Result<Unit>
    suspend fun updateBooking(booking: Booking): Result<Unit>
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit>
    fun getAllBookingsAdmin(): Flow<List<Booking>>
    // Full-replace for this owner — only appropriate when the caller genuinely means "this IS
    // the complete set now" (e.g. clearing the cache on logout with an empty list). Any other
    // caller computing a partial/merged snapshot from an in-memory list should use
    // upsertBookings instead: two of these racing (e.g. a tab's own status-filtered refresh
    // racing a targeted single-booking fetch) can otherwise silently delete whichever booking
    // isn't in the snapshot that happens to write last.
    fun setBookings(bookings: List<Booking>)
    // Insert-or-update only these rows; never deletes anything else for the owner, so it's safe
    // to call from multiple concurrent fetches without one clobbering another's rows.
    fun upsertBookings(bookings: List<Booking>)
    // Drops client-fabricated "book_..." placeholder rows (see createBooking) once a real,
    // server-confirmed booking has arrived. Only ever touches placeholder ids, so it's safe to
    // call alongside a concurrent upsertBookings without needing to combine them into one
    // snapshot first.
    fun clearPlaceholderBookings()

    // Reactive, per-account cache read: emits whatever is persisted locally for [ownerKey]
    // immediately on subscribe (cache-first), then again whenever the cache is updated.
    fun observeBookings(ownerKey: String): Flow<List<Booking>>
}
