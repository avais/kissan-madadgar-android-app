package pk.kissanmadadgar.mobile.data.repository

import pk.kissanmadadgar.mobile.core.security.SessionManager
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
import pk.kissanmadadgar.mobile.data.remote.dto.SupportResponse
import pk.kissanmadadgar.mobile.data.remote.dto.LocationRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.AvailableMachinesResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.MyMachinesResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.RentalBookingRequest
import pk.kissanmadadgar.mobile.data.remote.dto.UpdateProfileRequest
import pk.kissanmadadgar.mobile.data.remote.safeApiCall
import pk.kissanmadadgar.mobile.domain.model.User
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.domain.model.MachineryStatus
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.data.remote.dto.RentalBookingResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.RentalBookingsResponseDto
import pk.kissanmadadgar.mobile.domain.repository.AuthRepository
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun login(phone: String, cnic: String?, latitude: Double, longitude: Double): Result<String> {
        val result = safeApiCall {
            authApiService.sendOtp(OtpRequestDto(phone, cnic, latitude, longitude))
        }
        return result.map { responseDto ->
            responseDto.message ?: "او ٹی پی کامیابی کے ساتھ بھیج دیا گیا ہے۔"
        }
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
            val id = responseDto.userId?.toString() ?: responseDto.username ?: responseDto.cnic ?: responseDto.phone ?: ""
            val name = responseDto.firstName.orEmpty().trim()

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

            user
        }
    }

    override suspend fun logout(): Result<Unit> {
        try {
            val uploadsDir = java.io.File(context.filesDir, "uploads")
            if (uploadsDir.exists()) {
                uploadsDir.deleteRecursively()
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepositoryImpl", "Failed to clear cache on logout", e)
        }
        return Result.success(Unit)
    }

    override suspend fun getImplements(): Result<List<ImplementDto>> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return safeApiCall {
            authApiService.getImplements(authHeader)
        }
    }

    override suspend fun getDistricts(): Result<List<DistrictDto>> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
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

    override suspend fun getSupport(module: String?): Result<SupportResponse> {
        return safeApiCall {
            authApiService.getSupport(module)
        }
    }

    override suspend fun getAvailableMachines(
        latitude: Double,
        longitude: Double,
        type: String,
        page: Int,
        size: Int,
        districtId: Int?,
        keyword: String?
    ): Result<pk.kissanmadadgar.mobile.domain.model.PaginatedMachinery> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        android.util.Log.d("KissanMadadgar", "Repo getAvailableMachines calling API with: lat=$latitude, lng=$longitude, token=$authHeader, type=$type, page=$page, size=$size, districtId=$districtId, keyword=$keyword")
        val result = safeApiCall {
            authApiService.getAvailableMachines(authHeader, LocationRequestDto(latitude, longitude, type = type, page = page, size = size, districtId = districtId, keyword = keyword ?: ""))
        }
        android.util.Log.d("KissanMadadgar", "Repo getAvailableMachines API result: $result")
        return result.map { responseDto ->
            val list = responseDto.content ?: emptyList()
            val machines = list.mapIndexed { index, dto ->
                val picUrls = dto.machinePictures ?: emptyList()
                val parsedRating = dto.rating?.toDoubleOrNull() ?: 0.0
                val nameUrdu = dto.machineName ?: dto.machineNameAlt ?: ""
                Machinery(
                    id = "machine_${page}_$index",
                    providerId = "provider_${page}_$index",
                    providerName = dto.farmerName ?: "",
                    providerPhone = dto.mobile ?: "",
                    categoryId = "CAT_1",
                    nameUr = nameUrdu,
                    descriptionUr = dto.project?.projectName ?: "",
                    modelYear = 2026,
                    hourlyRate = 0.0,
                    // Prefer the machine's own coordinates (now returned by the backend) so map
                    // pins reflect real positions; fall back to the requester's own location for
                    // older responses that don't include per-machine lat/lng yet.
                    latitude = dto.latitude ?: latitude,
                    longitude = dto.longitude ?: longitude,
                    isAvailable = true,
                    status = MachineryStatus.APPROVED,
                    imageUrls = picUrls,
                    rating = parsedRating,
                    acresDone = 0.0,
                    distanceCoveredKm = 0.0,
                    districtUr = dto.farmerDistrict ?: "",
                    projectName = dto.project?.projectName,
                    projectLogo = dto.project?.logo,
                    subsidyText = dto.project?.subsidy,
                    distanceText = dto.distance,
                    fleetId = dto.fleetId
                )
            }
            val resolvedTotalPages = responseDto.totalPages ?: 1
            val resolvedCurrentPage = responseDto.page ?: page
            pk.kissanmadadgar.mobile.domain.model.PaginatedMachinery(
                machinery = machines,
                totalPages = resolvedTotalPages,
                totalElements = (responseDto.totalElements ?: machines.size).toLong(),
                isLast = resolvedCurrentPage >= resolvedTotalPages - 1,
                currentPage = resolvedCurrentPage,
                myBookingCounter = responseDto.myBookingCounter ?: 0
            )
        }
    }

    override suspend fun getRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Result<pk.kissanmadadgar.mobile.domain.model.RouteInfo> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = safeApiCall {
            authApiService.getRoute(
                authHeader,
                pk.kissanmadadgar.mobile.data.remote.dto.RouteRequestDto(originLat, originLng, destLat, destLng)
            )
        }
        return result.map { responseDto ->
            pk.kissanmadadgar.mobile.domain.model.RouteInfo(
                isRoadRoute = responseDto.status == "OK" && !responseDto.polyline.isNullOrBlank(),
                distanceMeters = responseDto.distanceMeters ?: 0L,
                encodedPolyline = responseDto.polyline,
                estimatedMinutes = responseDto.estimatedMinutes ?: 0
            )
        }
    }

    override suspend fun getHelperVideos(): Result<List<String>> {
        return safeApiCall { authApiService.getHelperVideos() }
    }

    override suspend fun getMyMachines(page: Int, size: Int): Result<MyMachinesResponseDto> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return safeApiCall {
            authApiService.getMyMachines(authHeader, page, size)
        }
    }

    override suspend fun updateProfile(name: String, cnic: String, address: String, districtId: Long?, mobile: String): Result<Unit> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val userId = sessionManager.getUserId()?.toLongOrNull()
        val request = UpdateProfileRequest(
            userId = userId,
            name = name,
            cnic = cnic,
            address = address,
            districtId = districtId,
            mobile = mobile
        )
        val result = safeApiCall {
            authApiService.updateProfile(authHeader, request)
        }
        return result.map { }
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
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val request = RentalBookingRequest(
            fleetId = fleetId,
            acres = acres,
            date = date,
            numberOfHours = numberOfHours,
            latitude = latitude,
            longitude = longitude,
            farmingActivityAddress = farmingActivityAddress
        )
        val result = safeApiCall {
            authApiService.createRentalBooking(authHeader, request)
        }
        return result.map { }
    }

    override suspend fun getRentalBookings(
        status: String?,
        page: Int?,
        size: Int?,
        id: String?,
        keyword: String?
    ): Result<pk.kissanmadadgar.mobile.domain.model.PaginatedBookings> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = safeApiCall {
            authApiService.getRentalBookings(authHeader, status, page, size, id, keyword)
        }
        val mappedResult = result.map { responseDto ->
            val list = responseDto.content ?: emptyList()
            val bookings = list.mapIndexed { index, dto ->
                val dateStr = dto.fleetRentalDate ?: ""
                val parsedDate = System.currentTimeMillis()
                
                val statusStr = dto.rentalRequestStatusUrdu ?: dto.rentalRequestStatus
                val mappedStatus = when (dto.rentalRequestStatus?.uppercase()) {
                    "PENDING" -> BookingStatus.PENDING
                    "ACCEPTED", "APPROVED" -> BookingStatus.ACCEPTED
                    "REJECTED" -> BookingStatus.REJECTED
                    "ACTIVE", "ONGOING", "STARTED_FROM_FARMER_SIDE", "STARTED_FROM_SERVICE_PROVIDER_SIDE" -> BookingStatus.ACTIVE
                    "COMPLETED" -> BookingStatus.COMPLETED
                    "CANCELLED" -> BookingStatus.CANCELLED
                    else -> {
                        when (statusStr) {
                            "PENDING", "جدید بکنگ" -> BookingStatus.PENDING
                            "ACCEPTED", "APPROVED", "منظور", "منظور شدہ" -> BookingStatus.ACCEPTED
                            "REJECTED", "مسترد", "مسترد شدہ" -> BookingStatus.REJECTED
                            "ACTIVE", "ONGOING", "کام جاری", "STARTED_FROM_FARMER_SIDE", "STARTED_FROM_SERVICE_PROVIDER_SIDE" -> BookingStatus.ACTIVE
                            "COMPLETED", "مکمل" -> BookingStatus.COMPLETED
                            "CANCELLED", "منسوخ" -> BookingStatus.CANCELLED
                            else -> BookingStatus.PENDING
                        }
                    }
                }
                
                val currentUserId = sessionManager.getUserId() ?: ""
                val currentUserName = sessionManager.getUserName() ?: ""
                val currentUserPhone = sessionManager.getUserPhone() ?: ""

                Booking(
                    id = dto.fleetRentalId?.toString() ?: "booking_$index",
                    farmerId = dto.serviceTakerId?.toString() ?: currentUserId,
                    farmerName = getCleanName(currentUserName),
                    farmerPhone = currentUserPhone,
                    machineryId = dto.fleetRentalId?.toString() ?: "machine_unknown",
                    machineryName = dto.machineDetails?.name ?: "",
                    bookingDate = dateStr,
                    durationHours = dto.fleetRentalDuration?.toInt() ?: 4,
                    totalPrice = 0.0,
                    status = mappedStatus,
                    createdAt = parsedDate,
                    // Prefer the precise farming address the taker entered over the coarser
                    // district field, when the backend actually sent one for this booking.
                    locationUr = dto.farmingAddress?.takeIf { it.isNotBlank() }
                        ?: dto.rentalDistrict
                        ?: "مقام دستیاب نہیں",
                    acres = dto.acre ?: 1.0,
                    providerName = getCleanName(dto.name ?: dto.serviceProviderName ?: ""),
                    providerPhone = dto.serviceProviderMobile ?: "",
                    machineryImageUrl = dto.machineDetails?.pictures?.firstOrNull(),
                    isApprovalAllowed = dto.isApprovalAllowed ?: false,
                    rentalRequestStatus = dto.rentalRequestStatus ?: "PENDING",
                    rentalRequestStatusUrdu = dto.rentalRequestStatusUrdu ?: statusStr ?: "جدید بکنگ",
                    serviceProviderId = dto.serviceProviderId,
                    serviceTakerId = dto.serviceTakerId,
                    isRatingDone = dto.isRatingDone ?: false
                )
            }
            pk.kissanmadadgar.mobile.domain.model.PaginatedBookings(
                bookings = bookings,
                totalPages = responseDto.totalPages ?: 1,
                totalElements = responseDto.totalElements ?: bookings.size.toLong(),
                isLast = responseDto.last ?: true,
                isFirst = responseDto.first ?: true,
                currentPage = responseDto.number ?: 0
            )
        }

        return mappedResult
    }

    override suspend fun approveBooking(bookingId: String): Result<String> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = safeApiCall {
            authApiService.approveBooking(authHeader, bookingId)
        }
        return result.map { responseBody ->
            val raw = responseBody.string()
            try {
                val map = com.google.gson.Gson().fromJson<Map<String, String>>(raw, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
                map["message"] ?: raw
            } catch (e: Exception) {
                raw
            }
        }
    }

    override suspend fun rejectBooking(bookingId: String, reason: String?): Result<String> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = safeApiCall {
            authApiService.rejectBooking(authHeader, bookingId, pk.kissanmadadgar.mobile.data.remote.api.RejectRequestBody(reason))
        }
        return result.map { responseBody ->
            val raw = responseBody.string()
            try {
                val map = com.google.gson.Gson().fromJson<Map<String, String>>(raw, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
                map["message"] ?: raw
            } catch (e: Exception) {
                raw
            }
        }
    }

    override suspend fun submitBookingFeedback(bookingId: String, rating: Int, comment: String): Result<String> {
        val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = safeApiCall {
            authApiService.submitBookingFeedback(
                authHeader,
                pk.kissanmadadgar.mobile.data.remote.api.BookingFeedbackRequestDto(
                    fleetRentalId = bookingId.toLongOrNull() ?: 0L,
                    rating = rating,
                    comments = comment
                )
            )
        }
        return result.map { responseBody ->
            val raw = responseBody.string()
            try {
                val map = com.google.gson.Gson().fromJson<Map<String, String>>(raw, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
                map["message"] ?: raw
            } catch (e: Exception) {
                raw
            }
        }
    }
}

private fun getCleanName(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return trimmed

    val parts = trimmed.split("\\s+".toRegex())
    if (parts.size >= 2) {
        val halfSize = parts.size / 2
        if (parts.size % 2 == 0) {
            val firstHalf = parts.subList(0, halfSize)
            val secondHalf = parts.subList(halfSize, parts.size)
            if (firstHalf == secondHalf) {
                return firstHalf.joinToString(" ")
            }
        }
        if (parts.size == 2 && parts[0] == parts[1]) {
            return parts[0]
        }
    }
    return trimmed
}
