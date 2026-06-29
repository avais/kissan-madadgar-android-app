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

interface AuthRepository {
    suspend fun login(phone: String, cnic: String?, latitude: Double, longitude: Double): Result<String>
    suspend fun verifySupplierCnic(cnic: String): Result<User>
    suspend fun verifyOtp(phoneNumber: String, otp: String, role: UserRole, guestToken: String? = null, type: String? = null): Result<User>
    suspend fun registerFarmer(phoneNumber: String, fullName: String, address: String): Result<User>
    suspend fun adminLogin(email: String, pass: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout(): Result<Unit>
    suspend fun getImplements(): Result<List<ImplementDto>>
    suspend fun getDistricts(): Result<List<DistrictDto>>
    suspend fun registerMachinery(payload: RegisterMachineryRequest): Result<RegisterMachineryResponse>
    suspend fun getGuestToken(request: GuestTokenRequest): Result<GuestTokenResponse>
    suspend fun getProfile(token: String): Result<MobileProfileResponse>
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
