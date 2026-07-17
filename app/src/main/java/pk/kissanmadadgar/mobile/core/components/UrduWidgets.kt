package pk.kissanmadadgar.mobile.core.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary

@Composable
fun AutoDismissAlert(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = 4000L,
    backgroundColor: Color = Color(0xFFE8F5E9), // Light premium green
    contentColor: Color = AgriGreenPrimary
) {
    var visible by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        visible = isVisible
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay(durationMs)
            visible = false
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun UrduButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AgriGreenPrimary,
    contentColor: Color = Color.White,
    fontSize: TextUnit = 18.sp
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
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun UrduTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    isPhoneNumber: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = if (isPhoneNumber) {
        androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done
        )
    } else {
        androidx.compose.foundation.text.KeyboardOptions.Default
    }
) {
    // Force RTL direction for Urdu input alignment
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = label, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                placeholder = { Text(text = placeholder, fontSize = 14.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                shape = RoundedCornerShape(12.dp),
                isError = errorText != null,
                keyboardOptions = keyboardOptions,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = AgriGreenPrimary,
                    focusedLabelColor = AgriGreenPrimary,
                    cursorColor = AgriGreenPrimary
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = Color.Black)
            )
            if (errorText != null) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun UrduLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AgriGreenPrimary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun PermissionDialog(
    rationaleText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AgriConfirmationDialog(
        title = "اجازت درکار ہے",
        onDismissRequest = onDismiss,
        confirmButtonText = "اجازت دیں",
        confirmButtonColor = AgriGreenPrimary,
        onConfirm = onConfirm,
        dismissButtonText = "کینسل کریں"
    ) {
        Text(
            text = rationaleText,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun AgriConfirmationDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "منظور کریں",
    confirmButtonColor: Color = AgriGreenPrimary,
    onConfirm: () -> Unit,
    dismissButtonText: String = "واپس",
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with title and cross button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = 8.dp, y = (-8).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Content
                    Box(modifier = Modifier.fillMaxWidth()) {
                        content()
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Confirm (Right side in RTL)
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButtonColor,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = confirmButtonText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Dismiss (Left side in RTL)
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = dismissButtonText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateReadyDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AgriConfirmationDialog(
        title = stringResource(id = pk.kissanmadadgar.mobile.R.string.update_ready_title),
        onDismissRequest = onDismiss,
        confirmButtonText = stringResource(id = pk.kissanmadadgar.mobile.R.string.update_ready_confirm_btn),
        onConfirm = onConfirm,
        dismissButtonText = stringResource(id = pk.kissanmadadgar.mobile.R.string.update_ready_dismiss_btn)
    ) {
        Text(
            text = stringResource(id = pk.kissanmadadgar.mobile.R.string.update_ready_message),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = Color.DarkGray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriAppHeader(
    title: String,
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadNotificationCount: Long = 0
) {
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
        modifier = modifier
            .shadow(
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
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 21.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.padding(end = 8.dp).size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                navigationIcon = {
                    val hasUnread = unreadNotificationCount > 0
                    // Runs continuously but is only applied to the icon/badge while hasUnread is
                    // true, so there's no cost to restarting the transition when the count changes.
                    val bellPulseTransition = rememberInfiniteTransition(label = "bell_unread_pulse")
                    val bellRotation by bellPulseTransition.animateFloat(
                        initialValue = -14f,
                        targetValue = 14f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(280, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bell_rotation"
                    )
                    val badgeScale by bellPulseTransition.animateFloat(
                        initialValue = 0.92f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "badge_pulse"
                    )

                    IconButton(
                        onClick = onBellClick,
                        modifier = Modifier.padding(start = 8.dp).size(48.dp)
                    ) {
                        // Not using BadgedBox here: its default badge offset pokes slightly above
                        // and outside the icon's own bounds, which lands outside the header
                        // Surface's clip = true region (see the shadow() modifier above) and gets
                        // sliced off — only a thin sliver of the badge was visible. Building the
                        // badge as a child aligned inside this Box keeps it fully within positive,
                        // in-bounds coordinates so it can never be clipped by the ancestor shape.
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(28.dp)
                                    .rotate(if (hasUnread) bellRotation else 0f)
                            )
                            if (hasUnread) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .scale(badgeScale)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD32F2F))
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriDetailHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
            .shadow(
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
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 21.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp).size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    }
}
