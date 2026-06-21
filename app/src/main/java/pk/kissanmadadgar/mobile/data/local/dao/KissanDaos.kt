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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings WHERE farmer_id = :farmerId")
    fun getBookingsByFarmerFlow(farmerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE machinery_id IN (SELECT id FROM machinery WHERE provider_id = :providerId)")
    fun getBookingsByProviderFlow(providerId: String): Flow<List<BookingEntity>>

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: String)
}
