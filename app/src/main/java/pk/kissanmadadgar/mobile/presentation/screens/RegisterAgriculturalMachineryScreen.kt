package pk.kissanmadadgar.mobile.presentation.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.components.UrduTextField
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import pk.kissanmadadgar.mobile.data.remote.dto.ImplementDto
import pk.kissanmadadgar.mobile.data.remote.dto.DistrictDto
import androidx.compose.ui.text.TextStyle
import pk.kissanmadadgar.mobile.core.components.CNICInputField
import pk.kissanmadadgar.mobile.core.components.PhoneNumberInput

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RegisterAgriculturalMachineryScreen(
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // --- Welcome Header Audio Assistant — shared NarrationManager singleton ---
    LaunchedEffect(Unit) {
        pk.kissanmadadgar.mobile.data.local.NarrationManager.initialize(context)
    }
    val activeSpeakingAudioId by pk.kissanmadadgar.mobile.data.local.NarrationManager.activeUtteranceId.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )
    
    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val rippleAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha2"
    )

    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (activeSpeakingAudioId != null) 1.0f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    // State holding selected machines (Set of strings)
    var machineQuantities by remember { mutableStateOf(emptyMap<String, Int>()) }
    var customMachineName by remember { mutableStateOf("") }
    
    data class MachineDialogData(val name: String, val drawableResId: Int, val imageUrls: List<String>, val currentQty: Int)
    var machineToSetQuantity by remember { mutableStateOf<MachineDialogData?>(null) }
    
    val implements by viewModel.implementsList.collectAsState()
    val isLoadingImplements by viewModel.isLoadingImplements.collectAsState()
    
    val districtsList by viewModel.districtsList.collectAsState()
    val districtNames = remember(districtsList) {
        districtsList.map { it.nameUrdu }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchImplements()
        viewModel.fetchDistricts()
    }
    
    val first3Implements = remember(implements) {
        implements.take(3)
    }
    
    val otherImplements = remember(implements) {
        if (implements.size > 3) implements.drop(3) else emptyList()
    }
    
    val otherMachineryOptions = remember(otherImplements) {
        otherImplements.map { it.nameUr }
    }
    
    fun sanitizeImageUrl(url: String?): String? {
        if (url == null) return null
        if (url.contains(":9089") && !url.contains(":9089/")) {
            return url.replace(":9089", ":9089/")
        }
        return url
    }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    val user by viewModel.currentUser.collectAsState()
    var phoneNumber by remember(user) { 
        mutableStateOf(
            user?.phoneNumber?.let {
                if (it.startsWith("+92")) "0" + it.substring(3) else it
            } ?: ""
        ) 
    }
    var cnic by remember { mutableStateOf(viewModel.getCurrentUserCnic()) }
    var fullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var otpCode by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Step state: 0 (Machine), 1 (District), 2 (Phone), 3 (OTP)
    var currentStep by remember { mutableStateOf(0) }

    // Error states
    var phoneError by remember { mutableStateOf<String?>(null) }
    var cnicError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }





    var customDropdownExpanded by remember { mutableStateOf(false) }

    // Progress percentage based on active step
    val progressFraction = remember(currentStep) {
        when (currentStep) {
            0 -> 0.33f
            1 -> 0.66f
            2 -> 1.00f
            else -> 0.00f
        }
    }

    Scaffold(
        topBar = {
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
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                    clip = true
                ),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = headerBrush)
                        .fillMaxWidth()
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = R.string.title_zarai_machine_register),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    ) { padding ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Top Progress Bar
                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = AgriGreenPrimary,
                    trackColor = AgriGreenLight
                )

                // Scrollable container for sequential wizard sections
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .animateContentSize(), // Auto animate heights when steps unlock
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // --- STEP 0: Select Machine (Multi Selection with Images) ---
                    if (currentStep >= 0) {
                        val formattedSummary = remember(machineQuantities, customMachineName) {
                            val list = mutableListOf<String>()
                            machineQuantities.forEach { (machine, qty) ->
                                if (machine == "دیگر مشینیں") {
                                    if (customMachineName.isNotEmpty()) {
                                        list.add("$customMachineName ($qty)")
                                    } else {
                                        list.add("دیگر ($qty)")
                                    }
                                } else {
                                    list.add("$machine ($qty)")
                                }
                            }
                            list.joinToString("، ")
                        }
                        
                        StepContainer(
                            stepIndex = 0,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_select_machine_title),
                            summaryText = stringResource(id = R.string.summary_machine, formattedSummary),
                            isCompleted = machineQuantities.isNotEmpty() && (!machineQuantities.containsKey("دیگر مشینیں") || customMachineName.isNotEmpty()) && currentStep > 0,
                            onEditClick = { currentStep = 0 }
                        ) {
                            if (isLoadingImplements) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AgriGreenPrimary)
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.step_select_machine_prompt),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AgriGreenPrimary,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Audio Assistant Button with Ripples
                                        Box(
                                            modifier = Modifier.size(48.dp).offset(y = (-6).dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Ripples (glowing waves when active or idle)
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
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer(
                                                        scaleX = rippleScale2,
                                                        scaleY = rippleScale2,
                                                        alpha = rippleAlpha2
                                                    )
                                                    .background(Color(0xFFFF6D00).copy(alpha = 0.4f), CircleShape)
                                            )

                                            // Main Button
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .graphicsLayer(
                                                        scaleX = buttonScale,
                                                        scaleY = buttonScale
                                                    )
                                                    .shadow(elevation = 4.dp, shape = CircleShape)
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = if (activeSpeakingAudioId == "step_1") {
                                                                listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                            } else {
                                                                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                            }
                                                        ),
                                                        shape = CircleShape
                                                    )
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        if (activeSpeakingAudioId == "step_1") {
                                                            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                                        } else {
                                                            val name1 = first3Implements.getOrNull(0)?.nameUr ?: context.getString(R.string.machine_super_seeder)
                                                            val name2 = first3Implements.getOrNull(1)?.nameUr ?: context.getString(R.string.machine_baler)
                                                            val name3 = first3Implements.getOrNull(2)?.nameUr ?: context.getString(R.string.machine_harvester)
                                                            
                                                            val rawText = context.getString(R.string.machine_registeration_step_1)
                                                            val textToSpeak = String.format(rawText, name1, name2, name3)
                                                            
                                                            pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "step_1")
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (activeSpeakingAudioId == "step_1") {
                                                    // Active sound wave equalizer animation
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
                                                                label = "bar_$i"
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(width = 2.dp, height = 14.dp)
                                                                    .graphicsLayer(scaleY = barHeightScale)
                                                                    .background(Color.White, RoundedCornerShape(1.dp))
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.SupportAgent,
                                                        contentDescription = "آڈیو گائیڈ",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 2x2 Grid for standard machines (using images)
                                    val othersStr = stringResource(id = R.string.machine_others)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val item1 = first3Implements.getOrNull(0)
                                        val name1 = item1?.nameUr ?: stringResource(id = R.string.machine_super_seeder)
                                        val fallback1 = R.drawable.super_seeder_custom
                                        val imgUrl1 = sanitizeImageUrl(item1?.picture1)
                                        val urls1 = listOf(
                                            sanitizeImageUrl(item1?.picture1),
                                            sanitizeImageUrl(item1?.picture2),
                                            sanitizeImageUrl(item1?.picture3),
                                            sanitizeImageUrl(item1?.picture4)
                                        ).filterNotNull()
                                        MachineOptionCard(
                                            name = name1,
                                            drawableResId = fallback1,
                                            imageUrl = imgUrl1,
                                            quantity = machineQuantities[name1],
                                            onClick = {
                                                machineToSetQuantity = MachineDialogData(name1, fallback1, urls1, machineQuantities[name1] ?: 0)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        val item2 = first3Implements.getOrNull(1)
                                        val name2 = item2?.nameUr ?: stringResource(id = R.string.machine_baler)
                                        val fallback2 = R.drawable.bailer
                                        val imgUrl2 = sanitizeImageUrl(item2?.picture1)
                                        val urls2 = listOf(
                                            sanitizeImageUrl(item2?.picture1),
                                            sanitizeImageUrl(item2?.picture2),
                                            sanitizeImageUrl(item2?.picture3),
                                            sanitizeImageUrl(item2?.picture4)
                                        ).filterNotNull()
                                        MachineOptionCard(
                                            name = name2,
                                            drawableResId = fallback2,
                                            imageUrl = imgUrl2,
                                            quantity = machineQuantities[name2],
                                            onClick = {
                                                machineToSetQuantity = MachineDialogData(name2, fallback2, urls2, machineQuantities[name2] ?: 0)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val item3 = first3Implements.getOrNull(2)
                                        val name3 = item3?.nameUr ?: stringResource(id = R.string.machine_harvester)
                                        val fallback3 = R.drawable.harvester
                                        val imgUrl3 = sanitizeImageUrl(item3?.picture1)
                                        val urls3 = listOf(
                                            sanitizeImageUrl(item3?.picture1),
                                            sanitizeImageUrl(item3?.picture2),
                                            sanitizeImageUrl(item3?.picture3),
                                            sanitizeImageUrl(item3?.picture4)
                                        ).filterNotNull()
                                        MachineOptionCard(
                                            name = name3,
                                            drawableResId = fallback3,
                                            imageUrl = imgUrl3,
                                            quantity = machineQuantities[name3],
                                            onClick = {
                                                machineToSetQuantity = MachineDialogData(name3, fallback3, urls3, machineQuantities[name3] ?: 0)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        val fallbackOthers = R.drawable.other_machinery_clean
                                        MachineOptionCard(
                                            name = othersStr,
                                            drawableResId = fallbackOthers,
                                            imageUrl = null,
                                            quantity = machineQuantities["دیگر مشینیں"],
                                            onClick = {
                                                if (machineQuantities.containsKey("دیگر مشینیں")) {
                                                     if (customMachineName.isNotEmpty()) {
                                                         val matchedImplement = otherImplements.find { it.nameUr == customMachineName }
                                                         val urls = listOf(
                                                             sanitizeImageUrl(matchedImplement?.picture1),
                                                             sanitizeImageUrl(matchedImplement?.picture2),
                                                             sanitizeImageUrl(matchedImplement?.picture3),
                                                             sanitizeImageUrl(matchedImplement?.picture4)
                                                         ).filterNotNull()
                                                         machineToSetQuantity = MachineDialogData("دیگر مشینیں", fallbackOthers, urls, machineQuantities["دیگر مشینیں"] ?: 0)
                                                     } else {
                                                        machineQuantities = machineQuantities - "دیگر مشینیں"
                                                    }
                                                } else {
                                                    machineQuantities = machineQuantities + ("دیگر مشینیں" to 0)
                                                    customDropdownExpanded = true
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                // Expand dropdown if "Others" selected
                                if (machineQuantities.containsKey("دیگر مشینیں")) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "دیگر زرعی مشین منتخب کریں",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    ExposedDropdownMenuBox(
                                        expanded = customDropdownExpanded,
                                        onExpandedChange = { customDropdownExpanded = !customDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = if (customMachineName.isEmpty()) "دیگر مشین منتخب کریں" else customMachineName,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customDropdownExpanded) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AgriGreenPrimary,
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = if (customMachineName.isEmpty()) Color.Gray else Color.Black
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = customDropdownExpanded,
                                            onDismissRequest = { customDropdownExpanded = false },
                                            modifier = Modifier.background(Color.White)
                                        ) {
                                            otherMachineryOptions.forEach { machine ->
                                                DropdownMenuItem(
                                                    text = { Text(text = machine, fontSize = 16.sp) },
                                                    onClick = {
                                                        customMachineName = machine
                                                        customDropdownExpanded = false
                                                        val matchedImplement = otherImplements.find { it.nameUr == machine }
                                                        val urls = listOf(
                                                            sanitizeImageUrl(matchedImplement?.picture1),
                                                            sanitizeImageUrl(matchedImplement?.picture2),
                                                            sanitizeImageUrl(matchedImplement?.picture3),
                                                            sanitizeImageUrl(matchedImplement?.picture4)
                                                        ).filterNotNull()
                                                        machineToSetQuantity = MachineDialogData("دیگر مشینیں", R.drawable.other_machinery_clean, urls, machineQuantities["دیگر مشینیں"] ?: 0)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                UrduButton(
                                    text = stringResource(id = R.string.btn_continue),
                                    onClick = {
                                        if (machineQuantities.containsKey("دیگر مشینیں") && customMachineName.trim().isEmpty()) {
                                            Toast.makeText(context, "براہ کرم دیگر مشین منتخب کریں", Toast.LENGTH_SHORT).show()
                                        } else {
                                            currentStep = 1
                                        }
                                    },
                                    enabled = machineQuantities.values.any { it > 0 } && (!machineQuantities.containsKey("دیگر مشینیں") || customMachineName.trim().isNotEmpty())
                                )
                            }
                        }
                    }
                }

                    
    // --- Machine Quantity Dialog ---
    if (machineToSetQuantity != null) {
        val data = machineToSetQuantity!!
        var tempQty by remember(machineToSetQuantity) { mutableStateOf(if (data.currentQty > 0) data.currentQty else 1) }
        var dialogSelectedDistrict by remember(machineToSetQuantity) { mutableStateOf(selectedDistrict) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { machineToSetQuantity = null },
            confirmButton = {
                UrduButton(
                    text = "محفوظ کریں",
                    onClick = {
                        machineQuantities = if (tempQty > 0) {
                            machineQuantities + (data.name to tempQty)
                        } else {
                            machineQuantities - data.name
                        }
                        selectedDistrict = dialogSelectedDistrict
                        machineToSetQuantity = null
                    },
                    enabled = tempQty >= 0 && (tempQty == 0 || dialogSelectedDistrict != null)
                )
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        machineQuantities = machineQuantities - data.name
                        machineToSetQuantity = null
                    }
                ) {
                    Text("منسوخ", color = Color.Gray)
                }
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (data.name == "دیگر مشینیں" && customMachineName.isNotEmpty()) customMachineName else data.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AgriGreenPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                    )
                    IconButton(
                        onClick = { machineToSetQuantity = null },
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Image Pager Slider
                    val pagerState = rememberPagerState(pageCount = { data.imageUrls.size.coerceAtLeast(1) })
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val url = data.imageUrls.getOrNull(page)
                            if (!url.isNullOrEmpty()) {
                                coil.compose.SubcomposeAsyncImage(
                                    model = url,
                                    contentDescription = data.name,
                                    contentScale = ContentScale.Fit,
                                    loading = {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(24.dp))
                                        }
                                    },
                                    error = {
                                        Image(
                                            painter = painterResource(id = data.drawableResId),
                                            contentDescription = data.name,
                                            modifier = Modifier.fillMaxSize().padding(8.dp)
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = data.drawableResId),
                                    contentDescription = data.name,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            }
                        }
                        
                        if (data.imageUrls.size > 1) {
                            Row(
                                Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(data.imageUrls.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) AgriGreenPrimary else Color.LightGray
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(6.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.prompt_how_many_machines),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // Audio Assistant Button for Count
                        Box(
                            modifier = Modifier.size(44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeSpeakingAudioId == "count") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                        .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (activeSpeakingAudioId == "count") {
                                                listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                            } else {
                                                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                            }
                                        ),
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable {
                                        if (activeSpeakingAudioId == "count") {
                                            pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                        } else {
                                            val machineName = if (data.name == "دیگر مشینیں" && customMachineName.isNotEmpty()) customMachineName else data.name
                                            val rawText = context.getString(R.string.machine_registeration_step_2_count)
                                            val textToSpeak = String.format(rawText, machineName)
                                            pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "count")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (activeSpeakingAudioId == "count") {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(1.dp),
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
                                                label = "bar_count_$i"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 1.5.dp, height = 12.dp)
                                                    .graphicsLayer(scaleY = barHeightScale)
                                                    .background(Color.White, RoundedCornerShape(0.5.dp))
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = "آڈیو گائیڈ",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (tempQty > 1) tempQty-- },
                            modifier = Modifier.background(AgriGreenLight, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Decrease", tint = AgriGreenPrimary)
                        }
                        
                        Text(
                            text = "$tempQty",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        IconButton(
                            onClick = { tempQty++ },
                            modifier = Modifier.background(AgriGreenLight, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.Default.Add, "Increase", tint = AgriGreenPrimary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. District selector section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ضلع منتخب کریں",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Audio Assistant Button for District
                            Box(
                                modifier = Modifier.size(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (activeSpeakingAudioId == "district") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                            .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .shadow(elevation = 2.dp, shape = CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = if (activeSpeakingAudioId == "district") {
                                                    listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                } else {
                                                    listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                }
                                            ),
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable {
                                            if (activeSpeakingAudioId == "district") {
                                                pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                            } else {
                                                val textToSpeak = context.getString(R.string.machine_registeration_step_2_district)
                                                pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "district")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (activeSpeakingAudioId == "district") {
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
                                                    label = "bar_dist_$i"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 1.5.dp, height = 12.dp)
                                                        .graphicsLayer(scaleY = barHeightScale)
                                                        .background(Color.White, RoundedCornerShape(0.5.dp))
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.SupportAgent,
                                            contentDescription = "آڈیو گائیڈ",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = dialogSelectedDistrict ?: "ضلع منتخب کریں",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AgriGreenPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = if (dialogSelectedDistrict == null) Color.Gray else Color.Black
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                districtNames.forEach { distName ->
                                    DropdownMenuItem(
                                        text = { Text(text = distName, fontSize = 16.sp) },
                                        onClick = {
                                            dialogSelectedDistrict = distName
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

                    // --- STEP 3: Phone Number Input ---
                    if (currentStep >= 1) {
                        StepContainer(
                            stepIndex = 1,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_phone_title),
                            summaryText = stringResource(id = R.string.summary_phone, phoneNumber),
                            isCompleted = fullName.trim().isNotEmpty() && phoneNumber.length >= 10 && cnic.length >= 13 && currentStep > 1,
                            onEditClick = { currentStep = 1 }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.register_name),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Audio Assistant Button for Name
                                    Box(
                                        modifier = Modifier.size(48.dp).offset(y = (-6).dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (activeSpeakingAudioId == "step_3_name") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .shadow(elevation = 4.dp, shape = CircleShape)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = if (activeSpeakingAudioId == "step_3_name") {
                                                            listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                        } else {
                                                            listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                        }
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (activeSpeakingAudioId == "step_3_name") {
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                                    } else {
                                                        val textToSpeak = context.getString(R.string.machine_registeration_step_3_name)
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "step_3_name")
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (activeSpeakingAudioId == "step_3_name") {
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
                                                            label = "bar_name_$i"
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 2.dp, height = 14.dp)
                                                                .graphicsLayer(scaleY = barHeightScale)
                                                                .background(Color.White, RoundedCornerShape(1.dp))
                                                        )
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.SupportAgent,
                                                    contentDescription = "آڈیو گائیڈ",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                UrduTextField(
                                    value = fullName,
                                    onValueChange = { 
                                        fullName = it
                                        nameError = null
                                    },
                                    label = stringResource(id = R.string.register_name),
                                    placeholder = "مثلاً: احمد خان",
                                    isPhoneNumber = false,
                                    errorText = nameError
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "شناختی کارڈ نمبر",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Audio Assistant Button for CNIC
                                    Box(
                                        modifier = Modifier.size(48.dp).offset(y = (-6).dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (activeSpeakingAudioId == "step_3_cnic") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .shadow(elevation = 4.dp, shape = CircleShape)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = if (activeSpeakingAudioId == "step_3_cnic") {
                                                            listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                        } else {
                                                            listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                        }
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (activeSpeakingAudioId == "step_3_cnic") {
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                                    } else {
                                                        val textToSpeak = context.getString(R.string.machine_registeration_step_3_cnic)
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "step_3_cnic")
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (activeSpeakingAudioId == "step_3_cnic") {
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
                                                            label = "bar_cnic_$i"
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 2.dp, height = 14.dp)
                                                                .graphicsLayer(scaleY = barHeightScale)
                                                                .background(Color.White, RoundedCornerShape(1.dp))
                                                        )
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.SupportAgent,
                                                    contentDescription = "آڈیو گائیڈ",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                CNICInputField(
                                    cnic = cnic,
                                    onCnicChange = { input ->
                                        cnic = input
                                        cnicError = null
                                    },
                                    isError = cnicError != null,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (cnicError != null) {
                                    Text(
                                        text = cnicError ?: "",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.step_phone_prompt),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Audio Assistant Button for Mobile
                                    Box(
                                        modifier = Modifier.size(48.dp).offset(y = (-6).dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (activeSpeakingAudioId == "step_3_mobile") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .shadow(elevation = 4.dp, shape = CircleShape)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = if (activeSpeakingAudioId == "step_3_mobile") {
                                                            listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                        } else {
                                                            listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                        }
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (activeSpeakingAudioId == "step_3_mobile") {
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                                    } else {
                                                        val textToSpeak = context.getString(R.string.machine_registeration_step_3_mobile)
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "step_3_mobile")
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (activeSpeakingAudioId == "step_3_mobile") {
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
                                                            label = "bar_mobile_$i"
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 2.dp, height = 14.dp)
                                                                .graphicsLayer(scaleY = barHeightScale)
                                                                .background(Color.White, RoundedCornerShape(1.dp))
                                                        )
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.SupportAgent,
                                                    contentDescription = "آڈیو گائیڈ",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                 PhoneNumberInput(
                                     phone = phoneNumber,
                                     onPhoneChange = { input ->
                                         phoneNumber = input
                                         phoneError = null
                                     },
                                     isError = phoneError != null,
                                     modifier = Modifier.fillMaxWidth()
                                 )
                                 if (phoneError != null) {
                                     Text(
                                         text = phoneError ?: "",
                                         color = Color.Red,
                                         fontSize = 12.sp,
                                         modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                                     )
                                 }

                                Spacer(modifier = Modifier.height(16.dp))

                                // WhatsApp green button
                                 Button(
                                     onClick = {
                                         if (fullName.trim().isEmpty() || 
                                             fullName.trim() == "معزز کاشتکار" || 
                                             fullName.trim() == "کاشتکار معزز") {
                                             nameError = context.getString(R.string.err_enter_original_name)
                                             return@Button
                                         }
                                         if (cnic.length < 13) {
                                             cnicError = "درست شناختی کارڈ نمبر درج کریں"
                                             return@Button
                                         }
                                         if (phoneNumber.startsWith("03") && phoneNumber.length == 11) {
                                             phoneError = null
                                             isSubmitting = true
                                             val finalMachineQuantities = machineQuantities.mapKeys { (mach, _) ->
                                                 if (mach == "دیگر مشینیں") {
                                                     customMachineName.trim().ifEmpty { mach }
                                                 } else {
                                                     mach
                                                 }
                                             }
                                             
                                             viewModel.registerFarmerMachinery(
                                                 machineQuantities = finalMachineQuantities,
                                                 district = selectedDistrict ?: "سرگودھا",
                                                 phoneNumber = phoneNumber,
                                                 cnic = cnic,
                                                 fullName = fullName,
                                                 onSuccess = { response ->
                                                     isSubmitting = false
                                                     if (response.isOtpSent == true) {
                                                         Toast.makeText(
                                                             context,
                                                             context.getString(R.string.msg_verification_code_sent),
                                                             Toast.LENGTH_SHORT
                                                         ).show()
                                                         currentStep = 2
                                                     } else {
                                                         showSuccessDialog = true
                                                     }
                                                 },
                                                 onError = { error ->
                                                     isSubmitting = false
                                                     Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                 }
                                             )
                                         } else {
                                             phoneError = context.getString(R.string.err_invalid_phone)
                                         }
                                     },
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .height(56.dp),
                                     enabled = !isSubmitting,
                                     shape = RoundedCornerShape(16.dp),
                                     colors = ButtonDefaults.buttonColors(
                                         containerColor = Color(0xFF25D366),
                                         contentColor = Color.White
                                     )
                                 ) {
                                     Row(
                                         verticalAlignment = Alignment.CenterVertically,
                                         horizontalArrangement = Arrangement.Center
                                     ) {
                                         if (isSubmitting) {
                                             CircularProgressIndicator(
                                                 color = Color.White,
                                                 modifier = Modifier.size(24.dp)
                                             )
                                         } else {
                                             Icon(
                                                 imageVector = Icons.Default.Sms,
                                                 contentDescription = "WhatsApp Icon",
                                                 modifier = Modifier.size(24.dp)
                                             )
                                             Spacer(modifier = Modifier.width(10.dp))
                                             Text(
                                                 text = stringResource(id = R.string.btn_get_otp),
                                                 fontSize = 16.sp,
                                                 fontWeight = FontWeight.Bold
                                             )
                                         }
                                     }
                                 }
                            }
                        }
                    }

                    // --- STEP 3: OTP Verification & Submit ---
                    if (currentStep >= 2) {
                        StepContainer(
                            stepIndex = 2,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_otp_title),
                            summaryText = "",
                            isCompleted = false,
                            onEditClick = {}
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.step_otp_prompt),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreenPrimary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Audio Assistant Button for OTP
                                    Box(
                                        modifier = Modifier.size(48.dp).offset(y = (-6).dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (activeSpeakingAudioId == "step_3_otp") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer(scaleX = rippleScale1, scaleY = rippleScale1, alpha = rippleAlpha1)
                                                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .shadow(elevation = 4.dp, shape = CircleShape)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = if (activeSpeakingAudioId == "step_3_otp") {
                                                            listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                        } else {
                                                            listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                        }
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (activeSpeakingAudioId == "step_3_otp") {
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.stop()
                                                    } else {
                                                        val textToSpeak = context.getString(R.string.machine_registeration_step_3_otp)
                                                        pk.kissanmadadgar.mobile.data.local.NarrationManager.speak(textToSpeak, "step_3_otp")
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (activeSpeakingAudioId == "step_3_otp") {
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
                                                            label = "bar_otp_$i"
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 2.dp, height = 14.dp)
                                                                .graphicsLayer(scaleY = barHeightScale)
                                                                .background(Color.White, RoundedCornerShape(1.dp))
                                                        )
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.SupportAgent,
                                                    contentDescription = "آڈیو گائیڈ",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = stringResource(id = R.string.demo_otp_hint),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                UrduTextField(
                                    value = otpCode,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 4) {
                                            otpCode = input
                                            otpError = null
                                        }
                                    },
                                    label = "او ٹی پی کوڈ درج کریں",
                                    placeholder = "مثلاً: 0000",
                                    errorText = otpError,
                                    isPhoneNumber = true
                                )

                                 Spacer(modifier = Modifier.height(16.dp))

                                 UrduButton(
                                     text = if (isSubmitting) "براہ کرم انتظار کریں..." else stringResource(id = R.string.btn_submit_application),
                                     onClick = {
                                         if (otpCode.length == 4) {
                                             isSubmitting = true
                                             if (user == null) {
                                                 // Guest user needs OTP validation to get login response
                                                 viewModel.selectRole(pk.kissanmadadgar.mobile.domain.model.UserRole.PROVIDER)
                                                 viewModel.verifyOtp(
                                                     phone = phoneNumber,
                                                     otp = otpCode,
                                                     guestToken = viewModel.getGuestToken(),
                                                     type = "MACHINE_REGISTRATION",
                                                     onSuccess = {
                                                         isSubmitting = false
                                                         showSuccessDialog = true
                                                     },
                                                     onError = { error ->
                                                         isSubmitting = false
                                                         otpError = error
                                                     }
                                                 )
                                             } else {
                                                 isSubmitting = false
                                                 showSuccessDialog = true
                                             }
                                         } else {
                                             otpError = context.getString(R.string.err_invalid_otp)
                                         }
                                     },
                                     enabled = otpCode.length == 4 && !isSubmitting
                                 )
                            }
                        }
                    }
                }
            }
        }
        if (showSuccessDialog) {
            SuccessDialog(
                title = "درخواست موصول ہو گئی!",
                message = "مشینری رجسٹریشن کی درخواست موصول ہو گئی ہے!",
                onDismiss = {
                    showSuccessDialog = false
                    onSuccess()
                }
            )
        }
    }
}

@Composable
fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* Do nothing to ensure it only closes with X button */ },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC8E6C9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(45.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "ٹھیک ہے",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepContainer(
    stepIndex: Int,
    currentStep: Int,
    title: String,
    summaryText: String,
    isCompleted: Boolean,
    onEditClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val isActive = currentStep == stepIndex

    when {
        isActive -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, AgriGreenPrimary) // Sharp premium green border
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AgriGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (stepIndex + 1).toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }

        isCompleted -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
                border = BorderStroke(1.dp, Color(0xFFE2EDE5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small green check circle
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(AgriGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = summaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E6F40),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    // Minimal Edit Button as a clickable pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable { onEditClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.btn_edit),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MachineOptionCard(
    name: String,
    drawableResId: Int,
    imageUrl: String?,
    quantity: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = quantity != null
    Card(
        modifier = modifier
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White // Soft premium light green background on select
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AgriGreenPrimary else Color(0xFFEEEEEE) // Rich border outline
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp // Clean flat unselected elevation, clean soft shadow selected
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Selected corner checkmark badge (aligned top-right/top-start depending on layout, we put it top-start)
            if (isSelected && (quantity ?: 0) > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(AgriGreenPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعداد: $quantity",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Card content (Image on top, Text at the bottom)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    coil.compose.SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(24.dp))
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = drawableResId),
                                contentDescription = name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .background(Color.White)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = name,
                        contentScale = ContentScale.Fit, // Fit content to avoid clipping
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .background(Color.White) // Solid white background
                            .padding(8.dp) // Elegant padding to prevent touching card edges
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) AgriGreenPrimary else Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
