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
import com.example.ui.components.ApprovalBadge
import com.example.ui.theme.*
import com.example.viewmodel.SuperServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: SuperServiceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val staffList by viewModel.staffList.collectAsState()
    val taskList by viewModel.taskList.collectAsState()
    val debtList by viewModel.debtList.collectAsState()
    val jobApplications by viewModel.jobApplications.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Approvals & Staff, 1: Performance & Payroll, 2: Debt & Billing, 3: Recruitment
    var selectedStaffForPayslip by remember { mutableStateOf<StaffMember?>(null) }
    var selectedDebtForPayment by remember { mutableStateOf<DebtAccount?>(null) }

    val pendingCount = staffList.count { !it.isApprovedByOwner || it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
    val approvedCount = staffList.count { it.isApprovedByOwner && it.approvalStatus == ApprovalStatus.APPROVED }
    val totalDebt = debtList.sumOf { it.debtBalance }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Summary KPI Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Owner & Administrator Control Center",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SuperIndigoSecondary
                    )
                )
                Text(
                    text = "superservice.kolkata@gmail.com • Master Facility Operations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metric 1: Pending Approvals
                    KpiCard(
                        title = "Pending Approvals",
                        value = "$pendingCount",
                        sub = if (pendingCount > 0) "Action Required" else "All Clear",
                        badgeColor = if (pendingCount > 0) StatusPendingBorder else StatusApprovedBorder,
                        modifier = Modifier.weight(1f)
                    )

                    // Metric 2: Active Technicians
                    KpiCard(
                        title = "Active Staff",
                        value = "$approvedCount",
                        sub = "Authorized",
                        badgeColor = SuperBluePrimary,
                        modifier = Modifier.weight(1f)
                    )

                    // Metric 3: Outstanding Debt
                    KpiCard(
                        title = "Debt Balance",
                        value = "₹${(totalDebt / 1000).toInt()}k",
                        sub = "Receivable",
                        badgeColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Sub Tabs
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SuperBluePrimary,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Staff Approvals ($pendingCount)", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text(AppStrings.get("payslip_generator", language), fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Payments, contentDescription = null) }
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text(AppStrings.get("debt_management", language), fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
            )
            Tab(
                selected = activeSubTab == 3,
                onClick = { activeSubTab = 3 },
                text = { Text("Hiring & CVs (${jobApplications.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.WorkOutline, contentDescription = null) }
            )
        }

        // TAB CONTENT
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            when (activeSubTab) {
                0 -> StaffApprovalsSection(
                    staffList = staffList,
                    language = language,
                    onApprove = { viewModel.approveStaff(it) },
                    onReject = { viewModel.rejectStaff(it) }
                )
                1 -> PerformanceAndPayrollSection(
                    staffList = staffList,
                    language = language,
                    onViewPayslip = { selectedStaffForPayslip = it }
                )
                2 -> DebtAndBillingSection(
                    debtList = debtList,
                    language = language,
                    onCollectPayment = { selectedDebtForPayment = it }
                )
                3 -> HiringAndRecruitmentSection(
                    applications = jobApplications,
                    language = language
                )
            }
        }
    }

    // Modal: Payslip & Payroll Export
    selectedStaffForPayslip?.let { staff ->
        PayslipDetailModal(
            staff = staff,
            language = language,
            onDismiss = { selectedStaffForPayslip = null }
        )
    }

    // Modal: Record Debt Payment
    selectedDebtForPayment?.let { debt ->
        RecordPaymentModal(
            debt = debt,
            onDismiss = { selectedDebtForPayment = null },
            onConfirmPayment = { amt ->
                viewModel.recordDebtPayment(debt.id, amt)
                selectedDebtForPayment = null
            }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    sub: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = badgeColor
            )
            Text(
                text = sub,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StaffApprovalsSection(
    staffList: List<StaffMember>,
    language: AppLanguage,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    var filterPendingOnly by remember { mutableStateOf(false) }

    val displayedStaff = if (filterPendingOnly) {
        staffList.filter { !it.isApprovedByOwner || it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
    } else {
        staffList
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Staff Roster & Admin Approvals",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                FilterChip(
                    selected = filterPendingOnly,
                    onClick = { filterPendingOnly = !filterPendingOnly },
                    label = { Text("Pending Only") },
                    leadingIcon = {
                        if (filterPendingOnly) {
                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        items(displayedStaff, key = { it.id }) { staff ->
            StaffApprovalCard(
                staff = staff,
                language = language,
                onApprove = { onApprove(staff.id) },
                onReject = { onReject(staff.id) }
            )
        }
    }
}

@Composable
private fun StaffApprovalCard(
    staff: StaffMember,
    language: AppLanguage,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isPending = !staff.isApprovedByOwner || staff.approvalStatus == ApprovalStatus.PENDING_APPROVAL

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.5.dp,
            if (isPending) StatusPendingBorder else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPending) 3.dp else 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("staff_card_${staff.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = staff.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SuperBluePrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = staff.id,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuperBluePrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${staff.department} • ${staff.facilitySite}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ApprovalBadge(status = staff.approvalStatus, isApproved = staff.isApprovedByOwner)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = staff.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = staff.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Interactive Owner Approval Buttons (Rendered prominently for pending staff)
            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = StatusPendingBorder.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Admin Action:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusPendingText
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Reject Button
                        OutlinedButton(
                            onClick = onReject,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("reject_btn_${staff.id}")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.get("reject_btn", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Approve Button
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApprovedBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("approve_btn_${staff.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.get("approve_btn", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = StatusApprovedBorder,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Authorized for Facility Operations & Duty Clock-In",
                        fontSize = 11.sp,
                        color = StatusApprovedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceAndPayrollSection(
    staffList: List<StaffMember>,
    language: AppLanguage,
    onViewPayslip: (StaffMember) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuperBlueLight.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, SuperBluePrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = SuperBluePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kolkata Operations Performance & Payroll Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SuperBlueDark
                        )
                        Text(
                            text = "Tracks duty hours, on-time SLA metrics & auto-computes payslips",
                            fontSize = 12.sp,
                            color = SuperTextSecondary
                        )
                    }
                }
            }
        }

        items(staffList) { staff ->
            Card(
                shape = RoundedCornerShape(14.dp),
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
                        Column {
                            Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${staff.id} • ${staff.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { onViewPayslip(staff) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Payslip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Performance Metric Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill("Rating", "${staff.performanceRating} ★", SuperAzureTertiary, Modifier.weight(1f))
                        MetricPill("On-Time SLA", "${staff.onTimeRate}%", Color(0xFF10B981), Modifier.weight(1f))
                        MetricPill("Total Hours", "${staff.totalHoursWorked.toInt()} hrs", Color(0xFF7C3AED), Modifier.weight(1f))
                        MetricPill("Base Pay", "₹${staff.baseSalary.toInt()}", Color(0xFFF59E0B), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DebtAndBillingSection(
    debtList: List<DebtAccount>,
    language: AppLanguage,
    onCollectPayment: (DebtAccount) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            val totalInvoiced = debtList.sumOf { it.invoicedAmount }
            val totalPaid = debtList.sumOf { it.paidAmount }
            val totalOutstanding = debtList.sumOf { it.debtBalance }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Facility Billing & Debt Recovery Portfolio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SuperIndigoSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Invoiced", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${totalInvoiced.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Collected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${totalPaid.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Column {
                            Text("Overdue Debt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${totalOutstanding.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        items(debtList) { debt ->
            val statusColor = when (debt.status) {
                DebtStatus.CURRENT -> Color(0xFF10B981)
                DebtStatus.OVERDUE_30 -> Color(0xFFF59E0B)
                DebtStatus.OVERDUE_60 -> Color(0xFFEA580C)
                DebtStatus.CRITICAL_90 -> Color(0xFFDC2626)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(debt.clientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(debt.facilityName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = "${debt.ageingDays} Days Overdue",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Outstanding Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "₹${debt.debtBalance.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (debt.debtBalance > 0) Color(0xFFDC2626) else Color(0xFF10B981)
                            )
                        }

                        if (debt.debtBalance > 0) {
                            Button(
                                onClick = { onCollectPayment(debt) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Collection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD1FAE5)
                            ) {
                                Text(
                                    "Fully Settled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HiringAndRecruitmentSection(
    applications: List<JobApplication>,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Technician Job Applications (${applications.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        items(applications) { app ->
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
                        Text(app.candidateName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuperBluePrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                app.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuperBluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(app.jobTitle, fontSize = 12.sp, color = SuperBluePrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Phone: ${app.phone} • Email: ${app.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Relevant Experience: ${app.experienceYears} Years", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PayslipDetailModal(
    staff: StaffMember,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    var exportSuccess by remember { mutableStateOf(false) }

    val basePay = staff.baseSalary
    val facilityAllowance = 2500.0 // Kolkata site allowance
    val overtimeHours = (staff.totalHoursWorked - 160.0).coerceAtLeast(0.0)
    val overtimePay = overtimeHours * 150.0
    val grossSalary = basePay + facilityAllowance + overtimePay
    val deductions = 1200.0 // PF / ESI
    val netSalary = grossSalary - deductions

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SUPER SERVICE KOLKATA",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = SuperIndigoSecondary
                        )
                        Text(
                            text = "Monthly Technical Staff Payslip",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuperBluePrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Nov 2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuperBluePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Employee details
                Text("Employee: ${staff.name} (${staff.id})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Department: ${staff.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Site: ${staff.facilitySite}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Total Hours Logged: ${staff.totalHoursWorked} hrs (Overtime: ${overtimeHours} hrs)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Breakdown
                SalaryRow("Basic Monthly Pay", "₹${basePay.toInt()}")
                SalaryRow("Kolkata Facility Site Allowance", "+₹${facilityAllowance.toInt()}")
                SalaryRow("Overtime Duty Pay", "+₹${overtimePay.toInt()}")
                SalaryRow("Gross Earnings", "₹${grossSalary.toInt()}", isBold = true)
                SalaryRow("Provident Fund (PF) & ESI", "-₹${deductions.toInt()}", isDeduction = true)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                SalaryRow("Net Salary Payable", "₹${netSalary.toInt()}", isBold = true, isHighlight = true)

                Spacer(modifier = Modifier.height(16.dp))

                if (exportSuccess) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusApprovedBg,
                        border = BorderStroke(1.dp, StatusApprovedBorder),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "Payslip exported to /Documents/SuperService_Payslip_${staff.id}.pdf & CSV",
                            fontSize = 11.sp,
                            color = StatusApprovedText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = { exportSuccess = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.get("export_payroll", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SalaryRow(
    label: String,
    amount: String,
    isBold: Boolean = false,
    isDeduction: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isHighlight) 14.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) SuperIndigoSecondary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = amount,
            fontSize = if (isHighlight) 16.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.SemiBold,
            color = when {
                isHighlight -> SuperBluePrimary
                isDeduction -> Color(0xFFEF4444)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun RecordPaymentModal(
    debt: DebtAccount,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(debt.debtBalance.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Facility Payment Collection", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Client: ${debt.clientName}", fontWeight = FontWeight.SemiBold)
                Text("Facility: ${debt.facilityName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Outstanding: ₹${debt.debtBalance.toInt()}", fontSize = 12.sp, color = Color(0xFFEF4444))

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Received (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    onConfirmPayment(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Confirm Collection")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
