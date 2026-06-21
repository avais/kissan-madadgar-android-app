package pk.kissanmadadgar.mobile.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object RoleSelection : Screen("role_selection")
    object FarmerLogin : Screen("farmer_login")
    object FarmerRegister : Screen("farmer_register")
    object SupplierLogin : Screen("supplier_login")
    object SupplierOtp : Screen("supplier_otp/{cnic}") {
        fun createRoute(cnic: String) = "supplier_otp/$cnic"
    }
    object AdminLogin : Screen("admin_login")
    object OtpVerification : Screen("otp_verification/{phoneNumber}/{role}") {
        fun createRoute(phoneNumber: String, role: String) = "otp_verification/$phoneNumber/$role"
    }
    
    // Farmer Portal
    object FarmerHome : Screen("farmer_home")
    object MachineryDetail : Screen("machinery_detail/{machineryId}") {
        fun createRoute(machineryId: String) = "machinery_detail/$machineryId"
    }
    object BookingConfirmation : Screen("booking_confirmation/{machineryId}") {
        fun createRoute(machineryId: String) = "booking_confirmation/$machineryId"
    }

    // Provider Portal
    object ProviderDashboard : Screen("provider_dashboard")
    object AddMachinery : Screen("add_machinery")

    // Admin Portal
    object AdminDashboard : Screen("admin_dashboard")
}
