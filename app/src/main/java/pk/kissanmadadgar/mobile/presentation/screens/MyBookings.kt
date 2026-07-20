package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream

// Whether the device's location services are on at all (any provider), regardless of whether
// this app has location permission — used to gate the QR show/scan flows, which both rely on a
// real device location fix for their proximity check.
fun isDeviceLocationEnabled(context: android.content.Context): Boolean {
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
        ?: return false
    return androidx.core.location.LocationManagerCompat.isLocationEnabled(locationManager)
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FarmerBookingsTab(viewModel: MainViewModel) {
    // Collected (not read directly) so this tab recomposes if the active role changes.
    viewModel.selectedRole.collectAsState()
    val bookings by viewModel.bookingsFlow.collectAsState()
    val actionState by viewModel.bookingActionState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val loggedInUserId = currentUser?.id ?: ""
    val isSubmittingFarmingAction by viewModel.isSubmittingFarmingAction.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val isRefreshingBookings by viewModel.isRefreshingBookings.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    // Bottom-sheet state: which booking to preview (opened by tapping a card; see FarmerRequestCard)
    var bottomSheetBooking by remember { mutableStateOf<Booking?>(null) }
    // Set only by the notification-tap deep link below (never by a normal card tap), so opening
    // a notification also scrolls the underlying list to that booking's card — not just the
    // sheet on top of it. Cleared once the scroll actually happens.
    var scrollToBookingId by remember { mutableStateOf<String?>(null) }
    var bookingToStartDirectly by remember { mutableStateOf<Booking?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Reflects the real outcome of FarmingUploadSyncManager.submitOrQueue: SYNCED means the
    // upload + start call already succeeded just now (internet was available), QUEUED means it's
    // sitting in the offline queue for the background poller to retry once connectivity returns.
    val showStartOutcomeToast: (pk.kissanmadadgar.mobile.data.remote.UploadOutcome) -> Unit = { outcome ->
        val msg = if (outcome == pk.kissanmadadgar.mobile.data.remote.UploadOutcome.SYNCED) {
            "کاشتکاری کامیابی سے شروع ہو گئی ہے۔"
        } else {
            "کاشتکاری شروع کرنے کی درخواست شیڈول کر دی گئی ہے۔"
        }
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    val handleDirectPhotoCaptured: (Bitmap) -> Unit = { bitmap ->
        val target = bookingToStartDirectly
        android.util.Log.d("StartFarmingFlow", "handleDirectPhotoCaptured: target bookingId=${target?.id}")
        if (target != null) {
            val dir = File(context.filesDir, "uploads")
            if (!dir.exists()) dir.mkdirs()
            val imageFile = File(dir, "start_${target.id}_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                android.util.Log.d("StartFarmingFlow", "Photo saved to: ${imageFile.absolutePath}")

                val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                android.util.Log.d("StartFarmingFlow", "hasFine: $hasFine, hasCoarse: $hasCoarse")

                if (hasFine || hasCoarse) {
                    try {
                        // getCurrentLocation() forces a fresh fix instead of returning whatever
                        // is sitting in Play Services' location cache (which is also what the
                        // tracking service's very first periodic callback tends to replay) —
                        // otherwise a quick start-then-stop could log identical start/end points.
                        val cancellationTokenSource = com.google.android.gms.tasks.CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token
                        ).addOnSuccessListener { loc ->
                            val lat = loc?.latitude ?: 0.0
                            val lng = loc?.longitude ?: 0.0
                            android.util.Log.d("StartFarmingFlow", "Fetched fresh location: lat=$lat, lng=$lng. Submitting...")
                            viewModel.enqueueFarmingStart(target.id, imageFile.absolutePath, lat, lng, showStartOutcomeToast)
                        }.addOnFailureListener { err ->
                            android.util.Log.w("StartFarmingFlow", "Failed to fetch location: ${err.message}. Submitting with fallback 0.0...")
                            viewModel.enqueueFarmingStart(target.id, imageFile.absolutePath, 0.0, 0.0, showStartOutcomeToast)
                        }
                    } catch (e: SecurityException) {
                        android.util.Log.e("StartFarmingFlow", "SecurityException fetching location: ${e.message}")
                        viewModel.enqueueFarmingStart(target.id, imageFile.absolutePath, 0.0, 0.0, showStartOutcomeToast)
                    }
                } else {
                    android.util.Log.w("StartFarmingFlow", "No location permission. Submitting with fallback 0.0...")
                    viewModel.enqueueFarmingStart(target.id, imageFile.absolutePath, 0.0, 0.0, showStartOutcomeToast)
                }
            } catch (e: Exception) {
                android.util.Log.e("StartFarmingFlow", "Failed to save start photo: ${e.message}")
                Toast.makeText(context, "تصویر محفوظ کرنے میں ناکامی: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        bookingToStartDirectly = null
    }

    var showCameraFrameDialogForDirectFlow by remember { mutableStateOf(false) }
    var showQrScannerForStartFlow by remember { mutableStateOf(false) }
    var bookingForQrStart by remember { mutableStateOf<Booking?>(null) }
    var bookingForQrPermissionStart by remember { mutableStateOf<Booking?>(null) }
    
    var showQrDialog by remember { mutableStateOf(false) }
    var bookingForShowingQrCode by remember { mutableStateOf<Booking?>(null) }
    var lastLatitude by remember { mutableStateOf<Double?>(null) }
    var lastLongitude by remember { mutableStateOf<Double?>(null) }

    // QR proximity validation (both showing and scanning the code) depends on a real device
    // location fix — if location services are off system-wide, that check would silently fail
    // or use a stale/last-known position. Gate both entry points on this before doing anything
    // else they already did.
    var showLocationDisabledDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Requesting PRIORITY_HIGH_ACCURACY updates while system location is off triggers the
        // OS/Play-Services' own native "turn on location" prompt (in whatever language the
        // device's system locale is set to, not this app's Urdu — outside app control). Checking
        // first and showing our own Urdu dialog instead means that system prompt never fires.
        if (!pk.kissanmadadgar.mobile.presentation.screens.isDeviceLocationEnabled(context)) {
            showLocationDisabledDialog = true
            return@LaunchedEffect
        }
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    lastLatitude = loc.latitude
                    lastLongitude = loc.longitude
                }
            }
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                2000
            ).setMinUpdateIntervalMillis(1000)
             .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(p0: com.google.android.gms.location.LocationResult) {
                        val latest = p0.lastLocation
                        if (latest != null) {
                            lastLatitude = latest.latitude
                            lastLongitude = latest.longitude
                            android.util.Log.d("QrScanValidation", "Farmer location updated: lat=${latest.latitude}, lng=${latest.longitude}")
                        }
                    }
                },
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {}
    }

    val locationPermissionLauncherForDirectFlow = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        showCameraFrameDialogForDirectFlow = true
    }

    val cameraPermissionLauncherForDirectFlow = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val hasLocation = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasLocation) {
                showCameraFrameDialogForDirectFlow = true
            } else {
                locationPermissionLauncherForDirectFlow.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    val cameraPermissionLauncherForQrStart = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val target = bookingForQrPermissionStart
            if (target != null) {
                bookingToStartDirectly = target
                bookingForQrStart = target
                showQrScannerForStartFlow = true
            }
        }
    }

    val onStartFarmingClick: (Booking) -> Unit = { booking ->
        android.util.Log.d("StartFarmingFlow", "onStartFarmingClick: bookingId=${booking.id}, status=${booking.status}, rentalRequestStatus=${booking.rentalRequestStatus}")
        val activeSession = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context)
        android.util.Log.d("StartFarmingFlow", "activeSession: $activeSession")
        if (activeSession != null && activeSession.bookingId != booking.id) {
            android.util.Log.w("StartFarmingFlow", "Blocked: another session is already active")
            Toast.makeText(context, "ایک وقت میں صرف ایک کاشتکاری سروس شروع کی جا سکتی ہے۔", Toast.LENGTH_LONG).show()
        } else {
            if (booking.rentalRequestStatus == "APPROVED") {
                val isProviderUser = booking.serviceProviderId != null &&
                        loggedInUserId.toLongOrNull() == booking.serviceProviderId
                if (isProviderUser) {
                    // Self-booking: same user is farmer and provider, so this first click IS the
                    // provider's start confirmation too. Start tracking now, since the local status
                    // will jump straight to STARTED_FROM_SERVICE_PROVIDER_SIDE and the provider-only
                    // start branch below will never run on this device.
                    pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.startTracking(context, booking.id)
                }
                bookingToStartDirectly = booking
                val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                android.util.Log.d("StartFarmingFlow", "hasCamera: $hasCamera")
                if (hasCamera) {
                    val hasLocation = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    android.util.Log.d("StartFarmingFlow", "hasLocation: $hasLocation")
                    if (hasLocation) {
                        android.util.Log.d("StartFarmingFlow", "Setting showCameraFrameDialogForDirectFlow = true")
                        showCameraFrameDialogForDirectFlow = true
                    } else {
                        android.util.Log.d("StartFarmingFlow", "Requesting location permission")
                        locationPermissionLauncherForDirectFlow.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } else {
                    android.util.Log.d("StartFarmingFlow", "Requesting camera permission")
                    cameraPermissionLauncherForDirectFlow.launch(android.Manifest.permission.CAMERA)
                }
            } else if (booking.rentalRequestStatus == "STARTED_FROM_FARMER_SIDE") {
                val isProviderUser = booking.serviceProviderId != null &&
                        loggedInUserId.toLongOrNull() == booking.serviceProviderId
                if (isProviderUser) {
                    // Provider starts directly online!
                    pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.startTracking(context, booking.id)
                    bookingToStartDirectly = booking
                    val dummyBitmap = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
                    handleDirectPhotoCaptured(dummyBitmap)
                }
            }
        }
    }

    var selectedFilter by remember {
        val hasActiveSession = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context) != null
        mutableStateOf<String?>(if (hasActiveSession) "ONGOING" else "PENDING")
    }
    var currentPage by remember { mutableStateOf(0) }
    // Search narrows within whichever tab (PENDING/ONGOING/COMPLETED) is currently selected —
    // it never replaces or hides the tabs. The status filter is always sent to the backend
    // alongside the keyword, exactly as if the user had picked that tab with no search at all.
    var searchKeyword by remember { mutableStateOf("") }

    LaunchedEffect(selectedFilter) {
        currentPage = 0
    }
    LaunchedEffect(searchKeyword) {
        currentPage = 0
    }

    val statusParam = when (selectedFilter) {
        "PENDING" -> "PENDING"
        "ONGOING" -> "APPROVED,STARTED_FROM_FARMER_SIDE,STARTED_FROM_SERVICE_PROVIDER_SIDE"
        "COMPLETED" -> "COMPLETED"
        else -> selectedFilter
    }

    LaunchedEffect(statusParam, currentPage, searchKeyword) {
        if (searchKeyword.isNotBlank()) {
            // Debounce keystrokes: each change to searchKeyword cancels this coroutine and
            // restarts it, so only the last keystroke in a burst actually survives to fire.
            kotlinx.coroutines.delay(400)
        }
        viewModel.fetchRentalBookings(
            status = statusParam,
            page = currentPage,
            size = 10,
            keyword = searchKeyword.ifBlank { null }
        )
    }

    // Notification-tap deep link (see NotificationsScreen.onClick / MainActivity's onNewIntent
    // and cold-start handling — all three just call setNotificationBookingId then land here on
    // the Bookings tab). fetchBookingById resolves the target regardless of which status filter
    // is currently loaded, since a notification's booking may not be in the currently-fetched page.
    val notificationBookingId by viewModel.notificationBookingId.collectAsState()
    LaunchedEffect(notificationBookingId) {
        notificationBookingId?.let { id -> viewModel.fetchBookingById(id) }
    }
    LaunchedEffect(notificationBookingId, bookings) {
        val id = notificationBookingId
        if (id != null) {
            val active = bookings.find { it.id == id }
            if (active != null) {
                // Land on whichever tab this booking actually belongs to — "جدید بکنگ" for
                // PENDING, "جاری" for ACCEPTED/ACTIVE, "مکمل" for COMPLETED. REJECTED bookings
                // have no dedicated tab (see filteredRequests below), so leave the current
                // filter as-is for those; the detail sheet below still opens regardless of
                // which filter/list is currently visible.
                selectedFilter = when (active.status) {
                    BookingStatus.PENDING -> "PENDING"
                    BookingStatus.ACCEPTED, BookingStatus.ACTIVE -> "ONGOING"
                    BookingStatus.COMPLETED -> "COMPLETED"
                    else -> selectedFilter
                }
                bottomSheetBooking = active
                scrollToBookingId = active.id
                viewModel.setNotificationBookingId(null)
            }
        }
    }

    val isLast by viewModel.bookingsIsLast.collectAsState()
    val listState = rememberLazyListState()

    // Auto-fetch the next page as the user nears the bottom of the list, instead of requiring
    // an explicit "اگلا صفحہ" tap. Guarded on !isLast (no more pages) and !isRefreshingBookings
    // (a fetch is already in flight) so scrolling near the bottom repeatedly can't fire off
    // duplicate requests for the same page.
    LaunchedEffect(listState, isLast, isRefreshingBookings) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= layoutInfo.totalItemsCount - 3
        }.collect { nearBottom ->
            if (nearBottom && !isLast && !isRefreshingBookings) {
                currentPage++
            }
        }
    }

    // Shared narration singleton (see NarrationManager) — see FarmerScreens.kt for the same
    // pattern; initialize() is idempotent so calling it again here is a safe no-op if some other
    // screen already started it this session.
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }
    val activeSpeakingAudioId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()

    val onSpeakClick: (Booking) -> Unit = { booking ->
        if (activeSpeakingAudioId == booking.id) {
            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
        } else {
            val text = buildBookingNarrationText(context, booking, loggedInUserId, currentUser?.fullName)
            pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, booking.id)
        }
    }



    val filteredRequests = remember(bookings, selectedFilter, searchKeyword) {
        val statusFiltered = when (selectedFilter) {
            "PENDING" -> bookings.filter { it.status == BookingStatus.PENDING }
            "ONGOING" -> bookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ACTIVE }
            "COMPLETED" -> bookings.filter { it.status == BookingStatus.COMPLETED }
            else -> bookings
        }
        val keywordFiltered = if (searchKeyword.isNotBlank()) {
            // Narrows within the tab already selected above — client-side safety net matching
            // whatever's already cached, on top of the backend's own keyword search (see the
            // fetch above), by booking id or either party's phone number.
            statusFiltered.filter {
                it.id.contains(searchKeyword, ignoreCase = true) ||
                    it.providerPhone.contains(searchKeyword, ignoreCase = true) ||
                    it.farmerPhone.contains(searchKeyword, ignoreCase = true)
            }
        } else {
            statusFiltered
        }
        // Newest request first. Booking ids are numeric strings assigned in increasing order by
        // the backend, so sorting by that numeric value (not lexicographically) keeps e.g. #9
        // above #10. The one exception — an in-progress timer pinned above this order — is
        // applied afterwards in visibleRequests below.
        keywordFiltered.sortedByDescending { it.id.toLongOrNull() ?: Long.MIN_VALUE }
    }

    // Only one farming session can ever be active at a time (single active_farming_session.json
    // slot — see FarmerRequestCard's own per-card polling above), so surfacing it means finding
    // that one id and pinning its card first. Polled the same way the card polls its own timer,
    // since getActiveSession reads a local file rather than a Flow the UI can just observe.
    var activeSessionBookingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            activeSessionBookingId = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context)?.bookingId
            kotlinx.coroutines.delay(1000L)
        }
    }

    val visibleRequests = remember(filteredRequests, activeSessionBookingId) {
        if (activeSessionBookingId != null) {
            filteredRequests.sortedByDescending { it.id == activeSessionBookingId }
        } else {
            filteredRequests
        }
    }
    val pendingCount = bookings.count { it.status == BookingStatus.PENDING }
    val ongoingCount = bookings.count { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ACTIVE }
    val finishedCount = bookings.count { it.status == BookingStatus.COMPLETED }

    // Scrolls the underlying list to the notification's booking (see scrollToBookingId above),
    // not just opening the sheet on top of it — so if the sheet gets dismissed, the booking is
    // right there in view instead of the list sitting wherever it happened to be scrolled.
    // Keyed on visibleRequests since the target only becomes findable once the correct filter
    // (set alongside scrollToBookingId) has actually re-filtered the list.
    LaunchedEffect(scrollToBookingId, visibleRequests, isOffline, isRefreshingBookings) {
        val targetId = scrollToBookingId
        if (targetId != null) {
            val index = visibleRequests.indexOfFirst { it.id == targetId }
            if (index >= 0) {
                // Request cards aren't the first items in this LazyColumn — RequestListHeader
                // and RequestDashboardStatsRow always precede them, plus one more (offline
                // banner or refresh bar) when either is showing. Must match that item structure
                // exactly, or this scrolls to the wrong row.
                val headerItemCount = 2 + (if (isOffline || isRefreshingBookings) 1 else 0)
                listState.animateScrollToItem(headerItemCount + index)
                scrollToBookingId = null
            }
        }
    }

    // Keep detail booking in sync with latest data
    val currentBottomSheetBooking = bottomSheetBooking?.let { bs ->
        bookings.find { it.id == bs.id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BookingsSearchBar(
                    keyword = searchKeyword,
                    onKeywordChange = { searchKeyword = it },
                    isSpeaking = activeSpeakingAudioId == "bookings_search_helper",
                    onSpeakClick = {
                        if (activeSpeakingAudioId == "bookings_search_helper") {
                            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                        } else {
                            pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(
                                context.getString(R.string.bookings_search_narration),
                                "bookings_search_helper"
                            )
                        }
                    }
                )
            }

            if (isOffline) {
                item { OfflineBanner() }
            } else {
                // Always emitted as an item (rather than conditionally added/removed) so its
                // appearance/disappearance animates its height via AnimatedVisibility instead of
                // abruptly popping the rest of the list up and down on every tab switch/refresh —
                // that abrupt reflow was what made switching tabs look like "refreshing twice".
                item {
                    AnimatedVisibility(
                        visible = isRefreshingBookings,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(999.dp)),
                            color = AgriGreenPrimary,
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                }
            }

            item {
                RequestDashboardStatsRow(
                    pendingCount = pendingCount,
                    ongoingCount = ongoingCount,
                    finishedCount = finishedCount,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            if (visibleRequests.isEmpty()) {
                item {
                    // Each status tab gets its own empty-state copy rather than one generic
                    // message — a farmer on "مکمل" (COMPLETED) with nothing there yet needs
                    // different wording than one on "جدید بکنگ" (PENDING) with no new requests.
                    // A blank search always wins regardless of tab, same as before.
                    val (emptyTitleRes, emptyDescRes) = when {
                        searchKeyword.isNotBlank() -> R.string.bookings_search_no_results to R.string.no_bookings_desc
                        selectedFilter == "PENDING" -> R.string.no_bookings_pending_title to R.string.no_bookings_pending_desc
                        selectedFilter == "ONGOING" -> R.string.no_bookings_ongoing_title to R.string.no_bookings_ongoing_desc
                        selectedFilter == "COMPLETED" -> R.string.no_bookings_completed_title to R.string.no_bookings_completed_desc
                        else -> R.string.no_bookings_available to R.string.no_bookings_desc
                    }
                    PremiumEmptyState(
                        message = stringResource(id = emptyTitleRes),
                        description = stringResource(id = emptyDescRes),
                        icon = Icons.Default.CalendarMonth
                    )
                }
            } else {
                itemsIndexed(visibleRequests, key = { _, item -> item.id }) { _, booking ->
                    FarmerRequestCard(
                        booking = booking,
                        modifier = Modifier.animateItemPlacement(),
                        requestNumber = booking.id,
                        onClick = { bottomSheetBooking = booking },
                        activeSpeakingAudioId = activeSpeakingAudioId,
                        onSpeakClick = onSpeakClick,
                        onAcceptRequest = { viewModel.acceptBookingRequest(booking.id) },
                        onRejectRequest = { reason -> viewModel.rejectBookingRequest(booking.id, reason) },
                        loggedInUserId = loggedInUserId,
                        onStartFarmingActivity = { onStartFarmingClick(booking) },
                        onResumeFarmingActivity = {
                            // Local tracking session was lost (app killed, service stopped, device
                            // rebooted) even though the server already knows this booking is started.
                            // Just restart the local foreground session — do NOT re-run the full
                            // start flow, since that would redundantly re-notify the server.
                            //
                            // Guard against hijacking another booking's still-running session: the
                            // single active_farming_session.json slot can only track one booking at
                            // a time, so if a different booking is genuinely active, resuming this
                            // one would silently overwrite (and lose track of) that one instead.
                            val activeSession = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context)
                            if (activeSession != null && activeSession.bookingId != booking.id) {
                                Toast.makeText(context, "ایک وقت میں صرف ایک کاشتکاری سروس شروع کی جا سکتی ہے۔", Toast.LENGTH_LONG).show()
                            } else {
                                pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.startTracking(context, booking.id)
                            }
                        },
                        onScanQrClick = {
                            if (!pk.kissanmadadgar.mobile.presentation.screens.isDeviceLocationEnabled(context)) {
                                showLocationDisabledDialog = true
                            } else {
                                val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCamera) {
                                    bookingToStartDirectly = booking
                                    bookingForQrStart = booking
                                    showQrScannerForStartFlow = true
                                } else {
                                    bookingForQrPermissionStart = booking
                                    cameraPermissionLauncherForQrStart.launch(android.Manifest.permission.CAMERA)
                                }
                            }
                        },
                        onShowQrClick = {
                            if (!pk.kissanmadadgar.mobile.presentation.screens.isDeviceLocationEnabled(context)) {
                                showLocationDisabledDialog = true
                            } else {
                                bookingForShowingQrCode = booking
                                showQrDialog = true
                            }
                        },
                        onStopFarmingActivity = { localPath, lat, lng, accuracy, speed, heading, altitude, isMock, totalServiceTime ->
                            viewModel.enqueueFarmingComplete(
                                bookingId = booking.id,
                                localFilePath = localPath,
                                latitude = lat,
                                longitude = lng,
                                accuracy = accuracy,
                                speed = speed,
                                heading = heading,
                                altitude = altitude,
                                isMock = isMock,
                                totalServiceTime = totalServiceTime
                            ) { outcome ->
                                val msg = if (outcome == pk.kissanmadadgar.mobile.data.remote.UploadOutcome.SYNCED) {
                                    "کاشتکاری مکمل کرنے کا ڈیٹا کامیابی سے اپلوڈ ہو گیا ہے۔"
                                } else {
                                    "کام مکمل کرنے کی درخواست شیڈول کر دی گئی ہے۔"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        onSubmitFeedback = { rating, comment, onResult ->
                            viewModel.submitBookingFeedback(booking.id, rating, comment, onResult)
                        }
                    )
                }
            }

            // Loading-more indicator: only while fetching an additional page (currentPage > 0),
            // not the initial page load — that's already covered by the LinearProgressIndicator
            // at the top of the list.
            if (currentPage > 0 && isRefreshingBookings && !isLast) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = AgriGreenPrimary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }

    }

    // Bottom sheet quick preview (shown on card tap)
    if (currentBottomSheetBooking != null) {
        FarmerBookingDetailBottomSheet(
            booking = currentBottomSheetBooking,
            onDismiss = { bottomSheetBooking = null },
            activeSpeakingAudioId = activeSpeakingAudioId,
            onSpeakClick = onSpeakClick,
            onAcceptRequest = { viewModel.acceptBookingRequest(currentBottomSheetBooking.id) },
            onRejectRequest = { reason -> viewModel.rejectBookingRequest(currentBottomSheetBooking.id, reason) },
            loggedInUserId = loggedInUserId,
            onStartFarmingActivity = {
                onStartFarmingClick(currentBottomSheetBooking)
                bottomSheetBooking = null
            }
        )
    }

    if (showCameraFrameDialogForDirectFlow) {
        CameraFrameDialog(
            onDismiss = {
                showCameraFrameDialogForDirectFlow = false
                bookingToStartDirectly = null
            },
            onPictureCaptured = { bitmap ->
                showCameraFrameDialogForDirectFlow = false
                handleDirectPhotoCaptured(bitmap)
            },
            userName = currentUser?.fullName ?: "کاشتکار"
        )
    }

    if (showLocationDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDisabledDialog = false },
            title = { Text(text = stringResource(id = R.string.location_disabled_title)) },
            text = { Text(text = stringResource(id = R.string.location_disabled_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showLocationDisabledDialog = false
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text(text = stringResource(id = R.string.location_disabled_open_settings), color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDisabledDialog = false }) {
                    Text(text = stringResource(id = R.string.location_disabled_cancel))
                }
            }
        )
    }

    if (showQrScannerForStartFlow) {
        val target = bookingForQrStart
        if (target != null) {
            QrScannerView(
                targetName = "کاشتکار",
                onScanCompleted = { qrContent ->
                    val isValid = validateScanData(qrContent, target, lastLatitude ?: 0.0, lastLongitude ?: 0.0, context)
                    if (isValid) {
                        showQrScannerForStartFlow = false
                        bookingForQrStart = null
                        
                        // Instantly start background service to capture logs
                        pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.startTracking(context, target.id)
                        
                        // Create dummy 1x1 bitmap to bypass picture taking requirement for starting provider journey
                        val dummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        handleDirectPhotoCaptured(dummyBitmap)
                    }
                },
                onCancel = {
                    showQrScannerForStartFlow = false
                    bookingForQrStart = null
                    bookingToStartDirectly = null
                }
            )
        }
    }
    if (showQrDialog) {
        val target = bookingForShowingQrCode
        if (target != null) {
            val qrContent = if (lastLatitude != null && lastLongitude != null) {
                "{" +
                "\"bookingId\":\"${target.id}\"," +
                "\"serviceProviderId\":${target.serviceProviderId ?: 0}," +
                "\"serviceTakerId\":${target.serviceTakerId ?: 0}," +
                "\"status\":\"STARTED_FROM_FARMER_SIDE\"," +
                "\"latitude\":$lastLatitude," +
                "\"longitude\":$lastLongitude" +
                "}"
            } else null

            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showQrDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showQrDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Gray
                                )
                            }
                            Text(
                                text = "کیو آر کوڈ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenPrimary
                            )
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                        
                        Text(
                            text = "برائے کاشتکاری تصدیق",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        if (qrContent != null) {
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .border(BorderStroke(2.dp, AgriGreenPrimary), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                QrCodeGenerator(content = qrContent)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .border(BorderStroke(2.dp, Color.LightGray), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = AgriGreenPrimary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "جی پی ایس لوکیشن حاصل کی جا رہی ہے...",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "درخواست نمبر: ${target.id}",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    BookingActionResultDialog(
        actionState = actionState,
        onDismiss = { viewModel.clearBookingActionState() },
        onApproveSuccess = { selectedFilter = "ONGOING" }
    )

    if (isSubmittingFarmingAction) {
        UploadingLoaderDialog()
    }
}


// Replaces the old static "آپ کی بکنگز" title card: a narration helper (explains the search
// to the user out loud) plus the actual search input, wired to the ?keyword= param on
// GET api/android/rental-bookings so users can find any booking by its id or either party's
// phone number, regardless of which status tab it's actually in.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingsSearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Same compact speak-button style as NotificationsScreen/AgriHomeStatCard —
            // SupportAgent while idle, VolumeUp while speaking, orange-to-red gradient circle.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isSpeaking) {
                                listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                            } else {
                                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                            }
                        ),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onSpeakClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.SupportAgent,
                    contentDescription = "بولیں",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.bookings_search_placeholder),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        IconButton(onClick = { onKeywordChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgriGreenPrimary,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )
        }
    }
}

@Composable
private fun OfflineBanner() {
    val offlineOrange = Color(0xFFF57C00)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF3E0))
            .border(BorderStroke(1.dp, offlineOrange.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = offlineOrange,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "آف لائن — مقامی ڈیٹا دکھایا جا رہا ہے",
            color = offlineOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun FarmerGuestBookingsTab(onLoginRedirect: () -> Unit, onClose: () -> Unit, supportMessage: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.guest_bookings_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.guest_bookings_desc),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    UrduButton(
                        text = stringResource(id = R.string.btn_login_register),
                        onClick = onLoginRedirect
                    )
                    if (!supportMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = supportMessage,
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
