package pk.kissanmadadgar.mobile.presentation.screens

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import pk.kissanmadadgar.mobile.core.components.AgriConfirmationDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    val title: String,
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
    val isAnySelected = selectedFilter != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RequestStatCard(
            title = "انتظار",
            value = pendingCount.toString(),
            color = ActionOrange,
            icon = Icons.Default.HourglassEmpty,
            isSelected = selectedFilter == "PENDING",
            isAnySelected = isAnySelected,
            onClick = {
                if (selectedFilter == "PENDING") onFilterSelected(null)
                else onFilterSelected("PENDING")
            },
            modifier = Modifier.weight(1f)
        )
        RequestStatCard(
            title = "جاری",
            value = ongoingCount.toString(),
            color = AgriGreenPrimary,
            icon = Icons.Default.Autorenew,
            isSelected = selectedFilter == "ONGOING",
            isAnySelected = isAnySelected,
            onClick = {
                if (selectedFilter == "ONGOING") onFilterSelected(null)
                else onFilterSelected("ONGOING")
            },
            modifier = Modifier.weight(1f)
        )
        RequestStatCard(
            title = "مکمل",
            value = finishedCount.toString(),
            color = DoneGreen,
            icon = Icons.Default.CheckCircle,
            isSelected = selectedFilter == "COMPLETED",
            isAnySelected = isAnySelected,
            onClick = {
                if (selectedFilter == "COMPLETED") onFilterSelected(null)
                else onFilterSelected("COMPLETED")
            },
            modifier = Modifier.weight(1f)
        )
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
    requestNumber: Int? = null,
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
                BookingStatusBadge(status = booking.status)
            }

            // Title
            Text(
                text = booking.machineryName,
                color = TextDark,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Compact 2-column info grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF7F8F6))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TileInfoRow(label = "کسان", value = booking.farmerName)
                if (booking.farmerPhone.isNotBlank()) {
                    TileInfoRow(label = "فون", value = booking.farmerPhone)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TileInfoChip(
                        label = "تاریخ",
                        value = formatDate(booking.bookingDate),
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
                        modifier = Modifier.weight(1f)
                    )
                    TileInfoChip(
                        label = "رقم",
                        value = "${formatNumber(booking.totalPrice)} Rs",
                        modifier = Modifier.weight(1f)
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
private fun RequestNumberChip(number: Int) {
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

@Composable
fun FarmerRequestCard(
    booking: Booking,
    requestNumber: Int? = null,
    onClick: () -> Unit = {}
) {
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
                BookingStatusBadge(status = booking.status)
            }

            // Title
            Text(
                text = booking.machineryName,
                color = TextDark,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Compact 2-column info grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF7F8F6))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TileInfoChip(
                        label = "تاریخ",
                        value = formatDate(booking.bookingDate),
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
                        modifier = Modifier.weight(1f)
                    )
                    TileInfoChip(
                        label = "رقم",
                        value = "${formatNumber(booking.totalPrice)} Rs",
                        modifier = Modifier.weight(1f)
                    )
                }
                TileInfoRow(label = "جگہ", value = booking.locationUr)
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
                BookingStatusBadge(status = booking.status)
            }

            // Title
            Text(
                text = booking.machineryName,
                color = TextDark,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

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
                    TileInfoRow(label = "کسان", value = booking.farmerName)
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
                        value = formatDate(booking.bookingDate),
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
                        modifier = Modifier.weight(1f)
                    )
                    TileInfoChip(
                        label = "رقم",
                        value = "${formatNumber(booking.totalPrice)} Rs",
                        modifier = Modifier.weight(1f)
                    )
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
            val nextAction = nextActionText(booking = booking, currentRole = currentRole)
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
fun BookingStatusBadge(status: BookingStatus) {
    val ui = statusUi(status)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ui.backgroundColor)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ui.label,
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
                text = "اب کیا کرنا ہے؟",
                color = AgriGreenPrimary,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = nextActionText(booking = booking, currentRole = currentRole),
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
            SimpleInfoRow(label = "کسان", value = booking.farmerName)
            if (booking.farmerPhone.isNotBlank()) {
                SimpleInfoRow(label = "فون", value = booking.farmerPhone)
            }
            SimpleInfoRow(label = "تاریخ", value = formatDate(booking.bookingDate))
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
                        text = "$number۔ ${stepUi.title}",
                        color = TextDark,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ذمہ داری: ${roleText(stepUi.role)}",
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
                    currentRole = currentRole
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
            label = "مکمل"
            textColor = DoneGreen
            backgroundColor = AgriGreenLight
        }
        bookingStatus == BookingStatus.REJECTED -> {
            label = "مسترد"
            textColor = DangerRed
            backgroundColor = Color(0xFFFFEBEE)
        }
        bookingStatus == BookingStatus.CANCELLED -> {
            label = "منسوخ"
            textColor = DisabledGray
            backgroundColor = Color(0xFFF0F0F0)
        }
        bookingStatus == BookingStatus.PENDING -> {
            label = "انتظار"
            textColor = ActionOrange
            backgroundColor = Color(0xFFFFF3E0)
        }
        unlocked -> {
            label = "اب کریں"
            textColor = ActionOrange
            backgroundColor = Color(0xFFFFF3E0)
        }
        else -> {
            label = "بند"
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
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
        title = "سروس حاصل ہوئی",
        role = UserRole.FARMER
    ),
    StepUi(
        step = BookingPhotoStep.SUBSIDY_STARTED,
        title = "سبسڈی شروع ہوئی",
        role = UserRole.PROVIDER
    ),
    StepUi(
        step = BookingPhotoStep.WORK_COMPLETED,
        title = "کام مکمل ہوا",
        role = UserRole.PROVIDER
    ),
    StepUi(
        step = BookingPhotoStep.FARMER_CONFIRMATION,
        title = "کسان کی تصدیق",
        role = UserRole.FARMER
    )
)

private fun nextActionText(
    booking: Booking,
    currentRole: UserRole
): String {
    if (booking.status == BookingStatus.PENDING) {
        return if (currentRole == UserRole.PROVIDER) {
            "درخواست دیکھیں، پھر منظور یا مسترد کریں۔"
        } else {
            "درخواست بھیج دی گئی ہے۔ منظوری کا انتظار کریں۔"
        }
    }

    if (booking.status == BookingStatus.REJECTED) {
        return "درخواست مسترد ہو چکی ہے۔ وجہ نیچے لکھی ہے۔"
    }

    if (booking.status == BookingStatus.CANCELLED) {
        return "درخواست منسوخ ہو چکی ہے۔"
    }

    if (booking.status == BookingStatus.COMPLETED) {
        return "سارا کام مکمل ہو گیا ہے۔"
    }

    val nextStep = lifecycleSteps().firstOrNull { step ->
        booking.lifecyclePhotos.none { it.step == step.step } && isStepUnlocked(booking, step.step)
    }

    return if (nextStep == null) {
        "تمام تصویری مراحل مکمل ہیں۔"
    } else if (currentRole == nextStep.role) {
        "آپ کی باری ہے: ${nextStep.title} کی تصویر شامل کریں۔"
    } else {
        "${roleText(nextStep.role)} کی باری ہے: ${nextStep.title}۔"
    }
}

private fun stepHelpText(
    stepUi: StepUi,
    photo: BookingLifecyclePhoto?,
    completed: Boolean,
    unlocked: Boolean,
    bookingStatus: BookingStatus,
    currentRole: UserRole
): String {
    if (completed && photo != null) {
        return "یہ مرحلہ مکمل ہے۔ تصویر نیچے موجود ہے۔"
    }

    return when {
        bookingStatus == BookingStatus.PENDING -> "پہلے درخواست منظور ہوگی۔"
        bookingStatus == BookingStatus.REJECTED -> "درخواست مسترد ہو گئی، اس لیے یہ مرحلہ بند ہے۔"
        bookingStatus == BookingStatus.CANCELLED -> "درخواست منسوخ ہو گئی، اس لیے یہ مرحلہ بند ہے۔"
        !unlocked -> "پہلے اوپر والا مرحلہ مکمل ہوگا۔"
        currentRole == stepUi.role -> "یہ آپ کی باری ہے۔ تصویر شامل کریں۔"
        else -> "${roleText(stepUi.role)} کی باری ہے۔"
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
    BookingStatus.PENDING -> StatusUi("انتظار", ActionOrange, Color(0xFFFFF3E0))
    BookingStatus.ACCEPTED -> StatusUi("منظور", AgriGreenPrimary, AgriGreenLight)
    BookingStatus.ACTIVE -> StatusUi("کام جاری", AgriGreenPrimary, AgriGreenLight)
    BookingStatus.COMPLETED -> StatusUi("مکمل", DoneGreen, AgriGreenLight)
    BookingStatus.REJECTED -> StatusUi("مسترد", DangerRed, Color(0xFFFFEBEE))
    BookingStatus.CANCELLED -> StatusUi("منسوخ", DisabledGray, Color(0xFFF0F0F0))
}

private fun roleText(role: UserRole): String = when (role) {
    UserRole.FARMER -> "کسان"
    UserRole.PROVIDER -> "سروس فراہم کرنے والا"
    UserRole.ADMIN -> "ایڈمن"
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd-MM-yyyy", Locale("ur", "PK")).format(Date(timestamp))
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

    val nextStep = lifecycleSteps().firstOrNull { step ->
        booking.lifecyclePhotos.none { it.step == step.step }
    }

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
                        contentDescription = "واپس",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بکنگ کی تفصیلات",
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
                            BookingStatusBadge(status = booking.status)
                        }

                        // Title
                        Text(
                            text = booking.machineryName,
                            color = TextDark,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

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
                                TileInfoRow(label = "کسان", value = booking.farmerName)
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
                                    value = formatDate(booking.bookingDate),
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
                                    modifier = Modifier.weight(1f)
                                )
                                TileInfoChip(
                                    label = "رقم",
                                    value = "${formatNumber(booking.totalPrice)} Rs",
                                    modifier = Modifier.weight(1f)
                                )
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

                        // "Ab mujhe kya karna hai?" section - Sleek, simple aesthetic text, NO icons!
                        if (booking.status != BookingStatus.COMPLETED && booking.status != BookingStatus.REJECTED && booking.status != BookingStatus.CANCELLED) {
                            val nextActionText = nextActionText(booking = booking, currentRole = currentRole)
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
                                            text = "اب مجھے کیا کرنا ہے؟",
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
                        if (booking.status == BookingStatus.PENDING && currentRole == UserRole.PROVIDER) {
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
                                text = "مکمل شدہ مراحل:",
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
                                            text = "${stepUi.title} مکمل ہو گیا",
                                            color = TextDark,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ذمہ داری: ${roleText(stepUi.role)}",
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
                                .padding(24.dp),
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                        Text("دوبارہ کھینچیں", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { cameraState = CameraFlowState.UPLOADING },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                                    ) {
                                        Text("تصویر قبول کریں (Accept Photo)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
