package pk.kissanmadadgar.mobile.domain.model

data class User(
    val id: String,
    val phoneNumber: String,
    val fullName: String,
    val role: UserRole,
    val profileImageUrl: String?,
    val isActive: Boolean
)

enum class UserRole {
    FARMER, PROVIDER, ADMIN
}

data class Farmer(
    val user: User,
    val preferredLanguage: String,
    val farmingAddress: String?
)

data class ServiceProvider(
    val user: User,
    val cnicNumber: String,
    val isVerified: Boolean,
    val averageRating: Double
)

data class Category(
    val id: String,
    val nameEn: String,
    val nameUr: String,
    val iconName: String
)

data class Machinery(
    val id: String,
    val providerId: String,
    val providerName: String,
    val providerPhone: String,
    val categoryId: String,
    val nameUr: String,
    val descriptionUr: String,
    val modelYear: Int,
    val hourlyRate: Double,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean,
    val status: MachineryStatus,
    val imageUrls: List<String>,
    val rating: Double,
    val acresDone: Double = 0.0,
    val distanceCoveredKm: Double = 0.0,
    val districtUr: String = "سرگودھا"
)

enum class MachineryStatus {
    PENDING, APPROVED, REJECTED
}

data class Booking(
    val id: String,
    val farmerId: String,
    val farmerName: String,
    val farmerPhone: String = "",
    val machineryId: String,
    val machineryName: String,
    val bookingDate: Long,
    val durationHours: Int,
    val totalPrice: Double,
    val status: BookingStatus,
    val createdAt: Long,
    val locationUr: String = "مقام دستیاب نہیں",
    val acres: Double? = null,
    val rejectionReason: String? = null,
    val lifecyclePhotos: List<BookingLifecyclePhoto> = emptyList()
)

enum class BookingStatus {
    PENDING, ACCEPTED, REJECTED, ACTIVE, COMPLETED, CANCELLED
}

enum class BookingPhotoStep {
    SERVICE_ACQUIRED, SUBSIDY_STARTED, WORK_COMPLETED, FARMER_CONFIRMATION
}

data class BookingLifecyclePhoto(
    val step: BookingPhotoStep,
    val imageLabelUr: String,
    val uploadedAt: Long
)

data class SensorTelemetry(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f
)
