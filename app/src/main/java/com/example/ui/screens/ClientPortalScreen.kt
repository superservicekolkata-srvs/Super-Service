package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.window.Dialog
import com.example.localization.AppLanguage
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SuperServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPortalScreen(
    viewModel: SuperServiceViewModel,
    modifier: Modifier = Modifier
) {
    val taskList by viewModel.taskList.collectAsState()
    val bookings by viewModel.clientBookings.collectAsState()
    val jobPostings by viewModel.jobPostings.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Project Logs & Sign-Off, 1: Book Service Audit, 2: Technical Careers
    var showBookModal by remember { mutableStateOf(false) }
    var selectedTaskForSignOff by remember { mutableStateOf<TaskItem?>(null) }
    var selectedJobForApply by remember { mutableStateOf<JobPosting?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Client Engagement & Facility Service Portal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SuperIndigoSecondary
                    )
                )
                Text(
                    text = "Track ongoing operations, confirm completed audits & request quotes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sub Navigation
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SuperBluePrimary
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Service Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Book Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("Careers", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (activeSubTab) {
                0 -> ClientServiceLogsList(
                    tasks = taskList,
                    onSignOff = { selectedTaskForSignOff = it }
                )
                1 -> BookingsList(
                    bookings = bookings,
                    onOpenBookingModal = { showBookModal = true }
                )
                2 -> CareersList(
                    jobs = jobPostings,
                    onApply = { selectedJobForApply = it }
                )
            }
        }
    }

    if (showBookModal) {
        BookServiceModal(
            onDismiss = { showBookModal = false },
            onConfirm = { name, phone, service, facility, date, slot, notes ->
                viewModel.createBooking(name, phone, service, facility, date, slot, notes)
                showBookModal = false
            }
        )
    }

    selectedTaskForSignOff?.let { task ->
        ClientSignOffModal(
            task = task,
            onDismiss = { selectedTaskForSignOff = null },
            onConfirmSignOff = { rating, feedback ->
                viewModel.confirmProjectCompletion(task.id, rating, feedback)
                selectedTaskForSignOff = null
            }
        )
    }

    selectedJobForApply?.let { job ->
        ApplyJobModal(
            job = job,
            onDismiss = { selectedJobForApply = null },
            onSubmit = { name, phone, email, exp ->
                viewModel.applyForJob(job.id, job.title, name, phone, email, exp)
                selectedJobForApply = null
            }
        )
    }
}

@Composable
private fun ClientServiceLogsList(
    tasks: List<TaskItem>,
    onSignOff: (TaskItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tasks) { task ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(task.facilitySite, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuperBluePrimary)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (task.isClientConfirmed) StatusApprovedBg else StatusPendingBg
                        ) {
                            Text(
                                text = if (task.isClientConfirmed) "Client Confirmed ★" else task.status.name.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isClientConfirmed) StatusApprovedText else StatusPendingText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(task.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Department: ${task.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Assigned Lead: ${task.assignedStaffName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (task.isClientConfirmed && task.clientRating != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuperBlueLight.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Rating: ${task.clientRating} / 5.0 ★", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuperBlueDark)
                                if (task.clientFeedback.isNotBlank()) {
                                    Text("\"${task.clientFeedback}\"", fontSize = 11.sp, color = SuperTextSecondary)
                                }
                            }
                        }
                    }

                    if (!task.isClientConfirmed && task.status == TaskStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onSignOff(task) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Delivery & Sign Off", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingsList(
    bookings: List<ClientBooking>,
    onOpenBookingModal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = onOpenBookingModal,
                colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Book On-Site Service Audit", fontWeight = FontWeight.Bold)
            }
        }

        items(bookings) { b ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(b.serviceType, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = StatusApprovedBg
                        ) {
                            Text(
                                b.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusApprovedText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Client: ${b.clientName} (${b.clientPhone})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Site: ${b.facilityLocation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Date & Slot: ${b.preferredDate} • ${b.preferredTimeSlot}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Estimated Quote: ₹${b.estimatedQuote.toInt()}", fontWeight = FontWeight.Bold, color = SuperBluePrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CareersList(
    jobs: List<JobPosting>,
    onApply: (JobPosting) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(jobs) { job ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${job.department} • ${job.facilitySite}", fontSize = 12.sp, color = SuperBluePrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Requirements: ${job.experienceRequired}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Compensation: ${job.monthlyCompensation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Open Vacancies: ${job.openPositions} technicians", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onApply(job) },
                        colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Apply for Position", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookServiceModal(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("HVAC Audit & Descaling") }
    var facility by remember { mutableStateOf("Sector V Tech Park") }
    var date by remember { mutableStateOf("2026-11-20") }
    var slot by remember { mutableStateOf("10:00 AM - 01:00 PM") }
    var notes by remember { mutableStateOf("") }

    val serviceTypes = listOf(
        "HVAC Audit & Descaling",
        "Generator Substation Check",
        "Deep Sanitization",
        "Fire & Hydrant Certification",
        "Elevator Safety Audit"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Book On-Site Facility Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Company / Client Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                var servExp by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = servExp, onExpandedChange = { servExp = !servExp }) {
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Service Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servExp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = servExp, onDismissRequest = { servExp = false }) {
                        serviceTypes.forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = { serviceType = s; servExp = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = facility,
                    onValueChange = { facility = it },
                    label = { Text("Kolkata Facility Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(clientName, phone, serviceType, facility, date, slot, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit Booking", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientSignOffModal(
    task: TaskItem,
    onDismiss: () -> Unit,
    onConfirmSignOff: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0f) }
    var feedback by remember { mutableStateOf("Service completed in full accordance with facility SLA.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Client Delivery Sign-Off", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(task.title, fontWeight = FontWeight.SemiBold)
                Text(task.facilitySite, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Satisfaction Rating: ${rating.toInt()} / 5 ★", fontWeight = FontWeight.Medium)
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Client Feedback / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSignOff(rating, feedback) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Sign & Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ApplyJobModal(
    job: JobPosting,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply: ${job.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("${job.department} • ${job.facilitySite}", fontSize = 12.sp, color = SuperBluePrimary)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = exp,
                    onValueChange = { exp = it },
                    label = { Text("Years of Relevant Experience") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSubmit(name, phone, email, exp.toIntOrNull() ?: 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary)
            ) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
