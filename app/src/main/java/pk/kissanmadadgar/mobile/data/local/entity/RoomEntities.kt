package pk.kissanmadadgar.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pk.kissanmadadgar.mobile.domain.model.BookingLifecyclePhoto

@Entity(
    tableName = "users",
    indices = [Index(value = ["phone_number"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    val role: String, // 'FARMER', 'PROVIDER', 'ADMIN'
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String?,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "farmers",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FarmerEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "preferred_language") val preferredLanguage: String = "ur",
    @ColumnInfo(name = "farming_address") val farmingAddress: String?
)

@Entity(
    tableName = "providers",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProviderEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "cnic_number") val cnicNumber: String,
    @ColumnInfo(name = "is_verified") val isVerified: Boolean = false,
    @ColumnInfo(name = "average_rating") val averageRating: Double = 0.0
)

@Entity(tableName = "machinery_categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name_en") val nameEn: String,
    @ColumnInfo(name = "name_ur") val nameUr: String,
    @ColumnInfo(name = "icon_name") val iconName: String
)

@Entity(
    tableName = "machinery",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["provider_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["provider_id"]),
        Index(value = ["category_id"]),
        Index(value = ["latitude", "longitude"])
    ]
)
data class MachineryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "provider_id") val providerId: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "name_ur") val nameUr: String,
    @ColumnInfo(name = "description_ur") val descriptionUr: String,
    @ColumnInfo(name = "hourly_rate") val hourlyRate: Double,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "is_available") val isAvailable: Boolean = true,
    val status: String = "PENDING", // 'PENDING', 'APPROVED', 'REJECTED'
    @ColumnInfo(name = "image_urls") val imageUrls: List<String>, // Needs Custom TypeConverter
    val rating: Double = 0.0,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

/**
 * Full-parity, per-user-scoped local cache of [pk.kissanmadadgar.mobile.domain.model.Booking].
 * Rows arrive straight from the server DTO (see BookingRepositoryImpl), not from locally-created
 * farmer/machinery rows, so — unlike the old version of this entity — there are no foreign keys
 * into FarmerEntity/MachineryEntity.
 */
@Entity(
    tableName = "bookings",
    indices = [
        Index(value = ["owner_user_id"]),
        Index(value = ["farmer_id"]),
        Index(value = ["machinery_id"])
    ]
)
data class BookingEntity(
    @PrimaryKey val id: String,
    // Scopes this row to the logged-in account that fetched it, so switching accounts on a
    // shared device never shows a previous user's cached bookings.
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String,
    @ColumnInfo(name = "farmer_id") val farmerId: String,
    @ColumnInfo(name = "farmer_name") val farmerName: String,
    @ColumnInfo(name = "farmer_phone") val farmerPhone: String,
    @ColumnInfo(name = "machinery_id") val machineryId: String,
    @ColumnInfo(name = "machinery_name") val machineryName: String,
    @ColumnInfo(name = "booking_date") val bookingDate: String,
    @ColumnInfo(name = "duration_hours") val durationHours: Int,
    @ColumnInfo(name = "total_price") val totalPrice: Double,
    val status: String, // BookingStatus enum name: PENDING, ACCEPTED, REJECTED, ACTIVE, COMPLETED, CANCELLED
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "location_ur") val locationUr: String,
    val acres: Double?,
    @ColumnInfo(name = "rejection_reason") val rejectionReason: String?,
    @ColumnInfo(name = "lifecycle_photos") val lifecyclePhotos: List<BookingLifecyclePhoto>, // via KissanConverters
    @ColumnInfo(name = "provider_name") val providerName: String,
    @ColumnInfo(name = "provider_phone") val providerPhone: String,
    @ColumnInfo(name = "machinery_image_url") val machineryImageUrl: String?,
    @ColumnInfo(name = "is_approval_allowed") val isApprovalAllowed: Boolean,
    // Raw server lifecycle status/label — the field the monotonic reconcile rule ranks on.
    @ColumnInfo(name = "rental_request_status") val rentalRequestStatus: String,
    @ColumnInfo(name = "rental_request_status_urdu") val rentalRequestStatusUrdu: String,
    @ColumnInfo(name = "service_provider_id") val serviceProviderId: Long?,
    @ColumnInfo(name = "service_taker_id") val serviceTakerId: Long?,
    @ColumnInfo(name = "is_rating_done", defaultValue = "0") val isRatingDone: Boolean = false,
    // Last time this row was written locally (fetch or optimistic update) — surfaced in the UI
    // as "last updated" so the user knows how stale the cache might be while offline.
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)
