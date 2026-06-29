@file:Suppress("DEPRECATION")
package pk.kissanmadadgar.mobile.presentation.screens

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
import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
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
import androidx.compose.ui.focus.FocusRequester
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerHomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onLoginRedirect: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToRegisterMachinery: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val user by viewModel.currentUser.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val userCnic by viewModel.userCnic.collectAsState()
    val userDistrict by viewModel.userDistrict.collectAsState()
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val districtsList by viewModel.districtsList.collectAsState()
    val profileResponse by viewModel.profileResponse.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDistricts()
    }

    LaunchedEffect(selectedTab, user) {
        if (selectedTab == 4 && user != null) {
            viewModel.fetchUserProfile()
        }
    }

    Scaffold(
        topBar = {
            AgriAppHeader(
                title = stringResource(id = R.string.app_name),
                onProfileClick = { selectedTab = 4 },
                onBellClick = { showNotificationsDialog = true }
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
                            onClick = { selectedTab = 0 },
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
                            onClick = { selectedTab = 1 },
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
                            onClick = { selectedTab = 2 },
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
                            onClick = { selectedTab = 3 },
                            icon = { Icon(imageVector = Icons.Default.Agriculture, contentDescription = null) },
                            label = { Text(text = "مشینیں", maxLines = 1, softWrap = false) },
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
                            onClick = { selectedTab = 4 },
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
                    onNavigateToBookings = { selectedTab = 2 },
                    onViewAllClick = { selectedTab = 1 },
                    onNavigateToProfile = { selectedTab = 4 },
                    onNavigateToRegisterMachinery = onNavigateToRegisterMachinery
                )
                1 -> FarmerSearchTab(viewModel, onNavigateToDetail, onNavigateToBooking)
                2 -> {
                    if (user == null) {
                        FarmerGuestBookingsTab({ onLoginRedirect(false) }, onClose = { selectedTab = 0 })
                    } else {
                        FarmerBookingsTab(viewModel)
                    }
                }
                3 -> {
                    if (user == null) {
                        FarmerGuestMachineriesTab({ onLoginRedirect(true) }, onClose = { selectedTab = 0 })
                    } else {
                        ProviderInventoryTab(viewModel, onNavigateToRegisterMachinery)
                    }
                }
                4 -> {
                    if (user == null) {
                        FarmerGuestProfileTab({ onLoginRedirect(false) })
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
                            onNameChanged = { newName ->
                                viewModel.updateCurrentUserName(newName)
                            },
                            onPhoneChanged = { newPhone ->
                                viewModel.updateCurrentUserPhone(newPhone)
                            },
                            onAddressChanged = { newAddress ->
                                viewModel.updateCurrentUserAddress(newAddress)
                            },
                            onCnicChanged = { newCnic ->
                                viewModel.updateCurrentUserCnic(newCnic)
                            },
                            onDistrictChanged = { newDistrict ->
                                viewModel.updateCurrentUserDistrict(newDistrict)
                            },
                            onLogout = onLogout,
                            onClose = { selectedTab = 0 }
                        )
                    }
                }
            }
            if (showNotificationsDialog) {
                AgriNotificationsDialog(onDismissRequest = { showNotificationsDialog = false })
            }
        }
    }
}

@Composable
fun WelcomeHeader(userName: String, lat: Double? = null, lng: Double? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var locationText by remember { mutableStateOf("") }

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
                            locationText = "، $shortAddress"
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
            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(95.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp, y = 16.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                val cleanName = userName.substringBefore("(").trim()
                Text(
                    text = "خوش آمدید، $cleanName$locationText",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اپنے کھیت کے لیے بہترین جدید ترین مشینیں کرایہ پر حاصل کریں",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
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
            .height(105.dp)
            .clickable { onClick() }
            .alpha(alphaFactor),
        shape = RoundedCornerShape(20.dp),
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
                .padding(12.dp)
        ) {
            // Watermarked background icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = watermarkColor,
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = valueColor
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    textAlign = TextAlign.Start
                )
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
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.super_seeder_custom),
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
                    Text(text = stringResource(id = R.string.distance_km_format, "1.2"), color = Color.Gray, fontSize = 13.sp)
                    Text(text = stringResource(id = R.string.hourly_rate_format, machinery.hourlyRate.toInt()) + stringResource(id = R.string.per_hour_suffix), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

@Composable
fun SchemeItem(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7F6), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = desc, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
    }
}

@Composable
fun FarmerMainTab(
    viewModel: MainViewModel,
    userName: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onNavigateToBookings: () -> Unit,
    onViewAllClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRegisterMachinery: () -> Unit
) {
    val availableList by viewModel.availableMachinery.collectAsState()
    val bookings by viewModel.farmerBookings.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null

    var selectedHomeFilter by remember { mutableStateOf<String?>(null) }
    
    var showRegisterInfoDialog by remember { mutableStateOf(false) }
    var showSchemesDialog by remember { mutableStateOf(false) }

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
                WelcomeHeader(userName = userName, lat = loc.first, lng = loc.second)
            }
        }

        item(key = "stats_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AgriHomeStatCard(
                    title = stringResource(id = R.string.home_stats_available),
                    value = availableList.size.toString(),
                    icon = Icons.Default.Agriculture,
                    color = AgriGreenPrimary,
                    isSelected = selectedHomeFilter == "AVAILABLE",
                    isAnySelected = isAnySelected,
                    onClick = {
                        selectedHomeFilter = if (selectedHomeFilter == "AVAILABLE") null else "AVAILABLE"
                    },
                    modifier = Modifier.weight(1f)
                )

                AgriHomeStatCard(
                    title = stringResource(id = R.string.home_stats_bookings),
                    value = if (user == null) "0" else bookings.size.toString(),
                    icon = Icons.Default.ReceiptLong,
                    color = Color(0xFFE65100),
                    isSelected = false,
                    isAnySelected = isAnySelected,
                    onClick = { onNavigateToBookings() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filteredList.isNotEmpty()) {
            item(key = "nearby_title") {
                val titleText = when (selectedHomeFilter) {
                    "AVAILABLE" -> "دستیاب اور فعال مشینیں"
                    else -> stringResource(id = R.string.nearby_machinery)
                }
                Text(
                    text = titleText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenPrimary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            item(key = "nearby_machinery_list") {
                val listState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

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
                        .offset(y = (-12).dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
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

            item(key = "extra_actions_tiles") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTileCard(
                        title = "زرعی مشین رجسٹر کریں؟",
                        subtitle = "اپنی مشین کرائے پر دیں",
                        icon = Icons.Default.Assignment,
                        color = AgriGreenPrimary,
                        onClick = onNavigateToRegisterMachinery,
                        modifier = Modifier.weight(1f)
                    )

                    ActionTileCard(
                        title = "محکمہ زراعت کی اسکیمیں",
                        subtitle = "حکومتی سبسڈی اور پروگرام",
                        icon = Icons.Default.Announcement,
                        color = Color(0xFF1976D2),
                        onClick = { showSchemesDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PremiumEmptyState(
                        message = "کوئی مشینری نہیں ملی",
                        description = "آپ کے منتخب کردہ معیار کے مطابق کوئی مشینری دستیاب نہیں ہے۔",
                        icon = Icons.Default.Agriculture
                    )
                }
            }
        }
    }

    if (showRegisterInfoDialog) {
        AgriConfirmationDialog(
            title = "زرعی مشین رجسٹر کریں؟",
            onDismissRequest = { showRegisterInfoDialog = false },
            confirmButtonText = "کردار تبدیل کریں",
            onConfirm = {
                showRegisterInfoDialog = false
                onNavigateToProfile()
            },
            dismissButtonText = "واپس"
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اپنی زرعی مشینری (جیسے سپر سیڈر، ٹریکٹر) کسان مددگار ایپ پر کرائے کے لیے فراہم کر کے اپنی آمدنی بڑھائیں۔",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "مشینری رجسٹر کرنے کے لیے، آپ کو اپنا کردار تبدیل کر کے 'سروس فراہم کنندہ' (Service Provider) کے طور پر لاگ ان کرنا ہوگا۔",
                    fontSize = 14.sp,
                    color = AgriGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }
        }
    }

    if (showSchemesDialog) {
        AgriConfirmationDialog(
            title = "محکمہ زراعت کی اسکیمیں",
            onDismissRequest = { showSchemesDialog = false },
            confirmButtonText = "ٹھیک ہے",
            onConfirm = { showSchemesDialog = false },
            dismissButtonText = "واپس"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "حکومتِ پنجاب کے محکمہ زراعت کی فعال اسکیمیں درج ذیل ہیں:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                SchemeItem(
                    title = "1. پنجاب کلین ائیر پروگرام (PCAP)",
                    desc = "سپر سیڈر کے ذریعے گندم کی بوائی پر کاشتکاروں کو 5,000 روپے فی ایکڑ سبسڈی فراہم کی جا رہی ہے۔"
                )

                SchemeItem(
                    title = "2. وزیراعلیٰ پنجاب گرین ٹریکٹر سکیم",
                    desc = "کاشتکاروں کو جدید ٹریکٹرز کی خریداری پر 10 لاکھ روپے تک کی نقد سبسڈی دی جائے گی۔"
                )

                SchemeItem(
                    title = "3. لیزر لینڈ لیولر پروگرام",
                    desc = "پانی کی بچت اور پیداوار بڑھانے کے لیے لیزر لیولر کی خریداری پر 50 فیصد تک سبسڈی۔"
                )
            }
        }
    }
}

fun createMarkerIcon(context: android.content.Context, color: Int, isUser: Boolean): org.maplibre.android.annotations.Icon {
    val size = if (isUser) 80 else 72
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val shadowPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        setColor(color)
        alpha = 40
        style = android.graphics.Paint.Style.FILL
    }
    
    val solidPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        setColor(color)
        style = android.graphics.Paint.Style.FILL
    }
    
    val strokePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        setColor(android.graphics.Color.WHITE)
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    val center = size / 2f
    canvas.drawCircle(center, center, size / 2f - 2f, shadowPaint)
    
    val innerRadius = size / 2.5f
    canvas.drawCircle(center, center, innerRadius, solidPaint)
    canvas.drawCircle(center, center, innerRadius, strokePaint)
    
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = if (isUser) 40f else 36f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    
    val emoji = if (isUser) "👤" else "🚜"
    val fontMetrics = textPaint.fontMetrics
    val textOffsetY = center - (fontMetrics.ascent + fontMetrics.descent) / 2
    
    canvas.drawText(emoji, center, textOffsetY, textPaint)

    return org.maplibre.android.annotations.IconFactory.getInstance(context).fromBitmap(bitmap)
}

@Composable
fun SimulatedFarmingMap(
    viewModel: MainViewModel,
    machineryList: List<Machinery>,
    onPinClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPin by remember { mutableStateOf<Machinery?>(null) }
    val userLocState by viewModel.userLocation.collectAsState()

    val context = LocalContext.current
    
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }
    
    // Memoize map style JSON to avoid re-creating the string on every recomposition
    val osmStyleJson = remember {
        """
        {
          "version": 8,
          "sources": {
            "osm-raster-tiles": {
              "type": "raster",
              "tiles": [
                "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
              ],
              "tileSize": 256,
              "attribution": "© OpenStreetMap contributors"
            }
          },
          "layers": [
            {
              "id": "osm-raster-layer",
              "type": "raster",
              "source": "osm-raster-tiles"
            }
          ]
        }
        """.trimIndent()
    }
    
    // Track if map has been initialized to avoid redundant setStyle calls
    var mapInitialized by remember { mutableStateOf(false) }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    val userIcon = remember(context) { createMarkerIcon(context, 0xFF2196F3.toInt(), true) }
    val machineryIcon = remember(context) { createMarkerIcon(context, 0xFF2E7D32.toInt(), false) }

    val userLocationTitle = stringResource(id = R.string.user_location_title)

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                view.getMapAsync { map ->
                    if (!mapInitialized) {
                        // Only set style once
                        map.setStyle(org.maplibre.android.maps.Style.Builder().fromJson(osmStyleJson)) { _ ->
                            mapInitialized = true
                        }
                        
                        map.addOnMapClickListener { _ ->
                            if (machineryList.isNotEmpty()) {
                                selectedPin = machineryList.first()
                            }
                            true
                        }
                    }
                    
                    // Update markers without re-setting the style
                    val userPos = LatLng(userLocState.first, userLocState.second)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userPos, 13.0))
                    
                    map.clear()
                    
                    // User position (Blue Marker)
                    map.addMarker(
                        MarkerOptions()
                            .position(userPos)
                            .title(userLocationTitle)
                            .icon(userIcon)
                    )
                    
                    // Machinery positions (Green Marker)
                    machineryList.forEach { machinery ->
                        val machineryPos = LatLng(userLocState.first + 0.003, userLocState.second + 0.003)
                        val snippetText = context.getString(R.string.hourly_rate_format, machinery.hourlyRate.toInt())
                        map.addMarker(
                            MarkerOptions()
                                .position(machineryPos)
                                .title(machinery.nameUr)
                                .snippet(snippetText)
                                .icon(machineryIcon)
                        )
                    }
                }
            }
        )

        // Floating "GPS Active" indicator
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

        // Clicked popup details
        selectedPin?.let { pin ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth(0.95f)
                    .clickable { onPinClicked(pin.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = pin.nameUr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                        val ratingText = stringResource(id = R.string.distance_rating_format, "2.4", pin.rating.toString())
                        Text(text = ratingText, fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun MachineryListItem(
    machinery: Machinery,
    isAuthorized: Boolean,
    viewModel: MainViewModel,
    onClick: () -> Unit,
    onBookClick: () -> Unit
) {
    val context = LocalContext.current
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

    val displayImages = remember(machinery.imageUrls) {
        if (machinery.imageUrls.isEmpty()) {
            listOf("seeder_main_1", "seeder_main_2", "seeder_main_3", "seeder_main_4")
        } else {
            machinery.imageUrls
        }
    }

    zoomedImageIndex?.let { index ->
        FullScreenImageViewer(
            imageNames = displayImages,
            initialIndex = index,
            onDismiss = { zoomedImageIndex = null }
        )
    }
    
    val displayPhone = if (isAuthorized) machinery.providerPhone else machinery.providerPhone.take(4) + "-*******"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dynamic Image Slider
            Box {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(displayImages) { index, imageName ->
                        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        val finalResId = if (resId != 0) resId else R.drawable.super_seeder_custom
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = finalResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(180.dp)
                                .clickable { zoomedImageIndex = index },
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
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
                        Text(text = machinery.districtUr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Badges Row (PCAP) - Center aligned with logo and Urdu text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFECF7F2))
                            .border(BorderStroke(1.dp, Color(0xFF0B5D34).copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.logo_pcap),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پنجاب کلین ائیر پروگرام (PCAP)",
                                color = Color(0xFF0B5D34),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Restructured Row: Left/Start contains Title & Details Column, Right/End contains Subsidy Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Owner Name and Details
                        Text(
                            text = machinery.providerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgriGreenPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Header (Tractor Icon + Machine Title)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ic_super_seeder),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = machinery.nameUr,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ic_location_round),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.distance_km_format, "1.2"), color = Color.DarkGray, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (isAuthorized) {
                                    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("phone", machinery.providerPhone)
                                    clipboardManager.setPrimaryClip(clipData)
                                    android.widget.Toast.makeText(context, "فون نمبر کاپی ہو گیا", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    pendingAction = {
                                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clipData = android.content.ClipData.newPlainText("phone", machinery.providerPhone)
                                        clipboardManager.setPrimaryClip(clipData)
                                        android.widget.Toast.makeText(context, "فون نمبر کاپی ہو گیا", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    showAuthFlow = true
                                }
                            }
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ic_phone_round),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayPhone,
                                color = Color.DarkGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Professional Subsidy Badge (Vertically centered relative to the details column)
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
                            .background(Color(0xFFECF7F2))
                            .border(BorderStroke(1.dp, Color(0xFF0B5D34).copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF0B5D34),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "سبسڈی سکیم",
                                color = Color(0xFF0B5D34),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "5,000 Rs. فی ایکڑ",
                                color = Color(0xFF0B5D34),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))

                // Icon-based stats (No English Syllables)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = machinery.rating.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.DarkGray)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Book Button
                        Button(
                            onClick = {
                                if (isAuthorized) {
                                    onBookClick()
                                } else {
                                    pendingAction = {
                                        onBookClick()
                                    }
                                    showAuthFlow = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(
                                text = "بک کریں",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Phone Icon Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AgriGreenPrimary)
                                .clickable {
                                    if (isAuthorized) {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${machinery.providerPhone}")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        pendingAction = {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                data = android.net.Uri.parse("tel:${machinery.providerPhone}")
                                            }
                                            context.startActivity(intent)
                                        }
                                        showAuthFlow = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerSearchTab(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val availableList by viewModel.availableMachinery.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null

    val filteredList = remember(query, availableList) {
        if (query.trim().isEmpty()) {
            availableList
        } else {
            availableList.filter { it.nameUr.contains(query, ignoreCase = true) || it.descriptionUr.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        UrduTextField(
            value = query,
            onValueChange = { query = it },
            label = stringResource(id = R.string.search_machinery_hint)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                MachineryListItem(
                    machinery = item,
                    isAuthorized = isAuthorized,
                    viewModel = viewModel,
                    onClick = { onNavigateToDetail(item.id) },
                    onBookClick = { onNavigateToBooking(item.id) }
                )
            }
        }
    }
}

@Composable
fun FarmerBookingsTab(viewModel: MainViewModel) {
    val bookings by viewModel.farmerBookings.collectAsState()

    var selectedFilter by remember { mutableStateOf<String?>(null) }

    val filteredRequests = remember(bookings, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> bookings.filter { it.status == BookingStatus.PENDING }
            "ONGOING" -> bookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ACTIVE }
            "COMPLETED" -> bookings.filter { it.status == BookingStatus.COMPLETED }
            else -> bookings
        }
    }

    val visibleRequests = filteredRequests.sortedByDescending { it.createdAt }
    val pendingCount = bookings.count { it.status == BookingStatus.PENDING }
    val ongoingCount = bookings.count { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.ACTIVE }
    val finishedCount = bookings.count { it.status == BookingStatus.COMPLETED }

    var selectedBookingForDetail by remember { mutableStateOf<Booking?>(null) }
    val currentDetailBooking = selectedBookingForDetail?.let { detail ->
        bookings.find { it.id == detail.id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RequestListHeader(
                    title = "آپ کی بکنگز",
                    subtitle = "اپنی مشینری بکنگز کی موجودہ حالت اور تفصیلات دیکھیں",
                    icon = Icons.Default.ReceiptLong
                )
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
                    PremiumEmptyState(
                        message = stringResource(id = R.string.no_bookings_available),
                        description = "نئی بکنگز اور درخواستیں یہاں نظر آئیں گی۔",
                        icon = Icons.Default.CalendarMonth
                    )
                }
            } else {
                itemsIndexed(visibleRequests, key = { _, item -> item.id }) { index, booking ->
                    FarmerRequestCard(
                        booking = booking,
                        requestNumber = index + 1,
                        onClick = { selectedBookingForDetail = booking }
                    )
                }
            }
        }

        if (currentDetailBooking != null) {
            BookingDetailOverlay(
                booking = currentDetailBooking,
                currentRole = UserRole.FARMER,
                onBack = { selectedBookingForDetail = null },
                onAcceptRequest = { viewModel.acceptBookingRequest(currentDetailBooking.id) },
                onRejectRequest = { reason -> viewModel.rejectBookingRequest(currentDetailBooking.id, reason) },
                onUploadStep = { step -> viewModel.uploadLifecyclePhoto(currentDetailBooking.id, step) }
            )
        }
    }
}

@Composable
fun FarmerProfileTab(
    name: String,
    phone: String,
    address: String,
    cnic: String,
    district: String,
    districtsList: List<String>,
    isNameEditable: Boolean,
    isPhoneEditable: Boolean,
    isAddressEditable: Boolean,
    isCnicEditable: Boolean,
    isDistrictEditable: Boolean,
    onNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onCnicChanged: (String) -> Unit,
    onDistrictChanged: (String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    var editableName by remember(name) { mutableStateOf(name) }
    var editablePhone by remember(phone) { mutableStateOf(phone) }
    var editableAddress by remember(address) { mutableStateOf(address) }
    var editableCnic by remember(cnic) { mutableStateOf(cnic) }
    var editableDistrict by remember(district) { mutableStateOf(district) }
    val maxCharLimit = 30
    val maxPhoneLimit = 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }


        
        OutlinedTextField(
            value = editableName,
            onValueChange = { input ->
                if (input.length <= maxCharLimit) {
                    editableName = input
                    onNameChanged(input)
                }
            },
            label = { Text("کسان کا نام") },
            placeholder = { Text("اپنا نام درج کریں") },
            singleLine = true,
            enabled = isNameEditable,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = AgriGreenPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = AgriGreenPrimary
            ),
            supportingText = {
                Text(
                    text = "${editableName.length}/$maxCharLimit",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = editablePhone,
            onValueChange = { input ->
                if (input.length <= maxPhoneLimit) {
                    editablePhone = input
                    onPhoneChanged(input)
                }
            },
            label = { Text("موبائل نمبر") },
            placeholder = { Text("اپنا موبائل نمبر درج کریں") },
            singleLine = true,
            enabled = isPhoneEditable,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = AgriGreenPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = AgriGreenPrimary
            ),
            supportingText = {
                Text(
                    text = "${editablePhone.length}/$maxPhoneLimit",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        CNICInputField(
            cnic = editableCnic,
            onCnicChange = { input ->
                editableCnic = input
                onCnicChanged(input)
            },
            enabled = isCnicEditable,
            modifier = Modifier.fillMaxWidth(0.95f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = editableAddress,
            onValueChange = { input ->
                editableAddress = input
                onAddressChanged(input)
            },
            label = { Text("فارمنگ ایڈریس / پتہ (اختیاری)") },
            placeholder = { Text("اپنا پتہ درج کریں") },
            singleLine = true,
            enabled = isAddressEditable,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = AgriGreenPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = AgriGreenPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        var dropdownExpanded by remember { mutableStateOf(false) }

        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded && isDistrictEditable,
            onExpandedChange = { if (isDistrictEditable) dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = editableDistrict.ifEmpty { "ضلع منتخب کریں" },
                onValueChange = {},
                readOnly = true,
                enabled = isDistrictEditable,
                label = { Text("ضلع") },
                trailingIcon = { if (isDistrictEditable) ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth(0.95f).menuAnchor(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = AgriGreenPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = AgriGreenPrimary
                )
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                districtsList.forEach { distName ->
                    DropdownMenuItem(
                        text = { Text(text = distName, fontSize = 16.sp) },
                        onClick = {
                            editableDistrict = distName
                            onDistrictChanged(distName)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        UrduButton(
            text = stringResource(id = R.string.btn_logout),
            onClick = onLogout,
            containerColor = Color.Red.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth(0.95f).height(50.dp),
            fontSize = 15.sp
        )
    }
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

    val displayImages = remember(item?.imageUrls) {
        if (item == null || item.imageUrls.isEmpty()) {
            listOf("seeder_main_1", "seeder_main_2", "seeder_main_3", "seeder_main_4")
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
                                val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                                val finalResId = if (resId != 0) resId else R.drawable.super_seeder_custom
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = finalResId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(300.dp)
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { zoomedImageIndex = index },
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
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
                                Text(text = stringResource(id = R.string.distance_km_format, "1.2"), color = Color.DarkGray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
                                        android.widget.Toast.makeText(context, "فون نمبر کاپی ہو گیا", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingAction = {
                                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clipData = android.content.ClipData.newPlainText("phone", item.providerPhone)
                                            clipboardManager.setPrimaryClip(clipData)
                                            android.widget.Toast.makeText(context, "فون نمبر کاپی ہو گیا", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        showAuthFlow = true
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
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
                                Text("سبسڈی", color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5000 فی ایکڑ",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    machineryId: String,
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var machinery by remember { mutableStateOf<Machinery?>(null) }
    var currentStep by remember { mutableStateOf(1) }
    
    // Form States
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var acres by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf(4) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val user by viewModel.currentUser.collectAsState()
    var farmerName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val focusRequesterName = remember { FocusRequester() }
    val focusRequesterAcres = remember { FocusRequester() }
    var showErrors by remember { mutableStateOf(false) }

    LaunchedEffect(machineryId) {
        machinery = viewModel.availableMachinery.value.find { it.id == machineryId }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(2500)
            onSuccess()
        }
    }

    if (showSuccess) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle, 
                    contentDescription = "کامیابی", 
                    tint = AgriGreenPrimary, 
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "بکنگ کی درخواست موصول ہو گئی",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "سپلائر جلد ہی آپ سے رابطہ کرے گا",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            AgriDetailHeader(
                title = "بکنگ کی تصدیق",
                onBackClick = { if (currentStep > 1) { currentStep = 1; showErrors = false } else onBack() }
            )
        }
    ) { padding ->
        if (machinery == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriGreenPrimary)
            }
        } else {
            val item = machinery!!
            
            KeyboardAwareContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Step Progress Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(step = 1, isActive = currentStep == 1, isCompleted = currentStep > 1, title = stringResource(id = R.string.booking_step_1_title))
                        Spacer(modifier = Modifier.width(16.dp).height(2.dp).background(if (currentStep > 1) AgriGreenPrimary else Color.LightGray))
                        StepIndicator(step = 2, isActive = currentStep == 2, isCompleted = currentStep > 2, title = stringResource(id = R.string.booking_step_2_title))
                    }

                    BookingCard(title = "مشین کی معلومات") {
                        val context = LocalContext.current
                        val isAuthorized = user != null
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Badges Row (PCAP) - Center aligned with logo and Urdu text
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFECF7F2))
                                        .border(BorderStroke(1.dp, Color(0xFF0B5D34).copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.logo_pcap),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "پنجاب کلین ائیر پروگرام (PCAP)",
                                            color = Color(0xFF0B5D34),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Restructured Row: Left/Start contains Title & Details Column, Right/End contains Subsidy Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Owner Name and Details
                                    Text(
                                        text = item.providerName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AgriGreenPrimary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Header (Tractor Icon + Machine Title)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.ic_super_seeder),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.nameUr,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.ic_location_round),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = stringResource(id = R.string.distance_km_format, "1.2"), color = Color.DarkGray, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clipData = android.content.ClipData.newPlainText("phone", item.providerPhone)
                                            clipboardManager.setPrimaryClip(clipData)
                                            android.widget.Toast.makeText(context, "فون نمبر کاپی ہو گیا", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.ic_phone_round),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val displayPhone = if (isAuthorized) item.providerPhone else item.providerPhone.take(4) + "-*******"
                                        Text(
                                            text = displayPhone,
                                            color = Color.DarkGray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Professional Subsidy Badge (Vertically centered relative to the details column)
                                Box(
                                    modifier = Modifier
                                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
                                        .background(Color(0xFFECF7F2))
                                        .border(BorderStroke(1.dp, Color(0xFF0B5D34).copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = Color(0xFF0B5D34),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "سبسڈی سکیم",
                                            color = Color(0xFF0B5D34),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "5,000 Rs. فی ایکڑ",
                                            color = Color(0xFF0B5D34),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (currentStep == 1) {
                        val dateError = if (selectedDateMillis == null) {
                            if (showErrors) "براہ کرم بکنگ کی تاریخ منتخب کریں" else null
                        } else null

                        BookingCard(title = "بکنگ کا وقت منتخب کریں") {
                            DatePickerField(
                                selectedDateMillis = selectedDateMillis,
                                onDateSelected = { selectedDateMillis = it },
                                isError = dateError != null
                            )
                            dateError?.let { err ->
                                Spacer(modifier = Modifier.height(4.dp))
                                ErrorMessage(message = err)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "مطلوبہ گھنٹے", fontSize = 16.sp, color = Color.DarkGray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (hours > 1) hours-- }) {
                                        Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = null, tint = AgriGreenPrimary)
                                    }
                                    Text(
                                        text = hours.toString(), 
                                        fontSize = 20.sp, 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    IconButton(onClick = { hours++ }) {
                                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = AgriGreenPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            text = stringResource(id = R.string.btn_next),
                            onClick = {
                                if (selectedDateMillis != null) {
                                    currentStep = 2
                                    showErrors = false
                                } else {
                                    showErrors = true
                                }
                            },
                            enabled = true,
                            containerColor = if (selectedDateMillis != null) AgriGreenPrimary else Color(0xFF89C2A5)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                    } else {
                        BookingCard(title = "ایکڑ اور رابطے کی تفصیلات") {
                            val acresDouble = acres.toDoubleOrNull()
                            val acresError = if (acres.isEmpty()) {
                                if (showErrors) "براہ کرم ایکڑ درج کریں" else null
                            } else if (acresDouble == null || acresDouble < 0.1 || acresDouble > 100.0) {
                                "براہ کرم 0.1 سے 100 کے درمیان درست ایکڑ درج کریں"
                            } else null

                            NumberInputField(
                                value = acres,
                                onValueChange = { acres = it },
                                label = "ایکڑ (لازمی)",
                                helperText = "کم از کم 0.1 اور زیادہ سے زیادہ 100 ایکڑ۔",
                                textFieldModifier = Modifier.focusRequester(focusRequesterAcres),
                                isError = acresError != null,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                            acresError?.let { err ->
                                Spacer(modifier = Modifier.height(4.dp))
                                ErrorMessage(message = err)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val maxCharLimit = 30
                            val nameError = if (farmerName.trim().isEmpty()) {
                                if (showErrors) "براہ کرم اپنا نام درج کریں" else null
                            } else if (farmerName.trim() == "کسان دوست" || farmerName.trim().lowercase() == "kissan dost") {
                                "براہ کرم اپنا اصل نام درج کریں (کسان دوست کے علاوہ)"
                            } else null

                            OutlinedTextField(
                                value = farmerName,
                                onValueChange = { 
                                    if (it.length <= maxCharLimit) {
                                        farmerName = it
                                    }
                                },
                                label = { Text("نام") },
                                placeholder = { Text("اپنا نام درج کریں", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterName),
                                isError = nameError != null,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedBorderColor = if (nameError != null) Color.Red else AgriGreenPrimary,
                                    unfocusedBorderColor = if (nameError != null) Color.Red else Color.Gray,
                                    focusedLabelColor = if (nameError != null) Color.Red else AgriGreenPrimary,
                                    errorBorderColor = Color.Red,
                                    errorLabelColor = Color.Red
                                ),
                                supportingText = {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        if (nameError != null) {
                                            Text(text = nameError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        Text(
                                            text = "${farmerName.length}/$maxCharLimit",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = user?.phoneNumber ?: "",
                                onValueChange = {},
                                label = { Text("موبائل نمبر") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = Color.DarkGray
                                )
                            )
                        }

                        val displayDate = selectedDateMillis?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "نامعلوم"
                        
                        val acresValue = acres.toDoubleOrNull() ?: 0.0
                        val total = acresValue * 5000.0
                        
                        val isAcresValid = acres.toDoubleOrNull() != null && acres.toDoubleOrNull()!! in 0.1..100.0
                        val isNameValid = farmerName.trim().isNotEmpty() && farmerName.trim() != "کسان دوست" && farmerName.trim().lowercase() != "kissan dost"
                        val isFormValid = selectedDateMillis != null && isAcresValid && isNameValid

                        SummaryCard(
                            items = listOf(
                                "تاریخ" to displayDate,
                                "مطلوبہ گھنٹے" to hours.toString(),
                                "ایکڑ" to if (acresValue > 0.0) acres else "-",
                                "کل رقم" to "${total.toInt()} Rs. (سبسڈی)"
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LoadingButton(
                                text = "بکنگ کی درخواست بھیجیں",
                                isLoading = isSubmitting,
                                enabled = !isSubmitting,
                                containerColor = if (isFormValid) AgriGreenPrimary else Color(0xFF89C2A5),
                                onClick = {
                                    if (isFormValid) {
                                        isSubmitting = true
                                        if (farmerName.trim().isNotEmpty()) {
                                            viewModel.updateCurrentUserName(farmerName)
                                        }
                                        viewModel.createBooking(item.id, selectedDateMillis!!, hours, item.hourlyRate, acresValue) {
                                            isSubmitting = false
                                            showSuccess = true
                                        }
                                    } else {
                                        showErrors = true
                                        if (!isNameValid) {
                                            focusRequesterName.requestFocus()
                                        } else if (!isAcresValid) {
                                            focusRequesterAcres.requestFocus()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            SecondaryButton(
                                text = "واپس",
                                onClick = { 
                                    currentStep = 1
                                    showErrors = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerGuestBookingsTab(onLoginRedirect: () -> Unit, onClose: () -> Unit) {
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
                }
            }
        }
    }
}

@Composable
fun FarmerGuestMachineriesTab(onLoginRedirect: () -> Unit, onClose: () -> Unit) {
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
                        text = "اپنی مشینیں دیکھنے کے لیے\nلاگ ان کریں",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اپنی مشینوں کا ریکارڈ رکھنے اور دوسرے کسانوں کو کرائے پر دینے کے لیے لاگ ان کرنا ضروری ہے۔",
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
                }
            }
        }
    }
}

@Composable
fun FarmerGuestProfileTab(onLoginRedirect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.guest_farmer_title), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = stringResource(id = R.string.guest_farmer_subtitle), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.guest_farmer_desc),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        UrduButton(
            text = stringResource(id = R.string.btn_login_create_account),
            onClick = onLoginRedirect
        )
    }
}

@Composable
fun FarmerMapTab(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val availableList by viewModel.availableMachinery.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SimulatedFarmingMap(
            viewModel = viewModel,
            machineryList = availableList,
            onPinClicked = onNavigateToDetail,
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
    var timerSeconds by remember { mutableIntStateOf(300) }
    
    LaunchedEffect(isOtpStage) {
        if (isOtpStage) {
            timerSeconds = 300
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

    val isPhoneValid = isValidPakistaniMobileNumber(phone)
    val triggerOtpSend = {
        if (requireCnic && cnic.length != 13) {
            cnicError = "درست شناختی کارڈ نمبر درج کریں"
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
                        onClick = onDismiss,
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
                Text(
                    text = stringResource(id = R.string.auth_req_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
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
                val finalResId = if (resId != 0) resId else R.drawable.super_seeder_custom

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

@Composable
fun StepIndicator(step: Int, isActive: Boolean, isCompleted: Boolean, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted || isActive) AgriGreenPrimary else Color.LightGray
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = step.toString(), 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = if (isActive) AgriGreenPrimary else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

private val ProviderTextDark = Color(0xFF17251B)
private val ProviderTextSoft = Color(0xFF4C5A50)

@Composable
fun ProviderInventoryTab(
    viewModel: MainViewModel,
    onNavigateToAddMachinery: () -> Unit
) {
    val myMachinery by viewModel.providerMachinery.collectAsState()
    val activeCount = myMachinery.count { it.status == MachineryStatus.APPROVED }
    val pendingCount = myMachinery.count { it.status == MachineryStatus.PENDING }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RequestListHeader(
                    title = "میری مشینری کی فہرست",
                    subtitle = "آپ کی رجسٹرڈ زرعی مشینیں اور ان کے اہم اعداد و شمار",
                    icon = Icons.Default.Agriculture
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, AgriGreenPrimary.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White, AgriGreenPrimary.copy(alpha = 0.06f))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AgriGreenPrimary.copy(alpha = 0.05f),
                                modifier = Modifier
                                    .size(54.dp)
                                    .align(Alignment.BottomEnd)
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
                                    Text(
                                        text = activeCount.toString(),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AgriGreenPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(AgriGreenPrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AgriGreenPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "فعال مشینیں",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProviderTextDark.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFF57C00).copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White, Color(0xFFF57C00).copy(alpha = 0.06f))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color(0xFFF57C00).copy(alpha = 0.05f),
                                modifier = Modifier
                                    .size(54.dp)
                                    .align(Alignment.BottomEnd)
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
                                    Text(
                                        text = pendingCount.toString(),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFF57C00)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF57C00).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            tint = Color(0xFFF57C00),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "زیر التواء مشینیں",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProviderTextDark.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            if (myMachinery.isEmpty()) {
                item {
                    PremiumEmptyState(
                        message = "کوئی مشینری دستیاب نہیں",
                        description = "نئی مشینری شامل کرنے کے لیے نیچے دیے گئے بٹن پر کلک کریں۔",
                        icon = Icons.Default.Agriculture
                    )
                }
            } else {
                items(myMachinery, key = { it.id }) { item ->
                    ProviderMachineryCard(item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddMachinery,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = AgriGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "مشینری شامل کریں",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProviderMachineryCard(item: Machinery) {
    val context = LocalContext.current
    val imageNames = remember(item.imageUrls) {
        item.imageUrls.ifEmpty { listOf("super_seeder_custom") }
    }
    val (statusText, color) = when (item.status) {
        MachineryStatus.PENDING -> stringResource(id = R.string.provider_status_pending) to Color(0xFFF57C00)
        MachineryStatus.APPROVED -> stringResource(id = R.string.provider_status_approved) to AgriGreenPrimary
        MachineryStatus.REJECTED -> stringResource(id = R.string.provider_status_rejected) to Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(imageNames) { imageName ->
                    val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        .takeIf { it != 0 } ?: R.drawable.super_seeder_custom

                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = item.nameUr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }

            if (imageNames.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(imageNames.size) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = 18.dp, height = 5.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AgriGreenPrimary.copy(alpha = 0.45f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.nameUr,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = ProviderTextDark,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = statusText, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderMachineryMetric(
                    label = "ماڈل",
                    value = item.modelYear.toString(),
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                ProviderMachineryMetric(
                    label = "مکمل ایکڑ",
                    value = "${formatDecimal(item.acresDone)} ایکڑ",
                    icon = Icons.Default.Agriculture,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderMachineryMetric(
                    label = "فاصلہ",
                    value = "${formatDecimal(item.distanceCoveredKm)} کلومیٹر",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
                ProviderMachineryMetric(
                    label = "درجہ بندی",
                    value = String.format(Locale.US, "%.1f ★", item.rating),
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProviderMachineryMetric(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F8F6))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AgriGreenPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AgriGreenPrimary,
                modifier = Modifier.size(15.dp)
            )
        }

        Column {
            Text(text = label, color = ProviderTextSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = value, color = ProviderTextDark, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
    }
}

private fun formatDecimal(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
