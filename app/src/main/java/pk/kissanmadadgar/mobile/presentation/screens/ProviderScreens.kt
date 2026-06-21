package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.components.UrduTextField
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Booking
import pk.kissanmadadgar.mobile.domain.model.BookingStatus
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.domain.model.MachineryStatus
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.presentation.MainViewModel

private val TextDark = Color(0xFF17251B)
private val TextSoft = Color(0xFF4C5A50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToAddMachinery: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "سپلائر پورٹل", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.padding(end = 16.dp).size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
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
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text(text = stringResource(id = R.string.provider_dashboard_title)) },
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
                    icon = { Icon(imageVector = Icons.Default.Agriculture, contentDescription = null) },
                    label = { Text(text = stringResource(id = R.string.provider_my_machinery)) },
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
            val guestName = stringResource(id = R.string.provider_guest_name)
            when (selectedTab) {
                0 -> ProviderHomeTab(viewModel, onNavigateToAddMachinery)
                1 -> ProviderInventoryTab(viewModel, onNavigateToAddMachinery)
                2 -> ProviderProfileTab(user?.fullName ?: guestName, user?.phoneNumber ?: "", onLogout)
            }
        }
    }
}

@Composable
fun ProviderHomeTab(
    viewModel: MainViewModel,
    onNavigateToAddMachinery: () -> Unit
) {
    val bookings by viewModel.providerBookings.collectAsState()

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
                    title = "بکنگ کی درخواستیں",
                    subtitle = "کسانوں سے آنے والی نئی بکنگز اور ان کے کام کی تفصیلات",
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
                        message = "کوئی درخواست دستیاب نہیں",
                        description = "نئی بکنگز اور درخواستیں یہاں نظر آئیں گی۔",
                        icon = Icons.Default.HourglassEmpty
                    )
                }
            } else {
                itemsIndexed(visibleRequests, key = { _, item -> item.id }) { index, request ->
                    ProviderRequestCard(
                        booking = request,
                        requestNumber = index + 1,
                        onAcceptRequest = { viewModel.acceptBookingRequest(request.id) },
                        onRejectRequest = { reason -> viewModel.rejectBookingRequest(request.id, reason) },
                        onClick = { selectedBookingForDetail = request }
                    )
                }
            }
        }

        if (currentDetailBooking != null) {
            BookingDetailOverlay(
                booking = currentDetailBooking,
                currentRole = UserRole.PROVIDER,
                onBack = { selectedBookingForDetail = null },
                onAcceptRequest = { viewModel.acceptBookingRequest(currentDetailBooking.id) },
                onRejectRequest = { reason -> viewModel.rejectBookingRequest(currentDetailBooking.id, reason) },
                onUploadStep = { step -> viewModel.uploadLifecyclePhoto(currentDetailBooking.id, step) }
            )
        }
    }
}

@Composable
fun ProviderInventoryTab(
    viewModel: MainViewModel,
    onNavigateToAddMachinery: () -> Unit
) {
    val myMachinery by viewModel.providerMachinery.collectAsState()

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
        item.imageUrls.ifEmpty { listOf("super_seeder") }
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
                        .takeIf { it != 0 } ?: R.drawable.super_seeder

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
                    color = TextDark,
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

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.provider_rent_per_hour, item.hourlyRate.toInt()),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextSoft
            )

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
            Text(text = label, color = TextSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = value, color = TextDark, fontWeight = FontWeight.Black, fontSize = 14.sp)
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

@Composable
fun ProviderProfileTab(
    name: String,
    phone: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = AgriGreenPrimary,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = phone, fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.label_role), fontSize = 16.sp)
                    Text(text = stringResource(id = R.string.role_provider_label), color = AgriGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF5F5F5))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.provider_account_status), fontSize = 16.sp)
                    Text(text = stringResource(id = R.string.provider_status_active), color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
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
fun AddMachineryScreen(
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var errorName by remember { mutableStateOf<String?>(null) }
    var errorRate by remember { mutableStateOf<String?>(null) }
    var errorDesc by remember { mutableStateOf<String?>(null) }
    
    val categories by viewModel.categories.collectAsState()

    val errorNameEmpty = stringResource(id = R.string.provider_err_name_empty)
    val errorRateEmpty = stringResource(id = R.string.provider_err_rate_empty)
    val errorDescEmpty = stringResource(id = R.string.provider_err_desc_empty)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.add_machinery_title), fontWeight = FontWeight.Black, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9F9F9)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RequestListHeader(
                    title = "نئی مشینری کا اندراج",
                    subtitle = "ایپ پر اپنی جدید مشینیں کرایہ پر دینے کے لیے درست معلومات درج کریں۔",
                    icon = Icons.Default.Agriculture
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UrduTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorName = null
                            },
                            label = stringResource(id = R.string.machinery_name),
                            placeholder = stringResource(id = R.string.provider_hint_machinery_name),
                            errorText = errorName
                        )

                        UrduTextField(
                            value = rate,
                            onValueChange = {
                                rate = it
                                errorRate = null
                            },
                            label = stringResource(id = R.string.machinery_rate_input),
                            placeholder = stringResource(id = R.string.provider_hint_machinery_rate),
                            errorText = errorRate
                        )

                        UrduTextField(
                            value = desc,
                            onValueChange = {
                                desc = it
                                errorDesc = null
                            },
                            label = stringResource(id = R.string.machinery_desc_input),
                            placeholder = stringResource(id = R.string.provider_hint_machinery_desc),
                            errorText = errorDesc
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "اہم معلومات",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مشینری رجسٹریشن کی معلومات جمع کرانے کے بعد، کسان مددگار منتظم (ایڈمن) 24 گھنٹے کے اندر آپ کی مشینری کی تصدیق کر کے اسے منظور کر دے گا۔",
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                UrduButton(
                    text = stringResource(id = R.string.provider_btn_send_request),
                    onClick = {
                        var isValid = true
                        if (name.trim().isEmpty()) {
                            errorName = errorNameEmpty
                            isValid = false
                        }
                        val doubleRate = rate.toDoubleOrNull()
                        if (doubleRate == null || doubleRate <= 0.0) {
                            errorRate = errorRateEmpty
                            isValid = false
                        }
                        if (desc.trim().isEmpty()) {
                            errorDesc = errorDescEmpty
                            isValid = false
                        }

                        if (isValid) {
                            val defaultCategoryId = categories.firstOrNull()?.id ?: "cat_1"
                            viewModel.addMachinery(name, defaultCategoryId, doubleRate!!, desc, onSuccess)
                        }
                    }
                )
            }
        }
    }
}
