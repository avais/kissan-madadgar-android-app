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
import pk.kissanmadadgar.mobile.data.remote.dto.ImplementDto
import pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto
import pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryRequest
import pk.kissanmadadgar.mobile.data.remote.dto.MachineRegistrationItem
import pk.kissanmadadgar.mobile.data.remote.dto.UserInfoDto
import pk.kissanmadadgar.mobile.data.remote.dto.MobileProfileResponse
import pk.kissanmadadgar.mobile.data.remote.dto.SupportResponse
import pk.kissanmadadgar.mobile.data.remote.api.AndroidNotificationDto
import pk.kissanmadadgar.mobile.data.remote.dto.GovernmentProjectDto
import javax.inject.Inject
import kotlin.coroutines.resume

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepo: AuthRepository,
    private val machineryRepo: MachineryRepository,
    private val bookingRepo: BookingRepository,
    private val sessionManager: SessionManager,
    private val authApiService: pk.kissanmadadgar.mobile.data.remote.api.AuthApiService
) : ViewModel() {

    // Declared ahead of init{} deliberately: viewModelScope uses Dispatchers.Main.immediate, so
    // the NetworkMonitor collector started in init{} below can run synchronously on the very
    // first emission — reading these before they're assigned (had they been declared further
    // down, in property-declaration order) would NPE on the still-null backing field.
    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline.asStateFlow()

    private val _isRefreshingBookings = MutableStateFlow(false)
    val isRefreshingBookings = _isRefreshingBookings.asStateFlow()

    // Also declared ahead of init{} for the same reason as above: init{} below calls
    // setSelectedTab(2) synchronously (via setSelectedTab -> _selectedTab.value = ...) when an
    // active farming session is detected at launch, which would otherwise NPE on this field's
    // still-null backing value if it were declared further down in property-declaration order.
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    init {
        // Refreshes the backend-driven string catalog at most once every 24h (no-op otherwise);
        // any failure is swallowed inside the store so this never blocks or affects app startup.
        viewModelScope.launch {
            pk.kissanmadadgar.mobile.core.strings.RemoteStringsStore.getInstance(context)
                .refreshIfNeeded(authApiService)
        }

        pk.kissanmadadgar.mobile.data.remote.FarmingUploadSyncManager.getInstance(
            apiService = authApiService,
            sessionManager = sessionManager,
            context = context
        ).startPolling {
            fetchRentalBookings("ONGOING")
            fetchRentalBookings("PENDING")
        }

        // Drives the offline banner on the Bookings tab and lets us auto-refresh the moment
        // connectivity returns instead of waiting for the user to re-open/re-filter the tab.
        viewModelScope.launch {
            pk.kissanmadadgar.mobile.data.local.NetworkMonitor.observe(context).collectLatest { online ->
                val wasOffline = _isOffline.value
                _isOffline.value = !online
                if (online && wasOffline) {
                    fetchRentalBookings("ONGOING")
                    fetchRentalBookings("PENDING")
                }
            }
        }

        // If a farming tracking session is already running in the background when the app
        // is (re)launched, land the user straight on the Bookings tab instead of Home.
        val activeSession = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context)
        if (activeSession != null) {
            setSelectedTab(2)

            // The session file surviving doesn't mean the actual foreground service is still
            // alive — a force-stop, an OS kill, or a reboot leaves this file on disk (it's just
            // JSON in filesDir) while the real Service process is gone, silently leaving GPS
            // tracking stopped under a UI that still shows a running timer. isServiceRunning()
            // is a plain in-memory flag that only a genuinely live instance can have set, so on
            // a fresh process launch it correctly reads false unless tracking is truly ongoing.
            // Relaunching here picks the real start time back up (see
            // FarmingTrackingService.startTracking / getPersistedStartTime) — the displayed
            // elapsed timer keeps counting from the original start, it never resets to zero.
            if (!pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.isServiceRunning()) {
                pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.startTracking(context, activeSession.bookingId)
            }
        }
    }

    // Resolves the same per-account cache key BookingRepositoryImpl uses internally for writes,
    // so the reactive read here (bookingsFlow) and every write (setBookings/createBooking/...)
    // always agree on which Room rows belong to the current session.
    private fun ownerKeyFor(user: User?): String {
        user?.id?.takeIf { it.isNotBlank() }?.let { return it }
        val guestToken = sessionManager.getGuestToken()?.takeIf { it.isNotBlank() }
        return if (guestToken != null) "guest_$guestToken" else "guest"
    }

    private suspend fun getFcmToken(): String? = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                cont.resume(if (task.isSuccessful) task.result else null)
            }
    }

    private fun syncPushToken() {
        viewModelScope.launch {
            try {
                val fcmToken = getFcmToken() ?: return@launch
                val authToken = sessionManager.getAuthToken() ?: return@launch
                val authHeader = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                authApiService.registerPushToken(
                    authHeader,
                    pk.kissanmadadgar.mobile.data.remote.api.PushTokenRequest(fcmToken)
                )
            } catch (e: Exception) {
                // Non-critical: the FirebaseMessagingService will re-sync the token on refresh.
            }
        }
    }

    fun fetchNotifications(page: Int = 0, append: Boolean = false) {
        viewModelScope.launch {
            val authToken = sessionManager.getAuthToken() ?: return@launch
            val authHeader = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
            _notificationsLoading.value = true
            try {
                val response = authApiService.getNotifications(authHeader, page, 10)
                if (response.isSuccessful) {
                    val pageBody = response.body()
                    val newItems = pageBody?.content ?: emptyList()
                    _notifications.value = if (append) _notifications.value + newItems else newItems
                    _notificationsTotalPages.value = pageBody?.totalPages ?: 1
                    _notificationsCurrentPage.value = pageBody?.number ?: page
                    _notificationsIsLast.value = pageBody?.last ?: true
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "fetchNotifications failed", e)
            } finally {
                _notificationsLoading.value = false
            }
        }
    }

    fun fetchUnreadNotificationCount() {
        viewModelScope.launch {
            val authToken = sessionManager.getAuthToken() ?: return@launch
            val authHeader = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
            try {
                val response = authApiService.getUnreadNotificationCount(authHeader)
                if (response.isSuccessful) {
                    _unreadNotificationCount.value = response.body()?.get("count") ?: 0L
                }
            } catch (e: Exception) {
                // Non-critical: the badge will just stay stale until the next successful fetch.
            }
        }
    }

    fun markNotificationRead(id: Long) {
        val alreadyRead = _notifications.value.find { it.id == id }?.read == true
        _notifications.value = _notifications.value.map { if (it.id == id) it.copy(read = true) else it }
        if (!alreadyRead) {
            _unreadNotificationCount.value = (_unreadNotificationCount.value - 1).coerceAtLeast(0)
        }
        viewModelScope.launch {
            val authToken = sessionManager.getAuthToken() ?: return@launch
            val authHeader = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
            try {
                authApiService.markNotificationRead(authHeader, id)
            } catch (e: Exception) {
                // Non-critical: the UI already reflects "read"; the server flag will settle
                // next time this notification is opened again.
            }
        }
    }

    // Generic sync-when-online, queue-when-offline behavior lives in
    // FarmingUploadSyncManager.submitOrQueue: with internet it attempts the upload right now and
    // suspends for the real result (SYNCED); without internet — or if that immediate attempt
    // fails — it falls back to the same on-disk queue + background poller as before (QUEUED).
    fun enqueueFarmingStart(
        bookingId: String,
        localFilePath: String,
        latitude: Double,
        longitude: Double,
        onResult: (pk.kissanmadadgar.mobile.data.remote.UploadOutcome) -> Unit = {}
    ) {
        val manager = pk.kissanmadadgar.mobile.data.remote.FarmingUploadSyncManager.getInstance(
            apiService = authApiService,
            sessionManager = sessionManager,
            context = context
        )

        viewModelScope.launch {
            // Force local status update immediately so the UI reflects the action right away,
            // regardless of whether the network call below resolves synchronously or gets
            // queued — must match who is actually starting it: the taker confirming goes to
            // STARTED_FROM_FARMER_SIDE ("کاشتکار نے تصدیق کی"), the provider confirming goes to
            // STARTED_FROM_SERVICE_PROVIDER_SIDE ("سروس کا آغاز ہوا").
            val currentList = bookingsFlow.value
            val target = currentList.find { it.id == bookingId }
            if (target != null) {
                val isTaker = _currentUser.value?.id == target.farmerId
                val newStatus = if (isTaker) "STARTED_FROM_FARMER_SIDE" else "STARTED_FROM_SERVICE_PROVIDER_SIDE"
                val newStatusUrdu = if (isTaker) "کاشتکار نے تصدیق کی" else "سروس کا آغاز ہوا"
                val updated = target.copy(
                    rentalRequestStatus = newStatus,
                    rentalRequestStatusUrdu = newStatusUrdu,
                    status = BookingStatus.ACTIVE
                )
                // upsertBookings, not setBookings: this is a single-row optimistic update, and
                // setBookings' full-replace semantics would drop any row a concurrent fetch has
                // already written that isn't in this currentList snapshot.
                bookingRepo.upsertBookings(listOf(updated))
            }

            _isSubmittingFarmingAction.value = true
            val outcome = try {
                manager.submitOrQueue(
                    actionType = "START",
                    bookingId = bookingId,
                    localFilePath = localFilePath,
                    latitude = latitude,
                    longitude = longitude,
                    onQueuedSyncSuccess = {
                        fetchRentalBookings("ONGOING")
                        fetchRentalBookings("PENDING")
                    }
                )
            } finally {
                _isSubmittingFarmingAction.value = false
            }
            if (outcome == pk.kissanmadadgar.mobile.data.remote.UploadOutcome.SYNCED) {
                fetchRentalBookings("ONGOING")
                fetchRentalBookings("PENDING")
            }
            onResult(outcome)
        }
    }

    fun enqueueFarmingComplete(
        bookingId: String,
        localFilePath: String,
        latitude: Double,
        longitude: Double,
        accuracy: Double,
        speed: Double,
        heading: Double,
        altitude: Double,
        isMock: Boolean,
        onResult: (pk.kissanmadadgar.mobile.data.remote.UploadOutcome) -> Unit = {}
    ) {
        val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "android_device"
        val manager = pk.kissanmadadgar.mobile.data.remote.FarmingUploadSyncManager.getInstance(
            apiService = authApiService,
            sessionManager = sessionManager,
            context = context
        )

        viewModelScope.launch {
            // Force local status update to COMPLETED immediately, same reasoning as above.
            val currentList = bookingsFlow.value
            val target = currentList.find { it.id == bookingId }
            if (target != null) {
                val updated = target.copy(
                    rentalRequestStatus = "COMPLETED",
                    rentalRequestStatusUrdu = "مکمل",
                    status = BookingStatus.COMPLETED
                )
                // upsertBookings, not setBookings — see enqueueFarmingStart above for why.
                bookingRepo.upsertBookings(listOf(updated))
            }

            // Must be read before the upload runs (it clears the active session file on success)
            // — same elapsed-active-seconds formula the live timer on the booking card uses
            // (paused duration excluded either way).
            val session = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context)
            val totalServiceTime = if (session != null) {
                val pausedMs = if (session.isPaused) {
                    session.pausedDurationMs + (System.currentTimeMillis() - session.lastPauseTimeMs)
                } else {
                    session.pausedDurationMs
                }
                ((System.currentTimeMillis() - session.startTimeMs - pausedMs) / 1000L).coerceAtLeast(0L)
            } else {
                0L
            }

            _isSubmittingFarmingAction.value = true
            val outcome = try {
                manager.submitOrQueue(
                    actionType = "COMPLETE",
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
                    totalServiceTime = totalServiceTime,
                    onQueuedSyncSuccess = {
                        fetchRentalBookings("ONGOING")
                        fetchRentalBookings("PENDING")
                    }
                )
            } finally {
                _isSubmittingFarmingAction.value = false
            }
            if (outcome == pk.kissanmadadgar.mobile.data.remote.UploadOutcome.SYNCED) {
                fetchRentalBookings("ONGOING")
                fetchRentalBookings("PENDING")
            }
            onResult(outcome)
        }
    }

    // Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    val selectedRole = _selectedRole.asStateFlow()

    private val _isContactAuthorized = MutableStateFlow(false)
    val isContactAuthorized = _isContactAuthorized.asStateFlow()

    private val _userAddress = MutableStateFlow(sessionManager.getUserAddress() ?: "")
    val userAddress = _userAddress.asStateFlow()

    private val _userCnic = MutableStateFlow(sessionManager.getUserCnic() ?: "")
    val userCnic = _userCnic.asStateFlow()

    // Implements API loading states
    private val _implementsList = MutableStateFlow<List<ImplementDto>>(emptyList())
    val implementsList = _implementsList.asStateFlow()

    private val _isLoadingImplements = MutableStateFlow(false)
    val isLoadingImplements = _isLoadingImplements.asStateFlow()

    // Districts API loading states
    private val _districtsList = MutableStateFlow<List<DistrictDto>>(emptyList())
    val districtsList = _districtsList.asStateFlow()

    private val _isLoadingDistricts = MutableStateFlow(false)
    val isLoadingDistricts = _isLoadingDistricts.asStateFlow()

    private val _userDistrict = MutableStateFlow(sessionManager.getUserDistrict() ?: "")
    val userDistrict = _userDistrict.asStateFlow()

    private val _profileResponse = MutableStateFlow<MobileProfileResponse?>(null)
    val profileResponse = _profileResponse.asStateFlow()

    private val _supportResponse = MutableStateFlow<SupportResponse?>(null)
    val supportResponse = _supportResponse.asStateFlow()



    // Machinery States
    val categories = machineryRepo.getCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _availableMachinery = MutableStateFlow<List<Machinery>>(emptyList())
    val availableMachinery = _availableMachinery.asStateFlow()

    // Search-tab pagination state for availableMachinery ("load more" as the user scrolls)
    private var availableMachineryLat = 0.0
    private var availableMachineryLng = 0.0
    private var availableMachineryType = "home"
    private var availableMachineryDistrictId: Int? = null
    private var availableMachineryKeyword: String? = null
    private var availableMachinerySize = 10
    private val _availableMachineryCurrentPage = MutableStateFlow(0)
    val availableMachineryCurrentPage = _availableMachineryCurrentPage.asStateFlow()
    private val _availableMachineryIsLastPage = MutableStateFlow(true)
    val availableMachineryIsLastPage = _availableMachineryIsLastPage.asStateFlow()
    private val _isLoadingMoreMachinery = MutableStateFlow(false)
    val isLoadingMoreMachinery = _isLoadingMoreMachinery.asStateFlow()
    private val _isLoadingAvailableMachinery = MutableStateFlow(false)
    val isLoadingAvailableMachinery = _isLoadingAvailableMachinery.asStateFlow()

    // Provider Specific States
    private val _providerMachinery = MutableStateFlow<List<Machinery>>(emptyList())
    val providerMachinery = _providerMachinery.asStateFlow()

    private val _providerApprovedCount = MutableStateFlow(0)
    val providerApprovedCount = _providerApprovedCount.asStateFlow()

    private val _providerPendingCount = MutableStateFlow(0)
    val providerPendingCount = _providerPendingCount.asStateFlow()

    private val _providerTotalAcres = MutableStateFlow("0")
    val providerTotalAcres = _providerTotalAcres.asStateFlow()

    private val _providerMachineryCurrentPage = MutableStateFlow(0)
    val providerMachineryCurrentPage = _providerMachineryCurrentPage.asStateFlow()

    private val _providerMachineryIsLast = MutableStateFlow(true)
    val providerMachineryIsLast = _providerMachineryIsLast.asStateFlow()

    private val _isLoadingMoreProviderMachinery = MutableStateFlow(false)
    val isLoadingMoreProviderMachinery = _isLoadingMoreProviderMachinery.asStateFlow()

    private val _bookingActionState = MutableStateFlow<BookingActionResult?>(null)
    val bookingActionState = _bookingActionState.asStateFlow()

    fun clearBookingActionState() {
        _bookingActionState.value = null
    }

    private val _bookingsTotalPages = MutableStateFlow(1)
    val bookingsTotalPages = _bookingsTotalPages.asStateFlow()

    private val _bookingsCurrentPage = MutableStateFlow(0)
    val bookingsCurrentPage = _bookingsCurrentPage.asStateFlow()

    private val _bookingsIsLast = MutableStateFlow(true)
    val bookingsIsLast = _bookingsIsLast.asStateFlow()

    private val _bookingsIsFirst = MutableStateFlow(true)
    val bookingsIsFirst = _bookingsIsFirst.asStateFlow()

    private val _notifications = MutableStateFlow<List<AndroidNotificationDto>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _notificationsLoading = MutableStateFlow(false)
    val notificationsLoading = _notificationsLoading.asStateFlow()

    private val _notificationsTotalPages = MutableStateFlow(1)
    val notificationsTotalPages = _notificationsTotalPages.asStateFlow()

    private val _notificationsCurrentPage = MutableStateFlow(0)
    val notificationsCurrentPage = _notificationsCurrentPage.asStateFlow()

    private val _notificationsIsLast = MutableStateFlow(true)
    val notificationsIsLast = _notificationsIsLast.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0L)
    val unreadNotificationCount = _unreadNotificationCount.asStateFlow()

    // True while enqueueFarmingStart/enqueueFarmingComplete are actively attempting the
    // synchronous online upload (see FarmingUploadSyncManager.submitOrQueue) — drives a blocking
    // loader so the user isn't left guessing whether their tap registered.
    private val _isSubmittingFarmingAction = MutableStateFlow(false)
    val isSubmittingFarmingAction = _isSubmittingFarmingAction.asStateFlow()

    private val _governmentProjects = MutableStateFlow<List<GovernmentProjectDto>>(emptyList())
    val governmentProjects = _governmentProjects.asStateFlow()

    private val _isLoadingGovernmentProjects = MutableStateFlow(false)
    val isLoadingGovernmentProjects = _isLoadingGovernmentProjects.asStateFlow()

    fun fetchGovernmentProjects() {
        viewModelScope.launch {
            _isLoadingGovernmentProjects.value = true
            try {
                val response = authApiService.getGovernmentProjects()
                if (response.isSuccessful) {
                    _governmentProjects.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "fetchGovernmentProjects failed", e)
            } finally {
                _isLoadingGovernmentProjects.value = false
            }
        }
    }

    private val _providerTotalRating = MutableStateFlow("0.0")
    val providerTotalRating = _providerTotalRating.asStateFlow()

    private val _providerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val providerBookings = _providerBookings.asStateFlow()

    val notificationBookingId = MutableStateFlow<String?>(null)

    fun setNotificationBookingId(id: String?) {
        notificationBookingId.value = id
        if (id != null) {
            setSelectedTab(2)
        }
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    // Farmer Specific States
    private val _farmerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val farmerBookings = _farmerBookings.asStateFlow()

    // Admin States
    val adminAllMachinery = machineryRepo.getAllMachineryAdmin().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val adminAllBookings = bookingRepo.getAllBookingsAdmin().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cache-first, per-account "My Bookings" feed: re-keys whenever the logged-in user changes
    // (login/logout/switch account on a shared device) so a previous account's cached bookings
    // are never shown, and immediately emits whatever Room already has for the new key the
    // instant something subscribes — no network round-trip needed before the UI can render.
    val bookingsFlow = _currentUser
        .map { ownerKeyFor(it) }
        .distinctUntilChanged()
        .flatMapLatest { ownerKey -> bookingRepo.observeBookings(ownerKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // GPS Location State (Punjab central fallback initially: 32.074, 72.686)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>>(Pair(32.074, 72.686))
    val userLocation = _userLocation.asStateFlow()

    // Gyroscope & Accelerometer Telemetry
    private val _sensorData = MutableStateFlow(SensorTelemetry())
    val sensorData = _sensorData.asStateFlow()

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    fun fetchAvailableMachines(lat: Double, lng: Double, type: String = "home", districtId: Int? = null, keyword: String? = null, size: Int = 10) {
        android.util.Log.d("KissanMadadgar", "MainViewModel fetchAvailableMachines called with: lat=$lat, lng=$lng, type=$type, districtId=$districtId, keyword=$keyword, size=$size")
        availableMachineryLat = lat
        availableMachineryLng = lng
        availableMachineryType = type
        availableMachineryDistrictId = districtId
        availableMachineryKeyword = keyword
        availableMachinerySize = size
        viewModelScope.launch {
            _isLoadingAvailableMachinery.value = true
            authRepo.getAvailableMachines(lat, lng, type, page = 0, size = size, districtId = districtId, keyword = keyword)
                .onSuccess { paginated ->
                    android.util.Log.d("KissanMadadgar", "MainViewModel fetchAvailableMachines success: ${paginated.machinery.size} machines")
                    _availableMachinery.value = paginated.machinery
                    _availableMachineryCurrentPage.value = paginated.currentPage
                    _availableMachineryIsLastPage.value = paginated.isLast
                }
                .onFailure { error ->
                    android.util.Log.e("KissanMadadgar", "MainViewModel fetchAvailableMachines failure", error)
                    _availableMachinery.value = emptyList()
                    _availableMachineryCurrentPage.value = 0
                    _availableMachineryIsLastPage.value = true
                }
            _isLoadingAvailableMachinery.value = false
        }
    }

    // Appends the next page of nearby machinery for the Search tab's infinite scroll.
    // No-op while a fetch is already in flight or the last page has already been reached.
    fun loadMoreAvailableMachines() {
        if (_isLoadingMoreMachinery.value || _availableMachineryIsLastPage.value) return
        val nextPage = _availableMachineryCurrentPage.value + 1
        _isLoadingMoreMachinery.value = true
        viewModelScope.launch {
            authRepo.getAvailableMachines(
                availableMachineryLat,
                availableMachineryLng,
                availableMachineryType,
                page = nextPage,
                size = availableMachinerySize,
                districtId = availableMachineryDistrictId,
                keyword = availableMachineryKeyword
            )
                .onSuccess { paginated ->
                    _availableMachinery.value = _availableMachinery.value + paginated.machinery
                    _availableMachineryCurrentPage.value = paginated.currentPage
                    _availableMachineryIsLastPage.value = paginated.isLast
                }
                .onFailure { error ->
                    android.util.Log.e("KissanMadadgar", "MainViewModel loadMoreAvailableMachines failure", error)
                }
            _isLoadingMoreMachinery.value = false
        }
    }

    fun updateSensorTelemetry(
        accelX: Float, accelY: Float, accelZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float
    ) {
        _sensorData.value = SensorTelemetry(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)
    }

    init {
        // Immediately set fallback support info so screens always have something to show
        fetchSupportInfo()

        // Load initial session if exists
        viewModelScope.launch {
            _isContactAuthorized.value = sessionManager.isContactAuthorized()
            val role = sessionManager.getUserRole()
            val userId = sessionManager.getUserId()
            if (role != null && userId != null) {
                _selectedRole.value = role
                val name = sessionManager.getUserName() ?: context.getString(R.string.default_user_name)
                val phone = sessionManager.getUserPhone() ?: sessionManager.getAuthToken() ?: ""
                val user = User(userId, phone, name, role, null, true)
                _currentUser.value = user

                loadRoleSpecificData(userId, role)
                fetchSupportInfo() // Fetch again once we have a real token
            } else {
                fetchGuestToken()
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
                    fetchProviderMachinery()
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

    fun fetchProviderMachinery(page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            if (page > 0) {
                _isLoadingMoreProviderMachinery.value = true
            }
            authRepo.getMyMachines(page = page, size = size)
                .onSuccess { response ->
                    _providerApprovedCount.value = response.approvedCount ?: 0
                    _providerPendingCount.value = response.pendingCount ?: 0
                    _providerTotalAcres.value = response.totalAcres ?: "0"
                    _providerTotalRating.value = response.totalRating ?: "0.0"
                    val machinesList = response.machines ?: emptyList()
                    val mappedList = machinesList.map { dto ->
                        val picUrls = dto.machinePictures ?: emptyList()
                        val statusMapped = when (dto.status) {
                            "APPROVED", "ACTIVE" -> MachineryStatus.APPROVED
                            "REJECTED" -> MachineryStatus.REJECTED
                            else -> MachineryStatus.PENDING
                        }
                        val nameUrdu = dto.machineName ?: ""
                        val ratingDouble = dto.rating?.toDoubleOrNull() ?: 0.0
                        val acresDouble = dto.acres?.toDoubleOrNull() ?: 0.0
                        Machinery(
                            id = dto.id?.toString() ?: "machine_my",
                            providerId = _currentUser.value?.id ?: "provider_me",
                            providerName = _currentUser.value?.fullName ?: "",
                            providerPhone = _currentUser.value?.phoneNumber ?: "",
                            categoryId = "CAT_1",
                            nameUr = nameUrdu,
                            descriptionUr = dto.project?.projectName ?: "",
                            modelYear = 2026,
                            hourlyRate = 0.0,
                            latitude = 0.0,
                            longitude = 0.0,
                            isAvailable = true,
                            status = statusMapped,
                            imageUrls = picUrls,
                            rating = ratingDouble,
                            acresDone = acresDouble,
                            distanceCoveredKm = 0.0,
                            districtUr = dto.district ?: "",
                            projectName = dto.project?.projectName,
                            projectLogo = dto.project?.logo,
                            subsidyText = dto.project?.subsidy,
                            distanceText = dto.statusUrdu
                        )
                    }
                    _providerMachinery.value = if (page == 0) mappedList else _providerMachinery.value + mappedList
                    _providerMachineryCurrentPage.value = page
                    val totalPages = response.totalPages ?: 1
                    _providerMachineryIsLast.value = (page + 1) >= totalPages
                }
                .onFailure {
                    if (page == 0) {
                        _providerMachinery.value = emptyList()
                        _providerApprovedCount.value = 0
                        _providerPendingCount.value = 0
                        _providerTotalAcres.value = "0"
                        _providerTotalRating.value = "0.0"
                        _providerMachineryIsLast.value = true
                    }
                }
            _isLoadingMoreProviderMachinery.value = false
        }
    }

    // Auth Flows
    private val _otpSentMessage = MutableStateFlow<String?>(null)
    val otpSentMessage = _otpSentMessage.asStateFlow()

    fun fetchImplements() {
        viewModelScope.launch {
            _isLoadingImplements.value = true
            authRepo.getImplements()
                .onSuccess { list ->
                    _implementsList.value = list.filter { it.status == "ACTIVE" }
                }
                .onFailure {
                    // Fallback to empty list or let UI handle fallback
                }
            _isLoadingImplements.value = false
        }
    }

    fun fetchDistricts() {
        viewModelScope.launch {
            _isLoadingDistricts.value = true
            authRepo.getDistricts()
                .onSuccess { list ->
                    _districtsList.value = list.filter { it.active }
                }
                .onFailure {
                    // Fallback or let UI handle fallback
                }
            _isLoadingDistricts.value = false
        }
    }

    fun sendOtp(phone: String, cnic: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val formattedPhone = if (phone.startsWith("+92")) {
                "0" + phone.substring(3)
            } else if (phone.startsWith("92")) {
                "0" + phone.substring(2)
            } else {
                phone
            }
            val location = _userLocation.value
            authRepo.login(formattedPhone, cnic, location.first, location.second)
                .onSuccess { message ->
                    _otpSentMessage.value = message
                    onSuccess()
                }
                .onFailure {
                    _otpSentMessage.value = null
                    onError(it.localizedMessage ?: "او ٹی پی بھیجنے میں خرابی پیش آئی۔")
                }
        }
    }

    fun verifyOtp(phone: String, otp: String, onSuccess: () -> Unit, onError: (String) -> Unit, guestToken: String? = null, type: String? = null) {
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
                    sessionManager.saveUserPhone(updatedProvider.phoneNumber)
                    loadRoleSpecificData(updatedProvider.id, UserRole.PROVIDER)
                    syncPushToken()
                    onSuccess()
                } else {
                    onError(context.getString(R.string.err_invalid_otp_code))
                }
                return@launch
            }
            val formattedPhone = if (phone.startsWith("+92")) {
                "0" + phone.substring(3)
            } else if (phone.startsWith("92")) {
                "0" + phone.substring(2)
            } else {
                phone
            }
            authRepo.verifyOtp(formattedPhone, otp, role, guestToken, type)
                .onSuccess { user ->
                    _currentUser.value = user
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveUserName(user.fullName)
                    
                    val currentToken = sessionManager.getAuthToken()
                    if (currentToken.isNullOrEmpty() || currentToken == phone || currentToken == user.phoneNumber) {
                        sessionManager.saveAuthToken(user.phoneNumber)
                    }
                    sessionManager.saveUserPhone(user.phoneNumber)
                    
                    _userAddress.value = sessionManager.getUserAddress() ?: ""
                    _userCnic.value = sessionManager.getUserCnic() ?: ""
                    _userDistrict.value = sessionManager.getUserDistrict() ?: ""
                    
                    fetchSupportInfo()
                    loadRoleSpecificData(user.id, role)
                    syncPushToken()
                    onSuccess()
                }
                .onFailure {
                    onError(it.localizedMessage ?: context.getString(R.string.err_invalid_otp_code))
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
            }
        }
    }

    fun updateCurrentUserPhone(newPhone: String) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val formattedPhone = if (newPhone.startsWith("+92")) {
                    "0" + newPhone.substring(3)
                } else if (newPhone.startsWith("92")) {
                    "0" + newPhone.substring(2)
                } else {
                    newPhone
                }
                val updated = current.copy(phoneNumber = formattedPhone)
                _currentUser.value = updated
                val currentToken = sessionManager.getAuthToken()
                if (currentToken.isNullOrEmpty() || currentToken == current.phoneNumber || !currentToken.contains(".")) {
                    sessionManager.saveAuthToken(formattedPhone)
                }
            }
        }
    }

    fun updateCurrentUserAddress(newAddress: String) {
        sessionManager.saveUserAddress(newAddress)
        _userAddress.value = newAddress
    }

    fun getCurrentUserAddress(): String {
        return _userAddress.value
    }

    fun updateCurrentUserCnic(newCnic: String) {
        sessionManager.saveUserCnic(newCnic)
        _userCnic.value = newCnic
    }

    fun getCurrentUserCnic(): String {
        return _userCnic.value
    }

    fun updateCurrentUserDistrict(newDistrict: String) {
        sessionManager.saveUserDistrict(newDistrict)
        _userDistrict.value = newDistrict
    }

    fun getCurrentUserDistrict(): String {
        return _userDistrict.value
    }

    // Backs the profile tab's "محفوظ کریں" button — POST api/android/profile/update. Local
    // state (name/phone/address/cnic/district) is only updated once the server confirms the
    // write, instead of the previous fire-and-forget local-only save, so the UI never shows a
    // value the backend rejected.
    fun saveProfile(
        name: String,
        phone: String,
        address: String,
        cnic: String,
        district: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            val districtId = districtsList.value.find { it.nameUrdu == district }?.id?.toLong()
            authRepo.updateProfile(
                name = name,
                cnic = cnic,
                address = address,
                districtId = districtId,
                mobile = phone
            ).onSuccess {
                updateCurrentUserName(name)
                updateCurrentUserPhone(phone)
                updateCurrentUserAddress(address)
                updateCurrentUserCnic(cnic)
                updateCurrentUserDistrict(district)
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.localizedMessage)
            }
        }
    }



    fun switchToProviderMode(onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updated = current.copy(role = UserRole.PROVIDER)
                _currentUser.value = updated
                _selectedRole.value = UserRole.PROVIDER
                sessionManager.saveUserRole(UserRole.PROVIDER)
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
            }
            onComplete()
        }
    }

    // True only while requestLogout() is attempting to flush a pending upload queue before
    // proceeding — drives a brief "syncing, please wait" indicator on the logout button.
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut = _isLoggingOut.asStateFlow()

    /**
     * Logout is gated on there being no unsynced farming-service upload left behind: silently
     * losing a start/complete record (with its photo/GPS log) that the server never learned
     * about is worse than a brief delay, so this never offers a "logout anyway" escape hatch.
     *
     *  - Nothing queued -> logs out immediately.
     *  - Something queued and online -> attempts one immediate flush; logs out only if that
     *    drains the queue, otherwise stays logged in and reports why.
     *  - Something queued and offline -> blocked immediately, no network attempt possible.
     */
    fun requestLogout(onResult: (LogoutOutcome) -> Unit) {
        viewModelScope.launch {
            // A live GPS-tracking session is a stronger reason to block than a queued upload:
            // there's nothing to "flush" here (the service is still actively recording), and
            // letting logout proceed would leave the service running unattached to any account
            // — its eventual upload would authenticate with whichever account is logged in by
            // then, not the one that actually did the work.
            if (pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context) != null) {
                onResult(LogoutOutcome.BLOCKED_ACTIVE_SESSION)
                return@launch
            }

            val syncManager = pk.kissanmadadgar.mobile.data.remote.FarmingUploadSyncManager.getInstance(
                apiService = authApiService,
                sessionManager = sessionManager,
                context = context
            )

            if (syncManager.hasPendingUploads()) {
                if (!pk.kissanmadadgar.mobile.data.local.NetworkMonitor.isOnline(context)) {
                    onResult(LogoutOutcome.BLOCKED_OFFLINE)
                    return@launch
                }
                _isLoggingOut.value = true
                val flushed = syncManager.flushNow()
                _isLoggingOut.value = false
                if (!flushed) {
                    onResult(LogoutOutcome.BLOCKED_SYNC_FAILED)
                    return@launch
                }
            }

            performLogoutInternal()
            onResult(LogoutOutcome.LOGGED_OUT)
        }
    }

    private suspend fun performLogoutInternal() {
        authRepo.logout()
        sessionManager.clearSession()

        // Reset every session-scoped StateFlow so a subsequent login (as the same
        // or a different account, without the process restarting) never briefly
        // shows the previous account's data before a fresh fetch overwrites it.
        _currentUser.value = null
        _selectedRole.value = null
        _isContactAuthorized.value = false
        _userAddress.value = ""
        _userCnic.value = ""
        _userDistrict.value = ""
        _profileResponse.value = null
        _supportResponse.value = null
        _otpSentMessage.value = null

        _availableMachinery.value = emptyList()
        _providerMachinery.value = emptyList()
        _providerApprovedCount.value = 0
        _providerPendingCount.value = 0
        _providerTotalAcres.value = "0"
        _providerTotalRating.value = "0.0"
        _providerMachineryCurrentPage.value = 0
        _providerMachineryIsLast.value = true
        _providerBookings.value = emptyList()
        _farmerBookings.value = emptyList()

        _bookingActionState.value = null
        _bookingsTotalPages.value = 1
        _bookingsCurrentPage.value = 0
        _bookingsIsLast.value = true
        _bookingsIsFirst.value = true
        notificationBookingId.value = null
        _unreadNotificationCount.value = 0L

        // The Room-backed cache is already scoped per account (see BookingRepositoryImpl),
        // so a subsequent login — same or different account — only ever reads/writes rows
        // under its own owner key and can never see this account's cached bookings. This
        // call just clears the transient "guest" bucket bookingsFlow falls back to the
        // instant _currentUser above goes null, so the guest screen never flashes stale rows.
        bookingRepo.setBookings(emptyList())

        fetchGuestToken()
    }

    fun getGuestToken(): String? {
        return sessionManager.getGuestToken()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken()
            if (!token.isNullOrEmpty()) {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                authRepo.getProfile(authHeader)
                    .onSuccess { response ->
                        _profileResponse.value = response
                        response.name?.let { updateCurrentUserName(it) }
                        response.cnic?.let { _userCnic.value = it; sessionManager.saveUserCnic(it) }
                        response.address?.let { _userAddress.value = it; sessionManager.saveUserAddress(it) }
                        response.district?.let { _userDistrict.value = it; sessionManager.saveUserDistrict(it) }
                        response.mobile?.let { updateCurrentUserPhone(it) }
                    }
                    .onFailure {
                        // Handle failure or fallback
                    }
            }
        }
    }

    fun fetchSupportInfo(module: String? = null) {
        viewModelScope.launch {
            android.util.Log.d("MainViewModel", "fetchSupportInfo: calling support API, module=$module")
            authRepo.getSupport(module)
                .onSuccess { response ->
                    android.util.Log.d("MainViewModel", "fetchSupportInfo success: message='${response.message}'")
                    _supportResponse.value = response
                }
                .onFailure { err ->
                    android.util.Log.e("MainViewModel", "fetchSupportInfo failed: ${err.localizedMessage}", err)
                    // Fallback if API fails
                    _supportResponse.value = pk.kissanmadadgar.mobile.data.remote.dto.SupportResponse(
                        message = "مدد کے لیے اس نمبر پر واٹس ایپ یا کال کریں۔ ہیلپ لائن 03215806176۔",
                        messageRoman = "Madad kay liya is number par whatsapp or call karain. help line 03215806176.",
                        helpline = "03215806176"
                    )
                }
        }
    }

    fun fetchGuestToken(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "android_device"
            val request = pk.kissanmadadgar.mobile.data.remote.dto.GuestTokenRequest(
                deviceId = deviceId,
                appVersion = pk.kissanmadadgar.mobile.BuildConfig.VERSION_NAME,
                signature = getAppSignatureHash(context)
            )
            authRepo.getGuestToken(request)
                .onSuccess { response ->
                    response.guestToken?.let { token ->
                        sessionManager.saveGuestToken(token)
                        sessionManager.saveUserRole(UserRole.PROVIDER)
                        _selectedRole.value = UserRole.PROVIDER
                        fetchSupportInfo()
                    }
                    onComplete()
                }
                .onFailure {
                    android.util.Log.e("MainViewModel", "Failed to fetch guest token", it)
                    onComplete()
                }
        }
    }

    // Farmer Machinery Interactions
    fun createBooking(
        machineryId: String,
        date: Long,
        hours: Int,
        rate: Double,
        acres: Double?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: run {
                onError("براہ کرم پہلے لاگ اِن کریں۔")
                return@launch
            }
            val machinery = _availableMachinery.value.find { it.id == machineryId }
                ?: machineryRepo.getMachineryById(machineryId)
                ?: run {
                    onError("منتخب کردہ مشینری نہیں ملی۔ براہ کرم دوبارہ کوشش کریں۔")
                    return@launch
                }

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val dateStr = sdf.format(java.util.Date(date))
            
            val fleetId = machinery.fleetId
            val acresVal = acres ?: 0.0
            val hoursVal = hours.toDouble()
            val lat = machinery.latitude
            val lng = machinery.longitude
            val address = userAddress.value.ifEmpty { machinery.districtUr }
            
            android.util.Log.d("MainViewModel", "Calling createRentalBooking API: fleetId=$fleetId, acres=$acresVal, date=$dateStr, hours=$hoursVal, lat=$lat, lng=$lng, address=$address")
            
            authRepo.createRentalBooking(
                fleetId = fleetId,
                acres = acresVal,
                date = dateStr,
                numberOfHours = hoursVal,
                latitude = lat,
                longitude = lng,
                farmingActivityAddress = address
            ).onSuccess {
                android.util.Log.d("MainViewModel", "createRentalBooking API call success")
                val booking = Booking(
                    id = "book_" + System.currentTimeMillis().toString().takeLast(6),
                    farmerId = user.id,
                    farmerName = user.fullName,
                    farmerPhone = user.phoneNumber,
                    machineryId = machineryId,
                    machineryName = machinery.nameUr,
                    bookingDate = dateStr,
                    durationHours = hours,
                    totalPrice = hours * rate,
                    status = BookingStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    locationUr = machinery.districtUr,
                    acres = acres,
                    providerName = machinery.providerName,
                    providerPhone = machinery.providerPhone,
                    machineryImageUrl = machinery.imageUrls.firstOrNull()
                )
                bookingRepo.createBooking(booking)
                // Immediately try to replace the optimistic placeholder above with the real
                // server-confirmed booking (see the filterNot("book_") in fetchRentalBookings).
                // A no-op if offline — the placeholder just stays visible until the next
                // successful fetch, which is the correct fallback.
                fetchRentalBookings("PENDING")
                onSuccess()
            }.onFailure { err ->
                android.util.Log.e("MainViewModel", "createRentalBooking API call failed", err)
                val message = "بکنگ میں خرابی پیش آئی: ${err.message}"
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                }
                onError(message)
            }
        }
    }

    fun fetchRentalBookings(status: String? = "PENDING", page: Int? = 0, size: Int? = 10, keyword: String? = null) {
        if (!pk.kissanmadadgar.mobile.data.local.NetworkMonitor.isOnline(context)) {
            // Offline: bookingsFlow is already showing whatever Room has cached for this
            // account — skip the doomed network call instead of logging a guaranteed failure.
            android.util.Log.d("MainViewModel", "fetchRentalBookings skipped — offline, serving cached data")
            return
        }
        viewModelScope.launch {
            _isRefreshingBookings.value = true
            authRepo.getRentalBookings(status, page, size, keyword = keyword).onSuccess { paginated ->
                android.util.Log.d("MainViewModel", "fetchRentalBookings success, count=${paginated.bookings.size}")
                paginated.bookings.forEachIndexed { i, b ->
                    android.util.Log.d("MainViewModel", "Booking $i: id=${b.id}, provider=${b.providerName}, machine=${b.machineryName}, date=${b.bookingDate}, status=${b.status}")
                }

                // Upsert only the freshly fetched rows instead of replacing the whole cached
                // list wholesale. This is a status-filtered fetch (e.g. just "ONGOING" or just
                // "PENDING"), and callers routinely fire off fetches for multiple statuses
                // back-to-back (see enqueueFarmingStart/enqueueFarmingComplete, which refresh
                // both ONGOING and PENDING right after a sync) — and a notification tap can also
                // trigger a concurrent fetchBookingById. A full-replace write built from an
                // in-memory snapshot of bookingsFlow.value would race all of these: whichever
                // one's snapshot was taken first (and so doesn't include what another just wrote)
                // silently deletes it when its own write lands last. upsertBookings only touches
                // the ids it's given, so concurrent writers can never clobber each other.
                // BookingRepositoryImpl.upsertBookings additionally guards each booking
                // individually against a stale/racing fetch reverting an optimistic local status
                // that has already progressed further (e.g. a queued start/complete upload still
                // sitting in FarmingUploadSyncManager's queue) — see its reconcile() for the
                // monotonic rentalRequestStatus-rank rule that replaces the old ad hoc
                // "pendingCompleteIds" check that used to live here.
                bookingRepo.upsertBookings(paginated.bookings)
                // createBooking() inserts a client-fabricated placeholder id ("book_" + a
                // timestamp fragment, see createBooking below) so the UI has something to show
                // the instant a new booking is submitted, before the server-confirmed row (with
                // its real numeric id) comes back. That placeholder never matches any real
                // server id, so it needs a separate, explicit drop once a real authoritative
                // fetch — this one — supersedes it; upsertBookings alone would never remove it
                // since it doesn't delete anything.
                bookingRepo.clearPlaceholderBookings()
                _bookingsTotalPages.value = paginated.totalPages
                _bookingsCurrentPage.value = paginated.currentPage
                _bookingsIsLast.value = paginated.isLast
                _bookingsIsFirst.value = paginated.isFirst
            }.onFailure { err ->
                android.util.Log.e("MainViewModel", "fetchRentalBookings failed", err)
            }
            _isRefreshingBookings.value = false
        }
    }

    // Resolves a single booking (by id) for deep-linking from a notification tap, where the
    // target booking's status is unknown and may not match whatever status-filtered page the
    // Bookings tab currently has loaded (see fetchRentalBookings). The backend's ?id= filter on
    // this same endpoint returns just that one booking, so this is a single-row lookup, not a
    // page scan. Deliberately does NOT touch
    // _bookingsTotalPages/_bookingsCurrentPage/_bookingsIsLast/_bookingsIsFirst — those drive the
    // pagination controls for whichever filter the user is actively browsing, and this is an
    // unrelated, orthogonal lookup that must not disturb them. Uses upsertBookings (not
    // setBookings) so this can never race-clobber whatever the tab's own fetchRentalBookings is
    // concurrently writing, and vice versa — see the comment in fetchRentalBookings for the full
    // reasoning.
    fun fetchBookingById(bookingId: String) {
        if (!pk.kissanmadadgar.mobile.data.local.NetworkMonitor.isOnline(context)) {
            android.util.Log.d("MainViewModel", "fetchBookingById skipped — offline, serving cached data")
            return
        }
        viewModelScope.launch {
            authRepo.getRentalBookings(status = null, page = 0, size = 1, id = bookingId).onSuccess { paginated ->
                if (paginated.bookings.none { it.id == bookingId }) {
                    // Surfaces here (rather than failing silently) if the backend's ?id= filter
                    // isn't actually narrowing to this booking — the notification-tap flow in
                    // MyBookings.kt matches by exact id, so if this ever fires, the sheet simply
                    // never opens rather than opening the wrong booking.
                    android.util.Log.w("MainViewModel", "fetchBookingById: requested id=$bookingId not present in response (got ${paginated.bookings.map { it.id }})")
                }
                bookingRepo.upsertBookings(paginated.bookings)
            }.onFailure { err ->
                android.util.Log.e("MainViewModel", "fetchBookingById failed for id=$bookingId", err)
            }
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
        machineQuantities: Map<String, Int>,
        district: String,
        phoneNumber: String,
        cnic: String,
        fullName: String,
        onSuccess: (pk.kissanmadadgar.mobile.data.remote.dto.RegisterMachineryResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // Save logged in user details before setting current user to providerUser
            val loggedInUser = _currentUser.value
            val loggedInUserId = loggedInUser?.id?.toLongOrNull()

            // 1. Log user in as a Service Provider with this phone number
            val providerUser = User(
                id = "usr_" + System.currentTimeMillis().toString().takeLast(6),
                phoneNumber = phoneNumber,
                fullName = fullName.trim().ifEmpty { "کاشتکار مالک (${district})" },
                role = UserRole.PROVIDER,
                profileImageUrl = null,
                isActive = true
            )

            // 2. Prepare JSON payload and send backend call
            val machinesList = mutableListOf<MachineRegistrationItem>()
            val selectedDistrictDto = districtsList.value.find { it.nameUrdu == district }
            val districtId = selectedDistrictDto?.id ?: 1

            machineQuantities.forEach { (machineType, quantity) ->
                val matchedImplement = implementsList.value.find { it.nameUr == machineType || it.name == machineType }
                val implementId = matchedImplement?.id ?: when (machineType) {
                    "سپرسیڈر", "Super Seeder" -> 8
                    "بیلر", "Baler" -> 9
                    "ہارویسٹر", "Harvester" -> 10
                    else -> 10
                }
                machinesList.add(
                    MachineRegistrationItem(
                        implementId = implementId,
                        number = quantity,
                        districtId = districtId
                    )
                )
            }

            val payload = RegisterMachineryRequest(
                machines = machinesList,
                userInfo = UserInfoDto(
                    id = loggedInUserId,
                    fullName = providerUser.fullName,
                    cnic = cnic,
                    mobileNumber = phoneNumber,
                    latitude = _userLocation.value.first,
                    longitude = _userLocation.value.second
                )
            )

            // Perform API call
            authRepo.registerMachinery(payload)
                .onSuccess { response ->
                    android.util.Log.d("MainViewModel", "Machinery registered successfully on server")
                    
                    // Register quantity of each selected machinery item locally for immediate UI display
                    var typeIndex = 0
                    machineQuantities.forEach { (machineType, quantity) ->
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
                                    "سپرسیڈر", "Super Seeder" -> listOf("other_machinery_clean")
                                    else -> emptyList()
                                },
                                rating = 5.0,
                                acresDone = 0.0,
                                distanceCoveredKm = 0.0,
                                districtUr = district
                            )
                            machineryRepo.addMachinery(machinery)
                        }
                        typeIndex++
                    }

                    if (response.isOtpSent == true) {
                        response.guestToken?.let { sessionManager.saveGuestToken(it) }
                    } else {
                        _currentUser.value = providerUser
                        _selectedRole.value = UserRole.PROVIDER
                        sessionManager.saveUserRole(UserRole.PROVIDER)
                        sessionManager.saveUserId(providerUser.id)
                        sessionManager.saveUserName(providerUser.fullName)
                        sessionManager.saveUserPhone(phoneNumber)
                        sessionManager.saveUserAddress(district)
                        sessionManager.saveUserCnic(cnic)

                        fetchProviderMachinery()
                    }

                    onSuccess(response)
                }
                .onFailure {
                    android.util.Log.e("MainViewModel", "Failed to register machinery on server", it)
                    onError(it.localizedMessage ?: "زرعی مشینری کی رجسٹریشن ناکام ہو گئی۔")
                }
        }
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            bookingRepo.updateBookingStatus(bookingId, status)
        }
    }

    fun acceptBookingRequest(bookingId: String) {
        viewModelScope.launch {
            authRepo.approveBooking(bookingId)
                .onSuccess { serverMessage ->
                    _bookingActionState.value = BookingActionResult.Success(serverMessage, "APPROVE")
                    fetchRentalBookings()
                }
                .onFailure { error ->
                    _bookingActionState.value = BookingActionResult.Failure(error.message ?: "منظوری میں خرابی پیش آئی", "APPROVE")
                }
        }
    }

    fun rejectBookingRequest(bookingId: String, reason: String?) {
        viewModelScope.launch {
            authRepo.rejectBooking(bookingId, reason)
                .onSuccess { serverMessage ->
                    _bookingActionState.value = BookingActionResult.Success(serverMessage, "REJECT")
                    fetchRentalBookings()
                }
                .onFailure { error ->
                    _bookingActionState.value = BookingActionResult.Failure(error.message ?: "مسترد کرنے میں خرابی پیش آئی", "REJECT")
                }
        }
    }

    // Self-contained result callback (rather than the shared _bookingActionState used by
    // accept/reject) since the feedback UI lives entirely inside the booking card/sheet itself
    // — swapping its own button for a "thanks" banner — and has no reason to pop the shared
    // approve/reject alert dialog or touch the tab filter.
    fun submitBookingFeedback(bookingId: String, rating: Int, comment: String, onResult: (success: Boolean, message: String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            authRepo.submitBookingFeedback(bookingId, rating, comment)
                .onSuccess { message -> onResult(true, message) }
                .onFailure { error -> onResult(false, error.message) }
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

// SHA-256 hash of the APK's signing certificate, so the backend can verify a guest-token
// request really came from a build signed with this app's key rather than trusting a static
// string. Falls back to an empty signature only if PackageManager genuinely can't resolve the
// signing info (should not happen for an installed, signed APK).
private fun getAppSignatureHash(context: Context): String {
    return try {
        val packageManager = context.packageManager
        val signatureBytes = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val packageInfo = packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo
            val signers = if (signingInfo?.hasMultipleSigners() == true) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo?.signingCertificateHistory
            }
            signers?.firstOrNull()?.toByteArray()
        } else {
            @Suppress("DEPRECATION")
            val packageInfo = packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_SIGNATURES
            )
            @Suppress("DEPRECATION")
            packageInfo.signatures?.firstOrNull()?.toByteArray()
        } ?: return ""
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(signatureBytes)
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        android.util.Log.e("MainViewModel", "Failed to compute app signature hash", e)
        ""
    }
}

sealed class BookingActionResult {
    data class Success(val message: String, val action: String) : BookingActionResult()
    data class Failure(val error: String, val action: String) : BookingActionResult()
}

enum class LogoutOutcome {
    LOGGED_OUT,
    // A GPS-tracking session is actively running for this account. Logging out doesn't stop
    // FarmingTrackingService, and the eventual upload authenticates with whichever account is
    // logged in when it finally syncs — letting logout proceed here would risk a different
    // account's token completing/uploading this account's farming record. No escape hatch.
    BLOCKED_ACTIVE_SESSION,
    // No internet at all, so a queued upload can't be attempted — logout is blocked outright
    // rather than offered as a "logout anyway and lose it" choice.
    BLOCKED_OFFLINE,
    // Was online and a flush was attempted, but at least one item is still stuck in the queue
    // (server error, etc.) — logout stays blocked so the data isn't silently discarded.
    BLOCKED_SYNC_FAILED
}
