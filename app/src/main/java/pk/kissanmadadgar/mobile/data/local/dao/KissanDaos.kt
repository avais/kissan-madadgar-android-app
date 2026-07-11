package pk.kissanmadadgar.mobile.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pk.kissanmadadgar.mobile.data.local.entity.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM farmers WHERE user_id = :userId")
    suspend fun getFarmerById(userId: String): FarmerEntity?

    @Query("SELECT * FROM providers WHERE user_id = :userId")
    suspend fun getProviderById(userId: String): ProviderEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
}

@Dao
interface MachineryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachinery(machinery: MachineryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM machinery_categories")
    suspend fun getCategories(): List<CategoryEntity>

    @Query("SELECT * FROM machinery WHERE is_available = 1 AND status = 'APPROVED'")
    fun getAvailableMachineryFlow(): Flow<List<MachineryEntity>>

    @Query("SELECT * FROM machinery WHERE provider_id = :providerId")
    fun getMachineryByProviderFlow(providerId: String): Flow<List<MachineryEntity>>

    @Query("SELECT * FROM machinery WHERE id = :id")
    suspend fun getMachineryById(id: String): MachineryEntity?
}

@Dao
interface BookingDao {
    // Reactive read the UI ultimately observes — scoped to the currently logged-in account.
    @Query("SELECT * FROM bookings WHERE owner_user_id = :ownerUserId ORDER BY created_at DESC")
    fun observeBookings(ownerUserId: String): Flow<List<BookingEntity>>

    // Unscoped, for the admin "all bookings" view.
    @Query("SELECT * FROM bookings ORDER BY created_at DESC")
    fun observeAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId LIMIT 1")
    suspend fun getById(bookingId: String): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOne(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bookings: List<BookingEntity>)

    @Query("DELETE FROM bookings WHERE owner_user_id = :ownerUserId")
    suspend fun deleteAllForOwner(ownerUserId: String)

    // createBooking() inserts a client-fabricated placeholder row (id "book_" + a timestamp
    // fragment) so the UI has something to show the instant a booking is submitted, before the
    // server-confirmed row (with its real numeric id) comes back. Targeted delete — only ever
    // touches placeholder rows — so it can run alongside upsertAll without the two racing calls
    // ever needing to agree on a single combined snapshot (see BookingRepositoryImpl.setBookings
    // vs upsertBookings for why a combined full-replace snapshot is unsafe under concurrency).
    @Query("DELETE FROM bookings WHERE owner_user_id = :ownerUserId AND id LIKE 'book%'")
    suspend fun deletePlaceholdersForOwner(ownerUserId: String)

    // Full-replace semantics for a given owner: clears whatever was cached for them and writes
    // the new snapshot in one transaction, matching the old in-memory setBookings() behavior
    // (including the empty-list-on-logout case) but persisted.
    @Transaction
    suspend fun replaceAllForOwner(ownerUserId: String, bookings: List<BookingEntity>) {
        deleteAllForOwner(ownerUserId)
        if (bookings.isNotEmpty()) upsertAll(bookings)
    }

    @Query(
        "UPDATE bookings SET status = :status, rental_request_status = :rentalRequestStatus, " +
        "rental_request_status_urdu = :rentalRequestStatusUrdu, cached_at = :cachedAt WHERE id = :bookingId"
    )
    suspend fun updateStatusFields(
        bookingId: String,
        status: String,
        rentalRequestStatus: String,
        rentalRequestStatusUrdu: String,
        cachedAt: Long
    )
}
