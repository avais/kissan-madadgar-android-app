package pk.kissanmadadgar.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pk.kissanmadadgar.mobile.data.local.converter.KissanConverters
import pk.kissanmadadgar.mobile.data.local.dao.*
import pk.kissanmadadgar.mobile.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        FarmerEntity::class,
        ProviderEntity::class,
        CategoryEntity::class,
        MachineryEntity::class,
        BookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(KissanConverters::class)
abstract class KissanDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun machineryDao(): MachineryDao
    abstract fun bookingDao(): BookingDao
}
