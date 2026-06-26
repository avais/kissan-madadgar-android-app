package pk.kissanmadadgar.mobile.presentation.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import pk.kissanmadadgar.mobile.core.components.PhoneNumberInput
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.presentation.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAgriculturalMachineryScreen(
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // State holding selected machines (Set of strings)
    var selectedMachines by remember { mutableStateOf(emptySet<String>()) }
    var customMachineName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf<Int?>(null) }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    val user by viewModel.currentUser.collectAsState()
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    var otpCode by remember { mutableStateOf("") }

    // Step state: 0 (Machine), 1 (Quantity), 2 (District), 3 (Phone), 4 (OTP)
    var currentStep by remember { mutableStateOf(0) }

    // Error states
    var phoneError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }

    // Districts list
    val districts = listOf(
        "سرگودھا", "چنیوٹ", "فیصل آباد", "لاہور", "ملتان", 
        "بھکر", "جھنگ", "گوجرانوالہ", "ساہیوال"
    )

    // Other machinery options (dummy data for UI/UX approval)
    val otherMachineryOptions = listOf(
        "کلٹیویٹر",
        "روٹا ویٹر",
        "لیزر لینڈ لیولر",
        "ڈسک ہیرو",
        "تھریشر",
        "چاف کٹر",
        "بیج ڈرل",
        "مٹی پلٹنے والا ہل"
    )

    var customDropdownExpanded by remember { mutableStateOf(false) }

    // Progress percentage based on active step
    val progressFraction = (currentStep.toFloat() + 1f) / 5f

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
                        val formattedSummary = remember(selectedMachines, customMachineName) {
                            val list = selectedMachines.toMutableList()
                            if (list.contains("دیگر مشینیں")) {
                                list.remove("دیگر مشینیں")
                                if (customMachineName.isNotEmpty()) {
                                    list.add(customMachineName)
                                } else {
                                    list.add("دیگر")
                                }
                            }
                            list.joinToString("، ")
                        }
                        
                        StepContainer(
                            stepIndex = 0,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_select_machine_title),
                            summaryText = stringResource(id = R.string.summary_machine, formattedSummary),
                            isCompleted = selectedMachines.isNotEmpty() && (!selectedMachines.contains("دیگر مشینیں") || customMachineName.isNotEmpty()) && currentStep > 0,
                            onEditClick = { currentStep = 0 }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.step_select_machine_prompt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // 2x2 Grid for standard machines (using images)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MachineOptionCard(
                                        name = stringResource(id = R.string.machine_super_seeder),
                                        drawableResId = R.drawable.super_seeder_custom,
                                        isSelected = selectedMachines.contains("سپرسیڈر"),
                                        onClick = {
                                            selectedMachines = if (selectedMachines.contains("سپرسیڈر")) {
                                                selectedMachines - "سپرسیڈر"
                                            } else {
                                                selectedMachines + "سپرسیڈر"
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MachineOptionCard(
                                        name = stringResource(id = R.string.machine_baler),
                                        drawableResId = R.drawable.bailer,
                                        isSelected = selectedMachines.contains("بیلر"),
                                        onClick = {
                                            selectedMachines = if (selectedMachines.contains("بیلر")) {
                                                selectedMachines - "بیلر"
                                            } else {
                                                selectedMachines + "بیلر"
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MachineOptionCard(
                                        name = stringResource(id = R.string.machine_harvester),
                                        drawableResId = R.drawable.harvester,
                                        isSelected = selectedMachines.contains("ہارویسٹر"),
                                        onClick = {
                                            selectedMachines = if (selectedMachines.contains("ہارویسٹر")) {
                                                selectedMachines - "ہارویسٹر"
                                            } else {
                                                selectedMachines + "ہارویسٹر"
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MachineOptionCard(
                                        name = stringResource(id = R.string.machine_others),
                                        drawableResId = R.drawable.other_machinery_clean,
                                        isSelected = selectedMachines.contains("دیگر مشینیں"),
                                        onClick = {
                                            selectedMachines = if (selectedMachines.contains("دیگر مشینیں")) {
                                                selectedMachines - "دیگر مشینیں"
                                            } else {
                                                selectedMachines + "دیگر مشینیں"
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Expand dropdown if "Others" selected
                                if (selectedMachines.contains("دیگر مشینیں")) {
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
                                        if (selectedMachines.contains("دیگر مشینیں") && customMachineName.trim().isEmpty()) {
                                            Toast.makeText(context, "براہ کرم دیگر مشین منتخب کریں", Toast.LENGTH_SHORT).show()
                                        } else {
                                            currentStep = 1
                                        }
                                    },
                                    enabled = selectedMachines.isNotEmpty() && (!selectedMachines.contains("دیگر مشینیں") || customMachineName.trim().isNotEmpty())
                                )
                            }
                        }
                    }

                    // --- STEP 1: Quantity selection ---
                    if (currentStep >= 1) {
                        StepContainer(
                            stepIndex = 1,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_qty_title),
                            summaryText = stringResource(id = R.string.summary_qty, quantity ?: 1),
                            isCompleted = quantity != null && currentStep > 1,
                            onEditClick = { currentStep = 1 }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.step_qty_prompt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Quick Select Numbers Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(5) { i ->
                                        val num = i + 1
                                        val isSelected = quantity == num
                                        val label = if (num == 5) "5+" else num.toString()

                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) AgriGreenPrimary else AgriGreenLight)
                                                .border(
                                                    border = BorderStroke(
                                                        2.dp,
                                                        if (isSelected) AgriGreenPrimary else Color.Transparent
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    quantity = num
                                                    currentStep = 2
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color.White else AgriGreenPrimary,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Custom counter controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            val cur = quantity ?: 1
                                            if (cur > 1) quantity = cur - 1
                                        },
                                        modifier = Modifier
                                            .background(AgriGreenLight, CircleShape)
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = AgriGreenPrimary
                                        )
                                    }
                                    
                                    Text(
                                        text = "${quantity ?: 1} مشینیں",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            val cur = quantity ?: 1
                                            quantity = cur + 1
                                        },
                                        modifier = Modifier
                                            .background(AgriGreenLight, CircleShape)
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = AgriGreenPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                UrduButton(
                                    text = stringResource(id = R.string.btn_continue),
                                    onClick = {
                                        if (quantity == null) quantity = 1
                                        currentStep = 2
                                    }
                                )
                            }
                        }
                    }

                    // --- STEP 2: District Dropdown ---
                    if (currentStep >= 2) {
                        StepContainer(
                            stepIndex = 2,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_district_title),
                            summaryText = stringResource(id = R.string.summary_district, selectedDistrict ?: ""),
                            isCompleted = selectedDistrict != null && currentStep > 2,
                            onEditClick = { currentStep = 2 }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.step_district_prompt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                var dropdownExpanded by remember { mutableStateOf(false) }

                                ExposedDropdownMenuBox(
                                    expanded = dropdownExpanded,
                                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedDistrict ?: "اپنا ضلع منتخب کریں",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AgriGreenPrimary,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = if (selectedDistrict == null) Color.Gray else Color.Black
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        districts.forEach { dist ->
                                            DropdownMenuItem(
                                                text = { Text(text = dist, fontSize = 16.sp) },
                                                onClick = {
                                                    selectedDistrict = dist
                                                    dropdownExpanded = false
                                                    currentStep = 3
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                UrduButton(
                                    text = stringResource(id = R.string.btn_continue),
                                    onClick = {
                                        if (selectedDistrict != null) {
                                            currentStep = 3
                                        } else {
                                            Toast.makeText(context, "براہ کرم ضلع کا انتخاب کریں", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = selectedDistrict != null
                                )
                            }
                        }
                    }

                    // --- STEP 3: Phone Number Input ---
                    if (currentStep >= 3) {
                        StepContainer(
                            stepIndex = 3,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_phone_title),
                            summaryText = stringResource(id = R.string.summary_phone, phoneNumber),
                            isCompleted = phoneNumber.length >= 10 && currentStep > 3,
                            onEditClick = { currentStep = 3 }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.step_phone_prompt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

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
                                        if (phoneNumber.startsWith("03") && phoneNumber.length == 11) {
                                            phoneError = null
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.msg_verification_code_sent),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            currentStep = 4
                                        } else {
                                            phoneError = context.getString(R.string.err_invalid_phone)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
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

                    // --- STEP 4: OTP Verification & Submit ---
                    if (currentStep >= 4) {
                        StepContainer(
                            stepIndex = 4,
                            currentStep = currentStep,
                            title = stringResource(id = R.string.step_otp_title),
                            summaryText = "",
                            isCompleted = false,
                            onEditClick = {}
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.step_otp_prompt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
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
                                    text = stringResource(id = R.string.btn_submit_application),
                                    onClick = {
                                        if (otpCode.length == 4) {
                                            val finalMachineTypes = selectedMachines.map { mach ->
                                                if (mach == "دیگر مشینیں") {
                                                    customMachineName.trim()
                                                } else {
                                                    mach
                                                }
                                            }.filter { it.isNotEmpty() }

                                            viewModel.registerFarmerMachinery(
                                                machineTypes = finalMachineTypes,
                                                quantity = quantity ?: 1,
                                                district = selectedDistrict ?: "سرگودھا",
                                                phoneNumber = phoneNumber,
                                                onSuccess = onSuccess
                                            )
                                        } else {
                                            otpError = context.getString(R.string.err_invalid_otp)
                                        }
                                    },
                                    enabled = otpCode.length == 4
                                )
                            }
                        }
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
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp) // Increased height to comfortably house a larger image + text
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
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(18.dp)
                        .background(AgriGreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
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
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = name,
                    contentScale = ContentScale.Fit, // Fit content to avoid clipping
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .background(Color.White) // Solid white background
                        .padding(8.dp) // Elegant padding to prevent touching card edges
                        .clip(RoundedCornerShape(14.dp))
                )
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
