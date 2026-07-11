package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.core.components.AgriConfirmationDialog
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary

/**
 * Star rating + comment popup for a COMPLETED booking. UI-only for now — onSubmit just hands back
 * (rating, comment); nothing is sent to the server yet.
 *
 * Below 3 stars, the comment becomes mandatory (poor experiences need an explanation), otherwise
 * it's optional.
 */
@Composable
fun FeedbackDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commentRequired = rating in 1..2

    AgriConfirmationDialog(
        title = "اپنی رائے دیں",
        onDismissRequest = onDismissRequest,
        confirmButtonText = "جمع کروائیں",
        onConfirm = {
            val trimmedComment = comment.trim()
            errorMessage = when {
                rating == 0 -> "براہ کرم ستاروں کے ذریعے درجہ بندی کریں۔"
                commentRequired && trimmedComment.isEmpty() -> "3 سے کم ستاروں کی صورت میں تبصرہ لکھنا لازمی ہے۔"
                else -> null
            }
            if (errorMessage == null) {
                onSubmit(rating, trimmedComment)
            }
        },
        dismissButtonText = "منسوخ کریں"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "آپ نے حاصل کردہ سروس کیسی محسوس کی؟",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF17251B),
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$i ستارے",
                        tint = if (i <= rating) Color(0xFFFFB300) else Color(0xFFCCCCCC),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                rating = i
                                errorMessage = null
                            }
                    )
                }
            }

            if (rating > 0) {
                Text(
                    text = ratingLabel(rating),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriGreenPrimary
                )
            }

            OutlinedTextField(
                value = comment,
                onValueChange = {
                    comment = it
                    if (it.isNotBlank()) errorMessage = null
                },
                label = { Text(if (commentRequired) "تبصرہ (لازمی)" else "تبصرہ (اختیاری)") },
                placeholder = { Text("اپنی رائے یہاں لکھیں...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = errorMessage != null
            )

            errorMessage?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun ratingLabel(rating: Int): String = when (rating) {
    1 -> "انتہائی ناقص"
    2 -> "ناقص"
    3 -> "اوسط"
    4 -> "اچھا"
    5 -> "بہترین"
    else -> ""
}
