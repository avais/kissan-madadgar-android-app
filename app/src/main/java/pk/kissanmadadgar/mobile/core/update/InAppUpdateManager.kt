package pk.kissanmadadgar.mobile.core.update

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UpdateState {
    data object Idle : UpdateState
    data class ImmediateAvailable(val updateInfo: AppUpdateInfo) : UpdateState
    data class FlexibleAvailable(val updateInfo: AppUpdateInfo) : UpdateState
    data object FlexibleDownloading : UpdateState
    data object FlexibleDownloaded : UpdateState
    data class Failed(val reason: String) : UpdateState
}

/**
 * Sole owner of Play Core in-app-update calls. MainActivity observes [state] and owns the
 * ActivityResultLauncher actually used to launch/resume an update flow, since Play Core requires
 * an Activity-scoped launcher.
 *
 * Convention: set a release's updatePriority >= [IMMEDIATE_UPDATE_PRIORITY_THRESHOLD] (0-5 scale,
 * configured per release in Play Console) to force the blocking Immediate flow; lower priorities
 * use the background Flexible flow.
 */
@Singleton
class InAppUpdateManager @Inject constructor(
    private val appUpdateManager: AppUpdateManager
) {
    companion object {
        private const val TAG = "InAppUpdate"
        const val IMMEDIATE_UPDATE_PRIORITY_THRESHOLD = 4
    }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state

    private val installListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADED -> _state.value = UpdateState.FlexibleDownloaded
            InstallStatus.DOWNLOADING -> _state.value = UpdateState.FlexibleDownloading
            InstallStatus.FAILED -> _state.value = UpdateState.Failed("Flexible update install failed")
            else -> Unit
        }
    }

    fun registerListener() {
        appUpdateManager.registerListener(installListener)
    }

    fun unregisterListener() {
        appUpdateManager.unregisterListener(installListener)
    }

    /**
     * Checks the current update status and decides Immediate vs Flexible from
     * [AppUpdateInfo.updatePriority]. Also resumes a stalled Immediate flow after the app was
     * backgrounded mid-update, and re-surfaces an already-downloaded Flexible update.
     */
    fun checkForUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info -> onAppUpdateInfo(info) }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch app update info", e)
                _state.value = UpdateState.Failed(e.message ?: "unknown error")
            }
    }

    private fun onAppUpdateInfo(info: AppUpdateInfo) {
        _state.value = when {
            info.installStatus() == InstallStatus.DOWNLOADED ->
                UpdateState.FlexibleDownloaded

            info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                UpdateState.ImmediateAvailable(info)

            info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                val isHighPriority = info.updatePriority() >= IMMEDIATE_UPDATE_PRIORITY_THRESHOLD
                when {
                    isHighPriority && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                        UpdateState.ImmediateAvailable(info)
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ->
                        UpdateState.FlexibleAvailable(info)
                    else -> {
                        Log.w(TAG, "Update available (priority=${info.updatePriority()}) but no allowed update type")
                        UpdateState.Idle
                    }
                }
            }

            else -> UpdateState.Idle
        }
    }

    /** Launches (or resumes) the given update flow using the Activity-owned launcher. */
    fun startUpdateFlow(
        updateInfo: AppUpdateInfo,
        type: Int,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                updateInfo,
                launcher,
                AppUpdateOptions.newBuilder(type).build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "startUpdateFlowForResult failed", e)
            _state.value = UpdateState.Failed(e.message ?: "unknown error")
        }
    }

    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }
}
