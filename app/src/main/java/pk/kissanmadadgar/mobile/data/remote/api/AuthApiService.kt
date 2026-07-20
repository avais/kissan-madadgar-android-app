package pk.kissanmadadgar.mobile.data.remote.api

import pk.kissanmadadgar.mobile.data.remote.dto.OtpRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.OtpResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.VerifyOtpRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.VerifyOtpResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.ImplementDto
import pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryRequest
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryResponse
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenRequest
import pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenResponse
import pk.kissanmadadgar.mobile.data.remote.dto.MobileProfileResponse
import pk.kissanmadadgar.mobile.data.remote.dto.SupportResponse
import pk.kissanmadadgar.mobile.data.remote.dto.AvailableMachineDto
import pk.kissanmadadgar.mobile.data.remote.dto.LocationRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.AvailableMachinesResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.RouteRequestDto
import pk.kissanmadadgar.mobile.data.remote.dto.RouteResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.MyMachinesResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.RentalBookingRequest
import pk.kissanmadadgar.mobile.data.remote.dto.UpdateProfileRequest
import pk.kissanmadadgar.mobile.data.remote.dto.RentalBookingsResponseDto
import pk.kissanmadadgar.mobile.data.remote.dto.GovernmentProjectDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.ResponseBody

interface AuthApiService {
    @POST("api/auth/android/otp/send")
    suspend fun sendOtp(
        @Body request: OtpRequestDto
    ): Response<OtpResponseDto>

    @POST("api/auth/android/otp/verify")
    suspend fun verifyOtp(
        @Header("Authorization") token: String?,
        @Body request: VerifyOtpRequestDto
    ): Response<VerifyOtpResponseDto>

    @GET("api/android/implements")
    suspend fun getImplements(
        @Header("Authorization") token: String
    ): Response<List<ImplementDto>>

    @GET("api/android/districts")
    suspend fun getDistricts(
        @Header("Authorization") token: String
    ): Response<List<DistrictDto>>

    @POST("api/android/fleet/register")
    suspend fun registerMachinery(
        @Header("Authorization") token: String,
        @Body request: RegisterMachineryRequest
    ): Response<RegisterMachineryResponse>

    @POST("api/auth/android/guest-token")
    suspend fun getGuestToken(
        @Body request: GuestTokenRequest
    ): Response<GuestTokenResponse>

    @GET("api/android/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<MobileProfileResponse>

    @GET("api/android/support")
    suspend fun getSupport(
        @Query("module") module: String? = null
    ): Response<SupportResponse>

    @POST("api/auth/android/getAvailableMachines")
    suspend fun getAvailableMachines(
        @Header("Authorization") token: String,
        @Body request: LocationRequestDto
    ): Response<AvailableMachinesResponseDto>

    @POST("api/auth/android/getRoute")
    suspend fun getRoute(
        @Header("Authorization") token: String,
        @Body request: RouteRequestDto
    ): Response<RouteResponseDto>

    @GET("api/android/my-machines")
    suspend fun getMyMachines(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = 0,
        @Query("size") size: Int? = 10
    ): Response<MyMachinesResponseDto>

    @POST("api/android/profile/update")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<okhttp3.ResponseBody>

    @POST("api/android/rental-booking")
    suspend fun createRentalBooking(
        @Header("Authorization") token: String,
        @Body request: RentalBookingRequest
    ): Response<okhttp3.ResponseBody>

    @GET("api/android/rental-bookings")
    suspend fun getRentalBookings(
        @Header("Authorization") token: String,
        @Query("status") status: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("id") id: String? = null,
        @Query("keyword") keyword: String? = null
    ): Response<RentalBookingsResponseDto>

    @POST("api/android/rental-booking/{id}/approve")
    suspend fun approveBooking(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<okhttp3.ResponseBody>

    @POST("api/android/rental-booking/{id}/reject")
    suspend fun rejectBooking(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: RejectRequestBody
    ): Response<okhttp3.ResponseBody>

    @POST("api/android/rental-booking/feedback")
    suspend fun submitBookingFeedback(
        @Header("Authorization") token: String,
        @Body body: BookingFeedbackRequestDto
    ): Response<okhttp3.ResponseBody>

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @POST("api/android/rental-booking/start-farming-taker")
    suspend fun startFarmingTaker(
        @Header("Authorization") token: String,
        @Body request: StartFarmingTakerRequest
    ): Response<Map<String, String>>

    @POST("api/android/rental-booking/start-farming-provider")
    suspend fun startFarmingProvider(
        @Header("Authorization") token: String,
        @Body request: StartFarmingProviderRequest
    ): Response<Map<String, String>>

    @POST("api/android/push-tokens/register")
    suspend fun registerPushToken(
        @Header("Authorization") token: String,
        @Body request: PushTokenRequest
    ): Response<Map<String, String>>

    @GET("api/android/push-tokens/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<AndroidNotificationsPageResponse>

    @GET("api/android/push-tokens/notifications/unread-count")
    suspend fun getUnreadNotificationCount(
        @Header("Authorization") token: String
    ): Response<Map<String, Long>>

    @PUT("api/android/push-tokens/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Map<String, String>>

    @GET("api/auth/android/government-projects")
    suspend fun getGovernmentProjects(): Response<List<GovernmentProjectDto>>

    // "How to use the app" tutorial video links (direct MP4 URLs) shown from the home screen's
    // helper tile, played in-app — see FullScreenHelperVideoPlayer.
    @GET("api/auth/android/helper-videos")
    suspend fun getHelperVideos(): Response<List<String>>

    // Flat key -> Urdu display text map mirroring the entries in res/values/strings.xml, so
    // in-app text can be updated from the backend without an app release. See RemoteStringsStore.
    @GET("api/auth/android/getmessagesvalue")
    suspend fun getMessagesValue(): Response<Map<String, String>>
}

data class PushTokenRequest(
    val fcmToken: String,
    val deviceId: String? = null
)

data class AndroidNotificationDto(
    val id: Long,
    val title: String,
    val body: String,
    val read: Boolean,
    val bookingId: Long?,
    val status: String?,
    val sentAt: String?
)

data class AndroidNotificationsPageResponse(
    @com.google.gson.annotations.SerializedName("content") val content: List<AndroidNotificationDto>?,
    @com.google.gson.annotations.SerializedName("totalPages") val totalPages: Int? = null,
    @com.google.gson.annotations.SerializedName("totalElements") val totalElements: Long? = null,
    @com.google.gson.annotations.SerializedName("last") val last: Boolean? = null,
    @com.google.gson.annotations.SerializedName("first") val first: Boolean? = null,
    @com.google.gson.annotations.SerializedName("number") val number: Int? = null,
    @com.google.gson.annotations.SerializedName("size") val size: Int? = null
)

data class StartFarmingTakerRequest(
    val rentalBookingId: Long,
    val startPicture: String, // UUID as String
    // Backend's DTO field is a String (JSON-encoded), not a nested object — sending a nested
    // object here throws "Cannot deserialize value of type `java.lang.String` from Object value"
    // server-side. Build with Gson().toJson(...), not toJsonTree(...).
    val startPictureMetaData: String
)

data class GpsLogRequest(
    val latitude: Double,
    val longitude: Double,
    val deviceId: String,
    val accuracy: Double,
    val speed: Double,
    val heading: Double,
    val altitude: Double,
    val mockLocationDetection: Boolean,
    val timestamp: Long
)

data class StartFarmingProviderRequest(
    val rentalBookingId: Long,
    val endPicture: String, // UUID as String
    // Same String-not-object contract as StartFarmingTakerRequest.startPictureMetaData above —
    // build with Gson().toJson(...), not toJsonTree(...).
    val endPictureMetaData: String,
    val gpsLogs: List<GpsLogRequest>
)

data class RejectRequestBody(val reason: String?)

data class BookingFeedbackRequestDto(
    val fleetRentalId: Long,
    val rating: Int,
    val comments: String
)
