package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.CNICInputField
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary

@Composable
fun FarmerProfileTab(
    name: String,
    phone: String,
    address: String,
    cnic: String,
    district: String,
    districtsList: List<String>,
    isNameEditable: Boolean,
    isPhoneEditable: Boolean,
    isAddressEditable: Boolean,
    isCnicEditable: Boolean,
    isDistrictEditable: Boolean,
    supportMessage: String?,
    onSaveClick: (name: String, phone: String, address: String, cnic: String, district: String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    var editableName by remember(name) { mutableStateOf(name) }
    var editablePhone by remember(phone) { mutableStateOf(phone) }
    var editableAddress by remember(address) { mutableStateOf(address) }
    var editableCnic by remember(cnic) { mutableStateOf(cnic) }
    var editableDistrict by remember(district) { mutableStateOf(district) }
    val maxCharLimit = 30
    // Save validation below requires exactly 11 digits starting with "03" (a Pakistani mobile
    // number) — the input cap and the "X/11" counter must match that, not allow typing further.
    val maxPhoneLimit = 11

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var cnicError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var districtError by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color(0xFF333333),
        focusedBorderColor = AgriGreenPrimary,
        unfocusedBorderColor = Color.Gray,
        disabledBorderColor = Color(0xFFCCCCCC),
        focusedLabelColor = AgriGreenPrimary,
        disabledLabelColor = Color(0xFF555555),
        errorBorderColor = Color.Red,
        errorLabelColor = Color.Red
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        OutlinedTextField(
            value = editableName,
            onValueChange = { input ->
                if (input.length <= maxCharLimit) {
                    editableName = input
                    nameError = null
                }
            },
            label = { Text("کسان کا نام") },
            placeholder = { Text("اپنا نام درج کریں") },
            singleLine = true,
            enabled = isNameEditable,
            isError = nameError != null,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Text(
                        text = "${editableName.length}/$maxCharLimit",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = editablePhone,
            onValueChange = { input ->
                if (input.length <= maxPhoneLimit) {
                    editablePhone = input
                    phoneError = null
                }
            },
            label = { Text("موبائل نمبر") },
            placeholder = { Text("اپنا موبائل نمبر درج کریں") },
            singleLine = true,
            enabled = isPhoneEditable,
            isError = phoneError != null,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (phoneError != null) {
                        Text(
                            text = phoneError!!,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Text(
                        text = "${editablePhone.length}/$maxPhoneLimit",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        CNICInputField(
            cnic = editableCnic,
            onCnicChange = { input ->
                editableCnic = input
                cnicError = null
            },
            enabled = isCnicEditable,
            isError = cnicError != null,
            modifier = Modifier.fillMaxWidth(0.95f)
        )

        if (cnicError != null) {
            Text(
                text = cnicError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(start = 12.dp, top = 4.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = editableAddress,
            onValueChange = { input ->
                editableAddress = input
                addressError = null
            },
            label = { Text("فارمنگ ایڈریس / پتہ") },
            placeholder = { Text("اپنا پتہ درج کریں") },
            singleLine = true,
            enabled = isAddressEditable,
            isError = addressError != null,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            supportingText = if (addressError != null) {
                {
                    Text(
                        text = addressError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        var dropdownExpanded by remember { mutableStateOf(false) }

        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded && isDistrictEditable,
            onExpandedChange = { if (isDistrictEditable) dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = editableDistrict.ifEmpty { "ضلع منتخب کریں" },
                onValueChange = {},
                readOnly = true,
                enabled = isDistrictEditable,
                label = { Text("ضلع") },
                isError = districtError != null,
                trailingIcon = { if (isDistrictEditable) ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth(0.95f).menuAnchor(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                supportingText = if (districtError != null) {
                    {
                        Text(
                            text = districtError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else null
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                districtsList.forEach { distName ->
                    DropdownMenuItem(
                        text = { Text(text = distName, fontSize = 16.sp) },
                        onClick = {
                            editableDistrict = distName
                            dropdownExpanded = false
                            districtError = null
                        }
                    )
                }
            }
        }
        
        if (!supportMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = supportMessage,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UrduButton(
                text = stringResource(id = R.string.btn_save),
                onClick = {
                    var hasError = false
                    
                    val trimmedName = editableName.trim()
                    if (trimmedName.isEmpty()) {
                        nameError = "براہ کرم اپنا نام درج کریں"
                        hasError = true
                    } else if (trimmedName == "معزز کاشتکار" || 
                               trimmedName == "کاشتکار معزز" || 
                               trimmedName.equals("mauziz kashatkar", ignoreCase = true) || 
                               trimmedName.equals("kashatkar", ignoreCase = true)) {
                        nameError = "براہ کرم اپنا اصل نام درج کریں"
                        hasError = true
                    } else {
                        nameError = null
                    }
                    
                    val trimmedPhone = editablePhone.trim()
                    if (trimmedPhone.isEmpty()) {
                        phoneError = "براہ کرم اپنا موبائل نمبر درج کریں"
                        hasError = true
                    } else if (!trimmedPhone.startsWith("03") || trimmedPhone.length != 11) {
                        phoneError = "براہ کرم درست 11 ہندسوں کا موبائل نمبر درج کریں"
                        hasError = true
                    } else {
                        phoneError = null
                    }
                    
                    val digitsCnic = editableCnic.filter { it.isDigit() }
                    if (digitsCnic.isEmpty()) {
                        cnicError = "شناختی کارڈ نمبر درج کریں"
                        hasError = true
                    } else if (digitsCnic.length != 13) {
                        cnicError = "درست شناختی کارڈ نمبر درج کریں"
                        hasError = true
                    } else {
                        cnicError = null
                    }
                    
                    val trimmedAddress = editableAddress.trim()
                    if (trimmedAddress.isEmpty()) {
                        addressError = "براہ کرم اپنا پتہ درج کریں"
                        hasError = true
                    } else {
                        addressError = null
                    }
                    
                    val trimmedDistrict = editableDistrict.trim()
                    if (trimmedDistrict.isEmpty() || trimmedDistrict == "ضلع منتخب کریں") {
                        districtError = "ضلع منتخب کریں"
                        hasError = true
                    } else {
                        districtError = null
                    }
                    
                    if (!hasError) {
                        onSaveClick(trimmedName, trimmedPhone, trimmedAddress, digitsCnic, trimmedDistrict)
                    }
                },
                containerColor = AgriGreenPrimary,
                modifier = Modifier.weight(1f).height(50.dp),
                fontSize = 15.sp
            )

            UrduButton(
                text = stringResource(id = R.string.btn_logout),
                onClick = onLogout,
                containerColor = Color.Red.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f).height(50.dp),
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FarmerGuestProfileTab(onLoginRedirect: () -> Unit, supportMessage: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.guest_farmer_title), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = stringResource(id = R.string.guest_farmer_subtitle), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.guest_farmer_desc),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        UrduButton(
            text = stringResource(id = R.string.btn_login_create_account),
            onClick = onLoginRedirect
        )

        if (!supportMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = supportMessage,
                fontSize = 13.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )
        }
    }
}
