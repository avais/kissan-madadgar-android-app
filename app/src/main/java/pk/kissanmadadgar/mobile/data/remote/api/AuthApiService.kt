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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApiService {
    @POST("otp/send")
    suspend fun sendOtp(
        @Body request: OtpRequestDto
    ): Response<OtpResponseDto>

    @POST("otp/verify")
    suspend fun verifyOtp(
        @Header("Authorization") token: String?,
        @Body request: VerifyOtpRequestDto
    ): Response<VerifyOtpResponseDto>

    @GET("http://192.168.100.249:9089/api/android/implements")
    suspend fun getImplements(
        @Header("Authorization") token: String
    ): Response<List<ImplementDto>>

    @GET("http://192.168.100.249:9089/api/android/districts")
    suspend fun getDistricts(
        @Header("Authorization") token: String
    ): Response<List<DistrictDto>>

    @POST("http://192.168.100.249:9089/api/android/fleet/register")
    suspend fun registerMachinery(
        @Header("Authorization") token: String,
        @Body request: RegisterMachineryRequest
    ): Response<RegisterMachineryResponse>

    @POST("guest-token")
    suspend fun getGuestToken(
        @Body request: GuestTokenRequest
    ): Response<GuestTokenResponse>

    @GET("http://192.168.100.249:9089/api/android/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<MobileProfileResponse>
}
