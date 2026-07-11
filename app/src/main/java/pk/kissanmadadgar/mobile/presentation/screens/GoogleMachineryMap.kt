package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import kotlinx.coroutines.launch
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Machinery

private class MachineryClusterItem(val machinery: Machinery) : ClusterItem {
    private val latLng = LatLng(machinery.latitude, machinery.longitude)
    override fun getPosition(): LatLng = latLng
    override fun getTitle(): String = machinery.nameUr
    override fun getSnippet(): String? = machinery.providerName
    override fun getZIndex(): Float = 0f
}

private fun machineryMarkerFallbackRes(nameUr: String): Int {
    val name = nameUr.lowercase()
    return when {
        name.contains("بیلر") || name.contains("baler") || name.contains("bailer") -> R.drawable.bailer
        name.contains("ہارویسٹر") || name.contains("harvester") -> R.drawable.harvester
        else -> R.drawable.super_seeder_custom
    }
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
                if (identifier != 0) identifier else machineryMarkerFallbackRes(machinery.nameUr)
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

@Composable
private fun MachineryPopupCard(
    machinery: Machinery,
    isSpeaking: Boolean,
    onToggleNarration: () -> Unit,
    onClick: () -> Unit,
    onBook: () -> Unit,
    onCall: () -> Unit,
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
 * machineryList only changes on an actual fetch, not on location updates.
 */
@Composable
private fun ClusteredMachineryGoogleMap(
    machineryList: List<Machinery>,
    userLatLng: LatLng,
    gesturesEnabled: Boolean,
    showControls: Boolean,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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

    LaunchedEffect(isMapLoaded, machineryList) {
        if (!isMapLoaded) return@LaunchedEffect
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

    Box(modifier = modifier) {
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

        selectedMachinery?.let { machinery ->
            MachineryPopupCard(
                machinery = machinery,
                isSpeaking = isSpeaking,
                onToggleNarration = {
                    if (isSpeaking) {
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                    } else {
                        val text = context.getString(
                            R.string.map_popup_narration_format,
                            machinery.providerName,
                            machinery.nameUr,
                            machinery.distanceText ?: "",
                            machinery.providerPhone,
                            machinery.rating.toString()
                        )
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "map_popup_narration")
                    }
                },
                onClick = { onNavigateToDetail(machinery.id) },
                onBook = { onNavigateToBooking?.invoke(machinery.id) },
                onCall = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:${machinery.providerPhone}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth(0.95f)
            )
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

/**
 * Full-screen, fully interactive machinery map. Opened directly (no intermediate small preview
 * step) whenever the user taps the "نقشہ" (Map) option in the Search tab.
 */
@Composable
fun FullScreenMachineryMap(
    machineryList: List<Machinery>,
    userLatLng: LatLng,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: ((String) -> Unit)?,
    onDismiss: () -> Unit
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
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToBooking = onNavigateToBooking,
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
