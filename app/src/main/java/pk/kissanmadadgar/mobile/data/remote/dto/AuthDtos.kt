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
    @SerializedName("features") val features: List<String>?,
    @SerializedName("userId") val userId: Long?
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
    @SerializedName("token") val guestToken: String?,
    @SerializedName("tokenType") val tokenType: String?,
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

data class SupportResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("messageRoman") val messageRoman: String?,
    @SerializedName("helpline") val helpline: String?
)

data class ProjectDto(
    @SerializedName("projectName") val projectName: String?,
    @SerializedName("logo") val logo: String?,
    @SerializedName("subsidy") val subsidy: String?
)

data class AvailableMachineDto(
    @SerializedName("farmerName") val farmerName: String?,
    @SerializedName("farmerDistrict") val farmerDistrict: String?,
    @SerializedName("machinePictures") val machinePictures: List<String>?,
    @SerializedName("distance") val distance: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("project") val project: ProjectDto?,
    @SerializedName("machineName") val machineName: String?,
    @SerializedName("MachineName") val machineNameAlt: String?,
    @SerializedName("fleetId") val fleetId: Long? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

data class LocationRequestDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("type") val type: String = "home",
    @SerializedName("page") val page: Int? = 0,
    @SerializedName("size") val size: Int? = 5,
    @SerializedName("districtId") val districtId: Int? = null,
    @SerializedName("keyword") val keyword: String? = ""
)

data class AvailableMachinesResponseDto(
    @SerializedName("totalElements") val totalElements: Int?,
    @SerializedName("totalPages") val totalPages: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("size") val size: Int?,
    @SerializedName("content") val content: List<AvailableMachineDto>?
)

data class MyMachineDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("machineName") val machineName: String?,
    @SerializedName("machinePictures") val machinePictures: List<String>?,
    @SerializedName("status") val status: String?,
    @SerializedName("statusUrdu") val statusUrdu: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("remarks") val remarks: String?,
    @SerializedName("acres") val acres: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("project") val project: ProjectDto?
)

data class MyMachinesResponseDto(
    @SerializedName("approvedCount") val approvedCount: Int?,
    @SerializedName("pendingCount") val pendingCount: Int?,
    @SerializedName("totalAcres") val totalAcres: String?,
    @SerializedName("totalRating") val totalRating: String?,
    @SerializedName("machines") val machines: List<MyMachineDto>?,
    @SerializedName("totalElements") val totalElements: Long? = null,
    @SerializedName("totalPages") val totalPages: Int? = null,
    @SerializedName("pageNumber") val pageNumber: Int? = null,
    @SerializedName("pageSize") val pageSize: Int? = null
)

data class UpdateProfileRequest(
    @SerializedName("userId") val userId: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("cnic") val cnic: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("districtId") val districtId: Long?,
    @SerializedName("mobile") val mobile: String?
)

data class RentalBookingRequest(
    @SerializedName("fleetId") val fleetId: Long?,
    @SerializedName("acres") val acres: Double,
    @SerializedName("date") val date: String,
    @SerializedName("numberOfHours") val numberOfHours: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("farmingActivityAddress") val farmingActivityAddress: String
)

data class MachineDetailsDto(
    @SerializedName("pictures") val pictures: List<String>?,
    @SerializedName("name") val name: String?,
    @SerializedName("projectName") val projectName: String?,
    @SerializedName("projectLogo") val projectLogo: String?,
    @SerializedName("subsidy") val subsidy: String?
)

data class RentalBookingResponseDto(
    @SerializedName("fleetRentalId") val fleetRentalId: Long?,
    @SerializedName("rentalRequestStatus") val rentalRequestStatus: String?,
    @SerializedName("rentalRequestStatusUrdu") val rentalRequestStatusUrdu: String?,
    @SerializedName("serviceProviderName") val serviceProviderName: String?,
    @SerializedName("serviceProviderMobile") val serviceProviderMobile: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("machineDetails") val machineDetails: MachineDetailsDto?,
    @SerializedName("fleetRentalDate") val fleetRentalDate: String?,
    @SerializedName("fleetRentalDuration") val fleetRentalDuration: Double?,
    @SerializedName("acre") val acre: Double?,
    @SerializedName("rentalDistrict") val rentalDistrict: String?,
    @SerializedName("farmingAddress") val farmingAddress: String?,
    @SerializedName("isApprovalAllowed") val isApprovalAllowed: Boolean?,
    @SerializedName("serviceProviderId") val serviceProviderId: Long?,
    @SerializedName("serviceTakerId") val serviceTakerId: Long?,
    @SerializedName("isRatingDone") val isRatingDone: Boolean?
)

data class RentalBookingsResponseDto(
    @SerializedName("content") val content: List<RentalBookingResponseDto>?,
    @SerializedName("totalPages") val totalPages: Int? = null,
    @SerializedName("totalElements") val totalElements: Long? = null,
    @SerializedName("last") val last: Boolean? = null,
    @SerializedName("first") val first: Boolean? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("size") val size: Int? = null
)
