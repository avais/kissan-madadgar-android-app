package pk.kissanmadadgar.mobile.core.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pk.kissanmadadgar.mobile.MainActivity
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.security.SessionManager
import pk.kissanmadadgar.mobile.data.remote.api.AuthApiService
import pk.kissanmadadgar.mobile.data.remote.api.PushTokenRequest
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class KissanFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authApiService: AuthApiService

    @Inject
    lateinit var sessionManager: SessionManager

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val notificationIdCounter = AtomicInteger(1000)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val authToken = sessionManager.getAuthToken() ?: return
        serviceScope.launch {
            try {
                val authHeader = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                authApiService.registerPushToken(authHeader, PushTokenRequest(fcmToken = token))
            } catch (e: Exception) {
                // Non-critical: the token will be re-synced on the next successful login.
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Backend sends a data-only payload (see AndroidPushNotificationService) precisely so this
        // method always runs — including while backgrounded — and we always control the tap intent.
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: return
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""
        val bookingId = remoteMessage.data["bookingId"]?.takeIf { it.isNotBlank() }
        showNotification(title, body, bookingId)
    }

    private fun showNotification(title: String, body: String, bookingId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (bookingId != null) {
                putExtra("navigate_booking_id", bookingId)
            }
        }
        val requestCode = bookingId?.hashCode() ?: notificationIdCounter.incrementAndGet()
        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.push_notification_channel_id))
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(notificationIdCounter.incrementAndGet(), notification)
        }
    }
}
