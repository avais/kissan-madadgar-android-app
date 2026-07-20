package pk.kissanmadadgar.mobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.play.core.install.model.AppUpdateType
import dagger.hilt.android.AndroidEntryPoint
import pk.kissanmadadgar.mobile.core.components.UpdateReadyDialog
import pk.kissanmadadgar.mobile.core.navigation.Screen
import pk.kissanmadadgar.mobile.core.strings.RemoteStringsContextWrapper
import pk.kissanmadadgar.mobile.core.theme.KissanMadadgarTheme
import pk.kissanmadadgar.mobile.core.update.InAppUpdateManager
import pk.kissanmadadgar.mobile.core.update.UpdateState
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import pk.kissanmadadgar.mobile.presentation.screens.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var inAppUpdateManager: InAppUpdateManager

    private val updateResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                Log.e("InAppUpdate", "Update flow did not complete, resultCode=${result.resultCode}")
            }
        }

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var sharedViewModel: MainViewModel? = null

    private val sensorListener = object : SensorEventListener {
        private var accelValues = FloatArray(3)
        private var gyroValues = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            val vm = sharedViewModel ?: return
            
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelValues = event.values.clone()
            } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroValues = event.values.clone()
            }
            
            vm.updateSensorTelemetry(
                accelValues[0], accelValues[1], accelValues[2],
                gyroValues[0], gyroValues[1], gyroValues[2]
            )
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            sharedViewModel?.updateUserLocation(location.latitude, location.longitude)
        }
        @Deprecated("Deprecated in parent class")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun startLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, locationListener)
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 5f, locationListener)
                }
                
                val lastKnownGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastKnownNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val bestLocation = lastKnownGps ?: lastKnownNetwork
                bestLocation?.let {
                    sharedViewModel?.updateUserLocation(it.latitude, it.longitude)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val locationIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (locationIndex != -1 && grantResults.isNotEmpty() && locationIndex < grantResults.size && grantResults[locationIndex] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
        startLocationUpdates()
        inAppUpdateManager.registerListener()
        inAppUpdateManager.checkForUpdate()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        locationManager.removeUpdates(locationListener)
        inAppUpdateManager.unregisterListener()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(RemoteStringsContextWrapper.wrap(newBase))
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("navigate_booking_id")?.let { bId ->
            // Same deep-link target as the in-app notifications list (see NotificationsScreen's
            // onClick) — land on the Bookings tab; FarmerBookingsTab (MyBookings.kt) resolves
            // the booking, switches to its correct status filter, and opens its detail sheet.
            sharedViewModel?.setSelectedTab(2)
            sharedViewModel?.setNotificationBookingId(bId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 100)
        }

        inAppUpdateManager.checkForUpdate()

        setContent {
            KissanMadadgarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                      Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        val viewModel: MainViewModel = hiltViewModel()
                        sharedViewModel = viewModel

                        val updateState by inAppUpdateManager.state.collectAsState()
                        LaunchedEffect(updateState) {
                            when (val current = updateState) {
                                is UpdateState.ImmediateAvailable ->
                                    inAppUpdateManager.startUpdateFlow(current.updateInfo, AppUpdateType.IMMEDIATE, updateResultLauncher)
                                is UpdateState.FlexibleAvailable ->
                                    inAppUpdateManager.startUpdateFlow(current.updateInfo, AppUpdateType.FLEXIBLE, updateResultLauncher)
                                else -> Unit
                            }
                        }
                        if (updateState is UpdateState.FlexibleDownloaded) {
                            UpdateReadyDialog(
                                onConfirm = { inAppUpdateManager.completeFlexibleUpdate() },
                                onDismiss = {}
                            )
                        }

                        LaunchedEffect(intent) {
                            intent.getStringExtra("navigate_booking_id")?.let { bId ->
                                // Same deep-link target as the in-app notifications list — land
                                // on the Bookings tab; FarmerBookingsTab (MyBookings.kt) resolves
                                // the booking, switches to its correct status filter, and opens
                                // its detail sheet.
                                viewModel.setSelectedTab(2)
                                viewModel.setNotificationBookingId(bId)
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = Screen.Splash.route
                        ) {
                            composable(Screen.Splash.route) {
                                SplashScreen(
                                    onNavigateNext = {
                                        val role = viewModel.selectedRole.value
                                        val user = viewModel.currentUser.value
                                        if (role != null && user != null) {
                                            val dest = when (role) {
                                                pk.kissanmadadgar.mobile.domain.model.UserRole.FARMER -> Screen.FarmerHome.route
                                                pk.kissanmadadgar.mobile.domain.model.UserRole.PROVIDER -> Screen.FarmerHome.route
                                                pk.kissanmadadgar.mobile.domain.model.UserRole.ADMIN -> Screen.FarmerHome.route
                                            }
                                            navController.navigate(dest) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(Screen.Onboarding.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            composable(Screen.Onboarding.route) {
                                OnboardingScreen(
                                    onFinish = {
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                )
                            }



                            composable(
                                route = Screen.FarmerLogin.route,
                                arguments = listOf(
                                    navArgument("requireCnic") {
                                        type = NavType.BoolType
                                        defaultValue = false
                                    }
                                )
                            ) { backStackEntry ->
                                val requireCnic = backStackEntry.arguments?.getBoolean("requireCnic") ?: false
                                FarmerAuthScreen(
                                    viewModel = viewModel,
                                    onDismiss = { navController.popBackStack() },
                                    onSuccess = {
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.FarmerHome.route) { inclusive = true }
                                        }
                                    },
                                    isDialog = false,
                                    requireCnic = requireCnic
                                )
                            }

                            composable(Screen.FarmerRegister.route) {
                                FarmerAuthScreen(
                                    viewModel = viewModel,
                                    onDismiss = { navController.popBackStack() },
                                    onSuccess = {
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.FarmerHome.route) { inclusive = true }
                                        }
                                    },
                                    isDialog = false
                                )
                            }




                            composable(
                                route = Screen.OtpVerification.route,
                                arguments = listOf(
                                    navArgument("phoneNumber") { type = NavType.StringType },
                                    navArgument("role") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val phone = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                                
                                OtpVerificationScreen(
                                    phoneNumber = phone,
                                    viewModel = viewModel,
                                    onSuccess = {
                                        Toast.makeText(this@MainActivity, getString(R.string.login_success_toast), Toast.LENGTH_SHORT).show()
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.FarmerHome.route) { inclusive = true }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Farmer Main Dash
                            composable(Screen.FarmerHome.route) {
                                FarmerHomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetail = { machineryId ->
                                        navController.navigate(Screen.MachineryDetail.createRoute(machineryId))
                                    },
                                    onNavigateToBooking = { machineryId ->
                                        navController.navigate(Screen.BookingConfirmation.createRoute(machineryId))
                                    },
                                    onLoginRedirect = { requireCnic ->
                                        navController.navigate(Screen.FarmerLogin.createRoute(requireCnic))
                                    },
                                    // FarmerHomeScreen itself gates this on
                                    // MainViewModel.requestLogout() (blocks on unsynced uploads)
                                    // and only invokes this callback once logout actually
                                    // completed — this is just the post-logout navigation.
                                    onLogout = {
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onNavigateToRegisterMachinery = {
                                        navController.navigate(Screen.RegisterAgriculturalMachinery.route)
                                    },
                                    onNavigateToNotifications = {
                                        // TEMP DEBUG (BellCrashDebug): logging every tap plus the
                                        // back-stack state right before navigating, to pin down
                                        // the still-unexplained white screen now that
                                        // launchSingleTop alone didn't fix it. Remove once solved.
                                        val stackBefore = navController.currentBackStack.value.map { it.destination.route }
                                        android.util.Log.d("BellCrashDebug", "bell tapped at ${System.currentTimeMillis()}; backstack before=$stackBefore; currentDest=${navController.currentDestination?.route}")
                                        if (navController.currentDestination?.route == Screen.FarmerHome.route) {
                                            navController.navigate(Screen.Notifications.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                        val stackAfter = navController.currentBackStack.value.map { it.destination.route }
                                        android.util.Log.d("BellCrashDebug", "after navigate(); backstack after=$stackAfter")
                                    },
                                    onNavigateToGovernmentSchemes = {
                                        navController.navigate(Screen.GovernmentSchemes.route)
                                    }
                                )
                            }

                            composable(Screen.Notifications.route) {
                                NotificationsScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        // TEMP DEBUG (BellCrashDebug): mirrors the bell-tap logging
                                        // above, on the other side of the nav — pinning down whether
                                        // the white screen comes from the back press itself. Remove
                                        // once solved.
                                        val stackBefore = navController.currentBackStack.value.map { it.destination.route }
                                        android.util.Log.d("BellCrashDebug", "back pressed at ${System.currentTimeMillis()}; backstack before=$stackBefore; currentDest=${navController.currentDestination?.route}")
                                        val popped = if (navController.currentDestination?.route == Screen.Notifications.route) {
                                            navController.popBackStack()
                                        } else {
                                            false
                                        }
                                        val stackAfter = navController.currentBackStack.value.map { it.destination.route }
                                        android.util.Log.d("BellCrashDebug", "after popBackStack(); popped=$popped; backstack after=$stackAfter; currentDest=${navController.currentDestination?.route}")
                                    },
                                    onNavigateToBooking = {
                                        // Land back on the Bookings tab (index 2) so
                                        // FarmerBookingsTab is actually composed and its
                                        // notificationBookingId effect can run.
                                        viewModel.setSelectedTab(2)
                                        if (navController.currentDestination?.route == Screen.Notifications.route) {
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            }

                            composable(Screen.GovernmentSchemes.route) {
                                GovernmentSchemesScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.RegisterAgriculturalMachinery.route) {
                                RegisterAgriculturalMachineryScreen(
                                    viewModel = viewModel,
                                    onSuccess = {
                                        viewModel.setSelectedTab(3)
                                        navController.popBackStack()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Screen.MachineryDetail.route,
                                arguments = listOf(navArgument("machineryId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("machineryId") ?: ""
                                MachineryDetailScreen(
                                    machineryId = id,
                                    viewModel = viewModel,
                                    onNavigateToBooking = {
                                        if (viewModel.currentUser.value == null) {
                                            navController.navigate(Screen.FarmerLogin.createRoute(false))
                                        } else {
                                            navController.navigate(Screen.BookingConfirmation.createRoute(id))
                                        }
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Screen.BookingConfirmation.route,
                                arguments = listOf(navArgument("machineryId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("machineryId") ?: ""
                                BookingConfirmationScreen(
                                    machineryId = id,
                                    viewModel = viewModel,
                                    onSuccess = {
                                        Toast.makeText(this@MainActivity, getString(R.string.booking_success_toast), Toast.LENGTH_LONG).show()
                                        viewModel.setSelectedTab(2)
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.FarmerHome.route) { inclusive = true }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }



                        }
                      }
                    }
                }
            }
        }
    }
}
