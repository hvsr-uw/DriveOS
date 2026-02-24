package com.rivian.driveos.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Compose head-unit surface for the prototype.
 *
 * The screen intentionally includes diagnostic state alongside user-facing
 * controls because engineering reviewers can inspect whether the UI is driven by
 * real data flow, cache state, and service events.
 */
@Composable
fun DriveDashboardScreen(viewModel: DriveDashboardViewModel) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8F5)),
        color = Color(0xFFF7F8F5)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(state, onRefresh = viewModel::refresh)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VehicleStatusPanel(state, Modifier.weight(1f))
                MediaPanel(
                    state = state,
                    modifier = Modifier.weight(1f),
                    onTogglePlayback = viewModel::togglePlayback,
                    onNextTrack = viewModel::nextTrack
                )
                NavigationPanel(state, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AlertsPanel(state.alerts, Modifier.weight(1f))
                DiagnosticsPanel(state, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Header(state: DashboardUiState, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(state.vehicleTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(state.connectivityBanner, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF4D5A55))
            Text(state.dataQualityLabel, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B726D))
        }
        Button(onClick = onRefresh) {
            Text("Refresh")
        }
    }
}

@Composable
private fun VehicleStatusPanel(state: DashboardUiState, modifier: Modifier = Modifier) {
    DashboardCard("Vehicle", modifier) {
        Metric("Range", state.rangeLabel)
        Metric("Battery", state.batteryLabel)
        Metric("Cabin", state.cabinLabel)
        Metric("Tires", state.tirePressureLabel)
        Metric("Mode", state.driveModeLabel)
    }
}

@Composable
private fun MediaPanel(
    state: DashboardUiState,
    modifier: Modifier = Modifier,
    onTogglePlayback: () -> Unit,
    onNextTrack: () -> Unit
) {
    DashboardCard("Media", modifier) {
        Text(state.mediaTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(state.mediaSubtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF59615C))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onTogglePlayback) {
                Text(state.mediaControlLabel)
            }
            OutlinedButton(onClick = onNextTrack) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun NavigationPanel(state: DashboardUiState, modifier: Modifier = Modifier) {
    DashboardCard("Navigation", modifier) {
        Text(state.navigationTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(state.navigationSubtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF59615C))
    }
}

@Composable
private fun AlertsPanel(alerts: List<AlertRow>, modifier: Modifier = Modifier) {
    DashboardCard("Connectivity alerts", modifier) {
        if (alerts.isEmpty()) {
            Text("No active alerts", style = MaterialTheme.typography.bodyMedium)
        } else {
            alerts.forEach { alert ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(alert.title, fontWeight = FontWeight.SemiBold)
                        Text(alert.message, color = Color(0xFF59615C))
                    }
                    Text(alert.severity, modifier = Modifier.width(92.dp), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsPanel(state: DashboardUiState, modifier: Modifier = Modifier) {
    DashboardCard("Service diagnostics", modifier) {
        Text(state.lastUpdatedLabel, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF59615C))
        if (state.eventRows.isEmpty()) {
            Text("No sync events yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            state.eventRows.forEach { event ->
                Column {
                    Text(event.type, fontWeight = FontWeight.SemiBold)
                    Text(event.message, color = Color(0xFF59615C))
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(title: String, modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2A3832), fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF59615C))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
