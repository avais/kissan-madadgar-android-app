package pk.kissanmadadgar.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pk.kissanmadadgar.mobile.domain.model.*

interface AuthRepository {
    suspend fun login(phoneNumber: String): Result<Boolean> // Returns true if needs OTP verification
    suspend fun verifySupplierCnic(cnic: String): Result<User>
    suspend fun verifyOtp(phoneNumber: String, otp: String, role: UserRole): Result<User>
    suspend fun registerFarmer(phoneNumber: String, fullName: String, address: String): Result<User>
    suspend fun adminLogin(email: String, pass: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout(): Result<Unit>
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
}
