package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SuperServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOperationsScreen(
    viewModel: SuperServiceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val currentStaff by viewModel.currentStaff.collectAsState()
    val taskList by viewModel.taskList.collectAsState()
    val isClockedIn by viewModel.isClockedIn.collectAsState()
    val dutySeconds by viewModel.dutySeconds.collectAsState()
    val isOffline by viewModel.isOfflineSimulated.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

    var statusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showCreateTaskModal by remember { mutableStateOf(false) }
    var selectedFacilityForClockIn by remember { mutableStateOf("Sector V Tech Park") }

    // Filter tasks by current staff (or show all if admin/supervisor)
    val myTasks = taskList.filter { task ->
        val matchesStaff = if (currentRole == UserRole.STAFF && currentStaff != null) {
            task.assignedStaffId == currentStaff?.id || task.assignedStaffName == currentStaff?.name
        } else {
            true // Show all if admin or guest
        }
        val matchesStatus = statusFilter == null || task.status == statusFilter
        matchesStaff && matchesStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Attendance & GPS Duty Clock-In Card (Rendered for Staff & Admin)
        DutyClockInCard(
            isClockedIn = isClockedIn,
            dutySeconds = dutySeconds,
            isOffline = isOffline,
            unsyncedCount = unsyncedCount,
            staffName = currentStaff?.name ?: "Bikram Halder",
            selectedFacility = selectedFacilityForClockIn,
            onFacilityChange = { selectedFacilityForClockIn = it },
            onClockIn = { viewModel.clockIn(selectedFacilityForClockIn) },
            onClockOut = { viewModel.clockOut() },
            onToggleOffline = { viewModel.toggleOfflineMode() },
            language = language
        )

        // Task List Header with Filter Chips & Create Action
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentRole == UserRole.STAFF) "My Facility Tasks (${myTasks.size})" else "All Facility Projects (${myTasks.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Button(
                    onClick = { showCreateTaskModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("create_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == TaskStatus.IN_PROGRESS,
                    onClick = { statusFilter = TaskStatus.IN_PROGRESS },
                    label = { Text("In Progress", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == TaskStatus.PENDING,
                    onClick = { statusFilter = TaskStatus.PENDING },
                    label = { Text("Pending", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == TaskStatus.COMPLETED,
                    onClick = { statusFilter = TaskStatus.COMPLETED },
                    label = { Text("Done", fontSize = 11.sp) }
                )
            }
        }

        // Lazy Column with Tasks
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(myTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    language = language,
                    onStatusChange = { newStatus -> viewModel.updateTaskStatus(task.id, newStatus) },
                    onToggleCheckpoint = { idx -> viewModel.toggleTaskCheckpoint(task.id, idx) }
                )
            }
        }
    }

    if (showCreateTaskModal) {
        CreateTaskModal(
            staffList = viewModel.staffList.value,
            onDismiss = { showCreateTaskModal = false },
            onCreate = { title, facility, dept, staffId, staffName, priority, deadline, checkpoints ->
                viewModel.createNewTask(title, facility, dept, staffId, staffName, priority, deadline, checkpoints)
                showCreateTaskModal = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DutyClockInCard(
    isClockedIn: Boolean,
    dutySeconds: Long,
    isOffline: Boolean,
    unsyncedCount: Int,
    staffName: String,
    selectedFacility: String,
    onFacilityChange: (String) -> Unit,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onToggleOffline: () -> Unit,
    language: AppLanguage
) {
    val hours = dutySeconds / 3600
    val minutes = (dutySeconds % 3600) / 60
    val seconds = dutySeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val facilities = listOf(
        "Sector V Tech Park",
        "New Town Eco Hub",
        "Park Street Commercial Tower",
        "Rajarhat Logistics Park",
        "Alipore Corporate Complex",
        "Ballygunge Executive Hub"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClockedIn) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (isClockedIn) SuperBluePrimary else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isClockedIn) Color(0xFF38BDF8) else SuperBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPS Duty Attendance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isClockedIn) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Offline sync badge with toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOffline) StatusPendingBg else StatusApprovedBg,
                    border = BorderStroke(1.dp, if (isOffline) StatusPendingBorder else StatusApprovedBorder),
                    modifier = Modifier.clickable { onToggleOffline() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOffline) StatusPendingBorder else StatusApprovedBorder)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOffline) "Offline ($unsyncedCount)" else "Synced",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOffline) StatusPendingText else StatusApprovedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isClockedIn) {
                // Facility Dropdown for Clock-in
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFacility,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Site Location (Geo-Fenced)", fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        facilities.forEach { fac ->
                            DropdownMenuItem(
                                text = { Text(fac) },
                                onClick = { onFacilityChange(fac); expanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS Coords: 22.5804° N, 88.4378° E (Kolkata Verification Active)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onClockIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("clock_in_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PunchClock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppStrings.get("clock_in", language), fontWeight = FontWeight.Bold)
                }
            } else {
                // Currently on duty: Live Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ACTIVE SHIFT • $selectedFacility",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Technician: $staffName",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    // Digital Clock Timer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onClockOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("clock_out_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppStrings.get("clock_out", language), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    language: AppLanguage,
    onStatusChange: (TaskStatus) -> Unit,
    onToggleCheckpoint: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFEF4444)
        TaskPriority.MEDIUM -> Color(0xFFF59E0B)
        TaskPriority.NORMAL -> SuperBluePrimary
    }

    val statusColor = when (task.status) {
        TaskStatus.PENDING -> Color(0xFFF59E0B)
        TaskStatus.IN_PROGRESS -> SuperBluePrimary
        TaskStatus.UNDER_INSPECTION -> Color(0xFF7C3AED)
        TaskStatus.COMPLETED -> Color(0xFF10B981)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Priority & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = priorityColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${task.priority.name} PRIORITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = task.status.name.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${task.facilitySite} • ${task.department}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = SuperBluePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Assigned: ${task.assignedStaffName} (${task.assignedStaffId})",
                        fontSize = 11.sp,
                        color = SuperBluePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = task.deadline,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Checkpoints Progress
            if (task.checkpoints.isNotEmpty()) {
                val completedCount = task.completedCheckpoints.size
                val totalCount = task.checkpoints.size
                val progress = completedCount.toFloat() / totalCount.toFloat()

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inspection Checkpoints ($completedCount/$totalCount)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuperBluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = SuperBluePrimary,
                    trackColor = SuperBlueLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkpoint items
                task.checkpoints.forEachIndexed { index, checkpoint ->
                    val isDone = task.completedCheckpoints.contains(index)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCheckpoint(index) }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { onToggleCheckpoint(index) },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = checkpoint,
                            fontSize = 12.sp,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Action: Change Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Update Progress:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (task.status != TaskStatus.IN_PROGRESS && task.status != TaskStatus.COMPLETED) {
                        OutlinedButton(
                            onClick = { onStatusChange(TaskStatus.IN_PROGRESS) },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Start", fontSize = 10.sp)
                        }
                    }
                    if (task.status != TaskStatus.UNDER_INSPECTION && task.status != TaskStatus.COMPLETED) {
                        OutlinedButton(
                            onClick = { onStatusChange(TaskStatus.UNDER_INSPECTION) },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Inspect", fontSize = 10.sp)
                        }
                    }
                    if (task.status != TaskStatus.COMPLETED) {
                        Button(
                            onClick = { onStatusChange(TaskStatus.COMPLETED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Done", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Task Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskModal(
    staffList: List<StaffMember>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, TaskPriority, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var facility by remember { mutableStateOf("Sector V Tech Park") }
    var dept by remember { mutableStateOf("HVAC & Aircon Maintenance") }
    var selectedStaff by remember { mutableStateOf(staffList.firstOrNull() ?: StaffMember("SS-EMP-04", "Subhashis Roy", "", "", "", "", "", true, ApprovalStatus.APPROVED)) }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }
    var deadline by remember { mutableStateOf("Today, 06:00 PM") }
    var checkpoint1 by remember { mutableStateOf("Perform initial safety and circuit breaker isolation") }
    var checkpoint2 by remember { mutableStateOf("Inspect component tolerances and measure operating temperature") }
    var checkpoint3 by remember { mutableStateOf("Verify final test runs and submit client logsheet") }

    val facilities = listOf(
        "Sector V Tech Park",
        "New Town Eco Hub",
        "Park Street Commercial Tower",
        "Rajarhat Logistics Park",
        "Alipore Corporate Complex",
        "Ballygunge Executive Hub"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Create Kolkata Facility Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Set specifications, deadline reminders & technician assignment", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title & Equipment") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                var facExp by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = facExp,
                    onExpandedChange = { facExp = !facExp }
                ) {
                    OutlinedTextField(
                        value = facility,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Facility Site") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facExp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = facExp, onDismissRequest = { facExp = false }) {
                        facilities.forEach { f ->
                            DropdownMenuItem(text = { Text(f) }, onClick = { facility = f; facExp = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var staffExp by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = staffExp,
                    onExpandedChange = { staffExp = !staffExp }
                ) {
                    OutlinedTextField(
                        value = "${selectedStaff.name} (${selectedStaff.id})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Technician") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = staffExp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = staffExp, onDismissRequest = { staffExp = false }) {
                        staffList.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.id}) - ${s.department}") },
                                onClick = { selectedStaff = s; staffExp = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Target Deadline & Reminder") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(
                                    title,
                                    facility,
                                    selectedStaff.department,
                                    selectedStaff.id,
                                    selectedStaff.name,
                                    priority,
                                    deadline,
                                    listOf(checkpoint1, checkpoint2, checkpoint3)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dispatch Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
