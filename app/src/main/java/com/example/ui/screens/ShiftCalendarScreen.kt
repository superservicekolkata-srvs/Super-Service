package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SuperServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftCalendarScreen(
    viewModel: SuperServiceViewModel,
    modifier: Modifier = Modifier
) {
    val shifts by viewModel.shiftList.collectAsState()
    var selectedShiftType by remember { mutableStateOf<ShiftType?>(null) }
    var notificationSentForShiftId by remember { mutableStateOf<String?>(null) }

    val filteredShifts = if (selectedShiftType != null) {
        shifts.filter { it.shiftType == selectedShiftType }
    } else {
        shifts
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Kolkata Facility Shift Scheduling & Roster",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = SuperIndigoSecondary
        )
        Text(
            text = "24/7 technical surveillance & preventative maintenance coverage",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Shift type filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedShiftType == null,
                onClick = { selectedShiftType = null },
                label = { Text("All", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedShiftType == ShiftType.MORNING,
                onClick = { selectedShiftType = ShiftType.MORNING },
                label = { Text("Morning (6A-2P)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedShiftType == ShiftType.GENERAL,
                onClick = { selectedShiftType = ShiftType.GENERAL },
                label = { Text("General (9A-6P)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedShiftType == ShiftType.NIGHT_VIGIL,
                onClick = { selectedShiftType = ShiftType.NIGHT_VIGIL },
                label = { Text("Night Vigil", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notificationSentForShiftId != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusApprovedBg,
                border = BorderStroke(1.dp, StatusApprovedBorder),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusApprovedText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Push notification & email alert dispatched to technician for shift coverage!",
                        fontSize = 11.sp,
                        color = StatusApprovedText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredShifts, key = { it.id }) { shift ->
                val typeColor = when (shift.shiftType) {
                    ShiftType.MORNING -> Color(0xFFF59E0B)
                    ShiftType.GENERAL -> SuperBluePrimary
                    ShiftType.NIGHT_VIGIL -> Color(0xFF7C3AED)
                    ShiftType.STANDBY -> Color(0xFFEF4444)
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = typeColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, typeColor)
                            ) {
                                Text(
                                    text = "${shift.shiftType.name.replace("_", " ")} SHIFT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = shift.dayOfWeek,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${shift.staffName} (${shift.staffId})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "${shift.department} • ${shift.facilitySite}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = SuperBluePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(shift.timing, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SuperBluePrimary)
                            }

                            IconButton(
                                onClick = { notificationSentForShiftId = shift.id },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Send Reminder Alert",
                                    tint = SuperBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
