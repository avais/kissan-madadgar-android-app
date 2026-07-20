package pk.kissanmadadgar.mobile.presentation.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.domain.model.RouteInfo
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import java.util.Locale

private class MachineryClusterItem(val machinery: Machinery) : ClusterItem {
    private val latLng = LatLng(machinery.latitude, machinery.longitude)
    override fun getPosition(): LatLng = latLng
    override fun getTitle(): String = machinery.nameUr
    override fun getSnippet(): String? = machinery.providerName
    override fun getZIndex(): Float = 0f
}

private fun machineryMarkerFallbackRes(): Int {
    return R.drawable.other_machinery_clean
}

// Machinery is spread across all of Punjab, not just around the user's own GPS position, so the
// map offers to re-query around wherever the user pans to instead of only ever the fixed device
// location. These two constants gate that: don't bother re-offering a search for a pan smaller
// than MIN_AREA_SEARCH_DISTANCE_METERS (avoids nagging on tiny nudges), and don't offer it at all
// below MIN_AREA_SEARCH_ZOOM (the backend returns "nearest N to a point", not a bounds query, so at
// province-level zoom that would just cluster results on one edge of the visible map).
private const val MIN_AREA_SEARCH_DISTANCE_METERS = 800.0
private const val MIN_AREA_SEARCH_ZOOM = 10f

private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusMeters = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
        kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadiusMeters * c
}

@Composable
private fun MachineryMarkerBadge(machinery: Machinery, sizeDp: Int = 48) {
    val context = LocalContext.current
    val imageUrl = machinery.imageUrls.firstOrNull()

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(BorderStroke(3.dp, AgriGreenPrimary), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http")) {
            // Cluster/marker content gets rendered to a bitmap (software canvas) to hand off
            // to the underlying map SDK as a marker icon — Coil's default hardware bitmaps
            // can't be drawn on a software canvas, so they must be disabled here specifically.
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size((sizeDp - 6).dp)
                    .clip(CircleShape)
            )
        } else {
            val resId = remember(imageUrl, machinery.nameUr) {
                val identifier = imageUrl?.let { context.resources.getIdentifier(it, "drawable", context.packageName) } ?: 0
                if (identifier != 0) identifier else machineryMarkerFallbackRes()
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size((sizeDp - 6).dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun ClusterBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(AgriGreenPrimary)
            .border(BorderStroke(3.dp, Color.White), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}

// The Compose-content marker API (MarkerComposable) isn't available at this maps-compose
// version, so the user's own position is drawn as a plain bitmap marker instead — a blue circle
// with a white ring and a simple person glyph, matching the machinery badge style but sized
// larger (machinery badges are 48dp) so the user's own position stands out clearly.
private fun createUserPositionBitmapDescriptor(context: android.content.Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val size = (60 * density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val center = size / 2f
    val radius = size / 2f - (3f * density)

    val fillPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.rgb(33, 150, 243)
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(center, center, radius, fillPaint)

    val strokePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 3f * density
    }
    canvas.drawCircle(center, center, radius, strokePaint)

    val glyphPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
    }
    val r = radius * 0.28f
    canvas.drawCircle(center, center - r * 1.3f, r, glyphPaint)
    val shoulders = android.graphics.RectF(center - r * 1.7f, center + r * 0.1f, center + r * 1.7f, center + r * 1.9f)
    canvas.drawRoundRect(shoulders, r, r, glyphPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// Marks where the "search this area" distances are measured from. Drawn as a soft radar-style
// pulse (a static double ring, since MarkerComposable isn't available at this maps-compose version
// to animate a real one) around a magnifying-glass badge — reads as "actively searching here" and
// matches the app's own circular-badge marker style instead of Google's generic teardrop pin.
private fun createSearchCenterBitmapDescriptor(context: android.content.Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val size = (72 * density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val center = size / 2f
    val coreRadius = size * 0.27f
    val outerPulseRadius = size * 0.48f
    val innerPulseRadius = size * 0.38f
    val accentColor = android.graphics.Color.rgb(255, 111, 0)

    canvas.drawCircle(center, center, outerPulseRadius, android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(50, 255, 111, 0)
        style = android.graphics.Paint.Style.FILL
    })
    canvas.drawCircle(center, center, innerPulseRadius, android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(90, 255, 111, 0)
        style = android.graphics.Paint.Style.FILL
    })
    canvas.drawCircle(center, center, coreRadius, android.graphics.Paint().apply {
        isAntiAlias = true
        color = accentColor
        style = android.graphics.Paint.Style.FILL
    })
    canvas.drawCircle(center, center, coreRadius, android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 3f * density
    })

    val glyphPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 2.6f * density
        strokeCap = android.graphics.Paint.Cap.ROUND
    }
    val glassRadius = coreRadius * 0.38f
    val glassCenterX = center - coreRadius * 0.14f
    val glassCenterY = center - coreRadius * 0.14f
    canvas.drawCircle(glassCenterX, glassCenterY, glassRadius, glyphPaint)
    canvas.drawLine(
        glassCenterX + glassRadius * 0.7f, glassCenterY + glassRadius * 0.7f,
        glassCenterX + glassRadius * 1.7f, glassCenterY + glassRadius * 1.7f,
        glyphPaint
    )

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
private fun MachineryPopupCard(
    machinery: Machinery,
    isSpeaking: Boolean,
    routeInfo: RouteInfo? = null,
    isRouteLoading: Boolean = false,
    onToggleNarration: () -> Unit,
    onClick: () -> Unit,
    onBook: () -> Unit,
    onCall: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = machinery.providerName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = machinery.nameUr,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = AgriGreenPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = machinery.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) Color(0xFFE65100) else Color(0xFFFF8F00))
                            .clickable(onClick = onToggleNarration),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = stringResource(id = R.string.content_description_listen_audio),
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.content_description_close_popup),
                            tint = Color.DarkGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = machinery.providerPhone, fontSize = 13.sp, color = Color.DarkGray)
                machinery.distanceText?.let { distance ->
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = distance,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            if (machinery.districtUr.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationCity, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = machinery.districtUr, fontSize = 13.sp, color = Color.DarkGray)
                }
            }

            // Road-connection distance/time — big, icon-led, simple wording, since this app's
            // users have low digital literacy: no raw numbers without an icon/unit next to them.
            if (isRouteLoading || routeInfo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                if (isRouteLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = AgriGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(id = R.string.route_loading_text), fontSize = 13.sp, color = Color.DarkGray)
                    }
                } else if (routeInfo != null) {
                    val accentColor = if (routeInfo.isRoadRoute) AgriGreenPrimary else Color(0xFFFF6F00)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f کلومیٹر", routeInfo.distanceMeters / 1000.0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.route_estimated_minutes_format, routeInfo.estimatedMinutes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_book),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriGreenPrimary)
                        .clickable(onClick = onCall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * Interactive Google Map with clustered machinery pins (photo-thumbnail badges) and a tap-to-book
 * popup. Used both by the small embedded preview (gestures disabled) and the full-screen dialog.
 * The camera auto-fits to bounds covering every machine plus the user's own position whenever the
 * machinery list changes (district filter, search, tab open) — never on every GPS tick, since
 * machineryList only changes on an actual fetch, not on location updates. That auto-fit stops once
 * the user taps "search this area" (see onSearchThisArea below) — from that point the user owns
 * the camera and results are drawn wherever they've panned, not re-centered on them.
 */
// Clustering() is gated behind maps-compose-utils' own experimental marker (no stable
// non-experimental clustering API exists in this library version) — this opts in to that
// intentionally, it is not suppressing a warning about our own code.
@OptIn(com.google.maps.android.compose.MapsComposeExperimentalApi::class)
@Composable
internal fun ClusteredMachineryGoogleMap(
    machineryList: List<Machinery>,
    userLatLng: LatLng,
    gesturesEnabled: Boolean,
    showControls: Boolean,
    isAuthorized: Boolean,
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: ((String) -> Unit)?,
    // Called with (lat, lng) when the user pans somewhere new and taps the "search this area"
    // chip. Null (the default) leaves that chip permanently hidden — used by embedded previews
    // that have no fetch of their own to trigger.
    onSearchThisArea: ((Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Guest tap-to-book must go through the same OTP gate as the listing view's Book button
    // (see MachineryListItem in FarmerSearchTab.kt) instead of navigating straight to booking.
    var showAuthFlow by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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
    // Google's Maps SDK has no direct setLocale API — it renders street/place labels and its own
    // UI (attribution, legal text) in whatever Locale the Context it was created with carries, so
    // this app forces Urdu regardless of the device's system locale.
    val urduMapContext = remember(context) {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale("ur"))
        context.createConfigurationContext(config)
    }
    val coroutineScope = rememberCoroutineScope()
    var selectedMachinery by remember { mutableStateOf<Machinery?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }
    val locationPermissionGranted = remember { hasLocationPermission(context) }

    // Audio narration for the selected machine's popup card — shared NarrationManager singleton.
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }
    val activeNarrationId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()
    val isSpeaking = activeNarrationId == "map_popup_narration"
    // Stop narration if the user picks a different marker or closes the popup.
    LaunchedEffect(selectedMachinery) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 13f)
    }

    val clusterItems = remember(machineryList) {
        machineryList.map { MachineryClusterItem(it) }
    }

    // Once true, the user has taken over the camera via "search this area" — the auto-fit below
    // stops running so a subsequent filter/keyword change doesn't yank the map back to fit the
    // user's own position again.
    var cameraUnderUserControl by remember { mutableStateOf(false) }
    var searchedCenter by remember { mutableStateOf(userLatLng) }
    var pendingAreaSearch by remember { mutableStateOf<LatLng?>(null) }
    val isSearchingArea by viewModel.isLoadingAvailableMachinery.collectAsState()

    // Road-connection line for whichever machinery marker is currently tapped, fetched on
    // demand only for that one machinery (not proactively for every visible pin, to keep
    // routing-API usage down). Reference point is the search-center marker if the user has
    // already used "search this area" at least once, otherwise the user's own live location —
    // captured once at the moment a marker is tapped (selectedMachinery is the only key below)
    // so a background GPS tick while the popup is open doesn't keep re-triggering fetches.
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    var isRouteLoading by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    LaunchedEffect(selectedMachinery) {
        val machinery = selectedMachinery
        if (machinery == null) {
            routeInfo = null
            routePoints = emptyList()
            isRouteLoading = false
            return@LaunchedEffect
        }
        val destination = if (cameraUnderUserControl) searchedCenter else userLatLng
        isRouteLoading = true
        routeInfo = null
        routePoints = emptyList()
        val origin = LatLng(machinery.latitude, machinery.longitude)
        val fetched = viewModel.getRoute(origin.latitude, origin.longitude, destination.latitude, destination.longitude)
        routeInfo = fetched
        // Only ever draw a line for a genuine road route — a straight "as the crow flies" dashed
        // line across fields/buildings read as broken/awkward, so the fallback case (no route
        // found, or a malformed polyline) draws nothing at all. The distance/time chip in the
        // popup card still shows in that case, just without a line on the map.
        routePoints = if (fetched != null && fetched.isRoadRoute && !fetched.encodedPolyline.isNullOrBlank()) {
            try {
                PolyUtil.decode(fetched.encodedPolyline)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        isRouteLoading = false
    }

    LaunchedEffect(isMapLoaded, machineryList) {
        if (!isMapLoaded || cameraUnderUserControl) return@LaunchedEffect
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(userLatLng)
        machineryList.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        try {
            if (machineryList.isEmpty()) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f))
            } else {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 140))
            }
        } catch (e: Exception) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 13f)
        }
    }

    // Once cameraUnderUserControl is true, the effect above stops re-fitting (by design, so a
    // subsequent filter change doesn't yank the camera back to the user's own position — see its
    // comment). But a fresh batch of results from a NEW "search this area" tap still needs to
    // bring itself into view, or the user has no way to tell whether anything new loaded without
    // manually panning around to look — fits to just the new results plus searchedCenter (not
    // userLatLng, which would fight the user's own pan) whenever a search-area fetch lands.
    LaunchedEffect(machineryList) {
        if (!cameraUnderUserControl || machineryList.isEmpty()) return@LaunchedEffect
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(searchedCenter)
        machineryList.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        try {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 140))
        } catch (e: Exception) {
            // Keep whatever camera position is already showing.
        }
    }

    // Offers "search this area" once the user finishes a pan/zoom (camera goes idle) far enough
    // from wherever was last searched. isMoving is true for the whole duration of a gesture/fling
    // and flips back to false once the camera settles — that false edge is the idle signal.
    if (onSearchThisArea != null) {
        var wasCameraMoving by remember { mutableStateOf(false) }
        LaunchedEffect(cameraPositionState) {
            snapshotFlow { cameraPositionState.isMoving }.collect { isMoving ->
                if (wasCameraMoving && !isMoving) {
                    val target = cameraPositionState.position.target
                    val moved = distanceMeters(
                        searchedCenter.latitude, searchedCenter.longitude,
                        target.latitude, target.longitude
                    )
                    pendingAreaSearch = if (moved > MIN_AREA_SEARCH_DISTANCE_METERS) target else null
                }
                wasCameraMoving = isMoving
            }
        }
    }

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalContext provides urduMapContext) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLoaded = { isMapLoaded = true },
                // Google's own "my location" blue dot is deliberately left off: it's driven by
                // Play Services' own fused location, a different source than the app's own GPS
                // listener that userLatLng comes from, so the two would drift apart and show two
                // slightly different positions on screen. The custom marker below is the single
                // source of truth for where the user is shown on this map.
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = gesturesEnabled,
                    zoomGesturesEnabled = gesturesEnabled,
                    tiltGesturesEnabled = gesturesEnabled,
                    rotationGesturesEnabled = gesturesEnabled,
                    myLocationButtonEnabled = false,
                    compassEnabled = gesturesEnabled
                )
            ) {
                val userIcon = remember(context) { createUserPositionBitmapDescriptor(context) }
                Marker(
                    state = MarkerState(position = userLatLng),
                    title = stringResource(id = R.string.user_location_title),
                    icon = userIcon
                )

                // Every distanceText on the machinery pins is measured from searchedCenter (the
                // backend computes it from whatever lat/lng the request carried), not from the
                // user's real GPS position once "search this area" has been used at least once —
                // without a visible marker at that point, the distance numbers in the popup card
                // have no reference on screen to make sense of. A plain default pin (not the same
                // circular badge style as the user/machinery markers) reads as a distinct
                // "reference point" rather than another one of those.
                if (cameraUnderUserControl) {
                    val searchCenterIcon = remember(context) { createSearchCenterBitmapDescriptor(context) }
                    Marker(
                        state = MarkerState(position = searchedCenter),
                        title = stringResource(id = R.string.search_center_marker_title),
                        icon = searchCenterIcon
                    )
                }

                // Road-connection line to whichever marker is selected — only ever drawn for a
                // genuine road route (routePoints stays empty in the fallback/no-route case, see
                // the LaunchedEffect(selectedMachinery) above, so there is deliberately no straight
                // "as the crow flies" line drawn across fields/buildings when ORS can't find one).
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = AgriGreenPrimary,
                        width = 10f
                    )
                }

                Clustering(
                    items = clusterItems,
                    onClusterItemClick = { item ->
                        selectedMachinery = item.machinery
                        true
                    },
                    clusterContent = { cluster ->
                        ClusterBadge(count = cluster.size)
                    },
                    clusterItemContent = { item ->
                        MachineryMarkerBadge(machinery = item.machinery)
                    }
                )
            }
        }

        if (showControls) {
            MapControls(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onZoomIn = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                },
                onZoomOut = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                },
                onMyLocation = if (locationPermissionGranted) {
                    {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                        }
                    }
                } else null
            )
        }

        if (onSearchThisArea != null) {
            // Kept visible while isSearchingArea is true even after pendingAreaSearch is cleared
            // below — otherwise the chip (and the loading spinner inside it) disappeared the
            // instant it was tapped, before the fetch had even started, so the user never actually
            // saw a loading state for the search they just triggered.
            if (pendingAreaSearch != null || isSearchingArea) {
                SearchThisAreaChip(
                    zoomedInEnough = cameraPositionState.position.zoom >= MIN_AREA_SEARCH_ZOOM,
                    isLoading = isSearchingArea,
                    onClick = {
                        pendingAreaSearch?.let { target ->
                            cameraUnderUserControl = true
                            searchedCenter = target
                            onSearchThisArea(target.latitude, target.longitude)
                        }
                        pendingAreaSearch = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }

        selectedMachinery?.let { machinery ->
            MachineryPopupCard(
                machinery = machinery,
                isSpeaking = isSpeaking,
                routeInfo = routeInfo,
                isRouteLoading = isRouteLoading,
                onToggleNarration = {
                    if (isSpeaking) {
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                    } else {
                        // Guests never see the real contact number (it's masked to something like
                        // "XXXXX-0304" in the UI), so it must not be read aloud either — use a
                        // separate narration that prompts booking/login instead of the phone number.
                        val text = if (isAuthorized) {
                            context.getString(
                                R.string.map_popup_narration_format,
                                machinery.providerName,
                                machinery.nameUr,
                                machinery.distanceText ?: "",
                                machinery.providerPhone,
                                machinery.rating.toString()
                            )
                        } else {
                            context.getString(
                                R.string.map_popup_narration_guest_format,
                                machinery.providerName,
                                machinery.nameUr,
                                machinery.distanceText ?: "",
                                machinery.rating.toString()
                            )
                        }
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "map_popup_narration")
                    }
                },
                onClick = { onNavigateToDetail(machinery.id) },
                onBook = {
                    if (isAuthorized) {
                        onNavigateToBooking?.invoke(machinery.id)
                    } else {
                        pendingAction = { onNavigateToBooking?.invoke(machinery.id) }
                        showAuthFlow = true
                    }
                },
                onCall = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:${machinery.providerPhone}")
                    }
                    context.startActivity(intent)
                },
                onClose = { selectedMachinery = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth(0.95f)
            )
        }

        AnimatedVisibility(
            visible = !isMapLoaded,
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            MapLoadingOverlay()
        }
    }
}

@Composable
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (onMyLocation != null) {
            MapControlButton(icon = Icons.Default.GpsFixed, onClick = onMyLocation)
        }
        MapControlButton(icon = Icons.Default.Add, onClick = onZoomIn)
        MapControlButton(icon = Icons.Default.Remove, onClick = onZoomOut)
    }
}

@Composable
private fun MapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(elevation = 3.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(22.dp))
    }
}

// Google Maps renders a bare beige/cream background the instant it's attached, then pops in
// tiles + markers only once onMapLoaded fires — with nothing covering that gap it reads as a
// half-broken blank screen. This overlay sits on top until isMapLoaded flips true, then fades out.
@Composable
private fun MapLoadingOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_loading")
    val pinScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin_scale"
    )
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer(scaleX = ringScale, scaleY = ringScale, alpha = ringAlpha)
                        .clip(CircleShape)
                        .background(AgriGreenPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer(scaleX = pinScale, scaleY = pinScale)
                        .clip(CircleShape)
                        .background(AgriGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.map_loading_text),
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun SearchThisAreaChip(
    zoomedInEnough: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .then(
                if (zoomedInEnough && !isLoading) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = AgriGreenPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.content_description_search_this_area),
                tint = if (zoomedInEnough) AgriGreenPrimary else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(
                id = if (zoomedInEnough) R.string.search_this_area else R.string.zoom_in_to_search_area
            ),
            color = if (zoomedInEnough) Color.Black else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/**
 * Full-screen, fully interactive machinery map. Opened directly (no intermediate small preview
 * step) whenever the user taps the "نقشہ" (Map) option in the Search tab.
 */
@Composable
fun FullScreenMachineryMap(
    machineryList: List<Machinery>,
    userLatLng: LatLng,
    isAuthorized: Boolean,
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: ((String) -> Unit)?,
    onDismiss: () -> Unit,
    onSearchThisArea: ((Double, Double) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) { onDismiss() }
        Box(modifier = Modifier.fillMaxSize()) {
            ClusteredMachineryGoogleMap(
                machineryList = machineryList,
                userLatLng = userLatLng,
                gesturesEnabled = true,
                showControls = true,
                isAuthorized = isAuthorized,
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToBooking = onNavigateToBooking,
                onSearchThisArea = onSearchThisArea,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp)
                    .shadow(elevation = 3.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Black)
            }
        }
    }
}
