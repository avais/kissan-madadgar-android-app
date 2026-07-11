package pk.kissanmadadgar.mobile.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pk.kissanmadadgar.mobile.domain.model.*
import pk.kissanmadadgar.mobile.domain.repository.*
import pk.kissanmadadgar.mobile.data.remote.dto.ImplementDto
import pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryRequest
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryResponse
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenRequest
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenResponse
import pk.kissanmadadgar.mobile.data.remote.dto.MobileProfileResponse
import pk.kissanmadadgar.mobile.data.remote.dto.SupportResponse
import pk.kissanmadadgar.mobile.data.remote.dto.MyMachinesResponseDto
import java.util.UUID

class InMemoryAuthRepository : AuthRepository {
    private val currentUserState = MutableStateFlow<User?>(null)
    
    // Initial users database
    private val mockUsers = mutableListOf(
        User("usr_farmer", "03001234567", "چوہدری اصغر علی", UserRole.FARMER, null, true),
        User("usr_provider", "03111234567", "میاں اختر رضوان", UserRole.PROVIDER, null, true),
        User("usr_admin", "admin@kissan.pk", "سید زین العابدین", UserRole.ADMIN, null, true)
    )

    override suspend fun login(phone: String, cnic: String?, latitude: Double, longitude: Double): Result<String> {
        // Any mobile number is allowed. Return mock success message.
        return Result.success("او ٹی پی کامیابی کے ساتھ بھیج دیا گیا ہے۔ یہ 15 منٹ میں ایکسپائر ہو جائے گا۔")
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String, role: UserRole, guestToken: String?, type: String?): Result<User> {
        if (otp == "0000" || otp.length == 4) {
            val existing = mockUsers.find { it.phoneNumber == phoneNumber && it.role == role }
            val user = if (existing != null) {
                existing
            } else {
                val newUser = User(
                    id = "usr_" + UUID.randomUUID().toString().take(6),
                    phoneNumber = phoneNumber,
                    fullName = if (role == UserRole.FARMER) "کسان دوست" else "سروس پرووائیڈر",
                    role = role,
                    profileImageUrl = null,
                    isActive = true
                )
                mockUsers.add(newUser)
                newUser
            }
            currentUserState.value = user
            return Result.success(user)
        }
        return Result.failure(Exception("غلط او ٹی پی کوڈ"))
    }

    override suspend fun registerFarmer(phoneNumber: String, fullName: String, address: String): Result<User> {
        val newUser = User(
            id = "usr_" + UUID.randomUUID().toString().take(6),
            phoneNumber = phoneNumber,
            fullName = fullName,
            role = UserRole.FARMER,
            profileImageUrl = null,
            isActive = true
        )
        mockUsers.add(newUser)
        currentUserState.value = newUser
        return Result.success(newUser)
    }

    override suspend fun verifySupplierCnic(cnic: String): Result<User> {
        kotlinx.coroutines.delay(1000)
        
        // Extract raw digits
        val digits = cnic.filter { it.isDigit() }
        if (digits.length != 13) {
            return Result.failure(Exception("شناختی کارڈ کا نمبر درست نہیں ہے۔"))
        }
        
        // Standardize as XXXXX-XXXXXXX-X format
        val formattedCnic = "${digits.substring(0, 5)}-${digits.substring(5, 12)}-${digits.substring(12)}"
        
        // Default correct CNIC (00000-0000000-0)
        if (formattedCnic == "00000-0000000-0") {
            val existing = mockUsers.find { it.role == UserRole.PROVIDER }
            val newUser = if (existing != null) {
                val updated = existing.copy(
                    fullName = "چوہدری طارق محمود",
                    phoneNumber = "03007654321"
                )
                mockUsers[mockUsers.indexOf(existing)] = updated
                updated
            } else {
                val created = User(
                    id = "usr_provider",
                    phoneNumber = "03007654321",
                    fullName = "چوہدری طارق محمود",
                    role = UserRole.PROVIDER,
                    profileImageUrl = null,
                    isActive = true
                )
                mockUsers.add(created)
                created
            }
            return Result.success(newUser)
        }
        
        // If it matches 12345-1234567-1, return the mock provider
        if (formattedCnic == "12345-1234567-1") {
            return Result.success(mockUsers.find { it.role == UserRole.PROVIDER }!!)
        }

        // Otherwise generate a new mock provider for demonstration
        val randomPhone = "03${(10..99).random()}${(1000000..9999999).random().toString().take(7)}"
        val newUser = User(
            id = "usr_" + UUID.randomUUID().toString().take(6),
            phoneNumber = randomPhone,
            fullName = "سروس فراہم کنندہ",
            role = UserRole.PROVIDER,
            profileImageUrl = null,
            isActive = true
        )
        mockUsers.add(newUser)
        return Result.success(newUser)
    }

    override suspend fun adminLogin(email: String, pass: String): Result<User> {
        if (email.contains("admin") && pass == "admin123") {
            val adminUser = mockUsers.find { it.role == UserRole.ADMIN }!!
            currentUserState.value = adminUser
            return Result.success(adminUser)
        }
        return Result.failure(Exception("غلط ای میل یا پاس ورڈ"))
    }

    override suspend fun getCurrentUser(): User? {
        return currentUserState.value
    }

    override suspend fun logout(): Result<Unit> {
        currentUserState.value = null
        return Result.success(Unit)
    }
    
    fun setCurrentUser(user: User?) {
        currentUserState.value = user
    }

    override suspend fun getImplements(): Result<List<ImplementDto>> {
        return Result.success(listOf(
            ImplementDto(8, "Super Seeder", "سپرسیڈر", null, null, null, null, "ACTIVE"),
            ImplementDto(9, "Baler", "بیلر", null, null, null, null, "ACTIVE"),
            ImplementDto(10, "Harvester", "ہارویسٹر", null, null, null, null, "ACTIVE")
        ))
    }

    override suspend fun getDistricts(): Result<List<DistrictDto>> {
        return Result.success(listOf(
            DistrictDto(1, "Faisalabad", "فیصل آباد", "DIST_1_FAISALABAD", true, 1, "Faisalabad", 5),
            DistrictDto(2, "Jhang", "جھنگ", "DIST_2_JHANG", true, 1, "Faisalabad", 4),
            DistrictDto(3, "Chiniot", "چنیوٹ", "DIST_3_CHINIOT", true, 1, "Faisalabad", 3)
        ))
    }

    override suspend fun registerMachinery(payload: RegisterMachineryRequest): Result<RegisterMachineryResponse> {
        val isGuest = getCurrentUser() == null
        return Result.success(RegisterMachineryResponse(
            success = true,
            message = "مشینری کامیابی کے ساتھ رجسٹر ہو گئی ہے۔",
            isOtpSent = isGuest,
            guestToken = if (isGuest) "mock_guest_token_" + System.currentTimeMillis() else null
        ))
    }

    override suspend fun getGuestToken(request: GuestTokenRequest): Result<GuestTokenResponse> {
        return Result.success(GuestTokenResponse(
            guestToken = "mock_guest_token_" + System.currentTimeMillis(),
            tokenType = "Bearer",
            message = "مہمان ٹوکن کامیابی سے حاصل کر لیا گیا ہے۔"
        ))
    }

    override suspend fun getProfile(token: String): Result<MobileProfileResponse> {
        val user = getCurrentUser()
        val name = user?.fullName ?: ""
        val mobile = user?.phoneNumber ?: ""
        return Result.success(
            MobileProfileResponse(
                name = name,
                editName = true,
                district = "",
                editDistrict = true,
                mobile = mobile,
                editMobile = false,
                cnic = "",
                editCnic = true,
                address = "",
                editAddress = true
            )
        )
    }

    override suspend fun getSupport(module: String?): Result<SupportResponse> {
        return Result.success(
            SupportResponse(
                message = "مدد کے لیے اس نمبر پر واٹس ایپ یا کال کریں۔ ہیلپ لائن 03215806176۔",
                messageRoman = "Madad kay liya is number par whatsapp or call karain. help line 03215806176.",
                helpline = "03215806176"
            )
        )
    }

    override suspend fun getAvailableMachines(
        latitude: Double,
        longitude: Double,
        type: String,
        page: Int,
        size: Int,
        districtId: Int?,
        keyword: String?
    ): Result<PaginatedMachinery> {
        return Result.success(PaginatedMachinery(emptyList(), totalPages = 1, totalElements = 0, isLast = true, currentPage = 0))
    }

    override suspend fun getMyMachines(page: Int, size: Int): Result<MyMachinesResponseDto> {
        return Result.success(MyMachinesResponseDto(0, 0, "0", "0.0", emptyList(), 0, 1, page, size))
    }

    override suspend fun updateProfile(name: String, cnic: String, address: String, districtId: Long?, mobile: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun createRentalBooking(
        fleetId: Long?,
        acres: Double,
        date: String,
        numberOfHours: Double,
        latitude: Double,
        longitude: Double,
        farmingActivityAddress: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getRentalBookings(
        status: String?,
        page: Int?,
        size: Int?,
        id: String?,
        keyword: String?
    ): Result<pk.kissanmadadgar.mobile.domain.model.PaginatedBookings> {
        return Result.success(
            pk.kissanmadadgar.mobile.domain.model.PaginatedBookings(
                bookings = emptyList(),
                totalPages = 1,
                totalElements = 0,
                isLast = true,
                isFirst = true,
                currentPage = 0
            )
        )
    }

    override suspend fun approveBooking(bookingId: String): Result<String> {
        return Result.success("بکنگ کامیابی کے ساتھ منظور کر لی گئی ہے۔")
    }

    override suspend fun rejectBooking(bookingId: String, reason: String?): Result<String> {
        return Result.success("بکنگ کامیابی کے ساتھ مسترد کر دی گئی ہے۔")
    }

    override suspend fun submitBookingFeedback(bookingId: String, rating: Int, comment: String): Result<String> {
        return Result.success("آپ کی فیڈبیک موصول ہو گئی، شکریہ!")
    }
}

class InMemoryMachineryRepository : MachineryRepository {
    private val machineryListState = MutableStateFlow<List<Machinery>>(emptyList())

    override fun getCategories(): Flow<List<Category>> = MutableStateFlow(emptyList())

    override fun getAvailableMachinery(): Flow<List<Machinery>> {
        return machineryListState.map { list ->
            list.filter { it.isAvailable && it.status == MachineryStatus.APPROVED }
        }
    }

    override fun getMachineryByProvider(providerId: String): Flow<List<Machinery>> {
        return machineryListState.map { list ->
            list.filter { it.providerId == providerId }
        }
    }

    override suspend fun getMachineryById(id: String): Machinery? {
        return machineryListState.value.find { it.id == id }
    }

    override suspend fun addMachinery(machinery: Machinery): Result<Unit> {
        val current = machineryListState.value.toMutableList()
        current.add(machinery)
        machineryListState.value = current
        return Result.success(Unit)
    }

    override suspend fun updateMachineryStatus(id: String, status: MachineryStatus): Result<Unit> {
        val current = machineryListState.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(status = status)
            machineryListState.value = current
            return Result.success(Unit)
        }
        return Result.failure(Exception("مشینری نہیں ملی"))
    }

    override fun getAllMachineryAdmin(): Flow<List<Machinery>> {
        return machineryListState
    }
}

