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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
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
import dagger.hilt.android.AndroidEntryPoint
import pk.kissanmadadgar.mobile.core.navigation.Screen
import pk.kissanmadadgar.mobile.core.theme.KissanMadadgarTheme
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import pk.kissanmadadgar.mobile.presentation.screens.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        locationManager.removeUpdates(locationListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }

        setContent {
            KissanMadadgarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val viewModel: MainViewModel = hiltViewModel()
                        sharedViewModel = viewModel

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
                                                pk.kissanmadadgar.mobile.domain.model.UserRole.PROVIDER -> Screen.ProviderDashboard.route
                                                pk.kissanmadadgar.mobile.domain.model.UserRole.ADMIN -> Screen.AdminDashboard.route
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
                                        navController.navigate(Screen.RoleSelection.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Screen.RoleSelection.route) {
                                RoleSelectionScreen(
                                    viewModel = viewModel,
                                    onRoleSelected = { role ->
                                        when (role) {
                                            pk.kissanmadadgar.mobile.domain.model.UserRole.FARMER -> {
                                                navController.navigate(Screen.FarmerHome.route)
                                            }
                                            pk.kissanmadadgar.mobile.domain.model.UserRole.PROVIDER -> {
                                                navController.navigate(Screen.SupplierLogin.route)
                                            }
                                            pk.kissanmadadgar.mobile.domain.model.UserRole.ADMIN -> {
                                                navController.navigate(Screen.AdminLogin.route)
                                            }
                                        }
                                    }
                                )
                            }

                            composable(Screen.FarmerLogin.route) {
                                FarmerAuthScreen(
                                    viewModel = viewModel,
                                    onDismiss = { navController.popBackStack() },
                                    onSuccess = {
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                        }
                                    },
                                    isDialog = false
                                )
                            }

                            composable(Screen.FarmerRegister.route) {
                                FarmerAuthScreen(
                                    viewModel = viewModel,
                                    onDismiss = { navController.popBackStack() },
                                    onSuccess = {
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                        }
                                    },
                                    isDialog = false
                                )
                            }

                            composable(Screen.SupplierLogin.route) {
                                SupplierLoginScreen(
                                    viewModel = viewModel,
                                    onNavigateToOtp = { cnic ->
                                        navController.navigate(
                                            Screen.SupplierOtp.createRoute(cnic)
                                        )
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Screen.SupplierOtp.route,
                                arguments = listOf(navArgument("cnic") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val cnic = backStackEntry.arguments?.getString("cnic") ?: ""
                                SupplierOtpVerificationScreen(
                                    cnic = cnic,
                                    viewModel = viewModel,
                                    onNavigateToHome = {
                                        navController.navigate(Screen.ProviderDashboard.route) {
                                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.AdminLogin.route) {
                                AdminLoginScreen(
                                    viewModel = viewModel,
                                    onSuccess = {
                                        navController.navigate(Screen.AdminDashboard.route) {
                                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
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
                                val role = backStackEntry.arguments?.getString("role") ?: "FARMER"
                                
                                OtpVerificationScreen(
                                    phoneNumber = phone,
                                    viewModel = viewModel,
                                    onSuccess = {
                                        Toast.makeText(this@MainActivity, getString(R.string.login_success_toast), Toast.LENGTH_SHORT).show()
                                        if (role == "FARMER") {
                                            navController.navigate(Screen.FarmerHome.route) {
                                                popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(Screen.ProviderDashboard.route) {
                                                popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                            }
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
                                    onLoginRedirect = {
                                        navController.navigate(Screen.FarmerLogin.route)
                                    },
                                    onLogout = {
                                        viewModel.logout {
                                            navController.navigate(Screen.RoleSelection.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
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
                                            navController.navigate(Screen.FarmerLogin.route)
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
                                        navController.navigate(Screen.FarmerHome.route) {
                                            popUpTo(Screen.FarmerHome.route) { inclusive = true }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Provider Main Dash
                            composable(Screen.ProviderDashboard.route) {
                                ProviderDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAddMachinery = {
                                        navController.navigate(Screen.AddMachinery.route)
                                    },
                                    onLogout = {
                                        viewModel.logout {
                                            navController.navigate(Screen.RoleSelection.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            composable(Screen.AddMachinery.route) {
                                AddMachineryScreen(
                                    viewModel = viewModel,
                                    onSuccess = {
                                        Toast.makeText(this@MainActivity, getString(R.string.machinery_registration_toast), Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Admin Main Dash
                            composable(Screen.AdminDashboard.route) {
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onLogout = {
                                        viewModel.logout {
                                            navController.navigate(Screen.RoleSelection.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
