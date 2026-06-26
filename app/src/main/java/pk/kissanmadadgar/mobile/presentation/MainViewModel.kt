package pk.kissanmadadgar.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.security.SessionManager
import pk.kissanmadadgar.mobile.domain.model.*
import pk.kissanmadadgar.mobile.domain.repository.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepo: AuthRepository,
    private val machineryRepo: MachineryRepository,
    private val bookingRepo: BookingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    val selectedRole = _selectedRole.asStateFlow()

    private val _isContactAuthorized = MutableStateFlow(false)
    val isContactAuthorized = _isContactAuthorized.asStateFlow()

    // Machinery States
    val categories = machineryRepo.getCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val availableMachinery = machineryRepo.getAvailableMachinery().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provider Specific States
    private val _providerMachinery = MutableStateFlow<List<Machinery>>(emptyList())
    val providerMachinery = _providerMachinery.asStateFlow()

    private val _providerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val providerBookings = _providerBookings.asStateFlow()

    // Farmer Specific States
    private val _farmerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val farmerBookings = _farmerBookings.asStateFlow()

    // Admin States
    val adminAllMachinery = machineryRepo.getAllMachineryAdmin().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val adminAllBookings = bookingRepo.getAllBookingsAdmin().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // GPS Location State (Punjab central fallback initially: 32.074, 72.686)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>>(Pair(32.074, 72.686))
    val userLocation = _userLocation.asStateFlow()

    // Gyroscope & Accelerometer Telemetry
    private val _sensorData = MutableStateFlow(SensorTelemetry())
    val sensorData = _sensorData.asStateFlow()

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    fun updateSensorTelemetry(
        accelX: Float, accelY: Float, accelZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        _sensorData.value = SensorTelemetry(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)
    }

    init {
        // Load initial session if exists
        viewModelScope.launch {
            _isContactAuthorized.value = sessionManager.isContactAuthorized()
            val role = sessionManager.getUserRole()
            val userId = sessionManager.getUserId()
            if (role != null && userId != null) {
                _selectedRole.value = role
                // Fetch mock details
                val name = sessionManager.getUserName() ?: context.getString(R.string.default_user_name)
                val phone = sessionManager.getAuthToken() ?: "03000000000"
                val user = User(userId, phone, name, role, null, true)
                _currentUser.value = user
                
                // If it's a provider/farmer, sync repository values
                if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                    authRepo.setCurrentUser(user)
                }
                
                loadRoleSpecificData(userId, role)
            }
        }
    }

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
        sessionManager.saveUserRole(role)
    }

    private fun loadRoleSpecificData(userId: String, role: UserRole) {
        viewModelScope.launch {
            when (role) {
                UserRole.FARMER -> {
                    launch {
                        bookingRepo.getBookingsForFarmer(userId).collectLatest {
                            _farmerBookings.value = it
                        }
                    }
                }
                UserRole.PROVIDER -> {
                    launch {
                        machineryRepo.getMachineryByProvider(userId).collectLatest {
                            _providerMachinery.value = it
                        }
                    }
                    launch {
                        bookingRepo.getBookingsForProvider(userId).collectLatest {
                            _providerBookings.value = it
                        }
                    }
                }
                UserRole.ADMIN -> {
                    // Handled lazily via stateIn flows
                }
            }
        }
    }

    // Auth Flows
    fun verifyOtp(phone: String, otp: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val role = _selectedRole.value ?: UserRole.FARMER
            val verifiedProvider = _currentUser.value?.takeIf { role == UserRole.PROVIDER && it.role == UserRole.PROVIDER }
            if (verifiedProvider != null) {
                if (otp.length == 4) {
                    val updatedProvider = verifiedProvider.copy(phoneNumber = phone)
                    _currentUser.value = updatedProvider
                    sessionManager.saveUserRole(UserRole.PROVIDER)
                    sessionManager.saveUserId(updatedProvider.id)
                    sessionManager.saveUserName(updatedProvider.fullName)
                    sessionManager.saveAuthToken(updatedProvider.phoneNumber)
                    if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                        authRepo.setCurrentUser(updatedProvider)
                    }
                    loadRoleSpecificData(updatedProvider.id, UserRole.PROVIDER)
                    onSuccess()
                } else {
                    onError(context.getString(R.string.err_invalid_otp_code))
                }
                return@launch
            }
            authRepo.verifyOtp(phone, otp, role)
                .onSuccess { user ->
                    _currentUser.value = user
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveUserName(user.fullName)
                    sessionManager.saveAuthToken(user.phoneNumber)
                    loadRoleSpecificData(user.id, role)
                    onSuccess()
                }
                .onFailure {
                    onError(it.localizedMessage ?: context.getString(R.string.err_invalid_otp_code))
                }
        }
    }

    fun registerFarmer(phone: String, name: String, address: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepo.registerFarmer(phone, name, address)
                .onSuccess { user ->
                    _currentUser.value = user
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveUserName(user.fullName)
                    sessionManager.saveAuthToken(user.phoneNumber)
                    loadRoleSpecificData(user.id, UserRole.FARMER)
                    onSuccess()
                }
                .onFailure {
                    onError(it.localizedMessage ?: context.getString(R.string.err_registration_failed))
                }
        }
    }

    fun verifySupplierCnic(cnic: String, onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepo.verifySupplierCnic(cnic)
                .onSuccess { user ->
                    _selectedRole.value = UserRole.PROVIDER
                    _currentUser.value = user
                    sessionManager.saveUserRole(UserRole.PROVIDER)
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveUserName(user.fullName)
                    sessionManager.saveAuthToken(user.phoneNumber)
                    loadRoleSpecificData(user.id, UserRole.PROVIDER)
                    onSuccess(user)
                }
                .onFailure {
                    onError(it.localizedMessage ?: "شناختی کارڈ کی تصدیق ناکام ہو گئی۔")
                }
        }
    }

    fun adminLogin(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepo.adminLogin(email, pass)
                .onSuccess { user ->
                    _currentUser.value = user
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveUserName(user.fullName)
                    sessionManager.saveAuthToken(email)
                    onSuccess()
                }
                .onFailure {
                    onError(it.localizedMessage ?: context.getString(R.string.err_invalid_credentials))
                }
        }
    }

    fun updateCurrentUserName(newName: String) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updated = current.copy(fullName = newName)
                _currentUser.value = updated
                sessionManager.saveUserName(newName)
                if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                    authRepo.setCurrentUser(updated)
                }
            }
        }
    }

    fun updateCurrentUserPhone(newPhone: String) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updated = current.copy(phoneNumber = newPhone)
                _currentUser.value = updated
                sessionManager.saveAuthToken(newPhone)
                if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                    authRepo.setCurrentUser(updated)
                }
            }
        }
    }

    fun updateCurrentUserAddress(newAddress: String) {
        sessionManager.saveUserAddress(newAddress)
    }

    fun getCurrentUserAddress(): String {
        return sessionManager.getUserAddress() ?: ""
    }

    fun switchToProviderMode(onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updated = current.copy(role = UserRole.PROVIDER)
                _currentUser.value = updated
                _selectedRole.value = UserRole.PROVIDER
                sessionManager.saveUserRole(UserRole.PROVIDER)
                if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                    authRepo.setCurrentUser(updated)
                }
            }
            onComplete()
        }
    }

    fun switchToFarmerMode(onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updated = current.copy(role = UserRole.FARMER)
                _currentUser.value = updated
                _selectedRole.value = UserRole.FARMER
                sessionManager.saveUserRole(UserRole.FARMER)
                if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                    authRepo.setCurrentUser(updated)
                }
            }
            onComplete()
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.logout()
            sessionManager.clearSession()
            _currentUser.value = null
            _selectedRole.value = null
            onComplete()
        }
    }

    // Farmer Machinery Interactions
    fun createBooking(machineryId: String, date: Long, hours: Int, rate: Double, acres: Double?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val machinery = machineryRepo.getMachineryById(machineryId) ?: return@launch
            val booking = Booking(
                id = "book_" + System.currentTimeMillis().toString().takeLast(6),
                farmerId = user.id,
                farmerName = user.fullName,
                farmerPhone = user.phoneNumber,
                machineryId = machineryId,
                machineryName = machinery.nameUr,
                bookingDate = date,
                durationHours = hours,
                totalPrice = if (acres != null) acres * 5000.0 else hours * rate,
                status = BookingStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                locationUr = machinery.districtUr,
                acres = acres,
                providerName = machinery.providerName,
                providerPhone = machinery.providerPhone
            )
            bookingRepo.createBooking(booking)
            onSuccess()
        }
    }

    // Provider Machinery Operations
    fun addMachinery(name: String, categoryId: String, rate: Double, desc: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val machinery = Machinery(
                id = "mach_" + System.currentTimeMillis().toString().takeLast(6),
                providerId = user.id,
                providerName = user.fullName,
                providerPhone = user.phoneNumber,
                categoryId = categoryId,
                nameUr = name,
                descriptionUr = desc,
                modelYear = 2024,
                hourlyRate = rate,
                latitude = 32.0,
                longitude = 72.5,
                isAvailable = true,
                status = MachineryStatus.PENDING, // requires admin approval
                imageUrls = emptyList(),
                rating = 0.0,
                acresDone = 0.0,
                distanceCoveredKm = 0.0
            )
            machineryRepo.addMachinery(machinery)
            onSuccess()
        }
    }

    fun registerFarmerMachinery(
        machineTypes: List<String>,
        quantity: Int,
        district: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // 1. Log user in as a Service Provider with this phone number
            val providerUser = User(
                id = "usr_" + System.currentTimeMillis().toString().takeLast(6),
                phoneNumber = phoneNumber,
                fullName = "کاشتکار مالک (${district})",
                role = UserRole.PROVIDER,
                profileImageUrl = null,
                isActive = true
            )
            _currentUser.value = providerUser
            _selectedRole.value = UserRole.PROVIDER
            sessionManager.saveUserRole(UserRole.PROVIDER)
            sessionManager.saveUserId(providerUser.id)
            sessionManager.saveUserName(providerUser.fullName)
            sessionManager.saveAuthToken(phoneNumber)
            sessionManager.saveUserAddress(district)
            
            if (authRepo is pk.kissanmadadgar.mobile.data.mock.InMemoryAuthRepository) {
                authRepo.setCurrentUser(providerUser)
            }
            
            // 2. Register quantity of each selected machinery item
            machineTypes.forEachIndexed { typeIndex, machineType ->
                val categoryId = when (machineType) {
                    "سپرسیڈر", "Super Seeder" -> "cat_4"
                    "بیلر", "Baler" -> "cat_3"
                    "ہارویسٹر", "Harvester" -> "cat_2"
                    else -> "cat_1"
                }

                for (i in 1..quantity) {
                    val suffix = if (quantity > 1) " $i" else ""
                    val machinery = Machinery(
                        id = "mach_" + System.currentTimeMillis().toString().takeLast(6) + "_${typeIndex}_$i",
                        providerId = providerUser.id,
                        providerName = providerUser.fullName,
                        providerPhone = phoneNumber,
                        categoryId = categoryId,
                        nameUr = "$machineType$suffix",
                        descriptionUr = "رابطہ نمبر: $phoneNumber۔ کسان رجسٹریشن کے تحت رجسٹرڈ شدہ مشینری۔",
                        modelYear = 2026,
                        hourlyRate = when (machineType) {
                            "سپرسیڈر", "Super Seeder" -> 2200.0
                            "بیلر", "Baler" -> 1800.0
                            "ہارویسٹر", "Harvester" -> 3500.0
                            else -> 1500.0
                        },
                        latitude = _userLocation.value.first + (i * 0.001) + (typeIndex * 0.001),
                        longitude = _userLocation.value.second + (i * 0.001) + (typeIndex * 0.001),
                        isAvailable = true,
                        status = MachineryStatus.APPROVED, // Approved directly for farmer convenience demo
                        imageUrls = when (machineType) {
                            "سپرسیڈر", "Super Seeder" -> listOf("seeder_main_1", "seeder_main_2", "seeder_main_3", "seeder_main_4")
                            else -> emptyList()
                        },
                        rating = 5.0,
                        acresDone = 0.0,
                        distanceCoveredKm = 0.0,
                        districtUr = district
                    )
                    machineryRepo.addMachinery(machinery)
                }
            }
            
            loadRoleSpecificData(providerUser.id, UserRole.PROVIDER)
            onSuccess()
        }
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            bookingRepo.updateBookingStatus(bookingId, status)
        }
    }

    fun acceptBookingRequest(bookingId: String) {
        updateBookingStatus(bookingId, BookingStatus.ACCEPTED)
    }

    fun rejectBookingRequest(bookingId: String, reason: String?) {
        viewModelScope.launch {
            val booking = findKnownBooking(bookingId) ?: return@launch
            bookingRepo.updateBooking(
                booking.copy(
                    status = BookingStatus.REJECTED,
                    rejectionReason = reason?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
        }
    }

    fun uploadLifecyclePhoto(bookingId: String, step: BookingPhotoStep, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val booking = findKnownBooking(bookingId) ?: return@launch
            val existingSteps = booking.lifecyclePhotos.map { it.step }.toSet()
            if (step in existingSteps || !isLifecycleStepUnlocked(booking, step)) {
                return@launch
            }

            val updatedPhotos = booking.lifecyclePhotos + BookingLifecyclePhoto(
                step = step,
                imageLabelUr = lifecycleImageLabel(step),
                uploadedAt = System.currentTimeMillis()
            )
            val updatedStatus = when (step) {
                BookingPhotoStep.SERVICE_ACQUIRED,
                BookingPhotoStep.SUBSIDY_STARTED,
                BookingPhotoStep.WORK_COMPLETED -> BookingStatus.ACTIVE
                BookingPhotoStep.FARMER_CONFIRMATION -> BookingStatus.COMPLETED
            }

            bookingRepo.updateBooking(
                booking.copy(
                    status = updatedStatus,
                    lifecyclePhotos = updatedPhotos
                )
            )
            onSuccess()
        }
    }

    private fun findKnownBooking(bookingId: String): Booking? {
        return _providerBookings.value.find { it.id == bookingId }
            ?: _farmerBookings.value.find { it.id == bookingId }
    }

    private fun isLifecycleStepUnlocked(booking: Booking, step: BookingPhotoStep): Boolean {
        val uploadedSteps = booking.lifecyclePhotos.map { it.step }.toSet()
        return when (step) {
            BookingPhotoStep.SERVICE_ACQUIRED -> booking.status == BookingStatus.ACCEPTED
            BookingPhotoStep.SUBSIDY_STARTED -> BookingPhotoStep.SERVICE_ACQUIRED in uploadedSteps
            BookingPhotoStep.WORK_COMPLETED -> BookingPhotoStep.SUBSIDY_STARTED in uploadedSteps
            BookingPhotoStep.FARMER_CONFIRMATION -> BookingPhotoStep.WORK_COMPLETED in uploadedSteps
        }
    }

    private fun lifecycleImageLabel(step: BookingPhotoStep): String {
        return when (step) {
            BookingPhotoStep.SERVICE_ACQUIRED -> "سروس حاصل کرنے کی تصویر"
            BookingPhotoStep.SUBSIDY_STARTED -> "سبسڈی شروع ہونے کی تصویر"
            BookingPhotoStep.WORK_COMPLETED -> "کام مکمل ہونے کی تصویر"
            BookingPhotoStep.FARMER_CONFIRMATION -> "کسان کی تصدیقی تصویر"
        }
    }

    // Admin Verification Operations
    fun updateMachineryStatus(machineryId: String, status: MachineryStatus) {
        viewModelScope.launch {
            machineryRepo.updateMachineryStatus(machineryId, status)
        }
    }

    fun authorizeContact() {
        _isContactAuthorized.value = true
        sessionManager.setContactAuthorized(true)
    }
}
