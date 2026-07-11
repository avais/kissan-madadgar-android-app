package pk.kissanmadadgar.mobile.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler

import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest


import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ColorFilter
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import pk.kissanmadadgar.mobile.core.components.AgriConfirmationDialog
import pk.kissanmadadgar.mobile.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingLifecyclePhoto
import pk.kissanmadadgar.mobile.domain.model.BookingPhotoStep
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.domain.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale

private val CardGray = Color(0xFFF4F5F2)
private val BorderGray = Color(0xFFD7DDD4)
private val TextDark = Color(0xFF17251B)
private val TextSoft = Color(0xFF4C5A50)
private val DoneGreen = Color(0xFF2E7D32)
private val ActionOrange = Color(0xFFF57C00)
private val DangerRed = Color(0xFFD32F2F)
private val DisabledGray = Color(0xFF8A948C)

private data class StepUi(
    val step: BookingPhotoStep,
    val titleResId: Int,
    val role: UserRole
)

private data class StatusUi(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
)

@Composable
fun RequestDashboardStatsRow(
    pendingCount: Int,
    ongoingCount: Int,
    finishedCount: Int,
    selectedFilter: String? = null,
    onFilterSelected: (String?) -> Unit = {}
) {
    data class FilterTab(
        val key: String,
        val label: String,
        val count: Int,
        val icon: ImageVector,
        val activeColor: Color,
        val activeBg: Color
    )

    val tabs = listOf(
        FilterTab("PENDING", "جدید بکنگ", pendingCount, Icons.Default.HourglassEmpty, ActionOrange, Color(0xFFFFF3E0)),
        FilterTab("ONGOING", "جاری", ongoingCount, Icons.Default.Autorenew, Color(0xFF1E88E5), Color(0xFFE3F2FD)),
        FilterTab("COMPLETED", "مکمل", finishedCount, Icons.Default.CheckCircle, DoneGreen, Color(0xFFE8F5E9))
    )

    val listState = rememberLazyListState()

    // Keep the selected tab scrolled fully into view whenever selectedFilter changes — including
    // the very first composition, where it may already be non-null (e.g. returning to this tab
    // with a filter still selected from before). Relying only on the click handler's own scroll
    // missed that case and left the selected tab clipped at the row's edge.
    LaunchedEffect(selectedFilter) {
        val index = tabs.indexOfFirst { it.key == selectedFilter }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(tabs) { index, tab ->
            val isSelected = selectedFilter == tab.key
            val animBg by animateColorAsState(
                targetValue = if (isSelected) tab.activeColor else Color.White,
                animationSpec = tween(200)
            )
            val animContent by animateColorAsState(
                targetValue = if (isSelected) Color.White else tab.activeColor,
                animationSpec = tween(200)
            )

            Box(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(animBg)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) tab.activeColor else tab.activeColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable {
                        if (isSelected) onFilterSelected(null) else onFilterSelected(tab.key)
                    }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = animContent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = tab.label,
                        color = animContent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    // Count badge
                    if (tab.count > 0) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else tab.activeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.count.toString(),
                                color = animContent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestStatCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector,
    isSelected: Boolean = false,
    isAnySelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardElevation = if (isSelected) 8.dp else 2.dp
    val alphaFactor = if (isAnySelected && !isSelected) 0.55f else 1.0f
    
    val cardBgColor = if (isSelected) color else Color.White
    val cardBorderColor = if (isSelected) color else color.copy(alpha = 0.15f)
    
    val valueColor = if (isSelected) Color.White else color
    val titleColor = if (isSelected) Color.White.copy(alpha = 0.9f) else TextDark.copy(alpha = 0.8f)
    val watermarkColor = if (isSelected) Color.White.copy(alpha = 0.15f) else color.copy(alpha = 0.05f)
    val badgeBgColor = if (isSelected) Color.White.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)
    val badgeIconColor = if (isSelected) Color.White else color

    Card(
        modifier = modifier
            .height(100.dp)
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
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = watermarkColor,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.BottomEnd)
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
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = valueColor
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeIconColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }
        }
    }
}


@Composable
fun RequestListHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AgriGreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSoft
                )
            }
        }
    }
}

@Composable
fun PremiumEmptyState(
    message: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F5F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DisabledGray,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextSoft,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProviderRequestCard(
    booking: Booking,
    requestNumber: String? = null,
    onAcceptRequest: () -> Unit = {},
    onRejectRequest: (String?) -> Unit = {},
    onClick: () -> Unit = {}
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    val accentColor = tileAccentColor(booking.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 3.dp,
                color = accentColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Request number chip + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                requestNumber?.let {
                    RequestNumberChip(number = it)
                } ?: Spacer(modifier = Modifier.weight(1f))
                BookingStatusBadge(status = booking.status, label = booking.rentalRequestStatusUrdu)
            }

            // PCAP Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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
                        Image(
                            painter = painterResource(id = R.drawable.logo_pcap),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.badge_pcap),
                            color = Color(0xFF0B5D34),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title & Subsidy Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.machineryName,
                    color = TextDark,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Subsidy Badge
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

            // Compact 2-column info grid (No Raqqim)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF7F8F6))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TileInfoRow(label = stringResource(id = R.string.label_farmer), value = booking.farmerName)
                if (booking.farmerPhone.isNotBlank()) {
                    TileInfoRow(label = "فون", value = booking.farmerPhone)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TileInfoChip(
                        label = "تاریخ",
                        value = booking.bookingDate,
                        modifier = Modifier.weight(1f)
                    )
                    booking.acres?.let {
                        TileInfoChip(
                            label = "رقبہ",
                            value = "${formatNumber(it)} ایکڑ",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TileInfoChip(
                        label = "وقت",
                        value = "${booking.durationHours} گھنٹے",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TileInfoRow(label = "جگہ", value = booking.locationUr)
            }

            if (booking.status == BookingStatus.PENDING) {
                ProviderRequestActions(
                    onAcceptClick = { showAcceptDialog = true },
                    onRejectClick = { showRejectDialog = true }
                )
            }

            if (booking.status == BookingStatus.REJECTED) {
                NoticeBox(
                    title = "درخواست مسترد",
                    message = booking.rejectionReason?.takeIf { it.isNotBlank() }
                        ?: "یہ درخواست مسترد ہو چکی ہے۔",
                    color = DangerRed
                )
            }
        }
    }

    if (showAcceptDialog) {
        AgriConfirmationDialog(
            title = "درخواست منظور کریں؟",
            onDismissRequest = { showAcceptDialog = false },
            confirmButtonText = "منظور کریں",
            confirmButtonColor = AgriGreenPrimary,
            onConfirm = {
                showAcceptDialog = false
                onAcceptRequest()
            },
            dismissButtonText = "واپس"
        ) {
            Text(text = "کسان کو بتایا جائے گا کہ درخواست منظور ہو گئی ہے۔", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }

    if (showRejectDialog) {
        AgriConfirmationDialog(
            title = "درخواست مسترد کریں؟",
            onDismissRequest = { showRejectDialog = false },
            confirmButtonText = "مسترد کریں",
            confirmButtonColor = DangerRed,
            onConfirm = {
                showRejectDialog = false
                onRejectRequest(rejectReason.ifBlank { null })
            },
            dismissButtonText = "واپس"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "اگر وجہ لکھ دیں تو کسان کو آسانی سے سمجھ آ جائے گی۔", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "وجہ") },
                    placeholder = { Text(text = "مثلاً مشین دستیاب نہیں") },
                    singleLine = false,
                    minLines = 2
                )
            }
        }
    }
}

@Composable
private fun RequestNumberChip(number: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AgriGreenLight)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "درخواست نمبر $number",
            color = AgriGreenPrimary,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// Builds the spoken narration text for a booking, picking role (farmer vs provider) and status
// specific templates. Extracted out of MyBookings.kt's onSpeakClick so it can be reused verbatim
// by BookingDetailScreen (the notification deep-link target) without duplicating — and risking
// diverging from — this logic.
fun buildBookingNarrationText(
    context: Context,
    booking: Booking,
    loggedInUserId: String,
    currentUserFullName: String?
): String {
    val spokenDate = try {
        val formats = listOf("dd-MM-yyyy", "dd-MMMM-yyyy", "dd-MMM-yyyy")
        var parsedDate: java.util.Date? = null
        for (fmt in formats) {
            try {
                val d = java.text.SimpleDateFormat(fmt, java.util.Locale.US).parse(booking.bookingDate)
                if (d != null) {
                    parsedDate = d
                    break
                }
            } catch (e: Exception) {}
        }
        if (parsedDate != null) {
            java.text.SimpleDateFormat("d MMMM", java.util.Locale("ur", "PK")).format(parsedDate)
        } else {
            booking.bookingDate
        }
    } catch (e: Exception) {
        booking.bookingDate
    }
    // A single account can be the farmer on one booking and the provider on another at the
    // same time (self-listed machinery owners can also rent from others), so the narration
    // role must be decided per booking from its own ids — never from the app-wide
    // farmer/provider mode toggle, which says nothing about this specific booking.
    val isProviderViewer = booking.serviceProviderId != null &&
            loggedInUserId.toLongOrNull() == booking.serviceProviderId
    // booking.farmerId falls back to the current user's own id whenever the API doesn't send
    // serviceTakerId (see AuthRepositoryImpl.getRentalBookings), which would otherwise make a
    // provider look like "the farmer" too on their own bookings. serviceProviderId is a real,
    // reliable field, so if it identifies this viewer as the provider, they can never also be
    // the farmer here — regardless of that fallback.
    val isFarmerViewer = !isProviderViewer && loggedInUserId == booking.farmerId
    // The API only tells us the farmer's real name/identity when the current user IS that
    // farmer (see AuthRepositoryImpl.getRentalBookings) — otherwise booking.farmerName is just
    // a generic placeholder. The narration templates below already contain the word "کسان"
    // (farmer), so injecting that placeholder as the name would stutter ("کسان کاشتکار").
    // Use a blank name in that case so the sentence still reads naturally.
    val spokenFarmerName = if (isFarmerViewer) booking.farmerName else ""

    return if (isProviderViewer) {
        when (booking.rentalRequestStatus) {
            // For a PENDING request, the API's "name" field (mapped into booking.providerName
            // — see AuthRepositoryImpl.getRentalBookings) is actually the requesting farmer's
            // name in this provider-viewer context, not blank/current-user placeholder logic.
            // "جناب %4$s" is a separate address to the logged-in provider themself, so that
            // uses currentUser's real name instead, since booking.providerName can be stale.
            "PENDING" -> context.getString(
                R.string.booking_explanation_voice_narration_provider,
                booking.providerName,
                booking.machineryName,
                spokenDate,
                currentUserFullName ?: booking.providerName
            )
            // Same fix as PENDING above: %1$s ("جناب %1$s") addresses the logged-in provider
            // themself, and %2$s ("کسان %2$s") is the requesting farmer's name — which is the
            // API's "name" field, carried in booking.providerName (see AuthRepositoryImpl).
            "APPROVED" -> context.getString(
                R.string.booking_explanation_voice_narration_approved_provider,
                currentUserFullName ?: booking.providerName,
                booking.providerName
            )
            "REJECTED" -> context.getString(
                R.string.booking_explanation_voice_narration_rejected_provider,
                booking.providerName
            )
            "STARTED_FROM_FARMER_SIDE" -> context.getString(
                R.string.booking_explanation_voice_narration_started_farmer_side_provider,
                booking.providerName
            )
            "STARTED_FROM_SERVICE_PROVIDER_SIDE" -> context.getString(
                R.string.booking_explanation_voice_narration_started_provider_side_provider,
                booking.providerName
            )
            // Same fix as PENDING/APPROVED above: "جناب %1$s" addresses the logged-in
            // provider themself, so use currentUser's real name instead of the API's
            // booking.providerName, which can be stale.
            "COMPLETED" -> context.getString(
                R.string.booking_explanation_voice_narration_completed_provider,
                currentUserFullName ?: booking.providerName,
                booking.machineryName,
                spokenDate
            )
            else -> context.getString(
                R.string.booking_explanation_voice_narration_provider,
                spokenFarmerName,
                booking.machineryName,
                spokenDate,
                booking.providerName
            )
        }
    } else if (booking.rentalRequestStatus == "COMPLETED") {
        context.getString(
            R.string.booking_explanation_voice_narration_completed,
            booking.machineryName,
            spokenDate,
            booking.providerName
        )
    } else if (booking.rentalRequestStatus == "APPROVED" && isFarmerViewer) {
        context.getString(
            R.string.booking_explanation_voice_narration_approved,
            booking.farmerName,
            booking.providerName
        )
    } else if (booking.rentalRequestStatus == "STARTED_FROM_FARMER_SIDE" && isFarmerViewer) {
        context.getString(
            R.string.booking_explanation_voice_narration_started_farmer_side_farmer,
            booking.farmerName,
            booking.providerName
        )
    } else if (booking.rentalRequestStatus == "STARTED_FROM_SERVICE_PROVIDER_SIDE" && isFarmerViewer) {
        context.getString(
            R.string.booking_explanation_voice_narration_started_provider_side_farmer,
            booking.farmerName,
            booking.providerName
        )
    } else {
        context.getString(
            R.string.booking_explanation_voice_narration,
            booking.machineryName,
            booking.providerName,
            spokenDate
        )
    }
}

// Shared accept/reject outcome dialog — extracted out of MyBookings.kt so BookingDetailScreen
// (the notification deep-link target) can show the exact same feedback without duplicating it.
@Composable
fun BookingActionResultDialog(
    actionState: pk.kissanmadadgar.mobile.presentation.BookingActionResult?,
    onDismiss: () -> Unit,
    onApproveSuccess: () -> Unit = {}
) {
    actionState?.let { state ->
        val isSuccess = state is pk.kissanmadadgar.mobile.presentation.BookingActionResult.Success
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (isSuccess) "کامیابی" else "خرابی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    text = if (state is pk.kissanmadadgar.mobile.presentation.BookingActionResult.Success) state.message else (state as pk.kissanmadadgar.mobile.presentation.BookingActionResult.Failure).error,
                    fontSize = 15.sp,
                    color = Color(0xFF17251B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state is pk.kissanmadadgar.mobile.presentation.BookingActionResult.Success && state.action == "APPROVE") {
                            onApproveSuccess()
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) Color(0xFF0B5D34) else Color(0xFFD32F2F)
                    )
                ) {
                    Text("ٹھیک ہے", color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerBookingDetailBottomSheet(
    booking: Booking,
    onDismiss: () -> Unit,
    activeSpeakingAudioId: String? = null,
    onSpeakClick: (Booking) -> Unit = {},
    onAcceptRequest: (() -> Unit)? = null,
    onRejectRequest: ((String?) -> Unit)? = null,
    loggedInUserId: String = "",
    onStartFarmingActivity: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = androidx.compose.ui.platform.LocalContext.current
    val statusInfo = statusUi(booking.status)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Status Banner — same "درخواست #id" pattern as the list card (FarmerRequestCard)
            // so the sheet can be matched back to the exact booking it's showing.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusInfo.backgroundColor)
                    .padding(vertical = 12.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "درخواست #${booking.id}",
                        color = statusInfo.textColor.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = booking.rentalRequestStatusUrdu,
                        color = statusInfo.textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Machine image + Provider info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Machine image
                val imageRes = getMachineryImageRes(context, booking.machineryImageUrl, booking.machineryName)
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AgriGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.machineryName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = booking.providerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenPrimary
                    )
                }

                // TTS Speaker Button with animations
                val isSpeaking = activeSpeakingAudioId == booking.id
                val infiniteTransition = rememberInfiniteTransition(label = "pulse_bs_${booking.id}")
                val rippleScale1 by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ripple1_bs"
                )
                val rippleAlpha1 by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleAlpha1_bs"
                )
                val buttonScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isSpeaking) 1.0f else 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "buttonScale_bs"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onSpeakClick(booking) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSpeaking) {
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
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer(
                                scaleX = buttonScale,
                                scaleY = buttonScale
                            )
                            .shadow(elevation = 3.dp, shape = CircleShape)
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
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpeaking) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 3) {
                                    val barHeightScale by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 300 + (i * 100)),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "bar_bs_$i"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(14.dp * barHeightScale)
                                            .background(Color.White, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "بولیں",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF7F8F6))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Date row
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AgriGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = "تاریخ", color = TextSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = booking.bookingDate, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFECECEC)))

                // Duration row
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AgriGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = "وقت", color = TextSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${booking.durationHours} گھنٹے", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                if (booking.acres != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFECECEC)))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(AgriGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Agriculture, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = "رقبہ", color = TextSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${formatNumber(booking.acres)} ایکڑ", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFECECEC)))

                // Location row
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AgriGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = "جگہ", color = TextSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = booking.locationUr, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Rejection reason
            if (booking.status == BookingStatus.REJECTED) {
                Spacer(modifier = Modifier.height(16.dp))
                NoticeBox(
                    title = "درخواست مسترد",
                    message = booking.rejectionReason?.takeIf { it.isNotBlank() } ?: "یہ درخواست مسترد ہو چکی ہے۔",
                    color = DangerRed,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Pending guidance
            if (booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E1))
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (booking.isApprovalAllowed) "⏳ درخواست دیکھیں، پھر منظور یا مسترد کریں۔" else "⏳ درخواست بھیج دی گئی ہے۔ سروس فراہم کنندہ کی منظوری کا انتظار کریں۔",
                        color = ActionOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                }
            }

            val showFarmingButton = (booking.rentalRequestStatus == "APPROVED" ||
                    booking.rentalRequestStatus == "STARTED_FROM_FARMER_SIDE" ||
                    booking.rentalRequestStatus == "STARTED_FROM_SERVICE_PROVIDER_SIDE") &&
                    loggedInUserId == booking.farmerId
            
            if (showFarmingButton) {
                val btnText = if (booking.rentalRequestStatus == "APPROVED") {
                    "کاشتکاری شروع کریں"
                } else {
                    "کاشتکاری جاری ہے"
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onStartFarmingActivity?.invoke()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                ) {
                    Text(
                        text = btnText,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (booking.isApprovalAllowed && booking.rentalRequestStatus == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onAcceptRequest?.invoke()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                    ) {
                        Text(text = "منظور کریں", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            onRejectRequest?.invoke("Rejected by farmer")
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DangerRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Text(text = "مسترد کریں", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Phone + Call button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FAF5))
                    .border(1.dp, AgriGreenPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "سروس فراہم کنندہ", color = TextSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = booking.providerName, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = booking.providerPhone, color = AgriGreenPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                // Big Call Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(AgriGreenPrimary, Color(0xFF0B5D34))
                            )
                        )
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${booking.providerPhone}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) { e.printStackTrace() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun FarmerRequestCard(
    booking: Booking,
    modifier: Modifier = Modifier,
    requestNumber: String? = null,
    onClick: () -> Unit = {},
    activeSpeakingAudioId: String? = null,
    onSpeakClick: (Booking) -> Unit = {},
    onAcceptRequest: (() -> Unit)? = null,
    onRejectRequest: ((String?) -> Unit)? = null,
    loggedInUserId: String = "",
    onStartFarmingActivity: (() -> Unit)? = null,
    onResumeFarmingActivity: (() -> Unit)? = null,
    onScanQrClick: (() -> Unit)? = null,
    onShowQrClick: (() -> Unit)? = null,
    onStopFarmingActivity: ((localFilePath: String, lat: Double, lng: Double, accuracy: Double, speed: Double, heading: Double, altitude: Double, isMock: Boolean) -> Unit)? = null,
    onSubmitFeedback: ((rating: Int, comment: String, onResult: (Boolean, String?) -> Unit) -> Unit)? = null
) {
    val statusInfo = statusUi(booking.status)
    val context = LocalContext.current
    var showRejectDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackSubmitted by remember { mutableStateOf(false) }
    var isSubmittingFeedback by remember { mutableStateOf(false) }

    val isSelfManaged = booking.farmerPhone.replace("-", "").replace(" ", "").trim() == booking.providerPhone.replace("-", "").replace(" ", "").trim()
    val isTrackerUser = isSelfManaged || (booking.serviceProviderId?.toString() == loggedInUserId)
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0L) }
    var showCameraFrameDialog by remember { mutableStateOf(false) }
    var showStopConfirmDialog by remember { mutableStateOf(false) }

    // Narration is handled by the shared NarrationManager singleton (see onSpeakClick, passed
    // in from the parent screen) — this card no longer spins up its own TextToSpeech engine per
    // booking card, which previously meant one engine instance per visible card in the list.
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }

    LaunchedEffect(booking.id) {
        if (isTrackerUser) {
            while (true) {
                val session = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getActiveSession(context)
                if (session != null && session.bookingId == booking.id) {
                    isRecording = true
                    isPaused = session.isPaused
                    recordingSeconds = if (session.isPaused) {
                        (session.lastPauseTimeMs - session.startTimeMs - session.pausedDurationMs) / 1000
                    } else {
                        (System.currentTimeMillis() - session.startTimeMs - session.pausedDurationMs) / 1000
                    }
                } else {
                    isRecording = false
                    isPaused = false
                    recordingSeconds = 0L
                }
                delay(1000L)
            }
        }
    }

    val permissionLauncherForInline = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCameraFrameDialog = true
        }
    }

    val isSpeaking = activeSpeakingAudioId == booking.id
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${booking.id}")
    
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )
    
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking) 1.0f else 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // ── Status color banner ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                statusInfo.textColor,
                                statusInfo.textColor.copy(alpha = 0.75f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Request number (optional)
                    requestNumber?.let {
                        Text(
                            text = "درخواست #$it",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } ?: Spacer(modifier = Modifier.size(0.dp))

                    Text(
                        text = booking.rentalRequestStatusUrdu,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ── Card body ────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Machine image + provider name row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Machine image
                    val imageRes = getMachineryImageRes(context, booking.machineryImageUrl, booking.machineryName)
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        // Provider name — large and bold
                        Text(
                            text = booking.providerName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            lineHeight = 26.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Machine name — secondary
                        Text(
                            text = booking.machineryName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSoft,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    // TTS Audio Speaker Button with ripples & animations (matching welcome button styling)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onSpeakClick(booking) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpeaking) {
                            // Ripples
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
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .graphicsLayer(
                                    scaleX = buttonScale,
                                    scaleY = buttonScale
                                )
                                .shadow(elevation = 3.dp, shape = CircleShape)
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
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSpeaking) {
                                // Equalizer lines animation
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until 3) {
                                        val barHeightScale by infiniteTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(durationMillis = 300 + (i * 100)),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "bar_$i"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(14.dp * barHeightScale)
                                                .background(Color.White, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = "بولیں",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEEEEEE))
                )

                // Date and Phone in the same horizontal row (Only Icons + Values)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AgriGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "\u200E" + booking.bookingDate,
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Phone/Call
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL
                                    ).apply {
                                        data = android.net.Uri.parse("tel:${booking.providerPhone}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AgriGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = booking.providerPhone,
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // District / Location (from API's rentalDistrict)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AgriGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AgriGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = booking.locationUr,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                val isProviderUser = booking.serviceProviderId != null &&
                        loggedInUserId.toLongOrNull() == booking.serviceProviderId

                val hasInternet = isNetworkAvailable(context)

                // booking.farmerId falls back to a session-derived id when the API omits serviceTakerId,
                // which doesn't always string-match loggedInUserId (different id sources). For a
                // self-managed booking (same person as farmer and provider), fall back to the more
                // reliable isProviderUser + isSelfManaged check so the start button still shows.
                val showFarmingButton = if (isSelfManaged) {
                    booking.rentalRequestStatus == "APPROVED" && (loggedInUserId == booking.farmerId || (isProviderUser && hasInternet))
                } else {
                    // The taker's APPROVED-stage start doesn't need internet at click time —
                    // onStartFarmingActivity routes through the camera capture flow which enqueues
                    // the start locally (see enqueueFarmingStart) and syncs once connectivity
                    // returns. Gating this on hasInternet left an offline taker with no way to start
                    // at all. The provider's STARTED_FROM_FARMER_SIDE confirmation stays
                    // internet-gated since it already has an offline fallback (showProviderScanQrButton).
                    (booking.rentalRequestStatus == "APPROVED" && loggedInUserId == booking.farmerId) ||
                        (hasInternet && booking.rentalRequestStatus == "STARTED_FROM_FARMER_SIDE" && isProviderUser)
                }
 
                // APPROVED must always offer the provider a way to scan the taker's QR, regardless of
                // the provider's own connectivity — the taker may be the one without internet, so
                // gating this on the provider's hasInternet could hide their only path to confirm.
                // STARTED_FROM_FARMER_SIDE keeps the existing !hasInternet gate (unchanged).
                val showProviderScanQrButton = isProviderUser &&
                        booking.serviceProviderId != booking.serviceTakerId &&
                        (booking.rentalRequestStatus == "APPROVED" ||
                            (booking.rentalRequestStatus == "STARTED_FROM_FARMER_SIDE" && !hasInternet))

                val isSelfManagedRunning = isRecording

                // The server can say tracking is already active (STARTED_FROM_SERVICE_PROVIDER_SIDE)
                // while no local session actually exists — e.g. the app was killed, the device
                // rebooted, or the foreground service got stopped by the OS. Previously this rendered
                // the same running-timer card (frozen at 00:00) with no way to recover. Detect that
                // mismatch and offer a Resume button instead of leaving the user stuck.
                val isStuckSelfManagedSession = !isRecording &&
                        booking.rentalRequestStatus == "STARTED_FROM_SERVICE_PROVIDER_SIDE" &&
                        isProviderUser

                if (isSelfManagedRunning) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "کاشتکاری کا جاری وقت" + if (isPaused) " (وقفہ ہے)" else "",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = formatFarmingDuration(recordingSeconds),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaused) Color(0xFFF57C00) else Color(0xFFD32F2F)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isPaused) {
                                    Button(
                                        onClick = {
                                            pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.resumeTracking(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("جاری رکھیں", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.pauseTracking(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Pause,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("وقفہ کریں", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        showStopConfirmDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("سروس روکیں", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else if (isStuckSelfManagedSession) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "سروس میں تعطل آگیا، دوبارہ شروع کریں",
                                color = Color(0xFFF57C00),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { onResumeFarmingActivity?.invoke() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "دوبارہ شروع کریں",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    val hasInternet = isNetworkAvailable(context)
                    val serviceShuruuLabel = stringResource(id = pk.kissanmadadgar.mobile.R.string.service_shuruu_karain)
                    val scanKarainLabel = stringResource(id = pk.kissanmadadgar.mobile.R.string.scan_karain)
                    
                    if (showFarmingButton) {
                        val btnText = if (booking.rentalRequestStatus == "APPROVED") {
                            serviceShuruuLabel
                        } else {
                            if (hasInternet) serviceShuruuLabel else scanKarainLabel
                        }
                        
                        val showQrIcon = !hasInternet && booking.rentalRequestStatus != "APPROVED"
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (showQrIcon) {
                                    onScanQrClick?.invoke()
                                } else {
                                    onStartFarmingActivity?.invoke()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            if (showQrIcon) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = btnText,
                                color = Color.White,
                                fontSize = if (btnText.length > 25) 11.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = if (btnText.length > 25) 13.sp else 16.sp
                            )
                        }
                    }

                    if (showProviderScanQrButton) {
                        val btnText = if (hasInternet) "کیو آر کوڈ اسکین کریں" else scanKarainLabel
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onScanQrClick?.invoke() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = btnText,
                                color = Color.White,
                                fontSize = if (btnText.length > 25) 11.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = if (btnText.length > 25) 13.sp else 16.sp
                            )
                        }
                    }
                }

                // QR code is only for the taker to display once they've started their side
                // (STARTED_FROM_FARMER_SIDE), so the provider can scan it to confirm even without
                // internet. It must NOT show while still APPROVED — that state only ever shows the
                // "سروس کا آغاز کریں" button (see showFarmingButton above), regardless of internet.
                // Uses isProviderUser (serviceProviderId-based) rather than the farmerId string
                // match — farmerId falls back to a session-derived id when the API omits
                // serviceTakerId, which can otherwise wrongly hide this button for a real taker.
                val showQrButton = !isSelfManaged && !isProviderUser &&
                    booking.rentalRequestStatus == "STARTED_FROM_FARMER_SIDE"

                if (showQrButton) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onShowQrClick?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "کیو آر کوڈ",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (booking.isApprovalAllowed && booking.rentalRequestStatus == "PENDING") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onAcceptRequest?.invoke() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                        ) {
                            Text(
                                text = "منظور کریں",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, DangerRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                        ) {
                            Text(
                                text = "مسترد کریں",
                                color = DangerRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Feedback is collected from the service taker only (the one who received the
                // service) once the booking is COMPLETED. feedbackSubmitted only flips once
                // POST api/android/rental-booking/feedback actually succeeds (see onSubmit
                // below) — a failed submission leaves the button in place so the user knows to
                // retry, rather than falsely thanking them for feedback that never saved.
                // booking.isRatingDone means the API already has a rating on file for this
                // booking (e.g. submitted from another device/session earlier) — nothing about
                // feedback is shown at all in that case, not even the "thanks" banner.
                if (booking.rentalRequestStatus == "COMPLETED" && loggedInUserId == booking.farmerId && !booking.isRatingDone) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (feedbackSubmitted) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "آپ کی فیڈبیک موصول ہو گئی، شکریہ!",
                                color = Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showFeedbackDialog = true },
                            enabled = !isSubmittingFeedback,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB300)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF57C00))
                        ) {
                            if (isSubmittingFeedback) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFF57C00))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اپنی فیڈبیک دیں",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (showFeedbackDialog) {
                FeedbackDialog(
                    onDismissRequest = { showFeedbackDialog = false },
                    onSubmit = { rating, comment ->
                        showFeedbackDialog = false
                        val submit = onSubmitFeedback
                        if (submit == null) {
                            // No handler wired up — same as before, treat it as UI-only.
                            feedbackSubmitted = true
                            return@FeedbackDialog
                        }
                        isSubmittingFeedback = true
                        submit(rating, comment) { success, message ->
                            isSubmittingFeedback = false
                            if (success) {
                                feedbackSubmitted = true
                                android.widget.Toast.makeText(context, "شکریہ! آپ کی رائے موصول ہو گئی ہے۔", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    message ?: "فیڈبیک جمع کروانے میں خرابی، دوبارہ کوشش کریں۔",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            }

            var rejectionComment by remember { mutableStateOf("") }
            if (showRejectDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showRejectDialog = false },
                    title = {
                        Text(
                            text = "مسترد کرنے کی وجہ لکھیں",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = rejectionComment,
                            onValueChange = { rejectionComment = it },
                            label = { Text("تبصرہ / وجہ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onRejectRequest?.invoke(rejectionComment)
                                showRejectDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Text("مسترد کریں", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRejectDialog = false }) {
                            Text("منسوخ کریں", color = Color.Gray)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Rejection notice
            if (booking.status == BookingStatus.REJECTED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "❌ " + (booking.rejectionReason?.takeIf { it.isNotBlank() }
                            ?: "یہ درخواست مسترد ہو چکی ہے۔"),
                        color = DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pending guidance hint
            if (booking.status == BookingStatus.PENDING) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF8E1))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (booking.isApprovalAllowed) "⏳ منظور یا مسترد کریں" else "⏳ منظوری کا انتظار کریں",
                        color = ActionOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showStopConfirmDialog) {
                pk.kissanmadadgar.mobile.core.components.AgriConfirmationDialog(
                    title = "سروس مکمل کریں",
                    onDismissRequest = { showStopConfirmDialog = false },
                    confirmButtonText = "جی ہاں",
                    confirmButtonColor = Color(0xFFD32F2F),
                    onConfirm = {
                        showStopConfirmDialog = false
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            showCameraFrameDialog = true
                        } else {
                            permissionLauncherForInline.launch(Manifest.permission.CAMERA)
                        }
                    },
                    dismissButtonText = "منسوخ"
                ) {
                    Text(
                        text = "کیا آپ واقعی سروس بند کر کے آخری تصویر لینا چاہتے ہیں؟",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            if (showCameraFrameDialog) {
                CameraFrameDialog(
                    onDismiss = { showCameraFrameDialog = false },
                    onPictureCaptured = { bitmap ->
                        showCameraFrameDialog = false

                        // Grab the last known metric and end the tracking session/foreground
                        // service right away — the user has already confirmed and taken the
                        // final photo, so the on-screen timer and location tracking must stop
                        // now regardless of how long the background upload to the server takes.
                        val lastLog = pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.getTrackLogs(context, booking.id).lastOrNull()
                        pk.kissanmadadgar.mobile.data.local.FarmingTrackingService.stopTracking(context, booking.id)

                        val dir = java.io.File(context.filesDir, "uploads")
                        if (!dir.exists()) dir.mkdirs()
                        val imageFile = java.io.File(dir, "end_${booking.id}_${System.currentTimeMillis()}.jpg")
                        try {
                            java.io.FileOutputStream(imageFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }

                            val lat = lastLog?.latitude ?: 0.0
                            val lng = lastLog?.longitude ?: 0.0
                            val altitude = lastLog?.altitude ?: 0.0
                            val speed = lastLog?.speed?.toDouble() ?: 0.0
                            val bearing = lastLog?.bearing?.toDouble() ?: 0.0
                            val isMock = lastLog?.isMock ?: false

                            onStopFarmingActivity?.invoke(
                                imageFile.absolutePath,
                                lat,
                                lng,
                                5.0,
                                speed,
                                bearing,
                                altitude,
                                isMock
                            )
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "تصویر محفوظ کرنے میں ناکامی: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    userName = booking.providerName,
                    titleText = stringResource(id = R.string.camera_frame_title_complete),
                    narrationTemplateRes = R.string.camera_frame_voice_narration_complete
                )
            }
        }
    }
}



@Composable
fun BookingLifecycleCard(
    booking: Booking,
    currentRole: UserRole,
    onAcceptRequest: () -> Unit = {},
    onRejectRequest: (String?) -> Unit = {},
    onUploadStep: (BookingPhotoStep) -> Unit = {}
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    val accentColor = tileAccentColor(booking.status)
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(width = 3.dp, color = accentColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                BookingStatusBadge(status = booking.status, label = booking.rentalRequestStatusUrdu)
            }

            // PCAP Badge (Visible to both roles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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
                        Image(
                            painter = painterResource(id = R.drawable.logo_pcap),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پنجاب کلین ائیر پروگرام (PCAP)",
                            color = Color(0xFF0B5D34),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title & Subsidy Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.machineryName,
                    color = TextDark,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Subsidy Badge
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

            if (currentRole == UserRole.FARMER) {
                // Service Provider Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "سروس فراہم کنندہ: ",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = booking.providerName,
                                fontSize = 16.sp,
                                color = AgriGreenPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "فون: ${booking.providerPhone}",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Phone Call Icon Button
                    IconButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${booking.providerPhone}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AgriGreenPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = AgriGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compact 2-column info grid (No Raqqim)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF7F8F6))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Relevant Person Info
                if (currentRole == UserRole.PROVIDER) {
                    TileInfoRow(label = stringResource(id = R.string.label_farmer), value = booking.farmerName)
                    if (booking.farmerPhone.isNotBlank()) {
                        TileInfoRow(label = "فون", value = booking.farmerPhone)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEAEAEA)))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TileInfoChip(
                        label = "تاریخ",
                        value = booking.bookingDate,
                        modifier = Modifier.weight(1f)
                    )
                    TileInfoChip(
                        label = "وقت",
                        value = "${booking.durationHours} گھنٹے",
                        modifier = Modifier.weight(1f)
                    )
                }
                booking.acres?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TileInfoChip(
                            label = "رقبہ",
                            value = "${formatNumber(it)} ایکڑ",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                TileInfoRow(label = "جگہ", value = booking.locationUr)
            }

            // Notice if rejected
            if (booking.status == BookingStatus.REJECTED) {
                NoticeBox(
                    title = "درخواست مسترد",
                    message = booking.rejectionReason?.takeIf { it.isNotBlank() }
                        ?: "یہ درخواست مسترد ہو چکی ہے۔",
                    color = DangerRed
                )
            }

            // Instruction Text
            val nextAction = nextActionText(booking = booking, currentRole = currentRole, context = context)
            if (nextAction.isNotBlank() && booking.status != BookingStatus.COMPLETED && booking.status != BookingStatus.REJECTED) {
                Text(
                    text = "ہدایت: $nextAction",
                    color = AgriGreenPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Actions
            if (booking.status == BookingStatus.PENDING && currentRole == UserRole.PROVIDER) {
                ProviderDecisionButtons(
                    onAcceptClick = { showAcceptDialog = true },
                    onRejectClick = { showRejectDialog = true }
                )
            } else if (booking.status == BookingStatus.ACCEPTED && currentRole == UserRole.PROVIDER) {
                if (booking.lifecyclePhotos.none { it.step == BookingPhotoStep.SERVICE_ACQUIRED }) {
                    UploadActionButton(text = "مشین پہنچ گئی (تصویر)", onClick = { onUploadStep(BookingPhotoStep.SERVICE_ACQUIRED) })
                }
            } else if (booking.status == BookingStatus.ACTIVE && currentRole == UserRole.PROVIDER) {
                if (booking.lifecyclePhotos.none { it.step == BookingPhotoStep.WORK_COMPLETED }) {
                    UploadActionButton(text = "کام مکمل ہو گیا (تصویر)", onClick = { onUploadStep(BookingPhotoStep.WORK_COMPLETED) })
                }
            }

            // Uploaded Photos
            if (booking.lifecyclePhotos.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    booking.lifecyclePhotos.forEach { photo ->
                        UploadedPhotoBox(photo = photo)
                    }
                }
            }
        }
    }

    if (showAcceptDialog) {
        AgriConfirmationDialog(
            title = "درخواست منظور کریں؟",
            onDismissRequest = { showAcceptDialog = false },
            confirmButtonText = "منظور کریں",
            confirmButtonColor = AgriGreenPrimary,
            onConfirm = {
                showAcceptDialog = false
                onAcceptRequest()
            },
            dismissButtonText = "واپس"
        ) {
            Text(text = "منظوری کے بعد کام کے مراحل شروع ہو جائیں گے۔", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }

    if (showRejectDialog) {
        AgriConfirmationDialog(
            title = "درخواست مسترد کریں؟",
            onDismissRequest = { showRejectDialog = false },
            confirmButtonText = "مسترد کریں",
            confirmButtonColor = DangerRed,
            onConfirm = {
                showRejectDialog = false
                onRejectRequest(rejectReason.ifBlank { null })
            },
            dismissButtonText = "واپس"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "اگر وجہ لکھ دیں تو کسان کو سمجھ آ جائے گی۔", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "وجہ") },
                    placeholder = { Text(text = "مثلاً مشین دستیاب نہیں") },
                    singleLine = false,
                    minLines = 2
                )
            }
        }
    }
}

@Composable
private fun UploadActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AgriGreenPrimary,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BookingStatusBadge(status: BookingStatus, label: String) {
    val ui = statusUi(status)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ui.backgroundColor)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = ui.textColor,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ServiceLifecycleStepper(
    booking: Booking,
    currentRole: UserRole,
    onUploadStep: (BookingPhotoStep) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "کام کا آسان راستہ",
            color = TextDark,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold
        )

        lifecycleSteps().forEachIndexed { index, stepUi ->
            val photo = booking.lifecyclePhotos.firstOrNull { it.step == stepUi.step }
            val completed = photo != null
            val unlocked = isStepUnlocked(booking = booking, step = stepUi.step)
            val canUpload = unlocked &&
                !completed &&
                booking.status !in listOf(BookingStatus.PENDING, BookingStatus.REJECTED, BookingStatus.CANCELLED) &&
                currentRole == stepUi.role

            StepCard(
                number = index + 1,
                stepUi = stepUi,
                photo = photo,
                completed = completed,
                unlocked = unlocked,
                canUpload = canUpload,
                bookingStatus = booking.status,
                currentRole = currentRole,
                onUploadStep = onUploadStep
            )
        }
    }
}

@Composable
fun PaymentSummaryCard(totalAmount: Double) {
    val subsidyAmount = totalAmount * 0.4
    val farmerAmount = totalAmount - subsidyAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFC8D6C8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "رقم کا حساب",
                color = TextDark,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
            AmountRow(label = "کل رقم", amount = totalAmount, strong = true)
            AmountRow(label = "حکومتی مدد", amount = subsidyAmount, color = DoneGreen)
            AmountRow(label = "کسان کی رقم", amount = farmerAmount, color = ActionOrange)
        }
    }
}

@Composable
private fun NextActionBox(
    booking: Booking,
    currentRole: UserRole
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.next_action_title),
                color = AgriGreenPrimary,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = nextActionText(booking = booking, currentRole = currentRole, context = context),
                color = TextDark,
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProviderDecisionButtons(
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAcceptClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgriGreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "درخواست منظور کریں",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        OutlinedButton(
            onClick = onRejectClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, DangerRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
        ) {
            Text(
                text = "درخواست مسترد کریں",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProviderRequestActions(
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onRejectClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DangerRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
        ) {
            Text(
                text = "مسترد کریں",
                color = DangerRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onAcceptClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgriGreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "منظور کریں",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImportantDetailsCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "اہم معلومات",
                color = TextDark,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
            SimpleInfoRow(label = stringResource(id = R.string.label_farmer), value = booking.farmerName)
            if (booking.farmerPhone.isNotBlank()) {
                SimpleInfoRow(label = "فون", value = booking.farmerPhone)
            }
            SimpleInfoRow(label = "تاریخ", value = booking.bookingDate)
            SimpleInfoRow(label = "جگہ", value = booking.locationUr)
            booking.acres?.let { SimpleInfoRow(label = "رقبہ", value = "${formatNumber(it)} ایکڑ") }
            SimpleInfoRow(label = "وقت", value = "${booking.durationHours} گھنٹے")
            SimpleInfoRow(label = "رقم", value = "${formatNumber(booking.totalPrice)} روپے")
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    stepUi: StepUi,
    photo: BookingLifecyclePhoto?,
    completed: Boolean,
    unlocked: Boolean,
    canUpload: Boolean,
    bookingStatus: BookingStatus,
    currentRole: UserRole,
    onUploadStep: (BookingPhotoStep) -> Unit
) {
    val context = LocalContext.current
    val circleColor = when {
        completed -> DoneGreen
        unlocked -> ActionOrange
        else -> DisabledGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                completed -> Color(0xFFB8D8BA)
                unlocked -> Color(0xFFFFD49A)
                else -> BorderGray
            }
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    } else if (unlocked) {
                        Text(
                            text = number.toString(),
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$number۔ ${stringResource(id = stepUi.titleResId)}",
                        color = TextDark,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.responsibility_format, roleText(stepUi.role, context)),
                        color = TextSoft,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                StepStateChip(
                    completed = completed,
                    unlocked = unlocked,
                    bookingStatus = bookingStatus
                )
            }

            Text(
                text = stepHelpText(
                    stepUi = stepUi,
                    photo = photo,
                    completed = completed,
                    unlocked = unlocked,
                    bookingStatus = bookingStatus,
                    currentRole = currentRole,
                    context = context
                ),
                color = TextSoft,
                fontSize = 16.sp,
                lineHeight = 23.sp
            )

            if (photo != null) {
                UploadedPhotoBox(photo = photo)
            }

            if (canUpload) {
                Button(
                    onClick = { onUploadStep(stepUi.step) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "تصویر شامل کریں",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StepStateChip(
    completed: Boolean,
    unlocked: Boolean,
    bookingStatus: BookingStatus
) {
    val label: String
    val textColor: Color
    val backgroundColor: Color

    when {
        completed -> {
            label = stringResource(id = R.string.state_done)
            textColor = DoneGreen
            backgroundColor = AgriGreenLight
        }
        bookingStatus == BookingStatus.REJECTED -> {
            label = stringResource(id = R.string.state_rejected)
            textColor = DangerRed
            backgroundColor = Color(0xFFFFEBEE)
        }
        bookingStatus == BookingStatus.CANCELLED -> {
            label = stringResource(id = R.string.state_cancelled)
            textColor = DisabledGray
            backgroundColor = Color(0xFFF0F0F0)
        }
        bookingStatus == BookingStatus.PENDING -> {
            label = stringResource(id = R.string.state_waiting)
            textColor = ActionOrange
            backgroundColor = Color(0xFFFFF3E0)
        }
        unlocked -> {
            label = stringResource(id = R.string.state_now)
            textColor = ActionOrange
            backgroundColor = Color(0xFFFFF3E0)
        }
        else -> {
            label = stringResource(id = R.string.state_closed)
            textColor = DisabledGray
            backgroundColor = Color(0xFFF0F0F0)
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UploadedPhotoBox(photo: BookingLifecyclePhoto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F5E9))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "تصویر شامل ہو گئی",
                color = DoneGreen,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = photo.imageLabelUr,
                color = TextSoft,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Text(
                text = formatDate(photo.uploadedAt),
                color = TextSoft,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun NoticeBox(
    title: String,
    message: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = TextDark,
                fontSize = 16.sp,
                lineHeight = 23.sp
            )
        }
    }
}

@Composable
private fun SimpleRequestMessage(status: BookingStatus) {
    val message = when (status) {
        BookingStatus.PENDING -> "کسان جواب کا انتظار کر رہا ہے۔ نیچے سے منظور یا مسترد کریں۔"
        BookingStatus.ACCEPTED -> "یہ درخواست منظور ہو چکی ہے۔"
        BookingStatus.ACTIVE -> "اس درخواست پر کام جاری ہے۔"
        BookingStatus.COMPLETED -> "یہ کام مکمل ہو چکا ہے۔"
        BookingStatus.REJECTED -> "یہ درخواست مسترد ہو چکی ہے۔"
        BookingStatus.CANCELLED -> "یہ درخواست منسوخ ہو چکی ہے۔"
    }
    val backgroundColor = when (status) {
        BookingStatus.PENDING -> Color(0xFFFFF8E8)
        BookingStatus.REJECTED -> Color(0xFFFFF1F1)
        BookingStatus.CANCELLED -> Color(0xFFF4F4F4)
        else -> AgriGreenLight
    }
    val accentColor = when (status) {
        BookingStatus.PENDING -> ActionOrange
        BookingStatus.REJECTED -> DangerRed
        BookingStatus.CANCELLED -> DisabledGray
        else -> AgriGreenPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = message,
                color = TextDark,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RequestDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF0F0F0))
    )
}

@Composable
private fun LargeRequestInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F7F4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AgriGreenPrimary,
                modifier = Modifier.size(21.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                color = TextSoft,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = TextDark,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SimpleInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = TextSoft,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.34f)
        )
        Text(
            text = value,
            color = TextDark,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.66f)
        )
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: Double,
    color: Color = TextDark,
    strong: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (strong) TextDark else TextSoft,
            fontSize = if (strong) 17.sp else 15.sp,
            lineHeight = 22.sp,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.SemiBold
        )
        Text(
            text = "${formatNumber(amount)} روپے",
            color = color,
            fontSize = if (strong) 18.sp else 16.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun lifecycleSteps(): List<StepUi> = listOf(
    StepUi(
        step = BookingPhotoStep.SERVICE_ACQUIRED,
        titleResId = R.string.step_service_acquired,
        role = UserRole.FARMER
    ),
    StepUi(
        step = BookingPhotoStep.SUBSIDY_STARTED,
        titleResId = R.string.step_subsidy_started,
        role = UserRole.PROVIDER
    ),
    StepUi(
        step = BookingPhotoStep.WORK_COMPLETED,
        titleResId = R.string.step_work_completed,
        role = UserRole.PROVIDER
    ),
    StepUi(
        step = BookingPhotoStep.FARMER_CONFIRMATION,
        titleResId = R.string.step_farmer_confirmation,
        role = UserRole.FARMER
    )
)
private fun nextActionText(
    booking: Booking,
    currentRole: UserRole,
    context: Context
): String {
    if (booking.status == BookingStatus.PENDING) {
        return if (currentRole == UserRole.PROVIDER) {
            context.getString(R.string.action_pending_provider)
        } else {
            context.getString(R.string.action_pending_farmer)
        }
    }

    if (booking.status == BookingStatus.REJECTED) {
        return context.getString(R.string.action_rejected)
    }

    if (booking.status == BookingStatus.CANCELLED) {
        return context.getString(R.string.action_cancelled)
    }

    if (booking.status == BookingStatus.COMPLETED) {
        return context.getString(R.string.action_completed)
    }

    val nextStep = lifecycleSteps().firstOrNull { step ->
        booking.lifecyclePhotos.none { it.step == step.step } && isStepUnlocked(booking, step.step)
    }

    return if (nextStep == null) {
        context.getString(R.string.action_all_steps_done)
    } else if (currentRole == nextStep.role) {
        context.getString(R.string.action_your_turn_format, context.getString(nextStep.titleResId))
    } else {
        context.getString(R.string.action_other_turn_format, roleText(nextStep.role, context), context.getString(nextStep.titleResId))
    }
}

private fun stepHelpText(
    stepUi: StepUi,
    photo: BookingLifecyclePhoto?,
    completed: Boolean,
    unlocked: Boolean,
    bookingStatus: BookingStatus,
    currentRole: UserRole,
    context: Context
): String {
    if (completed && photo != null) {
        return context.getString(R.string.help_step_completed)
    }

    return when {
        bookingStatus == BookingStatus.PENDING -> context.getString(R.string.help_pending)
        bookingStatus == BookingStatus.REJECTED -> context.getString(R.string.help_rejected)
        bookingStatus == BookingStatus.CANCELLED -> context.getString(R.string.help_cancelled)
        !unlocked -> context.getString(R.string.help_locked)
        currentRole == stepUi.role -> context.getString(R.string.help_your_turn)
        else -> context.getString(R.string.help_other_turn_format, roleText(stepUi.role, context))
    }
}

private fun isStepUnlocked(
    booking: Booking,
    step: BookingPhotoStep
): Boolean {
    val completedSteps = booking.lifecyclePhotos.map { it.step }.toSet()
    return when (step) {
        BookingPhotoStep.SERVICE_ACQUIRED -> booking.status in listOf(
            BookingStatus.ACCEPTED,
            BookingStatus.ACTIVE,
            BookingStatus.COMPLETED
        ) || BookingPhotoStep.SERVICE_ACQUIRED in completedSteps

        BookingPhotoStep.SUBSIDY_STARTED -> BookingPhotoStep.SERVICE_ACQUIRED in completedSteps
        BookingPhotoStep.WORK_COMPLETED -> BookingPhotoStep.SUBSIDY_STARTED in completedSteps
        BookingPhotoStep.FARMER_CONFIRMATION -> BookingPhotoStep.WORK_COMPLETED in completedSteps
    }
}

private fun statusUi(status: BookingStatus): StatusUi = when (status) {
    BookingStatus.PENDING -> StatusUi("جدید بکنگ", ActionOrange, Color(0xFFFFF3E0))
    BookingStatus.ACCEPTED -> StatusUi("منظور", Color(0xFF1976D2), Color(0xFFE3F2FD))
    BookingStatus.ACTIVE -> StatusUi("کام جاری", Color(0xFF1976D2), Color(0xFFE3F2FD))
    BookingStatus.COMPLETED -> StatusUi("مکمل", DoneGreen, AgriGreenLight)
    BookingStatus.REJECTED -> StatusUi("مسترد", DangerRed, Color(0xFFFFEBEE))
    BookingStatus.CANCELLED -> StatusUi("منسوخ", DisabledGray, Color(0xFFF0F0F0))
}

private fun getMachineryImageRes(context: Context, imageUrl: String?, machineryName: String): Int {
    if (!imageUrl.isNullOrBlank()) {
        val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
        if (resId != 0) return resId
    }
    val name = machineryName.lowercase()
    return when {
        name.contains("سیڈر") || name.contains("seeder") -> R.drawable.super_seeder_custom
        name.contains("بیلر") || name.contains("baler") || name.contains("bailer") -> R.drawable.bailer
        name.contains("ہارویسٹر") || name.contains("harvester") -> R.drawable.harvester
        else -> R.drawable.super_seeder_custom
    }
}


private fun roleText(role: UserRole, context: Context): String = when (role) {
    UserRole.FARMER -> context.getString(R.string.role_farmer_simple)
    UserRole.PROVIDER -> context.getString(R.string.role_provider_simple)
    UserRole.ADMIN -> context.getString(R.string.role_admin_simple)
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(timestamp))
}

private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        "%.1f".format(Locale.US, value)
    }
}

private fun tileAccentColor(status: BookingStatus): Color = when (status) {
    BookingStatus.PENDING -> ActionOrange
    BookingStatus.ACCEPTED -> AgriGreenPrimary
    BookingStatus.ACTIVE -> AgriGreenPrimary
    BookingStatus.COMPLETED -> DoneGreen
    BookingStatus.REJECTED -> DangerRed
    BookingStatus.CANCELLED -> DisabledGray
}

@Composable
private fun TileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = TextSoft,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 1.dp)
        )
        Text(
            text = value,
            color = TextDark,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TileInfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = TextSoft,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = TextDark,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

enum class CameraFlowState {
    NONE, VIEWFINDER, PREVIEW, UPLOADING, DONE
}

@Composable
fun BookingDetailOverlay(
    booking: Booking,
    currentRole: UserRole,
    onBack: () -> Unit,
    onAcceptRequest: () -> Unit,
    onRejectRequest: (String?) -> Unit,
    onUploadStep: (BookingPhotoStep) -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }

    var activeCameraStep by remember { mutableStateOf<BookingPhotoStep?>(null) }
    var cameraState by remember { mutableStateOf(CameraFlowState.NONE) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var stepBitmaps by remember { mutableStateOf(mapOf<BookingPhotoStep, Bitmap>()) }

    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            cameraState = CameraFlowState.PREVIEW
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High-contrast Header Design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AgriGreenPrimary)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = stringResource(id = R.string.btn_back),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.booking_details_title),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val accentColor = tileAccentColor(booking.status)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(width = 3.dp, color = accentColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header Row: Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                             BookingStatusBadge(status = booking.status, label = booking.rentalRequestStatusUrdu)
                        }

                        // PCAP Badge (Visible to both roles)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
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
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_pcap),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(id = R.string.badge_pcap),
                                        color = Color(0xFF0B5D34),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (currentRole == UserRole.FARMER) {
                            // Provider details Row with Subsidy Badge on the right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Owner Name
                                    Text(
                                        text = booking.providerName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AgriGreenPrimary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Machine Title
                                    val imageRes = getMachineryImageRes(context, booking.machineryImageUrl, booking.machineryName)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = booking.machineryName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Location
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_location_round),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = booking.locationUr,
                                            color = Color.DarkGray,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Phone number and Call Icon row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_phone_round),
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = booking.providerPhone,
                                                color = Color.DarkGray,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Phone Call Icon Button (consistent solid circle style, placed after phone details)
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(AgriGreenPrimary)
                                                .clickable {
                                                    try {
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                            data = android.net.Uri.parse("tel:${booking.providerPhone}")
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Call",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Subsidy Badge
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
                        } else {
                            // If provider, they see machinery name and the subsidy badge on the right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = booking.machineryName,
                                    color = TextDark,
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Subsidy Badge
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

                        // Compact 2-column info grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF7F8F6))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Relevant Person Info
                            if (currentRole == UserRole.PROVIDER) {
                                TileInfoRow(label = stringResource(id = R.string.label_farmer), value = booking.farmerName)
                                if (booking.farmerPhone.isNotBlank()) {
                                    TileInfoRow(label = stringResource(id = R.string.label_phone), value = booking.farmerPhone)
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEAEAEA)))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TileInfoChip(
                                    label = stringResource(id = R.string.label_date),
                                    value = booking.bookingDate,
                                    modifier = Modifier.weight(1f)
                                )
                                TileInfoChip(
                                    label = stringResource(id = R.string.label_time),
                                    value = stringResource(id = R.string.duration_hours_value, booking.durationHours),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TileInfoChip(
                                    label = stringResource(id = R.string.label_acres),
                                    value = booking.acres?.let { stringResource(id = R.string.acres_format, formatNumber(it)) } ?: "--",
                                    modifier = Modifier.weight(1f)
                                )
                                TileInfoChip(
                                    label = stringResource(id = R.string.label_request_date),
                                    value = booking.bookingDate,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (currentRole == UserRole.PROVIDER) {
                                TileInfoRow(label = stringResource(id = R.string.label_place), value = booking.locationUr)
                            }
                        }

                        // Notice if rejected
                        if (booking.status == BookingStatus.REJECTED) {
                            NoticeBox(
                                title = stringResource(id = R.string.request_rejected_title),
                                message = booking.rejectionReason?.takeIf { it.isNotBlank() }
                                    ?: stringResource(id = R.string.request_rejected_desc),
                                color = DangerRed
                            )
                        }

                        // "Ab mujhe kya karna hai?" section - Sleek, simple aesthetic text, NO icons!
                        if (booking.status != BookingStatus.COMPLETED && booking.status != BookingStatus.REJECTED && booking.status != BookingStatus.CANCELLED) {
                            val nextActionText = nextActionText(booking = booking, currentRole = currentRole, context = context)
                            if (nextActionText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AgriGreenLight)
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = stringResource(id = R.string.what_should_i_do_title),
                                            color = AgriGreenPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = nextActionText,
                                            color = TextDark,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Actions (Accept/Reject, upload photo, etc.)
                        if (booking.rentalRequestStatus == "PENDING" && (currentRole == UserRole.PROVIDER || booking.isApprovalAllowed)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAcceptDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                                ) {
                                    Text(
                                        text = "درخواست منظور کریں",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showRejectDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, DangerRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                                ) {
                                    Text(
                                        text = "درخواست مسترد کریں",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            val nextStepForAction = lifecycleSteps().firstOrNull { step ->
                                booking.lifecyclePhotos.none { it.step == step.step } && isStepUnlocked(booking, step.step)
                            }

                            if (nextStepForAction != null && currentRole == nextStepForAction.role) {
                                val buttonText = when (nextStepForAction.step) {
                                    BookingPhotoStep.SERVICE_ACQUIRED -> "مشین پہنچنے کی تصویر کھینچیں"
                                    BookingPhotoStep.SUBSIDY_STARTED -> "کام شروع ہونے کی تصویر کھینچیں"
                                    BookingPhotoStep.WORK_COMPLETED -> "کام مکمل ہونے کی تصویر کھینچیں"
                                    BookingPhotoStep.FARMER_CONFIRMATION -> "کام کی تصدیقی تصویر کھینچیں"
                                }

                                Button(
                                    onClick = {
                                        activeCameraStep = nextStepForAction.step
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            cameraLauncher.launch(null)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AgriGreenPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = buttonText,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Completed Steps List (sleek, simple, NO icons, clean layout)
                        val completedSteps = lifecycleSteps().filter { step ->
                            booking.lifecyclePhotos.any { it.step == step.step }
                        }
                        if (completedSteps.isNotEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEAEAEA)))
                            Text(
                                text = stringResource(id = R.string.completed_steps_header),
                                color = TextSoft,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            completedSteps.forEach { stepUi ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF7F8F6))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.step_completed_format, stringResource(id = stepUi.titleResId)),
                                            color = TextDark,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(id = R.string.responsibility_format, roleText(stepUi.role, context)),
                                            color = TextSoft,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Compact photo thumbnail
                                    val stepBmp = stepBitmaps[stepUi.step]
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE0E0E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (stepBmp != null) {
                                            Image(
                                                bitmap = stepBmp.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(AgriGreenPrimary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "✓",
                                                    color = AgriGreenPrimary,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- FULL-SCREEN SIMULATED CAMERA OVERLAY ---
        if (activeCameraStep != null && cameraState != CameraFlowState.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                when (cameraState) {
                    CameraFlowState.VIEWFINDER -> {
                        // Bypass mock viewfinder if phone system camera is triggered directly.
                        // However, we will allow fallback transition or simple placeholder
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        activeCameraStep = null
                                        cameraState = CameraFlowState.NONE
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "بند کریں",
                                        tint = Color.White
                                    )
                                }
                                Text(
                                    text = "کیمرہ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(48.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .border(3.dp, Color(0xFF2E7D32), RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32).copy(alpha = 0.35f),
                                    modifier = Modifier.size(90.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(horizontal = 24.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                            ) {
                                Text("کیمرہ کھولیں", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    CameraFlowState.PREVIEW -> {
                        // IMAGE PREVIEW SHOWING THE ACTUAL CAPTURED SHOT!
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تصویر کا معائنہ کریں",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 24.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bmp = capturedBitmap
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Captured picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Agriculture,
                                                contentDescription = null,
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(80.dp)
                                            )
                                            Text(
                                                text = "تصویر دستیاب نہیں",
                                                color = Color.LightGray,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (hasPermission) {
                                                cameraLauncher.launch(null)
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(2.dp, Color.White),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("دوبارہ کھینچیں", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { cameraState = CameraFlowState.UPLOADING },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                                    ) {
                                        Text("تصویر قبول کریں", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    CameraFlowState.UPLOADING -> {
                        var mockProgress by remember { mutableStateOf(0f) }
                        LaunchedEffect(Unit) {
                            while (mockProgress < 1f) {
                                delay(30)
                                mockProgress += 0.05f
                            }
                            cameraState = CameraFlowState.DONE
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "تصویر اپ لوڈ ہو رہی ہے...",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            LinearProgressIndicator(
                                progress = mockProgress.coerceAtMost(1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = AgriGreenPrimary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "${(mockProgress * 100).toInt().coerceAtMost(100)}%",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    CameraFlowState.DONE -> {
                        LaunchedEffect(Unit) {
                            delay(900)
                            activeCameraStep?.let { step ->
                                capturedBitmap?.let { bmp ->
                                    stepBitmaps = stepBitmaps + (step to bmp)
                                }
                                onUploadStep(step)
                            }
                            activeCameraStep = null
                            cameraState = CameraFlowState.NONE
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DoneGreen,
                                modifier = Modifier.size(90.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "تصویر اپ لوڈ مکمل ہو گئی!",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    CameraFlowState.NONE -> {}
                }
            }
        }
    }

    if (showAcceptDialog) {
        AgriConfirmationDialog(
            title = "درخواست منظور کریں؟",
            onDismissRequest = { showAcceptDialog = false },
            confirmButtonText = "منظور کریں",
            confirmButtonColor = AgriGreenPrimary,
            onConfirm = {
                showAcceptDialog = false
                onAcceptRequest()
            },
            dismissButtonText = "واپس"
        ) {
            Text(text = "منظوری کے بعد کام کے مراحل شروع ہو جائیں گے۔", fontSize = 16.sp, color = Color.DarkGray)
        }
    }

    if (showRejectDialog) {
        AgriConfirmationDialog(
            title = "درخواست مسترد کریں؟",
            onDismissRequest = { showRejectDialog = false },
            confirmButtonText = "مسترد کریں",
            confirmButtonColor = DangerRed,
            onConfirm = {
                showRejectDialog = false
                onRejectRequest(rejectReason.ifBlank { null })
            },
            dismissButtonText = "واپس"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "وجہ لکھ دیں تا کہ کسان کو معلوم ہو سکے۔", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "وجہ") },
                    placeholder = { Text(text = "مثلاً مشین دستیاب نہیں") },
                    singleLine = false,
                    minLines = 2
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    captureSignal: Long,
    onPictureCaptured: (Bitmap) -> Unit
) {
    var cameraInstance by remember { mutableStateOf<android.hardware.Camera?>(null) }

    LaunchedEffect(captureSignal) {
        if (captureSignal > 0L) {
            try {
                cameraInstance?.takePicture(
                    null,
                    null,
                    { data, _ ->
                        try {
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
                            val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                            val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                            )
                            onPictureCaptured(rotatedBitmap)
                        } catch (e: Exception) {
                            android.util.Log.e("CameraPreview", "Error processing photo", e)
                        }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("CameraPreview", "Error taking picture", e)
            }
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            val textureView = android.view.TextureView(ctx)
            textureView.surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                    try {
                        val cam = android.hardware.Camera.open()
                        cameraInstance = cam
                        cam.setPreviewTexture(surface)
                        cam.setDisplayOrientation(90) // Portrait orientation
                        cam.startPreview()
                    } catch (e: Exception) {
                        android.util.Log.e("CameraPreview", "Failed to start camera preview", e)
                    }
                }
                override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                    try {
                        cameraInstance?.stopPreview()
                        cameraInstance?.release()
                        cameraInstance = null
                    } catch (e: Exception) {}
                    return true
                }
                override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
            }
            textureView
        },
        modifier = modifier
    )
}

@Composable
fun CameraFrameDialog(
    onDismiss: () -> Unit,
    onPictureCaptured: (Bitmap) -> Unit,
    userName: String,
    titleText: String = stringResource(id = R.string.camera_frame_title_start),
    narrationTemplateRes: Int = R.string.camera_frame_voice_narration
) {
    val context = LocalContext.current
    var captureSignal by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = titleText,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .border(BorderStroke(3.dp, Color(0xFF2E7D32)), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        captureSignal = captureSignal,
                        onPictureCaptured = onPictureCaptured
                    )

                    val density = androidx.compose.ui.platform.LocalDensity.current
                    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        val w = this.size.width
                        val h = this.size.height
                        val len = with(density) { 30.dp.toPx() }
                        val strokeW = with(density) { 4.dp.toPx() }

                        // Top Left
                        this.drawLine(color = Color.White, start = Offset(0f, 0f), end = Offset(len, 0f), strokeWidth = strokeW)
                        this.drawLine(color = Color.White, start = Offset(0f, 0f), end = Offset(0f, len), strokeWidth = strokeW)

                        // Top Right
                        this.drawLine(color = Color.White, start = Offset(w, 0f), end = Offset(w - len, 0f), strokeWidth = strokeW)
                        this.drawLine(color = Color.White, start = Offset(w, 0f), end = Offset(w, len), strokeWidth = strokeW)

                        // Bottom Left
                        this.drawLine(color = Color.White, start = Offset(0f, h), end = Offset(len, h), strokeWidth = strokeW)
                        this.drawLine(color = Color.White, start = Offset(0f, h), end = Offset(0f, h - len), strokeWidth = strokeW)

                        // Bottom Right
                        this.drawLine(color = Color.White, start = Offset(w, h), end = Offset(w - len, h), strokeWidth = strokeW)
                        this.drawLine(color = Color.White, start = Offset(w, h), end = Offset(w, h - len), strokeWidth = strokeW)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300))
                            .clickable {
                                val voiceMsg = context.getString(narrationTemplateRes, userName)
                                pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(voiceMsg, "camera_frame_guide")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Hear Guidance",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.camera_frame_hear_guidance),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.camera_frame_cancel),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { captureSignal = System.currentTimeMillis() },
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5D34)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.camera_frame_capture_button),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun isNetworkAvailable(context: android.content.Context): Boolean {
    return pk.kissanmadadgar.mobile.data.local.NetworkMonitor.isOnline(context)
}

private fun formatFarmingDuration(totalSecs: Long): String {
    val h = totalSecs / 3600
    val m = (totalSecs % 3600) / 60
    val s = totalSecs % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

fun generateQrCodeBitmap(content: String): Bitmap? {
    try {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    } catch (e: Exception) {
        return null
    }
}

@Composable
fun QrCodeGenerator(content: String) {
    val bitmap = remember(content) { generateQrCodeBitmap(content) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier
                .size(240.dp)
                .border(BorderStroke(2.dp, Color.LightGray), RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("کیو آر کوڈ دستیاب نہیں ہے")
        }
    }
}

@Composable
fun QrScannerView(
    targetName: String,
    booking: Booking,
    currentLat: Double,
    currentLng: Double,
    onScanCompleted: (String) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(enabled = true) {
        onCancel()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        BarcodeCameraPreview(
            modifier = Modifier.fillMaxSize(),
            onBarcodeScanned = { qrContent ->
                onScanCompleted(qrContent)
            }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Text("کیو آر اسکینر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.size(48.dp))
            }

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(BorderStroke(4.dp, Color.White), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = size.height * laserY
                    drawLine(
                        color = Color.Red,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                
                Text(
                    text = "[ کیو آر اسکین ہو رہا ہے... ]",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "براہ کرم $targetName کے موبائل پر موجود کیو آر کوڈ فریم کے اندر لائیں۔",
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BarcodeCameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    var cameraInstance by remember { mutableStateOf<android.hardware.Camera?>(null) }
    var isProcessingFrame by remember { mutableStateOf(false) }

    val barcodeScanner = remember {
        com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            val textureView = android.view.TextureView(ctx)
            textureView.surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                    try {
                        val cam = android.hardware.Camera.open()
                        cameraInstance = cam
                        
                        val params = cam.parameters
                        val focusModes = params.supportedFocusModes
                        if (focusModes != null && focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            params.focusMode = android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                            try {
                                cam.parameters = params
                            } catch (e: Exception) {
                                android.util.Log.w("BarcodeCameraPreview", "Failed to set focus mode", e)
                            }
                        }

                        cam.setPreviewTexture(surface)
                        cam.setDisplayOrientation(90) // Portrait orientation
                        
                        cam.setPreviewCallback { data, camera ->
                            if (!isProcessingFrame) {
                                isProcessingFrame = true
                                try {
                                    val size = camera.parameters.previewSize
                                    val inputImage = com.google.mlkit.vision.common.InputImage.fromByteArray(
                                        data,
                                        size.width,
                                        size.height,
                                        90,
                                        com.google.mlkit.vision.common.InputImage.IMAGE_FORMAT_NV21
                                    )
                                    barcodeScanner.process(inputImage)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val barcodes = task.result
                                                val qrCode = barcodes?.firstOrNull()?.rawValue
                                                if (qrCode != null) {
                                                    onBarcodeScanned(qrCode)
                                                }
                                            }
                                            isProcessingFrame = false
                                        }
                                } catch (e: Exception) {
                                    isProcessingFrame = false
                                }
                            }
                        }
                        
                        cam.startPreview()
                    } catch (e: Exception) {
                        android.util.Log.e("BarcodeCameraPreview", "Failed to start camera preview", e)
                    }
                }
                
                override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
                
                override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                    try {
                        cameraInstance?.setPreviewCallback(null)
                        cameraInstance?.stopPreview()
                        cameraInstance?.release()
                        cameraInstance = null
                    } catch (e: Exception) {}
                    return true
                }
                
                override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
            }
            textureView
        },
        modifier = modifier
    )
}

fun validateScanData(
    qrContent: String,
    currentBooking: Booking,
    currentLat: Double,
    currentLng: Double,
    context: android.content.Context
): Boolean {
    android.util.Log.d("QrScanValidation", "Starting QR validation. Payload: $qrContent")
    android.util.Log.d("QrScanValidation", "Current device location: lat=$currentLat, lng=$currentLng")
    try {
        val json = org.json.JSONObject(qrContent)
        val bookingId = json.optString("bookingId")
        val serviceProviderId = json.optLong("serviceProviderId")
        val serviceTakerId = json.optLong("serviceTakerId")
        val qrStatus = json.optString("status")
        val qrLat = json.optDouble("latitude")
        val qrLng = json.optDouble("longitude")
        
        android.util.Log.d("QrScanValidation", "Parsed QR fields: bookingId=$bookingId, providerId=$serviceProviderId, takerId=$serviceTakerId, status=$qrStatus, lat=$qrLat, lng=$qrLng")
        
        // 1. Status check
        if (qrStatus != "APPROVED" && qrStatus != "STARTED_FROM_FARMER_SIDE") {
            android.util.Log.w("QrScanValidation", "Validation failed: status mismatch. Expected APPROVED or STARTED_FROM_FARMER_SIDE, got: $qrStatus")
            return false
        }
        
        // 2. Booking ID check
        if (bookingId != currentBooking.id) {
            android.util.Log.w("QrScanValidation", "Validation failed: bookingId mismatch. Expected: ${currentBooking.id}, got: $bookingId")
            return false
        }
        
        // 3. Service Provider ID check
        if (serviceProviderId != currentBooking.serviceProviderId) {
            android.util.Log.w("QrScanValidation", "Validation failed: providerId mismatch. Expected: ${currentBooking.serviceProviderId}, got: $serviceProviderId")
            return false
        }
        
        // 4. Service Taker ID check
        if (serviceTakerId != currentBooking.serviceTakerId) {
            android.util.Log.w("QrScanValidation", "Validation failed: takerId mismatch. Expected: ${currentBooking.serviceTakerId}, got: $serviceTakerId")
            return false
        }
        
        // 5. GPS distance check
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            qrLat, qrLng,
            currentLat, currentLng,
            results
        )
        val distance = results[0]
        android.util.Log.d("QrScanValidation", "Calculated distance: $distance meters")
        if (distance > 10.0f) {
            android.util.Log.w("QrScanValidation", "Validation failed: distance too large ($distance meters, max 10.0m)")
            return false
        }
        
        android.util.Log.i("QrScanValidation", "Validation successful!")
        android.widget.Toast.makeText(context, "تصدیق کامیاب رہی!", android.widget.Toast.LENGTH_SHORT).show()
        return true
    } catch (e: Exception) {
        android.util.Log.e("QrScanValidation", "Validation exception: ${e.message}", e)
        return false
    }
}
