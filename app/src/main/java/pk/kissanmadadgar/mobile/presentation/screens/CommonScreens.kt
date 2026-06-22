package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.core.theme.AgriGreenSecondary
import pk.kissanmadadgar.mobile.core.theme.TextPrimaryDark
import pk.kissanmadadgar.mobile.domain.model.UserRole
import pk.kissanmadadgar.mobile.presentation.MainViewModel

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    var animationStage by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(100)
        animationStage = 1
        delay(700)
        animationStage = 2
        delay(800)
        animationStage = 3
        delay(900)
        animationStage = 4
        delay(1500)
        onNavigateNext()
    }

    val logosAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
    )
    val logosScale by animateFloatAsState(
        targetValue = if (animationStage >= 1) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )
    val textOffsetY by animateFloatAsState(
        targetValue = if (animationStage >= 2) 0f else -30f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )

    val presentsAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 3) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )
    val badgeScale by animateFloatAsState(
        targetValue = if (animationStage >= 3) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val appAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 4) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )
    val appScale by animateFloatAsState(
        targetValue = if (animationStage >= 4) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Background Watermark of Agriculture Logo (only watermark, scaled down and unclipped)
        Image(
            painter = painterResource(id = R.drawable.logo_punjab_gov),
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .alpha(0.05f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding section: using the official GovPunjabHeader containing both logos
            GovPunjabHeader(
                isDarkMode = false,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .graphicsLayer(
                        alpha = logosAlpha,
                        scaleX = logosScale,
                        scaleY = logosScale
                    )
            )

            // Middle section: app icon + app name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer(
                            alpha = presentsAlpha,
                            scaleX = badgeScale * pulseScale,
                            scaleY = badgeScale * pulseScale
                        )
                        .size(110.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.app_name),
                    color = AgriGreenPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.graphicsLayer(
                        alpha = appAlpha,
                        scaleX = appScale,
                        scaleY = appScale
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.splash_subtitle),
                    color = Color(0xFF4C5A50),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer(alpha = appAlpha)
                )
            }

            // Bottom spacing balancing the top height
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OnboardingBadge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(AgriGreenSecondary, AgriGreenPrimary)
                ),
                shape = CircleShape
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            GovPunjabHeader(
                isDarkMode = false,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (page == 0) {
                        OnboardingBadge(imageVector = Icons.Default.Agriculture)
                    } else {
                        OnboardingBadge(imageVector = Icons.Default.Payments)
                    }
                    Spacer(modifier = Modifier.height(40.dp))

                    if (page == 0) {
                        Text(
                            text = stringResource(id = R.string.onboarding_title_1),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgriGreenPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(id = R.string.onboarding_desc_1),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E),
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.onboarding_title_2),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgriGreenPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(id = R.string.onboarding_desc_2),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E),
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.padding(bottom = 80.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (pagerState.currentPage == 0) AgriGreenPrimary else Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (pagerState.currentPage == 1) AgriGreenPrimary else Color.LightGray)
                )
            }
        }

        UrduButton(
            text = if (pagerState.currentPage == 0) stringResource(id = R.string.btn_next) else stringResource(id = R.string.btn_start),
            onClick = {
                if (pagerState.currentPage == 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                } else {
                    onFinish()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun RoleSelectionScreen(
    viewModel: MainViewModel,
    onRoleSelected: (UserRole) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            GovPunjabHeader(
                isDarkMode = false,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Top branding circle with Kissan Madadgar Icon
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AgriGreenPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.role_selection_subtitle),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4C5A50),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Kissan Option Card
            RoleCard(
                title = stringResource(id = R.string.role_farmer),
                subtitle = stringResource(id = R.string.role_farmer_subtitle),
                icon = Icons.Default.Group,
                onClick = {
                    viewModel.selectRole(UserRole.FARMER)
                    onRoleSelected(UserRole.FARMER)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Service Provider Option Card
            RoleCard(
                title = stringResource(id = R.string.role_provider),
                subtitle = stringResource(id = R.string.role_provider_subtitle),
                icon = Icons.Default.Agriculture,
                onClick = {
                    viewModel.selectRole(UserRole.PROVIDER)
                    onRoleSelected(UserRole.PROVIDER)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Admin Dedicated Button
            Row(
                modifier = Modifier
                    .clickable {
                        viewModel.selectRole(UserRole.ADMIN)
                        onRoleSelected(UserRole.ADMIN)
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.role_admin),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AgriGreenPrimary.copy(alpha = 0.5f),
                spotColor = AgriGreenPrimary
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(AgriGreenSecondary, AgriGreenPrimary)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, AgriGreenLight.copy(alpha = 0.25f))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF4C5A50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(color = AgriGreenLight, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
fun GovPunjabHeader(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val textColor = if (isDarkMode) Color.White else AgriGreenPrimary
    val subtitleColor = if (isDarkMode) AgriGreenLight else Color(0xFF4C5A50)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gov of Punjab Logo
        Image(
            painter = painterResource(id = R.drawable.logo_punjab_gov),
            contentDescription = null,
            modifier = Modifier.size(76.dp),
            contentScale = ContentScale.Fit
        )

        // Text Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(id = R.string.gov_header_title),
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.gov_header_subtitle),
                color = subtitleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        // PCAP Logo
        Image(
            painter = painterResource(id = R.drawable.logo_pcap),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )
    }
}
