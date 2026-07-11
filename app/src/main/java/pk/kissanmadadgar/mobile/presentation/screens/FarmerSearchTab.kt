package pk.kissanmadadgar.mobile.presentation.screens

import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.components.UrduTextField

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
                val imageListState = rememberLazyListState()
                LaunchedEffect(imageListState) {
                    snapshotFlow { imageListState.isScrollInProgress }
                        .distinctUntilChanged()
                        .collect { scrolling ->
                            if (!scrolling) {
                                val idx = imageListState.firstVisibleItemIndex
                                val offset = imageListState.firstVisibleItemScrollOffset
                                val itemSize = imageListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
                                val target = if (offset > itemSize / 2) idx + 1 else idx
                                imageListState.animateScrollToItem(target)
                            }
                        }
                }
                LazyRow(
                    state = imageListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(displayImages) { index, imageName ->
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
                                    Image(
                                        painter = painterResource(id = R.drawable.super_seeder_custom),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .height(180.dp)
                                    .clickable { zoomedImageIndex = index },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                            val finalResId = if (resId != 0) resId else R.drawable.super_seeder_custom
                            Image(
                                painter = painterResource(id = finalResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .height(180.dp)
                                    .clickable { zoomedImageIndex = index },
                                contentScale = ContentScale.Crop
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
                            val logoUrl = machinery.projectLogo
                            if (!logoUrl.isNullOrEmpty() && logoUrl.startsWith("http")) {
                                val cleanLogoUrl = if (logoUrl.contains("9089") && !logoUrl.contains("9089/")) {
                                    logoUrl.replace("9089", "9089/")
                                } else {
                                    logoUrl
                                }
                                coil.compose.SubcomposeAsyncImage(
                                    model = cleanLogoUrl,
                                    loading = {
                                        Box(
                                            modifier = Modifier.size(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(10.dp))
                                        }
                                    },
                                    error = {
                                        Image(
                                            painter = painterResource(id = R.drawable.logo_pcap),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_pcap),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = machinery.projectName ?: stringResource(id = R.string.pcap_fallback),
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
                            Image(
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
                            Image(
                                painter = painterResource(id = R.drawable.ic_location_round),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val distanceLabel = machinery.distanceText ?: stringResource(id = R.string.distance_km_format, "1.2")
                            Text(text = distanceLabel, color = Color.DarkGray, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (isAuthorized) {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("phone", machinery.providerPhone)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, context.getString(R.string.msg_phone_copied), Toast.LENGTH_SHORT).show()
                                } else {
                                    pendingAction = {
                                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipData = ClipData.newPlainText("phone", machinery.providerPhone)
                                        clipboardManager.setPrimaryClip(clipData)
                                        Toast.makeText(context, context.getString(R.string.msg_phone_copied), Toast.LENGTH_SHORT).show()
                                    }
                                    showAuthFlow = true
                                }
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_phone_round),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayPhone,
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible
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
                                text = stringResource(id = R.string.label_subsidy_scheme),
                                color = Color(0xFF0B5D34),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = machinery.subsidyText ?: stringResource(id = R.string.subsidy_fallback),
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
                                text = stringResource(id = R.string.btn_book),
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
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${machinery.providerPhone}")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        pendingAction = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${machinery.providerPhone}")
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

private enum class SearchViewMode { LIST, MAP }

@Composable
fun FarmerSearchTab(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit
) {
    // rememberSaveable, not remember: this composable is torn down and rebuilt from scratch
    // whenever the user navigates to MachineryDetailScreen and back (Compose Navigation only
    // keeps the current destination composed), so plain `remember` state — including whether the
    // full-screen map was open — would silently reset on every such round trip.
    var query by rememberSaveable { mutableStateOf("") }
    val availableList by viewModel.availableMachinery.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isAuthorized = user != null
    val isLoadingMore by viewModel.isLoadingMoreMachinery.collectAsState()
    val isLastPage by viewModel.availableMachineryIsLastPage.collectAsState()
    val isLoadingAvailableMachinery by viewModel.isLoadingAvailableMachinery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    var viewModeName by rememberSaveable { mutableStateOf(SearchViewMode.LIST.name) }
    val viewMode = SearchViewMode.valueOf(viewModeName)
    var isMapFullScreen by rememberSaveable { mutableStateOf(false) }
    val districtsList by viewModel.districtsList.collectAsState()
    var selectedDistrictId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedDistrict = districtsList.find { it.id == selectedDistrictId }
    val isLoadingDistricts by viewModel.isLoadingDistricts.collectAsState()

    LaunchedEffect(Unit) {
        if (districtsList.isEmpty()) {
            viewModel.fetchDistricts()
        }
    }

    // Audio narration helper explaining how to search (name/mobile number) and the map option.
    // Uses the shared NarrationManager singleton rather than its own TextToSpeech engine.
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }
    val activeNarrationId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()
    val isSpeaking = activeNarrationId == "search_tab_help"

    // Instant client-side narrowing over whatever is already loaded, so the list feels
    // responsive while the debounced server search below is still in flight. Must cover the same
    // fields the server's keyword search does (name/description AND provider name/phone) —
    // otherwise once the server response lands in availableList (e.g. phone-number matches that
    // don't mention the query in nameUr/descriptionUr at all), this filter would immediately
    // strip those results back out again, making it look like the server "isn't returning data".
    val filteredList = remember(query, availableList) {
        if (query.trim().isEmpty()) {
            availableList
        } else {
            availableList.filter {
                it.nameUr.contains(query, ignoreCase = true) ||
                    it.descriptionUr.contains(query, ignoreCase = true) ||
                    it.providerName.contains(query, ignoreCase = true) ||
                    it.providerPhone.contains(query, ignoreCase = true)
            }
        }
    }

    // Debounced server-side search: calls /getAvailableMachines with the keyword param
    // (and whatever district filter is active) instead of only filtering the already-fetched
    // page locally. Also refetches when the full-screen map opens, since it needs a much bigger
    // batch than the list's small paginated page to actually look populated. Skips the very
    // first composition since the tab's initial load already fetched the unfiltered list.
    var isFirstSearchEffect by remember { mutableStateOf(true) }
    LaunchedEffect(query, selectedDistrict, isMapFullScreen) {
        if (isFirstSearchEffect) {
            isFirstSearchEffect = false
        } else {
            delay(400)
            viewModel.fetchAvailableMachines(
                userLocation.first,
                userLocation.second,
                "search",
                districtId = selectedDistrict?.id,
                keyword = query.trim().ifEmpty { null },
                size = if (isMapFullScreen) 50 else 10
            )
        }
    }

    val listState = rememberLazyListState()

    // Load the next page once the user scrolls within a couple of items of the end.
    // The current keyword/district filters carry over automatically since they're stored
    // on the ViewModel and reused by loadMoreAvailableMachines.
    LaunchedEffect(listState, isLastPage, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisibleIndex >= layoutInfo.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && !isLastPage && !isLoadingMore) {
                    viewModel.loadMoreAvailableMachines()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UrduTextField(
                value = query,
                onValueChange = { query = it },
                label = stringResource(id = R.string.search_machinery_hint),
                modifier = Modifier.weight(1f)
            )
            SearchAudioHelperButton(
                isSpeaking = isSpeaking,
                onClick = {
                    if (isSpeaking) {
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                    } else {
                        val text = context.getString(R.string.search_tab_help_narration)
                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, "search_tab_help")
                    }
                }
            )
            DistrictFilterButton(
                districtsList = districtsList,
                isLoadingDistricts = isLoadingDistricts,
                selectedDistrict = selectedDistrict,
                onDistrictSelected = { selectedDistrictId = it?.id }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SearchViewModeToggle(
            viewMode = viewMode,
            onViewModeChange = { mode ->
                if (mode == SearchViewMode.MAP) {
                    // Map has no persistent embedded state — it always opens straight into the
                    // full-screen dialog rather than a small tap-to-expand preview.
                    isMapFullScreen = true
                } else {
                    viewModeName = mode.name
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
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

                if (isLoadingMore) {
                    item(key = "loading_more_machinery") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AgriGreenPrimary)
                        }
                    }
                }
            }

            // Loader for the debounced keyword/district search itself, distinct from
            // the infinite-scroll "load more" spinner above.
            if (isLoadingAvailableMachinery && !isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgriGreenPrimary)
                }
            }
        }
    }

    if (isMapFullScreen) {
        FullScreenMachineryMap(
            machineryList = availableList,
            userLatLng = com.google.android.gms.maps.model.LatLng(userLocation.first, userLocation.second),
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToBooking = onNavigateToBooking,
            onDismiss = { isMapFullScreen = false }
        )
    }
}

@Composable
private fun SearchViewModeToggle(
    viewMode: SearchViewMode,
    onViewModeChange: (SearchViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            SearchViewMode.LIST to (Icons.Default.List to stringResource(id = R.string.search_view_list)),
            SearchViewMode.MAP to (Icons.Default.Map to stringResource(id = R.string.search_view_map))
        ).forEach { (mode, iconAndLabel) ->
            val (icon, label) = iconAndLabel
            val isSelected = viewMode == mode
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) AgriGreenPrimary else Color.Transparent)
                    .clickable { onViewModeChange(mode) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.DarkGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color.DarkGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Speaker button that reads out how to search (by name or mobile number) and points to the map
 * option — same SupportAgent icon and amber/orange gradient language used for audio helpers
 * elsewhere in the app (see WelcomeHeader / ZaraiMachineRegisterBanner in FarmerScreens.kt).
 */
@Composable
private fun SearchAudioHelperButton(
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "search_audio_helper")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking) 1.0f else 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "buttonScale"
    )
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Restart),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Restart),
        label = "rippleAlpha"
    )

    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer(scaleX = rippleScale, scaleY = rippleScale, alpha = rippleAlpha)
                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
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
                .clickable(onClick = onClick),
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
                            label = "bar_search_help_$i"
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Compact icon-only district filter, meant to sit right next to the search box instead of
 * consuming a full row. Fills in and shows a badge dot once a district is active so the state
 * is still visible without needing to show the district name inline.
 */
@Composable
private fun DistrictFilterButton(
    districtsList: List<pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto>,
    isLoadingDistricts: Boolean,
    selectedDistrict: pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto?,
    onDistrictSelected: (pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto?) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val isActive = selectedDistrict != null

    Box {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isActive) AgriGreenPrimary else Color(0xFFF0F0F0))
                .border(
                    BorderStroke(1.dp, if (isActive) AgriGreenPrimary else Color(0xFFD7DDD4)),
                    RoundedCornerShape(12.dp)
                )
                .clickable { isMenuExpanded = true },
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingDistricts) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = if (isActive) Color.White else AgriGreenPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(id = R.string.select_district_hint),
                    tint = if (isActive) Color.White else Color.DarkGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            Text(
                text = stringResource(id = R.string.select_district_hint),
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.select_district_all),
                        fontWeight = if (selectedDistrict == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedDistrict == null) AgriGreenPrimary else Color.Black
                    )
                },
                onClick = {
                    onDistrictSelected(null)
                    isMenuExpanded = false
                }
            )
            districtsList.forEach { district ->
                val isSelected = selectedDistrict?.id == district.id
                DropdownMenuItem(
                    text = {
                        Text(
                            text = district.nameUrdu,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AgriGreenPrimary else Color.Black
                        )
                    },
                    onClick = {
                        onDistrictSelected(district)
                        isMenuExpanded = false
                    }
                )
            }
        }
    }
}
