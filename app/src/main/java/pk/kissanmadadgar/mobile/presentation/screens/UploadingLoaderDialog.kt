package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary

/**
 * Blocking loader shown while a farming start/complete action is being uploaded synchronously
 * (device has internet, so we wait for the real result instead of silently queueing it). Not
 * dismissible — the whole point of the synchronous path is a definitive success/queued outcome,
 * not an ambiguous "maybe it worked" state the user has to guess at.
 */
@Composable
fun UploadingLoaderDialog(message: String = "آپ کی درخواست اپ لوڈ ہو رہی ہے") {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "upload_pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(AgriGreenLight, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                CircularProgressIndicator(
                    color = AgriGreenPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF17251B),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "براہ کرم چند لمحوں کے لیے انتظار کریں۔",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
