package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.*
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.presentation.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierLoginScreen(
    viewModel: MainViewModel,
    onNavigateToOtp: (String) -> Unit,
    onBack: () -> Unit
) {
    var cnic by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Scaffold(
        containerColor = Color.White,
        topBar = {
            AgriDetailHeader(
                title = stringResource(id = R.string.supplier_login_title),
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AgriGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.supplier_login_cnic_req),
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                CNICInputField(
                    cnic = cnic,
                    onCnicChange = {
                        cnic = it
                        errorText = null
                    },
                    isError = errorText != null,
                    enabled = !isLoading,
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (cnic.length == 13) {
                                isLoading = true
                                viewModel.verifySupplierCnic(
                                    cnic = cnic,
                                    onSuccess = {
                                        isLoading = false
                                        onNavigateToOtp(cnic)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorText = err
                                    }
                                )
                            }
                        }
                    )
                )

                errorText?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorMessage(message = msg)
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = AgriGreenPrimary)
                } else {
                    PrimaryButton(
                        text = stringResource(id = R.string.btn_continue),
                        onClick = {
                            focusManager.clearFocus()
                            isLoading = true
                            viewModel.verifySupplierCnic(
                                cnic = cnic,
                                onSuccess = {
                                    isLoading = false
                                    onNavigateToOtp(cnic)
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorText = err
                                }
                            )
                        },
                        enabled = cnic.length == 13
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierOtpVerificationScreen(
    cnic: String,
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val supplier by viewModel.currentUser.collectAsState()
    var editablePhone by remember(supplier) { mutableStateOf(supplier?.phoneNumber ?: "") }
    val isPhoneValid = isValidPakistaniMobileNumber(editablePhone)
    
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val phoneErrorText = stringResource(id = R.string.auth_req_phone_error)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            AgriDetailHeader(
                title = stringResource(id = R.string.supplier_otp_title),
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                
                SupplierInfoCard(
                    name = supplier?.fullName ?: stringResource(id = R.string.supplier_name_unknown),
                    cnic = cnic,
                    phone = editablePhone,
                    onPhoneChange = {
                        editablePhone = it
                        errorText = null
                    },
                    isPhoneError = editablePhone.isNotEmpty() && !isPhoneValid
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.supplier_otp_sent_desc),
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OTPInput(
                    otp = otp,
                    onOtpChange = {
                        otp = it
                        errorText = null
                    },
                    isError = errorText != null,
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (!isPhoneValid) {
                                errorText = phoneErrorText
                            } else if (otp.length == 4) {
                                isLoading = true
                                viewModel.verifyOtp(
                                    phone = editablePhone,
                                    otp = otp,
                                    onSuccess = {
                                        isLoading = false
                                        onNavigateToHome()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorText = err
                                    }
                                )
                            }
                        }
                    )
                )

                errorText?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorMessage(message = msg)
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = AgriGreenPrimary)
                } else {
                    PrimaryButton(
                        text = stringResource(id = R.string.btn_verify),
                        onClick = {
                            focusManager.clearFocus()
                            if (!isPhoneValid) {
                                errorText = phoneErrorText
                                return@PrimaryButton
                            }
                            isLoading = true
                            viewModel.verifyOtp(
                                phone = editablePhone,
                                otp = otp,
                                onSuccess = {
                                    isLoading = false
                                    onNavigateToHome()
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorText = err
                                }
                            )
                        },
                        enabled = otp.length == 4 && isPhoneValid
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val errOtpLength = stringResource(id = R.string.err_otp_length)
    val otpFocusRequester = remember { FocusRequester() }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            AgriDetailHeader(
                title = stringResource(id = R.string.otp_title),
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Icon badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AgriGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.otp_subtitle),
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Display the phone number being verified
                Text(
                    text = phoneNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenPrimary
                )

                Spacer(modifier = Modifier.height(36.dp))

                // OTP digit boxes instead of plain text field
                OTPInput(
                    otp = otp,
                    onOtpChange = {
                        otp = it
                        errorText = null
                    },
                    isError = errorText != null,
                    focusRequester = otpFocusRequester
                )

                // Error display
                errorText?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorMessage(message = msg)
                }

                Spacer(modifier = Modifier.height(40.dp))

                PrimaryButton(
                    text = stringResource(id = R.string.btn_verify),
                    onClick = {
                        if (otp.length >= 4) {
                            viewModel.verifyOtp(phoneNumber, otp, onSuccess, { errorText = it })
                        } else {
                            errorText = errOtpLength
                        }
                    },
                    enabled = otp.length == 4
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.demo_otp_hint),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            AgriDetailHeader(
                title = stringResource(id = R.string.admin_login_title),
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Icon badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                UrduTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorText = null
                    },
                    label = stringResource(id = R.string.admin_email),
                    placeholder = "منتظم"
                )

                Spacer(modifier = Modifier.height(16.dp))

                UrduTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorText = null
                    },
                    label = stringResource(id = R.string.admin_password),
                    placeholder = "••••••••"
                )

                // Consistent error message component
                errorText?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorMessage(message = msg)
                }

                Spacer(modifier = Modifier.height(40.dp))

                PrimaryButton(
                    text = stringResource(id = R.string.btn_login),
                    onClick = {
                        viewModel.adminLogin(email, password, onSuccess, { errorText = it })
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(id = R.string.demo_admin_hint),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
