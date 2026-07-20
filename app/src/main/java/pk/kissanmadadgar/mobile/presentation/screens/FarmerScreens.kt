package pk.kissanmadadgar.mobile.presentation.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.mutableStateMapOf
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.components.UrduTextField
import pk.kissanmadadgar.mobile.core.components.AutoDismissAlert
import pk.kissanmadadgar.mobile.core.components.CNICInputField
import pk.kissanmadadgar.mobile.core.components.AgriDetailHeader
import pk.kissanmadadgar.mobile.core.components.*
import androidx.compose.foundation.BorderStroke
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.core.theme.AgriGreenSecondary
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.domain.model.MachineryStatus
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.core.UrduNarrations
import androidx.compose.ui.focus.FocusRequester
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import pk.kissanmadadgar.mobile.presentation.LogoutOutcome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.activity.compose.rememberLauncherForActivityResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerHomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onLoginRedirect: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToRegisterMachinery: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToGovernmentSchemes: () -> Unit = {}
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val userCnic by viewModel.userCnic.collectAsState()
    val userDistrict by viewModel.userDistrict.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val districtsList by viewModel.districtsList.collectAsState()
    val profileResponse by viewModel.profileResponse.collectAsState()
    val supportResponse by viewModel.supportResponse.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()
    var logoutBlockedReason by remember { mutableStateOf<LogoutOutcome?>(null) }

    // FarmerHomeScreen is the root destination (bottom of the nav stack), so a back press
    // here has nothing left to pop to and would otherwise exit the app immediately with no
    // warning. Require a second back press within 2s to actually exit, same double-tap-to-exit
    // pattern used elsewhere (WhatsApp, Instagram, etc.).
    var lastBackPressTime by remember { mutableStateOf(0L) }
    BackHandler(enabled = true) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, context.getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
        }
    }

    // Logout is gated on FarmingUploadSyncManager's queue being empty (see
    // MainViewModel.requestLogout) so an unsynced start/complete record is never silently
    // discarded — this just routes the result to the right UI feedback.
    val attemptLogout: () -> Unit = {
        viewModel.requestLogout { outcome ->
            if (outcome == LogoutOutcome.LOGGED_OUT) {
                onLogout()
            } else {
                logoutBlockedReason = outcome
            }
        }
    }

    val loc by viewModel.userLocation.collectAsState()
    // Keyed on selectedTab/user only (not loc) — GPS ticks every few seconds, and keying on
    // loc here re-fetched machinery and redrew the whole map on every single tick, which reset
    // any district filter, discarded "load more" pagination progress, and yanked the map back
    // to the user's position repeatedly. Fetch once per tab visit instead.
    LaunchedEffect(selectedTab, user) {
        if (selectedTab == 0 || selectedTab == 1) {
            val type = if (selectedTab == 1) "search" else "home"
            viewModel.fetchAvailableMachines(loc.first, loc.second, type)
        }
    }




    LaunchedEffect(selectedTab, user) {
        val module = when (selectedTab) {
            0 -> "HOME"
            1 -> "BOOKINGS"
            2 -> "MACHINES"
            3 -> "MARKET"
            4 -> "PROFILE"
            else -> "HOME"
        }
        viewModel.fetchSupportInfo(module = module)
    }

    LaunchedEffect(profileResponse) {
        if (profileResponse?.editDistrict == true) {
            viewModel.fetchDistricts()
        }
    }

    LaunchedEffect(selectedTab, user) {
        if (selectedTab == 4 && user != null) {
            viewModel.fetchUserProfile()
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            viewModel.fetchUnreadNotificationCount()
        }
    }

    Scaffold(
        topBar = {
            AgriAppHeader(
                title = stringResource(id = R.string.app_name),
                onProfileClick = { viewModel.setSelectedTab(4) },
                onBellClick = onNavigateToNotifications,
                unreadNotificationCount = unreadNotificationCount
            )
        },
        bottomBar = {
            val headerBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0A331A), // Deepest forest green
                    Color(0xFF15532D), // Rich dark green
                    Color(0xFF2E9B5C), // Emerald-lime shiny stripe
                    Color(0xFF15532D), // Rich dark green
                    Color(0xFF0A331A)  // Deepest forest green
                )
            )
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        clip = true
                    ),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = headerBrush)
                        .fillMaxWidth()
                ) {
                    NavigationBar(containerColor = Color.Transparent) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setSelectedTab(0) },
                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                            label = { Text(text = stringResource(id = R.string.tab_home)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AgriGreenPrimary,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setSelectedTab(1) },
                            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                            label = { Text(text = stringResource(id = R.string.tab_search)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AgriGreenPrimary,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setSelectedTab(2) },
                            icon = { Icon(imageVector = Icons.Default.List, contentDescription = null) },
                            label = { Text(text = stringResource(id = R.string.tab_bookings)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AgriGreenPrimary,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.setSelectedTab(3) },
                            icon = { Icon(imageVector = Icons.Default.Agriculture, contentDescription = null) },
                            label = { Text(text = stringResource(id = R.string.tab_machineries), maxLines = 1, softWrap = false) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AgriGreenPrimary,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.setSelectedTab(4) },
                            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                            label = { Text(text = stringResource(id = R.string.tab_profile)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AgriGreenPrimary,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {
            val guestName = stringResource(id = R.string.user_guest_name)
            when (selectedTab) {
                0 -> FarmerMainTab(
                    viewModel = viewModel,
                    userName = user?.fullName ?: guestName,
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToBooking = onNavigateToBooking,
                    onNavigateToBookings = { viewModel.setSelectedTab(2) },
                    onViewAllClick = { viewModel.setSelectedTab(1) },
                    onNavigateToProfile = { viewModel.setSelectedTab(4) },
                    onNavigateToRegisterMachinery = onNavigateToRegisterMachinery,
                    onNavigateToGovernmentSchemes = onNavigateToGovernmentSchemes
                )
                1 -> FarmerSearchTab(viewModel, onNavigateToDetail, onNavigateToBooking)
                2 -> {
                    if (user == null) {
                        FarmerGuestBookingsTab({ onLoginRedirect(false) }, onClose = { viewModel.setSelectedTab(0) }, supportMessage = supportResponse?.message)
                    } else {
                        FarmerBookingsTab(viewModel)
                    }
                }
                3 -> {
                    if (user == null) {
                        FarmerGuestMachineriesTab({ onLoginRedirect(true) }, onClose = { viewModel.setSelectedTab(0) }, supportMessage = supportResponse?.message)
                    } else {
                        ProviderInventoryTab(viewModel, onNavigateToRegisterMachinery)
                    }
                }
                4 -> {
                    if (user == null) {
                        FarmerGuestProfileTab({ onLoginRedirect(false) }, supportMessage = supportResponse?.message)
                    } else {
                        val rawPhone = user?.phoneNumber ?: ""
                        val displayPhone = if (rawPhone.startsWith("+92")) {
                            "0" + rawPhone.substring(3)
                        } else {
                            rawPhone
                        }
                        FarmerProfileTab(
                            name = user?.fullName ?: guestName,
                            phone = displayPhone,
                            address = userAddress,
                            cnic = userCnic,
                            district = userDistrict,
                            districtsList = districtsList.map { it.nameUrdu },
                            isNameEditable = profileResponse?.editName ?: true,
                            isPhoneEditable = profileResponse?.editMobile ?: true,
                            isAddressEditable = profileResponse?.editAddress ?: true,
                            isCnicEditable = profileResponse?.editCnic ?: true,
                            isDistrictEditable = profileResponse?.editDistrict ?: true,
                            supportMessage = supportResponse?.message,
                            onSaveClick = { finalName, finalPhone, finalAddress, finalCnic, finalDistrict ->
                                viewModel.saveProfile(finalName, finalPhone, finalAddress, finalCnic, finalDistrict) { success, error ->
                                    val message = if (success) {
                                        context.getString(R.string.msg_profile_saved)
                                    } else {
                                        error ?: context.getString(R.string.err_profile_save_failed)
                                    }
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            onLogout = attemptLogout,
                            onClose = { viewModel.setSelectedTab(0) }
                        )
                    }
                }
            }

            if (isLoggingOut) {
                UploadingLoaderDialog(message = "لاگ آؤٹ سے پہلے آپ کا ڈیٹا مطابقت پذیر ہو رہا ہے، براہ کرم انتظار کریں")
            }

            logoutBlockedReason?.let { reason ->
                val message = when (reason) {
                    LogoutOutcome.BLOCKED_ACTIVE_SESSION ->
                        "کاشتکاری کی سرگرمی ابھی جاری ہے۔ لاگ آؤٹ کرنے سے پہلے سروس روکیں۔"
                    LogoutOutcome.BLOCKED_OFFLINE ->
                        "آپ کی کاشتکاری کی سرگرمی ابھی تک سرور پر اپلوڈ نہیں ہوئی۔ یہ ڈیٹا ضائع ہونے سے بچانے کے لیے پہلے انٹرنیٹ سے جڑیں، پھر لاگ آؤٹ کریں۔"
                    LogoutOutcome.BLOCKED_SYNC_FAILED ->
                        "ڈیٹا اپلوڈ کرنے کی کوشش ناکام ہوئی۔ براہ کرم دوبارہ کوشش کریں یا کچھ دیر بعد لاگ آؤٹ کریں۔"
                    LogoutOutcome.LOGGED_OUT -> ""
                }
                AlertDialog(
                    onDismissRequest = { logoutBlockedReason = null },
                    title = {
                        Text(
                            text = "لاگ آؤٹ ممکن نہیں",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFD32F2F)
                        )
                    },
                    text = {
                        Text(text = message, fontSize = 15.sp, color = Color(0xFF17251B))
                    },
                    confirmButton = {
                        Button(
                            onClick = { logoutBlockedReason = null },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                        ) {
                            Text("ٹھیک ہے", color = Color.White)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

private val TriangleShape = object : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

@Composable
fun WelcomeHeader(
    userName: String,
    lat: Double? = null,
    lng: Double? = null,
    activeSpeakingAudioId: String?
) {
    val isSpeaking = activeSpeakingAudioId == "welcome"
    val context = androidx.compose.ui.platform.LocalContext.current

    var locationText by remember { mutableStateOf("") }
    var showAssistantTooltip by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(10000)
        showAssistantTooltip = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )
    
    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val rippleAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha2"
    )

    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking) 1.0f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    LaunchedEffect(lat, lng) {
        if (lat != null && lng != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val geocoder = android.location.Geocoder(context, java.util.Locale("ur"))
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val neighborhood = address.subLocality ?: address.thoroughfare
                        val city = address.locality ?: address.subAdminArea ?: address.adminArea
                        val shortAddress = if (!neighborhood.isNullOrBlank() && !city.isNullOrBlank() && neighborhood != city) {
                            "$neighborhood، $city"
                        } else {
                            city ?: neighborhood ?: ""
                        }
                        if (shortAddress.isNotBlank()) {
                            locationText = shortAddress
                        }
                    }
                } catch (e: Exception) {
                    // Ignore, keep empty
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AgriGreenPrimary.copy(alpha = 0.3f),
                spotColor = AgriGreenPrimary
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AgriGreenPrimary.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AgriGreenPrimary, Color(0xFF1B5E20))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Audio Assistant Column (Button + Tooltip)
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                // Animated Kissan Madadgar Icon Button with Ripples
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ripples (glowing waves when idle)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = rippleScale1,
                                scaleY = rippleScale1,
                                alpha = rippleAlpha1
                            )
                            .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = rippleScale2,
                                scaleY = rippleScale2,
                                alpha = rippleAlpha2
                            )
                            .background(Color(0xFFFF6D00).copy(alpha = 0.4f), CircleShape)
                    )

                    // Main Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer(
                                scaleX = buttonScale,
                                scaleY = buttonScale
                            )
                            .shadow(elevation = 6.dp, shape = CircleShape)
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
                            .clickable {
                                if (isSpeaking) {
                                    pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                } else {
                                    showAssistantTooltip = false
                                    val text = UrduNarrations.getWelcomeNarration(context)
                                    pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "welcome")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpeaking) {
                            // Active sound wave equalizer animation
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 4) {
                                    val barHeightScale by infiniteTransition.animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 300 + (i * 100)),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "bar_$i"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(16.dp * barHeightScale)
                                            .background(Color.White, RoundedCornerShape(1.5.dp))
                                    )
                                }
                            }
                        } else {
                            // Friendly human Kissan Madadgar assistant icon (headset agent)
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = stringResource(id = R.string.content_description_listen_audio_alt),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Speech Bubble Tooltip Popup
                if (showAssistantTooltip) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clickable { showAssistantTooltip = false }
                            .animateContentSize()
                    ) {
                        // Pointing triangle pointing UP to the button center
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(width = 12.dp, height = 6.dp)
                                .background(
                                    color = Color(0xFFFFF9C4),
                                    shape = TriangleShape
                                )
                        )
                        
                        // Speech Bubble Box
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                            border = BorderStroke(1.dp, Color(0xFFFBC02D).copy(alpha = 0.5f)),
                            modifier = Modifier.widthIn(max = 210.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(id = R.string.kissan_madadgar_popup_text),
                                    color = Color(0xFF5D4037),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.btn_close_tooltip),
                                    tint = Color(0xFF795548),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { showAssistantTooltip = false }
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(95.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp, y = 16.dp)
            )

            Column(modifier = Modifier.fillMaxWidth().padding(end = 68.dp)) {
                val cleanName = userName.substringBefore("(").trim()
                Text(
                    text = stringResource(id = R.string.welcome_user_format, cleanName),
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                if (locationText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationText,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgriHomeStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    isAnySelected: Boolean,
    onClick: () -> Unit,
    onSpeakClick: (() -> Unit)? = null,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardElevation = if (isSelected) 8.dp else 2.dp
    val alphaFactor = if (isAnySelected && !isSelected) 0.55f else 1.0f
    
    val cardBgColor = if (isSelected) color else Color.White
    val cardBorderColor = if (isSelected) color else color.copy(alpha = 0.15f)
    
    val valueColor = if (isSelected) Color.White else color
    val titleColor = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF3F4E42)
    val watermarkColor = if (isSelected) Color.White.copy(alpha = 0.15f) else color.copy(alpha = 0.05f)
    val badgeBgColor = if (isSelected) Color.White.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)
    val badgeIconColor = if (isSelected) Color.White else color

    Card(
        modifier = modifier
            .height(96.dp)
            .clickable { onClick() }
            .alpha(alphaFactor),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, cardBorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(color, color.copy(alpha = 0.85f))
                        } else {
                            listOf(Color.White, color.copy(alpha = 0.06f))
                        }
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Watermarked background icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = watermarkColor,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeIconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (onSpeakClick != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
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
                                .clickable { onSpeakClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSpeaking) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Blank value (see the map-select tile in FarmerMainTab) means there's no
                    // count to show — skip the digit + spacer entirely so the title gets the
                    // tile's full width instead of being squeezed and ellipsized.
                    if (value.isNotBlank()) {
                        Text(
                            text = value,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = valueColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedMachineryCard(machinery: Machinery, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.other_machinery_clean),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = machinery.nameUr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "${stringResource(id = R.string.provider_name_label)} ${machinery.providerName}", fontSize = 14.sp, color = Color.DarkGray)
                        Text(text = "${stringResource(id = R.string.provider_phone_label)} ${machinery.providerPhone}", fontSize = 14.sp, color = Color.Gray)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AgriGreenLight).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = stringResource(id = R.string.model_year_format, machinery.modelYear), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = machinery.distanceText ?: stringResource(id = R.string.distance_km_format, "1.2"), color = Color.Gray, fontSize = 13.sp)
                    Text(text = stringResource(id = R.string.hourly_rate_format, machinery.hourlyRate.toInt()) + stringResource(id = R.string.per_hour_suffix), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ZaraiMachineRegisterBanner(
    onClick: () -> Unit,
    activeSpeakingAudioId: String?,
    modifier: Modifier = Modifier
) {
    val isSpeaking = activeSpeakingAudioId == "banner_info"
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    
    // Rotate SweepGradient angle to create running border
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // Pulsing scale for the registration icon badge on the right
    val regIconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "regIconScale"
    )

    val borderBrush = remember(angle) {
        object : ShaderBrush() {
            override fun createShader(size: Size): android.graphics.Shader {
                return android.graphics.SweepGradient(
                    size.width / 2f,
                    size.height / 2f,
                    intArrayOf(
                        AgriGreenPrimary.toArgb(),
                        Color(0xFFFFB300).toArgb(), // Gold
                        Color(0xFF25D366).toArgb(), // WhatsApp green
                        AgriGreenPrimary.toArgb()
                    ),
                    null
                ).apply {
                    val matrix = android.graphics.Matrix()
                    matrix.postRotate(angle, size.width / 2f, size.height / 2f)
                    setLocalMatrix(matrix)
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AgriGreenPrimary.copy(alpha = 0.15f),
                spotColor = AgriGreenPrimary
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, borderBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFECF7F2), // Light mint green
                            Color(0xFFF9FDFB)  // Soft white
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Rightmost Icon in RTL (First child in code, gets placed on Right)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(AgriGreenPrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AppRegistration,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(scaleX = regIconScale, scaleY = regIconScale)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Middle Text Area in RTL (Second child in code)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(id = R.string.register_machine_banner_title),
                        color = AgriGreenPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Right
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 3. Leftmost Action Button in RTL (Audio Assistant Button)
                val context = LocalContext.current
                val buttonScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isSpeaking) 1.0f else 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "buttonScale"
                )
                val rippleScale1 by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ripple1"
                )
                val rippleAlpha1 by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleAlpha1"
                )

                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSpeaking) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale)
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
                            .clickable {
                                if (isSpeaking) {
                                    pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                } else {
                                    val text = context.getString(R.string.machine_registeration_info)
                                    pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "banner_info")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpeaking) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 3) {
                                    val barHeightScale by infiniteTransition.animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(400 + i * 150),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "bar_banner_$i"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(width = 2.5.dp, height = 12.dp)
                                            .graphicsLayer(scaleY = barHeightScale)
                                            .background(Color.White, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = stringResource(id = R.string.content_description_listen_audio),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionTileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, color.copy(alpha = 0.05f))
                    )
                )
                .padding(14.dp)
        ) {
            // Watermark Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-8).dp, y = 12.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FarmerMainTab(
    viewModel: MainViewModel,
    userName: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onNavigateToBookings: () -> Unit,
    onViewAllClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRegisterMachinery: () -> Unit,
    onNavigateToGovernmentSchemes: () -> Unit = {}
) {
    val context = LocalContext.current
    val availableList by viewModel.availableMachinery.collectAsState()
    // Server-computed total booking count (0 for a guest) — see MainViewModel.myBookingCounter.
    // Replaces the old local bookings.size count, which under-counted since the Room cache only
    // ever holds whichever booking statuses happened to already be fetched this session.
    val myBookingCounter by viewModel.myBookingCounter.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null
    val homeUserLocation by viewModel.userLocation.collectAsState()
    var showMachineryMapFromHome by remember { mutableStateOf(false) }

    // --- Shared narration singleton (see NarrationManager) so this screen doesn't spin up its
    // own TextToSpeech engine — initialize() is idempotent, safe to call every time this
    // composable enters composition. LaunchedEffect (not DisposableEffect): the shared instance
    // must outlive this screen, so it must never be stopped/shutdown when this leaves composition.
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }
    val activeSpeakingAudioId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()

    var selectedHomeFilter by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "tab_pulse")
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )
    
    var showRegisterInfoDialog by remember { mutableStateOf(false) }

    val filteredList = remember(availableList, selectedHomeFilter) {
        when (selectedHomeFilter) {
            "AVAILABLE" -> availableList.filter { it.isAvailable }
            else -> availableList
        }
    }

    val isAnySelected = selectedHomeFilter != null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "welcome_header") {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                val loc by viewModel.userLocation.collectAsState()
                WelcomeHeader(
                    userName = userName,
                    lat = loc.first,
                    lng = loc.second,
                    activeSpeakingAudioId = activeSpeakingAudioId
                )
            }
        }

        item(key = "zarai_machine_register_banner") {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ZaraiMachineRegisterBanner(
                    onClick = onNavigateToRegisterMachinery,
                    activeSpeakingAudioId = activeSpeakingAudioId
                )
            }
        }

        if (filteredList.isNotEmpty()) {
            item(key = "nearby_title") {
                val titleText = when (selectedHomeFilter) {
                    "AVAILABLE" -> stringResource(id = R.string.active_available_machines)
                    else -> stringResource(id = R.string.nearby_machinery)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = titleText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Audio Assistant Button for Nearby Machinery Booking
                    val isSpeaking = activeSpeakingAudioId == "booking_info"
                    val buttonScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isSpeaking) 1.0f else 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "buttonScale"
                    )

                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpeaking) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale)
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
                                .clickable {
                                    if (isSpeaking) {
                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                    } else {
                                        val text = context.getString(R.string.machine_booking_info)
                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "booking_info")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSpeaking) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until 3) {
                                        val barHeightScale by infiniteTransition.animateFloat(
                                            initialValue = 0.2f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(400 + i * 150),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "bar_booking_$i"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(width = 2.5.dp, height = 12.dp)
                                                .graphicsLayer(scaleY = barHeightScale)
                                                .background(Color.White, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = stringResource(id = R.string.content_description_listen_audio),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "nearby_machinery_list") {
                val listState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()
                val snapFlingBehavior = rememberSnapFlingBehavior(listState)

                val showLeftIndicator by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                    }
                }

                val showRightIndicator by remember(filteredList) {
                    derivedStateOf {
                        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                        lastVisible == null || lastVisible.index < filteredList.size - 1
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        state = listState,
                        flingBehavior = snapFlingBehavior,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(filteredList, key = { it.id }) { machinery ->
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                MachineryListItem(
                                    machinery = machinery,
                                    isAuthorized = isAuthorized,
                                    viewModel = viewModel,
                                    onClick = { onNavigateToDetail(machinery.id) },
                                    onBookClick = { onNavigateToBooking(machinery.id) }
                                )
                            }
                        }
                    }

                    // Green Left Indicator
                    if (showLeftIndicator) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 24.dp, top = 192.dp)
                                .shadow(elevation = 6.dp, shape = CircleShape)
                                .size(40.dp)
                                .background(AgriGreenPrimary.copy(alpha = 0.65f), CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(listState.firstVisibleItemIndex - 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Green Right Indicator
                    if (showRightIndicator) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 24.dp, top = 192.dp)
                                .shadow(elevation = 6.dp, shape = CircleShape)
                                .size(40.dp)
                                .background(AgriGreenPrimary.copy(alpha = 0.65f), CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item(key = "view_more_button") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-16).dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clickable { onViewAllClick() }
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECF7F2)),
                        border = BorderStroke(1.5.dp, AgriGreenPrimary.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.btn_more_machines),
                                color = AgriGreenPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item(key = "stats_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-8).dp)
                        .padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AgriHomeStatCard(
                        title = stringResource(id = R.string.home_stats_map_select),
                        value = "",
                        icon = Icons.Default.Map,
                        color = AgriGreenPrimary,
                        isSelected = selectedHomeFilter == "AVAILABLE",
                        isAnySelected = isAnySelected,
                        onClick = { showMachineryMapFromHome = true },
                        onSpeakClick = {
                            if (activeSpeakingAudioId == "stat_available") {
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                            } else {
                                val text = "معزز کاشتکار، نقشے پر اپنے قریب ${availableList.size} دستیاب زرعی مشینیں دیکھنے اور منتخب کرنے کے لیے یہاں ٹیپ کریں۔"
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "stat_available")
                            }
                        },
                        isSpeaking = activeSpeakingAudioId == "stat_available",
                        modifier = Modifier.weight(1f)
                    )

                    AgriHomeStatCard(
                        title = stringResource(id = R.string.home_stats_bookings),
                        value = if (user == null) "0" else myBookingCounter.toString(),
                        icon = Icons.Default.ReceiptLong,
                        color = Color(0xFFE65100),
                        isSelected = false,
                        isAnySelected = isAnySelected,
                        onClick = { onNavigateToBookings() },
                        onSpeakClick = {
                            if (activeSpeakingAudioId == "stat_bookings") {
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                            } else {
                                val count = if (user == null) 0 else myBookingCounter
                                val text = "معزز کاشتکار، آپ کی کل بکنگز کی تعداد $count ہے۔"
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "stat_bookings")
                            }
                        },
                        isSpeaking = activeSpeakingAudioId == "stat_bookings",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item(key = "extra_actions_tiles") {
                val helperVideoCoroutineScope = rememberCoroutineScope()
                var isLoadingHelperVideos by remember { mutableStateOf(false) }
                var activeHelperVideoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
                val videoUnavailableText = stringResource(id = R.string.helper_video_unavailable)

                if (activeHelperVideoUrls.isNotEmpty()) {
                    // Plays in-app, full screen, swipeable across every helper video the backend
                    // returns — see FullScreenHelperVideoPlayer.
                    FullScreenHelperVideoPlayer(
                        videoUrls = activeHelperVideoUrls,
                        onDismiss = { activeHelperVideoUrls = emptyList() }
                    )
                }

                // محکمہ زراعت کی اسکیمیں shrunk down from a full-width banner to the same small
                // AgriHomeStatCard size/style as "نقشے سے منتخب کریں" above, now sharing a row
                // with the new "how to use the app" tutorial-video tile. isSelected/isAnySelected
                // are hardcoded false here — these two tiles aren't part of the map-filter
                // selection state the stats row above uses, so they must never dim/highlight
                // because of it.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val schemesNarrationText = stringResource(id = R.string.schemes_card_narration)
                    val helperVideoNarrationText = stringResource(id = R.string.home_helper_video_narration)

                    AgriHomeStatCard(
                        title = stringResource(id = R.string.schemes_card_title),
                        value = "",
                        icon = Icons.Default.Announcement,
                        color = Color(0xFF1976D2),
                        isSelected = false,
                        isAnySelected = false,
                        onClick = onNavigateToGovernmentSchemes,
                        onSpeakClick = {
                            if (activeSpeakingAudioId == "stat_schemes") {
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                            } else {
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(schemesNarrationText, "stat_schemes")
                            }
                        },
                        isSpeaking = activeSpeakingAudioId == "stat_schemes",
                        modifier = Modifier.weight(1f)
                    )

                    AgriHomeStatCard(
                        title = stringResource(id = R.string.home_helper_video_title),
                        value = "",
                        icon = Icons.Default.PlayCircleFilled,
                        color = Color(0xFF00838F),
                        isSelected = false,
                        isAnySelected = false,
                        onSpeakClick = {
                            if (activeSpeakingAudioId == "stat_helper_video") {
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                            } else {
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(helperVideoNarrationText, "stat_helper_video")
                            }
                        },
                        isSpeaking = activeSpeakingAudioId == "stat_helper_video",
                        onClick = {
                            if (!isLoadingHelperVideos) {
                                isLoadingHelperVideos = true
                                helperVideoCoroutineScope.launch {
                                    val urls = viewModel.getHelperVideos()
                                    isLoadingHelperVideos = false
                                    if (urls.isEmpty()) {
                                        android.widget.Toast.makeText(context, videoUnavailableText, android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Opens directly — no picker screen — starting at the
                                        // first video; swipe up/down inside the player to move
                                        // through the rest.
                                        activeHelperVideoUrls = urls
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PremiumEmptyState(
                        message = stringResource(id = R.string.no_machinery_found_title),
                        description = stringResource(id = R.string.no_machinery_found_desc),
                        icon = Icons.Default.Agriculture
                    )
                }
            }
        }
    }

    if (showMachineryMapFromHome) {
        FullScreenMachineryMap(
            machineryList = availableList,
            userLatLng = com.google.android.gms.maps.model.LatLng(homeUserLocation.first, homeUserLocation.second),
            isAuthorized = isAuthorized,
            viewModel = viewModel,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToBooking = onNavigateToBooking,
            onDismiss = { showMachineryMapFromHome = false },
            // This request originates from panning the full-screen map itself, not the home tab's
            // own list fetch, so it's tagged "map" rather than "home".
            onSearchThisArea = { lat, lng ->
                viewModel.fetchAvailableMachines(lat, lng, "map", size = 25)
            }
        )
    }

    if (showRegisterInfoDialog) {
        AgriConfirmationDialog(
            title = stringResource(id = R.string.register_dialog_title),
            onDismissRequest = { showRegisterInfoDialog = false },
            confirmButtonText = stringResource(id = R.string.btn_change_role),
            onConfirm = {
                showRegisterInfoDialog = false
                onNavigateToProfile()
            },
            dismissButtonText = stringResource(id = R.string.btn_back_dialog)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.register_dialog_desc_1),
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.register_dialog_desc_2),
                    fontSize = 14.sp,
                    color = AgriGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SimulatedFarmingMap(
    viewModel: MainViewModel,
    machineryList: List<Machinery>,
    onPinClicked: (String) -> Unit,
    onNavigateToBooking: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Backed by the same Google Maps + clustering implementation used by the Search tab's map
    // (see GoogleMachineryMap.kt) instead of the previous MapLibre engine — MapLibre's native
    // library was ~11MB per CPU architecture and this app already ships Google Maps for the
    // Search tab, so standardizing on one engine removes that entirely without adding a new
    // dependency. gesturesEnabled=true / showControls=false matches the previous MapLibre
    // behavior here: full pan/pinch-zoom, no on-screen +/- buttons.
    val userLocState by viewModel.userLocation.collectAsState()
    val userLatLng = com.google.android.gms.maps.model.LatLng(userLocState.first, userLocState.second)
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null

    Box(modifier = modifier) {
        ClusteredMachineryGoogleMap(
            machineryList = machineryList,
            userLatLng = userLatLng,
            gesturesEnabled = true,
            showControls = false,
            isAuthorized = isAuthorized,
            viewModel = viewModel,
            onNavigateToDetail = onPinClicked,
            onNavigateToBooking = onNavigateToBooking,
            modifier = Modifier.fillMaxSize()
        )

        // Floating "GPS Active" indicator — preserved as-is from the previous implementation.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF2196F3), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(id = R.string.gps_active_label), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
        }
    }
}




private fun defaultMachineryImages(): List<String> {
    return listOf("other_machinery_clean")
}

private fun defaultMachineryImageRes(): Int {
    return R.drawable.other_machinery_clean
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryDetailScreen(
    machineryId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    val availableList by viewModel.availableMachinery.collectAsState()
    val item = availableList.find { it.id == machineryId }
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null
    var showAuthFlow by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var zoomedImageIndex by remember { mutableStateOf<Int?>(null) }

    if (showAuthFlow) {
        FarmerAuthScreen(
            viewModel = viewModel,
            onDismiss = { showAuthFlow = false },
            onSuccess = {
                pendingAction?.invoke()
                pendingAction = null
                showAuthFlow = false
            },
            isDialog = true
        )
    }

    val displayImages = remember(item?.imageUrls, item?.nameUr) {
        if (item == null || item.imageUrls.isEmpty()) {
            defaultMachineryImages()
        } else {
            item.imageUrls
        }
    }

    zoomedImageIndex?.let { index ->
        FullScreenImageViewer(
            imageNames = displayImages,
            initialIndex = index,
            onDismiss = { zoomedImageIndex = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.machinery_details_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriGreenPrimary)
            }
        } else {
            val context = LocalContext.current

            // Audio narration for this machine's details — reuses the same NarrationManager
            // singleton and narration text formats already used by the map popup card
            // (GoogleMachineryMap.kt's MachineryPopupCard), so the same tap reads out the same
            // info here on the full-screen details view.
            LaunchedEffect(Unit) {
                pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
            }
            val activeDetailNarrationId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()
            val isDetailSpeaking = activeDetailNarrationId == "machinery_detail_narration"
            // Stop narration if the user navigates away while it's still speaking, instead of
            // leaving it running in the background after this screen is gone.
            DisposableEffect(Unit) {
                onDispose {
                    if (pk.kissanmadadgar.mobile.data.local.NarrationManager.isSpeaking("machinery_detail_narration")) {
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                    }
                }
            }

            // Collect images to show in pager

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    
                    // 1. Dynamic Image Slider using LazyRow
                    Box {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(displayImages) { index, imageName ->
                                val imageModifier = Modifier
                                    .width(300.dp)
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { zoomedImageIndex = index }
                                if (imageName.startsWith("http")) {
                                    coil.compose.SubcomposeAsyncImage(
                                        model = imageName,
                                        loading = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(24.dp))
                                            }
                                        },
                                        error = {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = defaultMachineryImageRes()),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        },
                                        contentDescription = null,
                                        modifier = imageModifier,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                                    val finalResId = if (resId != 0) resId else defaultMachineryImageRes()
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = finalResId),
                                        contentDescription = null,
                                        modifier = imageModifier,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        }
                        
                        // Floating District Chip
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = item.districtUr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 2. Owner Information & 3. Phone Dialer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.providerName,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AgriGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.nameUr,
                                fontSize = 18.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                // item.distanceText already comes fully formatted from the server
                                // (e.g. "2351.1 کلومیٹر دور") — same field the search list and map
                                // popup use, so this now matches instead of showing a hardcoded "1.2".
                                Text(text = item.distanceText ?: stringResource(id = R.string.distance_km_format, "1.2"), color = Color.DarkGray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val displayPhone: String = if (isAuthorized) item.providerPhone else item.providerPhone.take(4) + "-*******"
                            
                            Text(
                                text = displayPhone,
                                color = Color.DarkGray,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    if (isAuthorized) {
                                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clipData = android.content.ClipData.newPlainText("phone", item.providerPhone)
                                        clipboardManager.setPrimaryClip(clipData)
                                        android.widget.Toast.makeText(context, context.getString(R.string.msg_phone_copied), android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingAction = {
                                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clipData = android.content.ClipData.newPlainText("phone", item.providerPhone)
                                            clipboardManager.setPrimaryClip(clipData)
                                            android.widget.Toast.makeText(context, context.getString(R.string.msg_phone_copied), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        showAuthFlow = true
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Audio narration — same SupportAgent/VolumeUp gradient-circle style
                            // used for narration everywhere else in the app.
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (isDetailSpeaking) {
                                                listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                            } else {
                                                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                            }
                                        )
                                    )
                                    .clickable {
                                        if (isDetailSpeaking) {
                                            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                        } else {
                                            // Guests never see the real contact number, so it must
                                            // not be read aloud either — same guest/authorized
                                            // narration split as the map popup card.
                                            val text = if (isAuthorized) {
                                                context.getString(
                                                    R.string.map_popup_narration_format,
                                                    item.providerName,
                                                    item.nameUr,
                                                    item.distanceText ?: "",
                                                    item.providerPhone,
                                                    item.rating.toString()
                                                )
                                            } else {
                                                context.getString(
                                                    R.string.map_popup_narration_guest_format,
                                                    item.providerName,
                                                    item.nameUr,
                                                    item.distanceText ?: "",
                                                    item.rating.toString()
                                                )
                                            }
                                            pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "machinery_detail_narration")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDetailSpeaking) Icons.Default.VolumeUp else Icons.Default.SupportAgent,
                                    contentDescription = stringResource(id = R.string.content_description_listen_audio),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dial Button
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(AgriGreenPrimary)
                                    .clickable {
                                        if (isAuthorized) {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                data = android.net.Uri.parse("tel:${item.providerPhone}")
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            pendingAction = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:${item.providerPhone}")
                                                }
                                                context.startActivity(intent)
                                            }
                                            showAuthFlow = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color(0xFFF5F5F5))
                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. Replace English Text Labels with Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.rating.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.clip(CircleShape).background(AgriGreenLight).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(text = stringResource(id = R.string.label_subsidy), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.subsidyText ?: stringResource(id = R.string.subsidy_fallback),
                                color = AgriGreenPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    UrduButton(
                        text = stringResource(id = R.string.btn_book_now),
                        onClick = {
                            if (isAuthorized) {
                                onNavigateToBooking()
                            } else {
                                pendingAction = {
                                    onNavigateToBooking()
                                }
                                showAuthFlow = true
                            }
                        }
                    )
                }
            }
        }
    }
}




@Composable
fun FarmerGuestMachineriesTab(onLoginRedirect: () -> Unit, onClose: () -> Unit, supportMessage: String?) {
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
                        text = stringResource(id = R.string.guest_machineries_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.guest_machineries_desc),
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


@Composable
fun FarmerMapTab(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: ((String) -> Unit)? = null
) {
    val availableList by viewModel.availableMachinery.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SimulatedFarmingMap(
            viewModel = viewModel,
            machineryList = availableList,
            onPinClicked = onNavigateToDetail,
            onNavigateToBooking = onNavigateToBooking,
            modifier = Modifier.fillMaxSize()
        )
    }
}

class PhoneVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalLength = text.text.length
        val trimmed = if (originalLength >= 11) text.text.substring(0..10) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3) out += "-"
        }
        val numberOffsetTranslator = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, originalLength)
                return if (clamped <= 3) clamped else clamped + 1
            }
            override fun transformedToOriginal(offset: Int): Int {
                val mapped = if (offset <= 4) offset else (offset - 1)
                return mapped.coerceIn(0, originalLength)
            }
        }
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), numberOffsetTranslator)
    }
}

@Composable
fun FarmerAuthScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    isDialog: Boolean = false,
    requireCnic: Boolean = false
) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var cnicError by remember { mutableStateOf<String?>(null) }
    val cnicFocusRequester = remember { FocusRequester() }
    var isOtpStage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccessStage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val otpSentMsg by viewModel.otpSentMessage.collectAsState()
    val supportResponse by viewModel.supportResponse.collectAsState()
    var timerSeconds by remember { mutableIntStateOf(300) }

    // Audio narration for the phone-entry stage — same shared TTS singleton and same
    // SupportAgent/VolumeUp gradient-circle icon used for narration everywhere else in the app
    // (see MyBookings.kt's BookingsSearchBar, NotificationsScreen.kt).
    val authNarrationId = "auth_req_narration"
    val activeNarrationId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()
    val isNarrationSpeaking = activeNarrationId == authNarrationId
    val authNarrationText = stringResource(id = R.string.auth_req_narration)

    LaunchedEffect(Unit) {
        viewModel.fetchSupportInfo(module = "LOGIN")
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }

    LaunchedEffect(isOtpStage) {
        if (isOtpStage) {
            timerSeconds = 300
            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
        } else {
            otp = ""
            errorMessage = null
        }
    }
    
    LaunchedEffect(isOtpStage, timerSeconds) {
        if (isOtpStage && timerSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            timerSeconds--
        }
    }
    
    val phoneFocusRequester = remember { FocusRequester() }
    val otpFocusRequester = remember { FocusRequester() }
    val phoneErrorText = stringResource(id = R.string.auth_req_phone_error)
    val otpLengthErrorText = stringResource(id = R.string.auth_req_otp_length_error)
    val cnicErrorText = stringResource(id = R.string.auth_req_cnic_error)

    val isPhoneValid = isValidPakistaniMobileNumber(phone)
    val triggerOtpSend = {
        if (requireCnic && cnic.length != 13) {
            cnicError = cnicErrorText
        } else if (!isPhoneValid) {
            errorMessage = phoneErrorText
        } else {
            isLoading = true
            errorMessage = null
            cnicError = null
            viewModel.sendOtp(
                phone = phone,
                cnic = if (requireCnic) cnic else null,
                onSuccess = {
                    isLoading = false
                    isOtpStage = true
                    timerSeconds = 300
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    }

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isDialog) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Back / Close button
                    androidx.compose.material3.IconButton(
                        onClick = {
                            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(bottom = 16.dp),
                        enabled = !isLoading && !isSuccessStage
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.auth_req_close),
                            tint = Color.Black
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AgriGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccessStage) Icons.Default.CheckCircle else if (isOtpStage) Icons.Default.LockOpen else Icons.Default.Phone,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (isSuccessStage) {
                SuccessMessage(message = stringResource(id = R.string.auth_req_success))
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1200)
                    onSuccess()
                }
            } else if (!isOtpStage) {
                // Stage 1: Phone Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_req_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Same compact speak-button style as MyBookings.kt/NotificationsScreen.kt —
                    // SupportAgent while idle, VolumeUp while speaking, orange-to-red gradient circle.
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isNarrationSpeaking) {
                                        listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                    } else {
                                        listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                    }
                                ),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                if (isNarrationSpeaking) {
                                    pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                } else {
                                    pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(authNarrationText, authNarrationId)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isNarrationSpeaking) Icons.Default.VolumeUp else Icons.Default.SupportAgent,
                            contentDescription = stringResource(id = R.string.content_description_listen_audio),
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.auth_req_desc),
                    color = Color.DarkGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))



                if (requireCnic) {
                    CNICInputField(
                        cnic = cnic,
                        onCnicChange = {
                            cnic = it
                            cnicError = null
                        },
                        isError = cnicError != null,
                        focusRequester = cnicFocusRequester,
                        enabled = !isLoading,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onNext = { phoneFocusRequester.requestFocus() }
                        )
                    )
                    
                    cnicError?.let { msg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        ErrorMessage(message = msg)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                PhoneNumberInput(
                    phone = phone,
                    onPhoneChange = {
                        phone = it
                        errorMessage = null
                    },
                    focusRequester = phoneFocusRequester,
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { triggerOtpSend() }
                    )
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorMessage(message = msg)
                }

                Spacer(modifier = Modifier.height(32.dp))

                LoadingButton(
                    text = stringResource(id = R.string.auth_req_get_whatsapp_code),
                    isLoading = isLoading,
                    onClick = triggerOtpSend,
                    enabled = isPhoneValid && (!requireCnic || cnic.length == 13),
                    containerColor = AgriGreenPrimary
                )
            } else {
                // Stage 2: OTP Input
                Text(
                    text = stringResource(id = R.string.auth_req_verify_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = otpSentMsg ?: stringResource(id = R.string.auth_req_otp_desc_sent),
                    color = Color.DarkGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                val performVerify = {
                    if (otp.length == 4) {
                        isLoading = true
                        viewModel.verifyOtp(
                            phone = phone,
                            otp = otp,
                            onSuccess = {
                                if (requireCnic) {
                                    viewModel.updateCurrentUserCnic(cnic)
                                }
                                isLoading = false
                                isSuccessStage = true
                            },
                            onError = { errorMsg ->
                                isLoading = false
                                errorMessage = errorMsg
                            }
                        )
                    } else {
                        errorMessage = otpLengthErrorText
                    }
                }
                


                OTPInput(
                    otp = otp,
                    onOtpChange = { inputOtp ->
                        otp = inputOtp
                        errorMessage = null
                        // Auto-submit when length reaches exactly 4
                        if (inputOtp.length == 4 && !isLoading) {
                            performVerify()
                        }
                    },
                    isError = errorMessage != null,
                    focusRequester = otpFocusRequester,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { performVerify() }
                    )
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorMessage(message = msg)
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isOtpComplete = otp.length == 4
                LoadingButton(
                    text = stringResource(id = R.string.auth_req_verify_btn),
                    isLoading = isLoading,
                    onClick = performVerify,
                    enabled = isOtpComplete
                )

                Spacer(modifier = Modifier.height(20.dp))

                val isResendEnabled = timerSeconds == 0 && !isLoading
                TextButton(
                    onClick = {
                        if (timerSeconds == 0) {
                            triggerOtpSend()
                        }
                    },
                    enabled = isResendEnabled
                ) {
                    val baseText = stringResource(id = R.string.auth_req_resend_code)
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    val timerText = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
                    val displayText = if (timerSeconds > 0) {
                        "$baseText ($timerText)"
                    } else {
                        baseText
                    }
                    Text(
                        text = displayText,
                        color = if (isResendEnabled) AgriGreenPrimary else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            val supportMessage = supportResponse?.message
            if (!supportMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = supportMessage,
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (isDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                KeyboardAwareContainer {
                    content()
                }
            }
        }
    } else {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                AgriDetailHeader(
                    title = if (isOtpStage) stringResource(id = R.string.auth_req_verify_title) else stringResource(id = R.string.auth_req_title),
                    onBackClick = {
                        if (isOtpStage) {
                            isOtpStage = false
                        } else {
                            onDismiss()
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                KeyboardAwareContainer {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    imageNames: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { imageNames.size })
        val scales = remember { mutableStateMapOf<Int, Float>() }
        val offsets = remember { mutableStateMapOf<Int, Offset>() }

        val currentScale = scales[pagerState.currentPage] ?: 1f

        LaunchedEffect(pagerState.currentPage) {
            // Reset other pages to default 1x scale and zero offset when page changes
            scales.keys.forEach { page ->
                if (page != pagerState.currentPage) {
                    scales[page] = 1f
                    offsets[page] = Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = currentScale == 1f
            ) { page ->
                val imageName = imageNames[page]
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                val finalResId = if (resId != 0) resId else R.drawable.other_machinery_clean

                val scale = scales[page] ?: 1f
                val offset = offsets[page] ?: Offset.Zero

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val currentConstraints = constraints
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentConstraints, scale) {
                                if (scale > 1f) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                                        scales[page] = newScale
                                        if (newScale > 1f) {
                                            val maxOffsetX = (newScale - 1f) * currentConstraints.maxWidth / 2f
                                            val maxOffsetY = (newScale - 1f) * currentConstraints.maxHeight / 2f
                                            val newX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                            val newY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                            offsets[page] = Offset(newX, newY)
                                        } else {
                                            offsets[page] = Offset.Zero
                                        }
                                    }
                                } else {
                                    detectPinchZoom { zoom ->
                                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                                        scales[page] = newScale
                                    }
                                }
                            }
                            .pointerInput(currentConstraints, scale) {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        if (scale > 1f) {
                                            scales[page] = 1f
                                            offsets[page] = Offset.Zero
                                        } else {
                                            scales[page] = 3f
                                            val maxOffsetX = (3f - 1f) * currentConstraints.maxWidth / 2f
                                            val maxOffsetY = (3f - 1f) * currentConstraints.maxHeight / 2f
                                            val centerX = currentConstraints.maxWidth / 2f
                                            val centerY = currentConstraints.maxHeight / 2f
                                            val targetX = (centerX - tapOffset.x) * (3f - 1f)
                                            val targetY = (centerY - tapOffset.y) * (3f - 1f)
                                            offsets[page] = Offset(
                                                targetX.coerceIn(-maxOffsetX, maxOffsetX),
                                                targetY.coerceIn(-maxOffsetY, maxOffsetY)
                                            )
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageName.startsWith("http")) {
                            coil.compose.SubcomposeAsyncImage(
                                model = imageName,
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(24.dp))
                                    }
                                },
                                error = {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.other_machinery_clean),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = finalResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                }
            }

            // Slide Index Indicator at the bottom
            if (imageNames.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${imageNames.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

suspend fun PointerInputScope.detectPinchZoom(onZoom: (Float) -> Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled && event.changes.size >= 2) {
                val zoomChange = event.calculateZoom()
                if (zoomChange != 1f) {
                    onZoom(zoomChange)
                    event.changes.forEach { it.consume() }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
    }
}



