package pk.kissanmadadgar.mobile.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pk.kissanmadadgar.mobile.domain.model.BookingLifecyclePhoto

class KissanConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromLifecyclePhotoList(value: List<BookingLifecyclePhoto>?): String {
        return Gson().toJson(value ?: emptyList<BookingLifecyclePhoto>())
    }

    @TypeConverter
    fun toLifecyclePhotoList(value: String?): List<BookingLifecyclePhoto> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<BookingLifecyclePhoto>>() {}.type
            Gson().fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
