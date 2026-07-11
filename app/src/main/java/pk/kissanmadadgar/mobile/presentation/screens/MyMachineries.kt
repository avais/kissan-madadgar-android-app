package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.domain.model.MachineryStatus
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import java.util.Locale

private val ProviderTextDark = Color(0xFF17251B)
private val ProviderTextSoft = Color(0xFF4C5A50)

@Composable
fun ProviderInventoryTab(
    viewModel: MainViewModel,
    onNavigateToAddMachinery: () -> Unit
) {
    val myMachinery by viewModel.providerMachinery.collectAsState()
    val activeCount by viewModel.providerApprovedCount.collectAsState()
    val pendingCount by viewModel.providerPendingCount.collectAsState()
    val totalAcres by viewModel.providerTotalAcres.collectAsState()
    val totalRating by viewModel.providerTotalRating.collectAsState()
    val currentPage by viewModel.providerMachineryCurrentPage.collectAsState()
    val isLastPage by viewModel.providerMachineryIsLast.collectAsState()
    val isLoadingMore by viewModel.isLoadingMoreProviderMachinery.collectAsState()
    val listState = rememberLazyListState()

    // GET api/android/my-machines is otherwise only fetched once at login (or right after
    // registering a new machine) — refetch page 0 every time this tab is opened so an admin's
    // approval/rejection made while the session is already open shows up here without
    // requiring a logout/login.
    LaunchedEffect(Unit) {
        viewModel.fetchProviderMachinery(page = 0)
    }

    // Infinite scroll: fetch the next page once the user nears the bottom of the list,
    // mirroring the same near-bottom pattern FarmerBookingsTab uses for bookings pagination.
    LaunchedEffect(listState, isLastPage, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= layoutInfo.totalItemsCount - 3
        }.collect { nearBottom ->
            if (nearBottom && !isLastPage && !isLoadingMore) {
                viewModel.fetchProviderMachinery(page = currentPage + 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RequestListHeader(
                    title = stringResource(id = R.string.my_machinery_list_title),
                    subtitle = stringResource(id = R.string.my_machinery_list_subtitle),
                    icon = Icons.Default.Agriculture
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Active
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
                                        text = stringResource(id = R.string.provider_stat_active_machines),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProviderTextDark.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Card 2: Pending
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
                                        text = stringResource(id = R.string.provider_stat_pending_machines),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProviderTextDark.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 3: Total Acres
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.White, Color(0xFF1E88E5).copy(alpha = 0.06f))
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color(0xFF1E88E5).copy(alpha = 0.05f),
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
                                            text = totalAcres,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E88E5)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E88E5).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Agriculture,
                                                contentDescription = null,
                                                tint = Color(0xFF1E88E5),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(id = R.string.provider_stat_total_acres),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProviderTextDark.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Card 4: Total Rating
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFFFBC02D).copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.White, Color(0xFFFBC02D).copy(alpha = 0.06f))
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFBC02D).copy(alpha = 0.05f),
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
                                            text = totalRating,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFBC02D)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFBC02D).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFBC02D),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(id = R.string.provider_stat_average_rating),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProviderTextDark.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (myMachinery.isEmpty()) {
                item {
                    PremiumEmptyState(
                        message = stringResource(id = R.string.provider_empty_machinery_title),
                        description = stringResource(id = R.string.provider_empty_machinery_desc),
                        icon = Icons.Default.Agriculture
                    )
                }
            } else {
                items(myMachinery, key = { it.id }) { item ->
                    ProviderMachineryCard(item = item)
                }
            }

            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(28.dp))
                    }
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
                    text = stringResource(id = R.string.btn_add_machinery),
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
            val providerImageListState = rememberLazyListState()
            LaunchedEffect(providerImageListState) {
                snapshotFlow { providerImageListState.isScrollInProgress }
                    .distinctUntilChanged()
                    .collect { scrolling ->
                        if (!scrolling) {
                            val idx = providerImageListState.firstVisibleItemIndex
                            val offset = providerImageListState.firstVisibleItemScrollOffset
                            val itemSize = providerImageListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
                            val target = if (offset > itemSize / 2) idx + 1 else idx
                            providerImageListState.animateScrollToItem(target)
                        }
                    }
            }
            LazyRow(
                state = providerImageListState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(imageNames) { imageName ->
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
                                    painter = painterResource(id = R.drawable.super_seeder_custom),
                                    contentDescription = item.nameUr,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .height(190.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )
                            },
                            contentDescription = item.nameUr,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    } else {
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
                    label = stringResource(id = R.string.label_acres_done),
                    value = stringResource(id = R.string.acres_suffix_val, formatDecimal(item.acresDone)),
                    icon = Icons.Default.Agriculture,
                    modifier = Modifier.weight(1f)
                )
                ProviderMachineryMetric(
                    label = stringResource(id = R.string.label_rating),
                    value = String.format(Locale.US, "%.1f ★", item.rating),
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderMachineryMetric(
                    label = stringResource(id = R.string.label_district),
                    value = item.districtUr,
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
                if (!item.projectName.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF7F8F6))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val logoUrl = item.projectLogo
                        if (!logoUrl.isNullOrEmpty() && logoUrl.startsWith("http")) {
                            coil.compose.SubcomposeAsyncImage(
                                model = logoUrl,
                                loading = {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(10.dp))
                                    }
                                },
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AgriGreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.label_scheme),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = item.projectName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProviderTextDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    ProviderMachineryMetric(
                        label = stringResource(id = R.string.label_scheme),
                        value = stringResource(id = R.string.label_no_scheme),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
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
