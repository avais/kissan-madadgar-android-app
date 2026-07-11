package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.AgriDetailHeader
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.data.local.NarrationManager
import pk.kissanmadadgar.mobile.data.remote.dto.GovernmentProjectDto
import pk.kissanmadadgar.mobile.presentation.MainViewModel

/**
 * Full "محکمہ زراعت کی اسکیمیں" screen — fetched live from
 * /api/auth/android/government-projects rather than hardcoded, so scheme copy/subsidy amounts/
 * pictures can change server-side without an app release. Narration audio text also comes from
 * the API (audioNarration per project) instead of a fixed in-app string.
 */
@Composable
fun GovernmentSchemesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val projects by viewModel.governmentProjects.collectAsState()
    val isLoading by viewModel.isLoadingGovernmentProjects.collectAsState()

    LaunchedEffect(Unit) {
        if (projects.isEmpty()) {
            viewModel.fetchGovernmentProjects()
        }
    }

    Scaffold(
        topBar = { AgriDetailHeader(title = "محکمہ زراعت کی اسکیمیں", onBackClick = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {
            when {
                isLoading && projects.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AgriGreenPrimary
                    )
                }
                projects.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "فی الحال کوئی اسکیم دستیاب نہیں ہے۔",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF17251B)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "حکومتِ پنجاب کے محکمہ زراعت کی فعال اسکیمیں",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF17251B)
                            )
                        }

                        itemsIndexed(projects) { index, project ->
                            SchemeCard(project = project, utteranceId = "gov_scheme_$index")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchemeCard(
    project: GovernmentProjectDto,
    utteranceId: String
) {
    var showGallery by remember { mutableStateOf(false) }
    val activeUtteranceId by NarrationManager.activeUtteranceId.collectAsState()
    val isSpeaking = activeUtteranceId == utteranceId

    val pictures = project.implementPictures.orEmpty()
    val headerImage = pictures.firstOrNull() ?: project.logo
    val galleryPictures = if (pictures.firstOrNull() != null) pictures.drop(1) else pictures

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // ── Header banner image ──────────────────────────────────
            if (!headerImage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(AgriGreenLight)
                ) {
                    SubcomposeAsyncImage(
                        model = headerImage,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AgriGreenPrimary)
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = R.drawable.super_seeder_custom),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (!project.shortName.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = project.shortName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = project.name,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = AgriGreenPrimary,
                    lineHeight = 26.sp
                )

                if (!project.implementNameUrdu.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(AgriGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = project.implementNameUrdu,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF444444),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Divider(color = Color(0xFFF0F0F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!project.audioNarration.isNullOrBlank()) {
                        Button(
                            onClick = {
                                if (isSpeaking) {
                                    NarrationManager.stop()
                                } else {
                                    NarrationManager.speak(project.audioNarration, utteranceId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpeaking) Color(0xFFE65100) else Color(0xFFFF8F00)
                            )
                        ) {
                            NarrationIcon(isSpeaking)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpeaking) "روکیں" else "بیانیہ سنیں",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (galleryPictures.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showGallery = !showGallery },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AgriGreenPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriGreenPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showGallery) "تصاویر چھپائیں" else "مزید تصاویر",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (showGallery && galleryPictures.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(galleryPictures) { url ->
                            SubcomposeAsyncImage(
                                model = url,
                                loading = {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(AgriGreenLight, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                                    }
                                },
                                error = {
                                    Image(
                                        painter = painterResource(id = R.drawable.super_seeder_custom),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NarrationIcon(isSpeaking: Boolean) {
    if (isSpeaking) {
        val infiniteTransition = rememberInfiniteTransition(label = "narration_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
            label = "scale"
        )
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
    } else {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
