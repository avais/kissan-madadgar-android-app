package pk.kissanmadadgar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.core.components.UrduButton
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.domain.model.MachineryStatus
import pk.kissanmadadgar.mobile.presentation.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Agriculture, contentDescription = null) },
                    label = { Text(text = stringResource(id = R.string.admin_tab_requests)) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AgriGreenPrimary, selectedTextColor = AgriGreenPrimary)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = null) },
                    label = { Text(text = stringResource(id = R.string.admin_tab_analytics)) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AgriGreenPrimary, selectedTextColor = AgriGreenPrimary)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = null) },
                    label = { Text(text = stringResource(id = R.string.admin_tab_logs)) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AgriGreenPrimary, selectedTextColor = AgriGreenPrimary)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {
            when (selectedTab) {
                0 -> AdminRequestsTab(viewModel)
                1 -> AdminAnalyticsTab()
                2 -> AdminLogsTab(onLogout)
            }
        }
    }
}

@Composable
fun AdminRequestsTab(viewModel: MainViewModel) {
    val allMachinery by viewModel.adminAllMachinery.collectAsState()
    val pendingList = allMachinery.filter { it.status == MachineryStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.admin_pipeline_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AgriGreenPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (pendingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(id = R.string.admin_no_pending_requests), color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = item.nameUr, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = stringResource(id = R.string.hourly_rate_format, item.hourlyRate.toInt()), color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = item.descriptionUr, fontSize = 14.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.updateMachineryStatus(item.id, MachineryStatus.APPROVED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = stringResource(id = R.string.btn_approve))
                                }
                                
                                Button(
                                    onClick = { viewModel.updateMachineryStatus(item.id, MachineryStatus.REJECTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = stringResource(id = R.string.btn_reject))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = stringResource(id = R.string.admin_analytics_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AgriGreenLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stringResource(id = R.string.admin_total_farmers), color = AgriGreenPrimary, fontSize = 14.sp)
                        Text(text = stringResource(id = R.string.admin_farmers_count_dummy), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AgriGreenPrimary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AgriGreenLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stringResource(id = R.string.admin_total_providers), color = AgriGreenPrimary, fontSize = 14.sp)
                        Text(text = stringResource(id = R.string.admin_providers_count_dummy), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AgriGreenPrimary)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(id = R.string.admin_total_bookings_volume), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = stringResource(id = R.string.admin_bookings_count_dummy), fontSize = 24.sp, color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminLogsTab(onLogout: () -> Unit) {
    val logs = listOf(
        stringResource(id = R.string.admin_log_1),
        stringResource(id = R.string.admin_log_2),
        stringResource(id = R.string.admin_log_3),
        stringResource(id = R.string.admin_log_4),
        stringResource(id = R.string.admin_log_5)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.admin_audit_logs_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = log, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        UrduButton(
            text = stringResource(id = R.string.admin_btn_logout),
            onClick = onLogout,
            containerColor = Color.Red.copy(alpha = 0.8f)
        )
    }
}
