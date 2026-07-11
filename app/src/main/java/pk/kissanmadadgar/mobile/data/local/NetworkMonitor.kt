package pk.kissanmadadgar.mobile.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Single shared source of truth for connectivity checks. Replaces the two previously-duplicated
 * ConnectivityManager checks (FarmingUploadSyncManager.isInternetAvailable and a private
 * isNetworkAvailable in BookingLifecycleComponents.kt).
 */
object NetworkMonitor {

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Reactive connectivity stream driven by NetworkCallback (not polling), so the offline
     * banner and auto-retry-on-reconnect logic react the moment the OS reports a change instead
     * of on some fixed interval.
     */
    fun observe(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        trySend(isOnline(context))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(isOnline(context))
            }

            override fun onLost(network: Network) {
                trySend(isOnline(context))
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(isOnline(context))
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
