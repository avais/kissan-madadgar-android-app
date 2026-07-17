package pk.kissanmadadgar.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

// Does NOT override attachBaseContext() to wrap with RemoteStringsContextWrapper (unlike
// MainActivity, which still does) — ActivityThread.handleReceiver() hard-casts the Application's
// base context to android.app.ContextImpl when instantiating certain BroadcastReceivers, and a
// wrapped (non-ContextImpl) Application context throws a ClassCastException there. That crashed
// this app on every incoming push, because Google Play Services' legacy GCM compatibility receiver
// (com.google.firebase.iid.FirebaseInstanceIdReceiver, bundled by firebase-messaging itself) is
// what several OEM builds still use to actually deliver FCM messages. All real UI text still goes
// through MainActivity's own attachBaseContext() wrap — every stringResource()/getString() call
// in Compose screens resolves via the Activity context, not the Application context — so remote
// string overrides keep working everywhere that matters. Only the notification channel name/
// description built below (a non-critical, rarely-seen string) and the FCM service's channel-id
// lookup lose remote-override support, which is an acceptable trade-off against the app crashing
// outright on every push.
@HiltAndroidApp
class KissanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createPushNotificationChannel()
    }

    private fun createPushNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.push_notification_channel_id),
                getString(R.string.push_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.push_notification_channel_description)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
