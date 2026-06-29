package pk.kissanmadadgar.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OtpRequestDto(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("cnic")
    val cnic: String?,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)

data class OtpResponseDto(
    @SerializedName("guestToken")
    val guestToken: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val token: String?
)

data class VerifyOtpRequestDto(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("type")
    val type: String? = null
)

data class VerifyOtpResponseDto(
    @SerializedName("token")
    val token: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("roles") val roles: List<String>?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("cnic") val cnic: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("districtName") val districtName: String?,
    @SerializedName("districtId") val districtId: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("features") val features: List<String>?
)

data class ImplementDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("nameUr") val nameUr: String,
    @SerializedName("picture1") val picture1: String?,
    @SerializedName("picture2") val picture2: String?,
    @SerializedName("picture3") val picture3: String?,
    @SerializedName("picture4") val picture4: String?,
    @SerializedName("status") val status: String?
)

data class DistrictDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("nameUrdu") val nameUrdu: String,
    @SerializedName("code") val code: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("divisionId") val divisionId: Int,
    @SerializedName("divisionName") val divisionName: String?,
    @SerializedName("markazCount") val markazCount: Int
)

// ---- Register Machinery DTOs ----

data class MachineRegistrationItem(
    @SerializedName("implementId") val implementId: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("districtId") val districtId: Int
)

data class UserInfoDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("cnic") val cnic: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class RegisterMachineryRequest(
    @SerializedName("machines") val machines: List<MachineRegistrationItem>,
    @SerializedName("userInfo") val userInfo: UserInfoDto
)

data class RegisterMachineryResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("isOtpSent") val isOtpSent: Boolean? = null,
    @SerializedName("guestToken") val guestToken: String? = null
)

data class GuestTokenRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("appVersion") val appVersion: String,
    @SerializedName("signature") val signature: String
)

data class GuestTokenResponse(
    @SerializedName("guestToken") val guestToken: String?,
    @SerializedName("message") val message: String?
)

data class MobileProfileResponse(
    @SerializedName("name") val name: String?,
    @SerializedName("editname") val editName: Boolean?,
    @SerializedName("district") val district: String?,
    @SerializedName("editDistirct") val editDistrict: Boolean?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("editMobile") val editMobile: Boolean?,
    @SerializedName("cnic") val cnic: String?,
    @SerializedName("editCnic") val editCnic: Boolean?,
    @SerializedName("address") val address: String?,
    @SerializedName("editAddress") val editAddress: Boolean?
)
