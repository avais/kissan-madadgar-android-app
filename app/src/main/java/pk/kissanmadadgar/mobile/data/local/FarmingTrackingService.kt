package pk.kissanmadadgar.mobile.data.local

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.location.LocationCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class LocationMetric(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val isMock: Boolean,
    val timestamp: Long
)

data class ActiveSession(
    val bookingId: String,
    val startTimeMs: Long,
    val isPaused: Boolean = false,
    val pausedDurationMs: Long = 0L,
    val lastPauseTimeMs: Long = 0L
)

class FarmingTrackingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var bookingId: String = ""

    companion object {
        private const val NOTIFICATION_ID = 9982
        private const val CHANNEL_ID = "farming_tracker_channel"
        private const val CHANNEL_NAME = "Farming Activity Tracker"
        private const val ACTION_PAUSE = "pk.kissanmadadgar.mobile.ACTION_PAUSE_TRACKING"
        private const val ACTION_RESUME = "pk.kissanmadadgar.mobile.ACTION_RESUME_TRACKING"

        // Set true in onCreate/false in onDestroy of a live Service instance in this process.
        // Defaults to false on a fresh process start, which is exactly what happens after a
        // force-stop, an OS kill, or a reboot — so "session file says active, but this flag is
        // false" reliably means the service isn't actually alive right now, distinct from just
        // being backgrounded (same process, flag still true). Used to decide whether an app
        // launch needs to relaunch the real service instead of trusting the persisted file alone.
        @Volatile
        private var isRunning: Boolean = false

        fun isServiceRunning(): Boolean = isRunning

        fun getActiveSession(context: Context): ActiveSession? {
            val file = File(context.filesDir, "active_farming_session.json")
            if (!file.exists()) return null
            return try {
                val content = file.readText()
                Gson().fromJson(content, ActiveSession::class.java)
            } catch (e: Exception) {
                null
            }
        }

        fun saveSessionObject(context: Context, session: ActiveSession) {
            val file = File(context.filesDir, "active_farming_session.json")
            try {
                file.writeText(Gson().toJson(session))
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Persists the paused flag AND tells the live service instance (if any) to actually stop
        // listening for location updates — previously this only flipped the JSON flag, which
        // saveLocationMetric() checked before *writing* a log entry, but the GPS listener itself
        // stayed registered and the notification/service kept running regardless.
        fun pauseTracking(context: Context) {
            val session = getActiveSession(context)
            if (session != null && !session.isPaused) {
                val updated = session.copy(
                    isPaused = true,
                    lastPauseTimeMs = System.currentTimeMillis()
                )
                saveSessionObject(context, updated)
            }
            sendServiceCommand(context, ACTION_PAUSE)
        }

        fun resumeTracking(context: Context) {
            val session = getActiveSession(context)
            if (session != null && session.isPaused) {
                val duration = System.currentTimeMillis() - session.lastPauseTimeMs
                val updated = session.copy(
                    isPaused = false,
                    pausedDurationMs = session.pausedDurationMs + duration,
                    lastPauseTimeMs = 0L
                )
                saveSessionObject(context, updated)
            }
            sendServiceCommand(context, ACTION_RESUME)
        }

        private fun sendServiceCommand(context: Context, action: String) {
            try {
                val intent = Intent(context, FarmingTrackingService::class.java).setAction(action)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Service may already be gone (e.g. killed while paused) — the persisted session
                // file above is still the source of truth the UI reads from either way.
                android.util.Log.e("FarmingTrackingService", "Failed to send $action", e)
            }
        }

        fun clearActiveSession(context: Context) {
            val file = File(context.filesDir, "active_farming_session.json")
            if (file.exists()) {
                file.delete()
            }
        }

        // The single active_farming_session.json slot only ever holds ONE booking at a time, so
        // if a different booking's tracking gets started, this booking's original start time
        // would otherwise be lost — and re-"resuming" it later would restart the timer from zero.
        // Persist each booking's true start time separately (independent of which booking
        // currently occupies the single active slot) so resuming always continues the real
        // elapsed duration.
        private fun startTimeFile(context: Context, bookingId: String) =
            File(context.filesDir, "farming_start_$bookingId.json")

        fun getPersistedStartTime(context: Context, bookingId: String): Long? {
            val file = startTimeFile(context, bookingId)
            if (!file.exists()) return null
            return try {
                file.readText().trim().toLongOrNull()
            } catch (e: Exception) {
                null
            }
        }

        fun clearPersistedStartTime(context: Context, bookingId: String) {
            val file = startTimeFile(context, bookingId)
            if (file.exists()) {
                file.delete()
            }
        }

        fun startTracking(context: Context, bookingId: String) {
            val startTimeMs = getPersistedStartTime(context, bookingId) ?: System.currentTimeMillis().also {
                try {
                    startTimeFile(context, bookingId).writeText(it.toString())
                } catch (e: Exception) {
                    // Ignore
                }
            }
            // Preserve any existing pause bookkeeping for this same booking (e.g. the service was
            // killed while paused and this is relaunching it) instead of resetting isPaused /
            // pausedDurationMs to defaults, which would silently un-pause a paused session.
            val existing = getActiveSession(context)
            val session = if (existing != null && existing.bookingId == bookingId) {
                existing.copy(startTimeMs = startTimeMs)
            } else {
                ActiveSession(bookingId, startTimeMs)
            }
            saveSessionObject(context, session)
            val intent = Intent(context, FarmingTrackingService::class.java).apply {
                putExtra("booking_id", bookingId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTracking(context: Context, bookingId: String? = null) {
            clearActiveSession(context)
            if (bookingId != null) {
                clearPersistedStartTime(context, bookingId)
            }
            val intent = Intent(context, FarmingTrackingService::class.java)
            context.stopService(intent)
        }

        fun getTrackLogs(context: Context, bookingId: String): List<LocationMetric> {
            val dir = File(context.filesDir, "track_logs")
            val file = File(dir, "${bookingId}_tracks.json")
            if (!file.exists()) return emptyList()
            return try {
                val content = file.readText()
                val type = object : TypeToken<ArrayList<LocationMetric>>() {}.type
                Gson().fromJson(content, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun clearTrackLogs(context: Context, bookingId: String) {
            val dir = File(context.filesDir, "track_logs")
            val file = File(dir, "${bookingId}_tracks.json")
            if (file.exists()) {
                file.delete()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY restarts this service with a null Intent after an OS kill, which used to
        // leave bookingId empty and silently stop GPS logging forever (saveLocationMetric's
        // isBlank() guard) while the UI kept showing a running timer. Fall back to whatever
        // booking the persisted session file says is active so a system-triggered restart
        // actually resumes real tracking instead of doing nothing.
        bookingId = intent?.getStringExtra("booking_id")
            ?: getActiveSession(this)?.bookingId
            ?: bookingId

        createNotificationChannel()

        // Android requires startForeground() within seconds of any startForegroundService()
        // call or the process gets killed with a ForegroundServiceDidNotStartInTimeException —
        // that applies just as much to a PAUSE/RESUME command as to the initial start, since
        // sendServiceCommand() above always calls startForegroundService(), and this might be
        // landing on a freshly-created instance rather than an already-live one. Always call it
        // first, then branch; on an already-live instance this just refreshes the notification.
        when (intent?.action) {
            ACTION_PAUSE -> {
                startForeground(NOTIFICATION_ID, createNotification(paused = true))
                locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            }
            ACTION_RESUME -> {
                startForeground(NOTIFICATION_ID, createNotification(paused = false))
                startLocationTracking()
            }
            else -> {
                // A normal (re)start. If the persisted session says this booking is paused —
                // e.g. the service was killed while paused and is only now being relaunched —
                // come back up paused too, instead of silently resuming active GPS tracking
                // the user had deliberately stopped.
                val currentlyPaused = getActiveSession(this)?.isPaused == true
                startForeground(NOTIFICATION_ID, createNotification(paused = currentlyPaused))
                if (!currentlyPaused) {
                    startLocationTracking()
                }
            }
        }
        return START_STICKY
    }

    private fun startLocationTracking() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            pk.kissanmadadgar.mobile.core.AppConfig.GPS_TRACKING_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(pk.kissanmadadgar.mobile.core.AppConfig.GPS_TRACKING_MIN_UPDATE_INTERVAL_MS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    saveLocationMetric(loc)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            android.util.Log.e("FarmingTrackingService", "Lost location permission. Could not request updates.")
        }
    }

    private fun saveLocationMetric(loc: Location) {
        if (bookingId.isBlank()) return
        val session = getActiveSession(this)
        if (session != null && session.isPaused) {
            android.util.Log.d("FarmingTrackingService", "Skipped logging metric: service is paused")
            return
        }
        // LocationCompat.isMock does this exact SDK_INT branch (isMock on 31+, isFromMockProvider
        // below) internally, without this call site needing to reference the deprecated API directly.
        val isMock = LocationCompat.isMock(loc)
        val metric = LocationMetric(
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitude = loc.altitude,
            speed = loc.speed,
            bearing = loc.bearing,
            accuracy = loc.accuracy,
            isMock = isMock,
            timestamp = System.currentTimeMillis()
        )

        try {
            val dir = File(filesDir, "track_logs")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${bookingId}_tracks.json")
            val list = if (file.exists()) {
                try {
                    val content = file.readText()
                    val type = object : TypeToken<ArrayList<LocationMetric>>() {}.type
                    Gson().fromJson<ArrayList<LocationMetric>>(content, type) ?: ArrayList()
                } catch (e: Exception) {
                    ArrayList()
                }
            } else {
                ArrayList()
            }
            list.add(metric)
            file.writeText(Gson().toJson(list))
            android.util.Log.d("FarmingTrackingService", "Saved track log count = ${list.size} for booking = $bookingId")
        } catch (e: Exception) {
            android.util.Log.e("FarmingTrackingService", "Failed to save track metric", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }

        // stopTracking() (the deliberate "سروس روکیں" flow) clears the session file BEFORE
        // calling stopService(), so if a file still exists here, this teardown came from
        // somewhere else — the user stopping location access from the OS notification/quick
        // settings, a battery manager killing the service, or the OS reclaiming memory. Mark it
        // paused rather than leaving isPaused=false, which would make the UI show a phantom
        // running timer with no GPS tracking actually happening underneath it.
        val session = getActiveSession(this)
        if (session != null && !session.isPaused) {
            saveSessionObject(this, session.copy(isPaused = true, lastPauseTimeMs = System.currentTimeMillis()))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(paused: Boolean = false): Notification {
        val pausedTitle = "کاشتکاری کا ٹریکر وقفے میں ہے"
        val pausedDesc = "لوکیشن ریکارڈنگ فی الحال وقفے میں ہے۔ ایپ میں جا کر دوبارہ شروع کریں۔"
        val runningTitle = "کاشتکاری کا ٹریکر جاری ہے"
        val runningDesc = "محکمۂ زراعت: آپ کے فیلڈ کی لوکیشن اور رفتار ریکارڈ کی جا رہی ہے۔"
        return if (paused) {
            buildTrackerNotification(pausedTitle, pausedDesc)
        } else {
            buildTrackerNotification(runningTitle, runningDesc)
        }
    }

    private fun buildTrackerNotification(title: String, desc: String): Notification {
        
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            putExtra("navigate_booking_id", bookingId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
