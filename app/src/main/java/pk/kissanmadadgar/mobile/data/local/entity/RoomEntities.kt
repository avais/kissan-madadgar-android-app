package pk.kissanmadadgar.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = FarmerEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["farmer_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = MachineryEntity::class,
            parentColumns = ["id"],
            childColumns = ["machinery_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["farmer_id"]),
        Index(value = ["machinery_id"])
    ]
)
data class BookingEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "farmer_id") val farmerId: String,
    @ColumnInfo(name = "machinery_id") val machineryId: String,
    @ColumnInfo(name = "booking_date") val bookingDate: Long,
    @ColumnInfo(name = "duration_hours") val durationHours: Int,
    @ColumnInfo(name = "total_price") val totalPrice: Double,
    val status: String = "PENDING", // 'PENDING', 'ACCEPTED', 'REJECTED', 'ACTIVE', 'COMPLETED', 'CANCELLED'
    @ColumnInfo(name = "created_at") val createdAt: Long
)
