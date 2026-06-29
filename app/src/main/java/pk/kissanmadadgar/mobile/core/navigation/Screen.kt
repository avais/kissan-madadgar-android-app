package pk.kissanmadadgar.mobile.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object FarmerLogin : Screen("farmer_login?requireCnic={requireCnic}") {
        fun createRoute(requireCnic: Boolean) = "farmer_login?requireCnic=$requireCnic"
    }
    object FarmerRegister : Screen("farmer_register")
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
    object RegisterAgriculturalMachinery : Screen("register_agricultural_machinery")
}
