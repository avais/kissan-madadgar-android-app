package pk.kissanmadadgar.mobile.data.remote

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pk.kissanmadadgar.mobile.core.security.SessionManager
import pk.kissanmadadgar.mobile.data.remote.api.AuthApiService
import pk.kissanmadadgar.mobile.data.remote.api.StartFarmingTakerRequest
import pk.kissanmadadgar.mobile.data.local.FarmingTrackingService
import java.io.File

enum class UploadOutcome { SYNCED, QUEUED }

data class PendingUpload(
    val actionType: String = "START", // "START" or "COMPLETE"
    val bookingId: String,
    val localFilePath: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double = 0.0,
    val speed: Double = 0.0,
    val heading: Double = 0.0,
    val altitude: Double = 0.0,
    val isMock: Boolean = false,
    val deviceId: String = "",
    val timestamp: Long
)

class FarmingUploadSyncManager private constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val context: Context
) {
    private val syncJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + syncJob)
    private var isPolling = false
    private var onSyncSuccessCallback: (() -> Unit)? = null
    private var consecutiveFailures = 0

    fun hasPendingUploads(): Boolean {
        return getQueue().isNotEmpty()
    }

    /**
     * One-shot attempt to drain the entire pending-upload queue right now, on the caller's own
     * coroutine — used to give logout a chance to sync before it proceeds instead of waiting on
     * the next ~10s poll tick. Returns true if the queue is empty by the time this returns
     * (nothing was queued, or everything just synced); false if there's still at least one item
     * left (offline, or a real upload failure).
     */
    suspend fun flushNow(): Boolean {
        if (getQueue().isEmpty()) return true
        if (!isInternetAvailable()) return false
        processQueueAndReturnStatus()
        return getQueue().isEmpty()
    }

    companion object {
        @Volatile
        private var INSTANCE: FarmingUploadSyncManager? = null

        fun getInstance(
            apiService: AuthApiService,
            sessionManager: SessionManager,
            context: Context
        ): FarmingUploadSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FarmingUploadSyncManager(apiService, sessionManager, context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun enqueueUpload(
        actionType: String = "START",
        bookingId: String,
        localFilePath: String,
        latitude: Double,
        longitude: Double,
        accuracy: Double = 0.0,
        speed: Double = 0.0,
        heading: Double = 0.0,
        altitude: Double = 0.0,
        isMock: Boolean = false,
        deviceId: String = ""
    ) {
        val pending = PendingUpload(
            actionType = actionType,
            bookingId = bookingId,
            localFilePath = localFilePath,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            heading = heading,
            altitude = altitude,
            isMock = isMock,
            deviceId = deviceId,
            timestamp = System.currentTimeMillis()
        )
        
        scope.launch {
            try {
                val queue = getQueue()
                queue.add(pending)
                saveQueue(queue)
                android.util.Log.d("FarmingUploadSyncManager", "Enqueued pending upload $actionType for bookingId=$bookingId")
            } catch (e: Exception) {
                android.util.Log.e("FarmingUploadSyncManager", "Failed to enqueue upload", e)
            }
        }
    }

    fun startPolling(onSyncSuccess: () -> Unit) {
        onSyncSuccessCallback = onSyncSuccess
        if (isPolling) return

        if (getQueue().isEmpty()) {
            android.util.Log.d("FarmingUploadSyncManager", "Queue is empty. Skip startPolling.")
            return
        }

        isPolling = true
        consecutiveFailures = 0

        scope.launch {
            while (isPolling) {
                if (isInternetAvailable()) {
                    val queueBefore = getQueue()
                    if (queueBefore.isEmpty()) {
                        stopPolling()
                        break
                    }

                    val success = processQueueAndReturnStatus()

                    val queueAfter = getQueue()
                    if (queueAfter.isEmpty()) {
                        android.util.Log.d("FarmingUploadSyncManager", "Queue is empty. Stopping polling loop.")
                        stopPolling()
                        break
                    }

                    if (success) {
                        consecutiveFailures = 0
                    } else {
                        consecutiveFailures++
                        android.util.Log.d("FarmingUploadSyncManager", "Sync failure count = $consecutiveFailures")
                        if (consecutiveFailures >= 50) {
                            android.util.Log.w("FarmingUploadSyncManager", "Reached 50 failures. Stopping polling loop.")
                            stopPolling()
                            break
                        }
                    }
                }
                
                val delayTime = if (consecutiveFailures > 0) 100000L else 10000L
                delay(delayTime)
            }
        }
    }

    fun stopPolling() {
        isPolling = false
    }

    private suspend fun processQueueAndReturnStatus(): Boolean {
        while (true) {
            val queue = getQueue()
            if (queue.isEmpty()) return true

            val item = queue.first()
            val file = File(item.localFilePath)
            if (!file.exists()) {
                // File does not exist, remove it from queue
                queue.removeAt(0)
                saveQueue(queue)
                continue
            }

            val success = performUpload(item)
            if (!success) return false

            // Remove from queue now that the upload actually succeeded.
            queue.removeAt(0)
            saveQueue(queue)

            withContext(Dispatchers.Main) {
                val toastMsg = if (item.actionType == "COMPLETE") {
                    "کاشتکاری مکمل کرنے کا ڈیٹا کامیابی سے اپلوڈ ہو گیا ہے۔"
                } else {
                    "کاشتکاری شروع کرنے کا ڈیٹا کامیابی سے اپلوڈ ہو گیا ہے۔"
                }
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                onSyncSuccessCallback?.invoke()
            }
        }
    }

    /**
     * Uploads the photo and calls the matching start/complete endpoint for a single item.
     * Shared by the background queue processor above and the synchronous online path below —
     * the network work is identical either way, only what happens before/after it (queueing vs.
     * an immediate result) differs. Does NOT touch the on-disk queue itself; callers decide
     * whether/how to persist the item depending on which path they're on.
     */
    private suspend fun performUpload(item: PendingUpload): Boolean {
        val file = File(item.localFilePath)
        if (!file.exists()) return true

        return try {
            val token = sessionManager.getAuthToken() ?: sessionManager.getGuestToken() ?: ""
            val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"

            // 1. Upload file to /api/files/upload
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val uploadResponse = apiService.uploadFile(multipartBody)

            if (!uploadResponse.isSuccessful || uploadResponse.body() == null) {
                android.util.Log.e("FarmingUploadSyncManager", "Failed file upload API call code=${uploadResponse.code()}")
                return false
            }

            val responseMap = uploadResponse.body()!!
            val rawFileName = responseMap["fileName"] ?: ""

            // Extract UUID (strip extension)
            val pictureUuid = if (rawFileName.contains(".")) {
                rawFileName.substring(0, rawFileName.lastIndexOf('.'))
            } else {
                rawFileName
            }

            // 2. Call kickoff or completion endpoint
            val trackList = FarmingTrackingService.getTrackLogs(context, item.bookingId)
            val metaDataJson = if (item.actionType == "COMPLETE" && trackList.isNotEmpty()) {
                Gson().toJson(trackList)
            } else {
                val metaDataMap = mapOf(
                    "latitude" to item.latitude,
                    "longitude" to item.longitude,
                    "timestamp" to item.timestamp
                )
                Gson().toJson(metaDataMap)
            }

            val bookingIdLong = item.bookingId.toLongOrNull() ?: 0L
            val startResponse = if (item.actionType == "COMPLETE") {
                val gpsLogs = if (trackList.isNotEmpty()) {
                    trackList.map { metric ->
                        pk.kissanmadadgar.mobile.data.remote.api.GpsLogRequest(
                            latitude = metric.latitude,
                            longitude = metric.longitude,
                            deviceId = item.deviceId,
                            accuracy = 5.0,
                            speed = metric.speed.toDouble(),
                            heading = metric.bearing.toDouble(),
                            altitude = metric.altitude,
                            mockLocationDetection = metric.isMock
                        )
                    }
                } else {
                    listOf(
                        pk.kissanmadadgar.mobile.data.remote.api.GpsLogRequest(
                            latitude = item.latitude,
                            longitude = item.longitude,
                            deviceId = item.deviceId,
                            accuracy = item.accuracy,
                            speed = item.speed,
                            heading = item.heading,
                            altitude = item.altitude,
                            mockLocationDetection = item.isMock
                        )
                    )
                }

                val request = pk.kissanmadadgar.mobile.data.remote.api.StartFarmingProviderRequest(
                    rentalBookingId = bookingIdLong,
                    endPicture = pictureUuid,
                    endPictureMetaData = metaDataJson,
                    gpsLogs = gpsLogs
                )
                apiService.startFarmingProvider(token = authHeader, request = request)
            } else {
                apiService.startFarmingTaker(
                    token = authHeader,
                    request = StartFarmingTakerRequest(
                        rentalBookingId = bookingIdLong,
                        startPicture = pictureUuid,
                        startPictureMetaData = metaDataJson
                    )
                )
            }

            if (startResponse.isSuccessful) {
                val logType = if (item.actionType == "COMPLETE") "start-farming-provider" else "start-farming-taker"
                android.util.Log.d("FarmingUploadSyncManager", "Successfully registered $logType for bookingId=${item.bookingId}")

                if (file.exists()) {
                    file.delete()
                }
                if (item.actionType == "COMPLETE") {
                    FarmingTrackingService.clearTrackLogs(context, item.bookingId)
                }
                true
            } else {
                android.util.Log.e("FarmingUploadSyncManager", "Failed API call code=${startResponse.code()}")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("FarmingUploadSyncManager", "Error processing upload item for bookingId=${item.bookingId}", e)
            false
        }
    }

    /**
     * Generic entry point for a farming start/complete action: if the device currently has
     * internet, the upload is attempted right now and this suspends until the real result is
     * known (SYNCED on success) — no more waiting on the next ~10s poll tick just because
     * connectivity happened to be fine. If there's no internet, or the immediate attempt hits a
     * transient failure (dropped connection, server hiccup), the action is written to the same
     * on-disk queue as before and picked up by the background poller (QUEUED) — nothing is ever
     * silently lost either way, it just degrades to the existing best-effort retry path.
     */
    suspend fun submitOrQueue(
        actionType: String = "START",
        bookingId: String,
        localFilePath: String,
        latitude: Double,
        longitude: Double,
        accuracy: Double = 0.0,
        speed: Double = 0.0,
        heading: Double = 0.0,
        altitude: Double = 0.0,
        isMock: Boolean = false,
        deviceId: String = "",
        onQueuedSyncSuccess: () -> Unit
    ): UploadOutcome {
        if (isInternetAvailable()) {
            val item = PendingUpload(
                actionType = actionType,
                bookingId = bookingId,
                localFilePath = localFilePath,
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                speed = speed,
                heading = heading,
                altitude = altitude,
                isMock = isMock,
                deviceId = deviceId,
                timestamp = System.currentTimeMillis()
            )
            val success = withContext(Dispatchers.IO) { performUpload(item) }
            if (success) {
                return UploadOutcome.SYNCED
            }
            // Had internet but the call itself failed — fall through to the queue instead of
            // losing the action.
        }

        enqueueUpload(actionType, bookingId, localFilePath, latitude, longitude, accuracy, speed, heading, altitude, isMock, deviceId)
        startPolling(onQueuedSyncSuccess)
        return UploadOutcome.QUEUED
    }

    private fun getQueueFile(): File {
        val dir = File(context.filesDir, "uploads")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "pending_uploads.json")
    }

    private fun getQueue(): ArrayList<PendingUpload> {
        val file = getQueueFile()
        if (!file.exists()) return ArrayList()
        return try {
            val content = file.readText()
            val type = object : TypeToken<ArrayList<PendingUpload>>() {}.type
            Gson().fromJson(content, type) ?: ArrayList()
        } catch (e: Exception) {
            ArrayList()
        }
    }

    private fun saveQueue(queue: ArrayList<PendingUpload>) {
        try {
            val file = getQueueFile()
            file.writeText(Gson().toJson(queue))
        } catch (e: Exception) {
            android.util.Log.e("FarmingUploadSyncManager", "Failed to save queue", e)
        }
    }

    private fun isInternetAvailable(): Boolean {
        return pk.kissanmadadgar.mobile.data.local.NetworkMonitor.isOnline(context)
    }
}
