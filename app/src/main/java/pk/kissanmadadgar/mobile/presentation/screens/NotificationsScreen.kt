package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.core.components.AgriDetailHeader
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.data.remote.api.AndroidNotificationDto
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.notificationsLoading.collectAsState()
    val totalPages by viewModel.notificationsTotalPages.collectAsState()
    val isLast by viewModel.notificationsIsLast.collectAsState()
    var currentPage by remember { mutableStateOf(0) }
    val isFirst = currentPage == 0
    val context = LocalContext.current

    // TEMP DEBUG (BellCrashDebug): logs exactly once per distinct instance of this composable
    // (first composition + teardown), to see how many instances actually get created and whether
    // they overlap, since the white-screen repro leaves no OS-level trace. Remove once solved.
    val instanceId = remember { System.currentTimeMillis() }
    remember(instanceId) {
        android.util.Log.d("BellCrashDebug", "NotificationsScreen COMPOSED instance=$instanceId")
        instanceId
    }
    DisposableEffect(instanceId) {
        onDispose {
            android.util.Log.d("BellCrashDebug", "NotificationsScreen DISPOSED instance=$instanceId")
        }
    }

    LaunchedEffect(currentPage) {
        android.util.Log.d("BellCrashDebug", "NotificationsScreen instance=$instanceId fetchNotifications page=$currentPage")
        viewModel.fetchNotifications(page = currentPage, append = false)
    }

    // Shared narration singleton (see NarrationManager) — same pattern as MyBookings.kt and
    // FarmerScreens.kt; initialize() is idempotent so calling it again here is a safe no-op if
    // some other screen already started it this session.
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }
    val activeSpeakingAudioId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()
    val onSpeakClick: (AndroidNotificationDto) -> Unit = { item ->
        val utteranceId = item.id.toString()
        if (activeSpeakingAudioId == utteranceId) {
            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
        } else {
            val text = if (item.body.isNotBlank()) "${item.title}۔ ${item.body}" else item.title
            pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(text, utteranceId)
        }
    }

    Scaffold(
        topBar = { AgriDetailHeader(title = "نوٹیفیکیشنز", onBackClick = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {
            when {
                isLoading && notifications.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AgriGreenPrimary
                    )
                }
                notifications.isEmpty() -> {
                    EmptyNotificationsState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications, key = { it.id }) { item ->
                            NotificationCard(
                                item = item,
                                onClick = {
                                    if (!item.read) viewModel.markNotificationRead(item.id)
                                    item.bookingId?.let { bId ->
                                        // FarmerBookingsTab (MyBookings.kt) resolves this id,
                                        // switches to its correct status filter (PENDING/
                                        // ONGOING/COMPLETED), and opens its detail sheet once
                                        // we land on the Bookings tab.
                                        viewModel.setNotificationBookingId(bId.toString())
                                        onNavigateToBooking()
                                    }
                                },
                                activeSpeakingAudioId = activeSpeakingAudioId,
                                onSpeakClick = onSpeakClick
                            )
                        }

                        if (totalPages > 1) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { if (!isFirst) currentPage-- },
                                        enabled = !isFirst,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, if (isFirst) Color.LightGray else AgriGreenPrimary),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = AgriGreenPrimary,
                                            disabledContentColor = Color.LightGray
                                        )
                                    ) {
                                        Text(text = "← پچھلا صفحہ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = "صفحہ ${currentPage + 1} از $totalPages",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF17251B)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Button(
                                        onClick = { if (!isLast) currentPage++ },
                                        enabled = !isLast,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AgriGreenPrimary,
                                            disabledContainerColor = Color.LightGray,
                                            contentColor = Color.White,
                                            disabledContentColor = Color.White
                                        )
                                    ) {
                                        Text(text = "اگلا صفحہ →", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "آپ کے پاس کوئی نیا نوٹیفیکیشن نہیں ہے۔",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17251B),
            textAlign = TextAlign.Center
        )
        Text(
            text = "بکنگ کی درخواستوں، منظوری اور سرگرمی کی تمام اطلاعات یہاں ظاہر ہوں گی۔",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

private data class NotificationVisual(val icon: ImageVector, val color: Color)

private fun visualFor(title: String): NotificationVisual {
    return when {
        title.contains("منظور") -> NotificationVisual(Icons.Default.CheckCircle, Color(0xFF2E7D32))
        title.contains("مسترد") -> NotificationVisual(Icons.Default.Cancel, Color(0xFFD32F2F))
        title.contains("آغاز") -> NotificationVisual(Icons.Default.PlayCircleFilled, Color(0xFFEF6C00))
        title.contains("مکمل") -> NotificationVisual(Icons.Default.TaskAlt, Color(0xFF6A1B9A))
        title.contains("درخواست") -> NotificationVisual(Icons.Default.NotificationsActive, Color(0xFF1565C0))
        else -> NotificationVisual(Icons.Default.Notifications, AgriGreenPrimary)
    }
}

private fun relativeTime(sentAt: String?): String {
    if (sentAt.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val date = parser.parse(sentAt) ?: return ""
        val diffMs = Date().time - date.time
        val minutes = diffMs / 60000
        val hours = minutes / 60
        val days = hours / 24
        when {
            minutes < 1 -> "ابھی ابھی"
            minutes < 60 -> "$minutes منٹ پہلے"
            hours < 24 -> "$hours گھنٹے پہلے"
            days < 7 -> "$days دن پہلے"
            else -> SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(date)
        }
    } catch (e: Exception) {
        ""
    }
}

@Composable
private fun NotificationCard(
    item: AndroidNotificationDto,
    onClick: () -> Unit,
    activeSpeakingAudioId: String? = null,
    onSpeakClick: (AndroidNotificationDto) -> Unit = {}
) {
    val visual = visualFor(item.title)
    val isSpeaking = activeSpeakingAudioId == item.id.toString()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.read) Color.White else Color(0xFFF1F8F1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.read) 2.dp else 6.dp),
        border = if (!item.read) BorderStroke(1.dp, AgriGreenPrimary.copy(alpha = 0.35f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(visual.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = if (item.read) FontWeight.SemiBold else FontWeight.Black,
                        color = Color(0xFF17251B),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AgriGreenPrimary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.body,
                    fontSize = 13.sp,
                    color = Color(0xFF555555)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = relativeTime(item.sentAt),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    // So the user can tell at a glance which booking this notification is about
                    // — same "درخواست #id" wording used on the booking card and detail sheet.
                    item.bookingId?.let { bId ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(AgriGreenPrimary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "درخواست #$bId",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenPrimary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Audio narration helper — same icon/gradient/size as the speak button in
            // FarmerScreens.kt's AgriHomeStatCard (SupportAgent while idle, VolumeUp while
            // speaking, orange-to-red gradient circle) so it reads as the same control.
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
                    .clickable { onSpeakClick(item) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.SupportAgent,
                    contentDescription = "بولیں",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
