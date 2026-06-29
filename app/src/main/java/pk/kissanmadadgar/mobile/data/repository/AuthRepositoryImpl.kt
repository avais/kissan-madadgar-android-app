package pk.kissanmadadgar.mobile.data.repository

import pk.kissanmadadgar.mobile.core.security.SessionManager
import pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository
import pk.kissanmadadgar.mobile.data.remote.api.AuthApiService
import pk.kissanmadadgar.mobile.data.remote.dto.OtpRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.VerifyOtpRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.ImplementDto
import pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryRequest
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryResponse
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenRequest
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenResponse
import pk.kissanmadadgar.mobile.data.remote.dto.MobileProfileResponse
import pk.kissanmadadgar.mobile.data.remote.safeApiCall
import pk.kissanmadadgar.mobile.domain.model.User
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    // Delegate to keep in-memory mock functionalities working for endpoints not yet created in backend
    private val mockDelegate = InMemoryAuthRepository()

    override suspend fun login(phone: String, cnic: String?, latitude: Double, longitude: Double): Result<String> {
        val result = safeApiCall {
            authApiService.sendOtp(OtpRequestDto(phone, cnic, latitude, longitude))
        }
        return result.map { responseDto ->
            responseDto.message ?: "او ٹی پی کامیابی کے ساتھ بھیج دیا گیا ہے۔"
        }
    }

    override suspend fun verifySupplierCnic(cnic: String): Result<User> {
        val result = mockDelegate.verifySupplierCnic(cnic)
        result.onSuccess { user ->
            mockDelegate.setCurrentUser(user)
            sessionManager.saveUserId(user.id)
            sessionManager.saveUserName(user.fullName)
            sessionManager.saveUserPhone(user.phoneNumber)
            sessionManager.saveUserCnic(cnic)
        }
        return result
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String, role: UserRole, guestToken: String?, type: String?): Result<User> {
        val formattedPhone = if (phoneNumber.startsWith("+92")) {
            "0" + phoneNumber.substring(3)
        } else if (phoneNumber.startsWith("92")) {
            "0" + phoneNumber.substring(2)
        } else {
            phoneNumber
        }
        val authHeader = guestToken?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
        val result = safeApiCall {
            authApiService.verifyOtp(authHeader, VerifyOtpRequestDto(formattedPhone, otp, type))
        }
        return result.map { responseDto ->
            val roles = responseDto.roles ?: emptyList()
            val mappedRole = when {
                roles.contains("ROLE_ADMIN") -> UserRole.ADMIN
                roles.contains("ROLE_PROVIDER") || roles.contains("ROLE_SUPPLIER") -> UserRole.PROVIDER
                roles.contains("ROLE_FARMER") -> UserRole.FARMER
                else -> role
            }
            val id = responseDto.username ?: responseDto.cnic ?: responseDto.phone ?: ""
            val name = (responseDto.firstName.orEmpty() + " " + responseDto.lastName.orEmpty()).trim()
                .ifEmpty { if (mappedRole == UserRole.FARMER) "کسان دوست" else "سروس پرووائیڈر" }
            
            val user = User(
                id = id,
                phoneNumber = responseDto.phone ?: responseDto.mobileNumber ?: formattedPhone,
                fullName = name,
                role = mappedRole,
                profileImageUrl = null,
                isActive = true
            )
            
            // Save response variables to SessionManager
            sessionManager.saveAuthToken(responseDto.token ?: "")
            sessionManager.saveUserRole(mappedRole)
            sessionManager.saveUserId(id)
            sessionManager.saveUserName(name)
            sessionManager.saveUserPhone(user.phoneNumber)
            responseDto.cnic?.let { sessionManager.saveUserCnic(it) }
            responseDto.address?.let { sessionManager.saveUserAddress(it) }
            responseDto.districtName?.let { sessionManager.saveUserDistrict(it) }
            
            mockDelegate.setCurrentUser(user)
            
            user
        }
    }

    override suspend fun registerFarmer(phoneNumber: String, fullName: String, address: String): Result<User> {
        val result = mockDelegate.registerFarmer(phoneNumber, fullName, address)
        result.onSuccess { user ->
            mockDelegate.setCurrentUser(user)
            sessionManager.saveUserId(user.id)
            sessionManager.saveUserName(user.fullName)
            sessionManager.saveUserPhone(user.phoneNumber)
            sessionManager.saveUserAddress(address)
        }
        return result
    }

    override suspend fun adminLogin(email: String, pass: String): Result<User> {
        val result = mockDelegate.adminLogin(email, pass)
        result.onSuccess { user ->
            mockDelegate.setCurrentUser(user)
            sessionManager.saveUserId(user.id)
            sessionManager.saveUserName(user.fullName)
            sessionManager.saveAuthToken(email)
        }
        return result
    }

    override suspend fun getCurrentUser(): User? {
        return mockDelegate.getCurrentUser()
    }

    override suspend fun logout(): Result<Unit> {
        return mockDelegate.logout()
    }

    override suspend fun getImplements(): Result<List<ImplementDto>> {
        val token = sessionManager.getAuthToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return safeApiCall {
            authApiService.getImplements(authHeader)
        }
    }

    override suspend fun getDistricts(): Result<List<DistrictDto>> {
        val token = sessionManager.getAuthToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return safeApiCall {
            authApiService.getDistricts(authHeader)
        }
    }

    override suspend fun registerMachinery(payload: RegisterMachineryRequest): Result<RegisterMachineryResponse> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return safeApiCall {
            authApiService.registerMachinery(authHeader, payload)
        }
    }

    override suspend fun getGuestToken(request: GuestTokenRequest): Result<GuestTokenResponse> {
        return safeApiCall {
            authApiService.getGuestToken(request)
        }
    }

    override suspend fun getProfile(token: String): Result<MobileProfileResponse> {
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return safeApiCall {
            authApiService.getProfile(authHeader)
        }
    }
}
