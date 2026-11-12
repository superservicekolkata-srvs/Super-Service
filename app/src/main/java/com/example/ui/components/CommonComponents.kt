package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TopSuperBar(
    role: UserRole?,
    staff: StaffMember?,
    language: AppLanguage,
    isOffline: Boolean,
    unsyncedCount: Int,
    onToggleOffline: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLangDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.super_service_logo),
                        contentDescription = "Super Service Kolkata Logo",
                        modifier = Modifier
                            .height(38.dp)
                            .padding(end = 10.dp)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SUPER SERVICE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = SuperIndigoSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SuperBluePrimary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "TM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuperBluePrimary,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Kolkata Facility & Ops",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Controls: Offline Sync chip, Language button, Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Offline sync chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOffline) StatusPendingBg else StatusApprovedBg,
                        border = BorderStroke(
                            1.dp,
                            if (isOffline) StatusPendingBorder else StatusApprovedBorder
                        ),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { onToggleOffline() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOffline) StatusPendingBorder else StatusApprovedBorder)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOffline) "Offline ($unsyncedCount)" else "Synced",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOffline) StatusPendingText else StatusApprovedText
                            )
                        }
                    }

                    // Language Selector
                    IconButton(
                        onClick = { showLangDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = language.code.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Switch Account / Logout
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Switch Account / Logout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Sub-row: User Identity Bar & E2EE Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val roleLabel = when (role) {
                        UserRole.ADMIN -> "Admin: superservice.kolkata@gmail.com"
                        UserRole.STAFF -> "Staff: ${staff?.id ?: ""} (${staff?.name ?: ""})"
                        UserRole.GUEST -> "Guest / Client Access"
                        null -> "Authentication Portal"
                    }
                    val roleColor = when (role) {
                        UserRole.ADMIN -> Color(0xFF7C3AED)
                        UserRole.STAFF -> SuperBluePrimary
                        UserRole.GUEST -> Color(0xFF059669)
                        null -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(roleColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = roleLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = roleColor
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "256-Bit E2EE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF059669)
                    )
                }
            }
        }
    }

    if (showLangDialog) {
        LanguageDialog(
            current = language,
            onDismiss = { showLangDialog = false },
            onSelect = {
                onSelectLanguage(it)
                showLangDialog = false
            }
        )
    }
}

@Composable
fun ApprovalBadge(status: ApprovalStatus, isApproved: Boolean) {
    val (bg, text, border, label) = when {
        status == ApprovalStatus.PENDING_APPROVAL || !isApproved -> {
            Quad(StatusPendingBg, StatusPendingText, StatusPendingBorder, "Approval Restricted: Pending Owner")
        }
        status == ApprovalStatus.APPROVED && isApproved -> {
            Quad(StatusApprovedBg, StatusApprovedText, StatusApprovedBorder, "Owner Approved")
        }
        else -> {
            Quad(StatusRejectedBg, StatusRejectedText, StatusRejectedBorder, "Approval Declined")
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                contentDescription = label,
                tint = text,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = text
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun LogoutConfirmDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = AppStrings.get("logout_confirm_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = AppStrings.get("logout_confirm_desc", language))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Sign Out")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LanguageDialog(
    current: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Language / भाषा चुनें / ভাষা নির্বাচন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AppLanguage.values().forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (lang == current) SuperBlueLight else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (lang == current) SuperBluePrimary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(lang) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.localName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (lang == current) SuperBluePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (lang == current) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = SuperBluePrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun MfaDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var otp by remember { mutableStateOf("882149") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = SuperBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enterprise 2FA Verification", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "A 6-digit Multi-Factor Authentication passcode was dispatched to superservice.kolkata@gmail.com for Admin Owner verification.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("6-Digit OTP Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onVerify(otp) },
                colors = ButtonDefaults.buttonColors(containerColor = SuperBluePrimary)
            ) {
                Text("Verify & Authorize")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
