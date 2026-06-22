package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.gestures.forEachGesture
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
import pk.kissanmadadgar.mobile.core.components.*
import androidx.compose.foundation.BorderStroke
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.core.theme.AgriGreenSecondary
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.domain.model.Machinery
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
    onLoginRedirect: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name), color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.padding(end = 16.dp).size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AgriGreenPrimary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = AgriGreenPrimary) {
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {
            val guestName = stringResource(id = R.string.user_guest_name)
            when (selectedTab) {
                0 -> FarmerMainTab(viewModel, user?.fullName ?: guestName, onNavigateToDetail, onNavigateToBooking, onNavigateToBookings = { selectedTab = 2 })
                1 -> FarmerSearchTab(viewModel, onNavigateToDetail, onNavigateToBooking)
                2 -> {
                    if (user == null) {
                        FarmerGuestBookingsTab(onLoginRedirect, onClose = { selectedTab = 0 })
                    } else {
                        FarmerBookingsTab(viewModel)
                    }
                }
                3 -> {
                    if (user == null) {
                        FarmerGuestProfileTab(onLoginRedirect)
                    } else {
                        FarmerProfileTab(
                            name = user?.fullName ?: guestName,
                            phone = user?.phoneNumber ?: "",
                            onNameChanged = { newName ->
                                viewModel.updateCurrentUserName(newName)
                            },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(userName: String) {
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
                Text(
                    text = "خوش آمدید، $userName!",
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
                    .size(56.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeIconColor,
                            modifier = Modifier.size(15.dp)
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
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.super_seeder),
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
fun FarmerMainTab(
    viewModel: MainViewModel,
    userName: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onNavigateToBookings: () -> Unit
) {
    val availableList by viewModel.availableMachinery.collectAsState()
    val bookings by viewModel.farmerBookings.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null

    var selectedHomeFilter by remember { mutableStateOf<String?>(null) }

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "welcome_header") {
            WelcomeHeader(userName = userName)
        }

        item(key = "stats_header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(filteredList, key = { it.id }) { machinery ->
                MachineryListItem(
                    machinery = machinery,
                    isAuthorized = isAuthorized,
                    viewModel = viewModel,
                    onClick = { onNavigateToDetail(machinery.id) },
                    onBookClick = { onNavigateToBooking(machinery.id) }
                )
            }
        } else {
            item {
                PremiumEmptyState(
                    message = "کوئی مشینری نہیں ملی",
                    description = "آپ کے منتخب کردہ معیار کے مطابق کوئی مشینری دستیاب نہیں ہے۔",
                    icon = Icons.Default.Agriculture
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
        if (machinery.imageUrls.isEmpty()) listOf("super_seeder") else machinery.imageUrls
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
                        val finalResId = if (resId != 0) resId else R.drawable.super_seeder
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
                                fontWeight = FontWeight.Medium
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
    onNameChanged: (String) -> Unit,
    onLogout: () -> Unit
) {
    var editableName by remember(name) { mutableStateOf(name) }
    val maxCharLimit = 30

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
            tint = AgriGreenPrimary,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
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
            modifier = Modifier.fillMaxWidth(0.9f),
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
        
        Text(text = phone, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.label_language), fontSize = 16.sp)
                    Text(text = stringResource(id = R.string.val_language_urdu), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF5F5F5))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.label_role), fontSize = 16.sp)
                    Text(text = stringResource(id = R.string.role_farmer_label), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        UrduButton(
            text = stringResource(id = R.string.btn_logout),
            onClick = onLogout,
            containerColor = Color.Red.copy(alpha = 0.85f)
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
        if (item == null || item.imageUrls.isEmpty()) listOf("super_seeder") else item.imageUrls
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
                                val finalResId = if (resId != 0) resId else R.drawable.super_seeder
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
            TopAppBar(
                title = { Text(text = "بکنگ کی تصدیق", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep = 1 else onBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
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
                        Column {
                            Text(text = item.nameUr, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AgriGreenPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            val rateText = "سبسڈی: 5000 Rs. فی ایکڑ"
                            Text(text = rateText, fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    if (currentStep == 1) {
                        BookingCard(title = "بکنگ کا وقت منتخب کریں") {
                            DatePickerField(
                                selectedDateMillis = selectedDateMillis,
                                onDateSelected = { selectedDateMillis = it }
                            )
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
                            onClick = { currentStep = 2 },
                            enabled = selectedDateMillis != null
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                    } else {
                        BookingCard(title = "ایکڑ اور رابطے کی تفصیلات") {
                            val acresDouble = acres.toDoubleOrNull()
                            val acresError = if (acres.isNotEmpty() && (acresDouble == null || acresDouble < 0.1 || acresDouble > 100.0)) {
                                "براہ کرم 0.1 سے 100 کے درمیان درست ایکڑ درج کریں"
                            } else null

                            NumberInputField(
                                value = acres,
                                onValueChange = { acres = it },
                                label = "ایکڑ (لازمی)",
                                helperText = "کم از کم 0.1 اور زیادہ سے زیادہ 100 ایکڑ۔",
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
                            OutlinedTextField(
                                value = farmerName,
                                onValueChange = { 
                                    if (it.length <= maxCharLimit) {
                                        farmerName = it
                                    }
                                },
                                label = { Text("نام") },
                                placeholder = { Text("اپنا نام درج کریں", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
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
                                    focusedBorderColor = AgriGreenPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = AgriGreenPrimary
                                ),
                                supportingText = {
                                    Text(
                                        text = "${farmerName.length}/$maxCharLimit",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
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
                        val isNameValid = farmerName.trim().isNotEmpty()
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
                                enabled = isFormValid,
                                onClick = {
                                    isSubmitting = true
                                    if (farmerName.trim().isNotEmpty()) {
                                        viewModel.updateCurrentUserName(farmerName)
                                    }
                                    viewModel.createBooking(item.id, selectedDateMillis!!, hours, item.hourlyRate, acresValue) {
                                        isSubmitting = false
                                        showSuccess = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            SecondaryButton(
                                text = "واپس",
                                onClick = { currentStep = 1 },
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
    isDialog: Boolean = false
) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpStage by remember { mutableStateOf(false) }
    var showSmsFallback by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccessStage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val phoneFocusRequester = remember { FocusRequester() }
    val otpFocusRequester = remember { FocusRequester() }
    val phoneErrorText = stringResource(id = R.string.auth_req_phone_error)
    val otpLengthErrorText = stringResource(id = R.string.auth_req_otp_length_error)

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        imageVector = if (isDialog) Icons.Default.Close else Icons.Default.ArrowBack,
                        contentDescription = if (isDialog) stringResource(id = R.string.auth_req_close) else stringResource(id = R.string.auth_req_back_btn),
                        tint = Color.Black
                    )
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

                val isPhoneValid = isValidPakistaniMobileNumber(phone)
                val triggerOtpSend = {
                    if (isPhoneValid) {
                        isLoading = true
                        errorMessage = null
                        isOtpStage = true
                        isLoading = false
                    } else {
                        errorMessage = phoneErrorText
                    }
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

                if (!showSmsFallback) {
                    LoadingButton(
                        text = stringResource(id = R.string.auth_req_get_whatsapp_code),
                        isLoading = isLoading,
                        onClick = triggerOtpSend,
                        enabled = isPhoneValid,
                        containerColor = Color(0xFF25D366)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LoadingButton(
                            text = stringResource(id = R.string.auth_req_whatsapp),
                            isLoading = isLoading,
                            onClick = triggerOtpSend,
                            modifier = Modifier.weight(1f),
                            enabled = isPhoneValid,
                            containerColor = Color(0xFF25D366)
                        )

                        SecondaryButton(
                            text = stringResource(id = R.string.auth_req_sms),
                            onClick = triggerOtpSend,
                            modifier = Modifier.weight(1f),
                            enabled = isPhoneValid && !isLoading
                        )
                    }
                }
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
                    text = stringResource(id = R.string.auth_req_otp_desc_sent),
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

                TextButton(
                    onClick = {
                        isOtpStage = false
                        showSmsFallback = true
                        otp = ""
                        errorMessage = null
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_req_resend_code),
                        color = AgriGreenPrimary,
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
            containerColor = Color.White
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
                .background(Color.Black),
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
                val finalResId = if (resId != 0) resId else R.drawable.super_seeder

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
    forEachGesture {
        awaitPointerEventScope {
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
