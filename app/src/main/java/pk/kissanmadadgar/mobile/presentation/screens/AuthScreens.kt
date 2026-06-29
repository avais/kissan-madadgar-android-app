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
fun OtpVerificationScreen(
    phoneNumber: String,
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val displayPhone = if (phoneNumber.startsWith("+92")) "0" + phoneNumber.substring(3) else phoneNumber

    var otp by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        otp = ""
        errorText = null
    }
    
    val errOtpLength = stringResource(id = R.string.err_otp_length)
    val otpFocusRequester = remember { FocusRequester() }
    val otpSentMsg by viewModel.otpSentMessage.collectAsState()

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
                    text = otpSentMsg ?: stringResource(id = R.string.otp_subtitle),
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Display the phone number being verified
                Text(
                    text = displayPhone,
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

