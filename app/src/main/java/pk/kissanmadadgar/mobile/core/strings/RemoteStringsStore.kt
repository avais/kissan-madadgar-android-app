package pk.kissanmadadgar.mobile.core.strings

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pk.kissanmadadgar.mobile.data.remote.api.AuthApiService
import java.util.concurrent.TimeUnit

// Caches the backend's Urdu display-text catalog (GET api/auth/android/getmessagesvalue), keyed
// by the same names used in res/values/strings.xml, so copy can be corrected/updated from the
// server without an app release. Consumed transparently via RemoteStringsContextWrapper, which
// falls back to the bundled strings.xml value for any key this store doesn't have (not yet
// fetched, fetch failed, or the key simply isn't present in the response) — so this cache being
// empty or stale never breaks anything that already worked off the bundled resources.
class RemoteStringsStore private constructor(context: Context) {
    // Deliberately NOT context.applicationContext: this is constructed from
    // KissanApplication.attachBaseContext(), and Context.getApplicationContext() returns null
    // at that point in the app's bootstrap (the framework hasn't registered the Application as
    // its own applicationContext yet) — that null caused a crash on every launch. A plain
    // getSharedPreferences() call works fine directly on the Context we're given.
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var cachedMap: Map<String, String> = loadFromDisk()

    private fun loadFromDisk(): Map<String, String> {
        val json = prefs.getString(KEY_PAYLOAD, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson<Map<String, String>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse cached remote strings", e)
            emptyMap()
        }
    }

    fun get(key: String): String? = cachedMap[key]

    // Only hits the network if the cache has never been populated or is older than 24 hours
    // (or [force] is set). Any failure (network, non-2xx, parse) is logged and swallowed —
    // whatever was already cached (possibly nothing) simply keeps being used.
    suspend fun refreshIfNeeded(apiService: AuthApiService, force: Boolean = false) {
        val lastFetchedAt = prefs.getLong(KEY_FETCHED_AT, 0L)
        val isStale = force || (System.currentTimeMillis() - lastFetchedAt) > TimeUnit.HOURS.toMillis(24)
        if (!isStale) return

        try {
            val response = withContext(Dispatchers.IO) { apiService.getMessagesValue() }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                cachedMap = body
                prefs.edit()
                    .putString(KEY_PAYLOAD, Gson().toJson(body))
                    .putLong(KEY_FETCHED_AT, System.currentTimeMillis())
                    .apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh remote strings", e)
        }
    }

    companion object {
        private const val TAG = "RemoteStringsStore"
        private const val PREFS_NAME = "kissan_remote_strings"
        private const val KEY_PAYLOAD = "payload"
        private const val KEY_FETCHED_AT = "fetched_at"

        @Volatile
        private var instance: RemoteStringsStore? = null

        fun getInstance(context: Context): RemoteStringsStore =
            instance ?: synchronized(this) {
                instance ?: RemoteStringsStore(context).also { instance = it }
            }
    }
}
