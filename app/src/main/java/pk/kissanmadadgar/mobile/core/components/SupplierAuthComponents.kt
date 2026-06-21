package pk.kissanmadadgar.mobile.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary

// ==========================================
// 1. BUTTON COMPONENTS
// ==========================================

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AgriGreenPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color(0xFFF0F0F0),
    contentColor: Color = Color.DarkGray
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AgriGreenPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 2. INPUT COMPONENTS
// ==========================================

class CNICGuideTransformation : androidx.compose.ui.text.input.VisualTransformation {
    private var latestOriginalLength = 0

    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val original = text.text
        latestOriginalLength = original.length
        var out = ""
        val displayLength = 13
        val rawString = original.take(displayLength)
        
        for (i in 0 until displayLength) {
            val char = if (i < rawString.length) rawString[i] else '_'
            out += char
            if (i == 4 || i == 11) out += "-"
        }
        
        val offsetTranslator = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, latestOriginalLength)
                return when {
                    clamped <= 4 -> clamped
                    clamped <= 11 -> clamped + 1
                    else -> clamped + 2
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 5 -> offset
                    offset <= 13 -> offset - 1
                    else -> offset - 2
                }.coerceIn(0, latestOriginalLength)
            }
        }
        
        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(out),
            offsetTranslator
        )
    }
}

@Composable
fun CNICInputField(
    cnic: String,
    onCnicChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        imeAction = androidx.compose.ui.text.input.ImeAction.Done
    ),
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default
) {
    val cnicTransformation = remember { CNICGuideTransformation() }
    OutlinedTextField(
        value = cnic,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            if (digits.length <= 13) {
                onCnicChange(digits)
            }
        },
        label = { Text("شناختی کارڈ نمبر (CNIC)", color = if (isError) Color.Red else Color.DarkGray) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        isError = isError,
        visualTransformation = cnicTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = if (isError) Color.Red else AgriGreenPrimary,
            unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
            focusedLabelColor = if (isError) Color.Red else AgriGreenPrimary,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red
        ),
        enabled = enabled
    )
}


class PhoneGuideTransformation : androidx.compose.ui.text.input.VisualTransformation {
    private var latestOriginalLength = 0

    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val original = text.text
        latestOriginalLength = original.length
        var out = ""
        val displayLength = 11
        val rawString = original.take(displayLength)
        
        for (i in 0 until displayLength) {
            val char = if (i < rawString.length) rawString[i] else '_'
            out += char
            if (i == 3) out += "-"
        }
        
        val offsetTranslator = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, latestOriginalLength)
                return if (clamped <= 3) clamped else clamped + 1
            }
            override fun transformedToOriginal(offset: Int): Int {
                val mapped = if (offset <= 3) offset else (offset - 1)
                return mapped.coerceIn(0, latestOriginalLength)
            }
        }
        
        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(out),
            offsetTranslator
        )
    }
}

@Composable
fun PhoneNumberInput(
    phone: String,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        imeAction = androidx.compose.ui.text.input.ImeAction.Done
    ),
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default
) {
    val phoneTransformation = remember { PhoneGuideTransformation() }
    OutlinedTextField(
        value = phone,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            if (digits.length <= 11) {
                onPhoneChange(digits)
            }
        },
        label = { Text(stringResource(id = R.string.phone_number_label), color = if (isError) Color.Red else Color.DarkGray) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        isError = isError,
        visualTransformation = phoneTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = if (isError) Color.Red else AgriGreenPrimary,
            unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
            focusedLabelColor = if (isError) Color.Red else AgriGreenPrimary,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red
        ),
        enabled = enabled
    )
}

fun isValidPakistaniMobileNumber(phone: String): Boolean {
    return Regex("^03\\d{9}$").matches(phone)
}

@Composable
fun OTPInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        imeAction = androidx.compose.ui.text.input.ImeAction.Done
    ),
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Visual Boxes (Drawn underneath)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val char = if (i < otp.length) otp[i].toString() else ""
                val isFocusedBox = i == otp.length
                
                val borderColor = animateColorAsState(
                    targetValue = when {
                        isError -> Color.Red
                        isFocusedBox -> AgriGreenPrimary
                        else -> Color.LightGray
                    },
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
                
                val scale = animateFloatAsState(
                    targetValue = if (isFocusedBox) 1.05f else 1.0f,
                    animationSpec = androidx.compose.animation.core.tween(150)
                )
                
                Card(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, borderColor.value),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isFocusedBox) 6.dp else 1.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Hidden input container drawn ON TOP of the visual boxes to intercept all gestures safely
        androidx.compose.foundation.text.BasicTextField(
            value = otp,
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }
                if (digits.length <= 4) {
                    onOtpChange(digits)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .focusRequester(focusRequester)
                .graphicsLayer(alpha = 0.01f),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

// ==========================================
// 3. FEEDBACK COMPONENTS
// ==========================================

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF2F2))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(20.dp))
        Text(
            text = message,
            color = Color.Red,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = AgriGreenPrimary, modifier = Modifier.size(24.dp))
        Text(
            text = message,
            color = AgriGreenPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

// ==========================================
// 4. LAYOUT COMPONENTS
// ==========================================

@Composable
fun KeyboardAwareContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// ==========================================
// 5. SUPPLIER INFO COMPONENTS
// ==========================================

@Composable
fun SupplierInfoCard(
    name: String,
    cnic: String,
    phone: String,
    onPhoneChange: (String) -> Unit,
    isPhoneError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("سپلائر کا نام", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AgriGreenPrimary)
            }
            
            Column {
                Text("شناختی کارڈ نمبر", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                // Format CNIC for display
                val displayCnic = if (cnic.length == 13) {
                    "${cnic.substring(0, 5)}-${cnic.substring(5, 12)}-${cnic.substring(12)}"
                } else cnic
                Text(displayCnic, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
            }
            
            Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 4.dp))
            
            Text("رابطہ نمبر", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
            
            PhoneNumberInput(
                phone = phone,
                onPhoneChange = onPhoneChange,
                isError = isPhoneError,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// 6. FORM COMPONENTS (Booking)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "بکنگ کی تاریخ",
    isError: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    val displayDate = selectedDateMillis?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "تاریخ منتخب کریں"

    Box(modifier = modifier.clickable { showDialog = true }) {
        OutlinedTextField(
            value = displayDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = if (isError) Color.Red else Color.DarkGray) },
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = AgriGreenPrimary)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false, 
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = if (isError) Color.Red else Color.Gray,
                disabledLabelColor = if (isError) Color.Red else AgriGreenPrimary,
                disabledTrailingIconColor = AgriGreenPrimary
            )
        )
    }

    if (showDialog) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
        ) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        showDialog = false
                    }) {
                        Text("OK", color = AgriGreenPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    dateValidator = { utcTimeMillis ->
                        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        utcTimeMillis >= today
                    }
                )
            }
        }
    }
}

@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    helperText: String? = null,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
        imeAction = androidx.compose.ui.text.input.ImeAction.Done
    ),
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onValueChange(input)
                }
            },
            label = { Text(label, color = if (isError) Color.Red else Color.DarkGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = if (isError) Color.Red else AgriGreenPrimary,
                unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
                focusedLabelColor = if (isError) Color.Red else AgriGreenPrimary,
                errorBorderColor = Color.Red,
                errorLabelColor = Color.Red
            )
        )
        if (helperText != null) {
            Text(
                text = helperText,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun NotesInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.DarkGray) },
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
            imeAction = androidx.compose.ui.text.input.ImeAction.Default
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = AgriGreenPrimary,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = AgriGreenPrimary
        )
    )
}

@Composable
fun BookingCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            content()
        }
    }
}

@Composable
fun SummaryCard(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, AgriGreenPrimary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "بکنگ کا خلاصہ",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AgriGreenPrimary
            )
            Divider(color = Color(0xFFE0E0E0))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
