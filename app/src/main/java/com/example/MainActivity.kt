package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.UserRole
import com.example.ui.components.LogoutConfirmDialog
import com.example.ui.components.MfaDialog
import com.example.ui.components.TopSuperBar
import com.example.ui.screens.*
import com.example.ui.theme.SuperServiceTheme
import com.example.viewmodel.SuperServiceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SuperServiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperServiceTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: SuperServiceViewModel) {
    val currentRole by viewModel.currentUserRole.collectAsState()
    val currentStaff by viewModel.currentStaff.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val tabIndex by viewModel.currentTabIndex.collectAsState()
    val isOffline by viewModel.isOfflineSimulated.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val showMfa by viewModel.showMfaPrompt.collectAsState()

    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopSuperBar(
                role = currentRole,
                staff = currentStaff,
                language = language,
                isOffline = isOffline,
                unsyncedCount = unsyncedCount,
                onToggleOffline = { viewModel.toggleOfflineMode() },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onLogoutClick = {
                    if (currentRole != null) {
                        showLogoutConfirm = true
                    } else {
                        viewModel.logout()
                    }
                }
            )
        },
        bottomBar = {
            if (currentRole != null) {
                SuperBottomNavigation(
                    role = currentRole!!,
                    currentIndex = tabIndex,
                    language = language,
                    onSelectTab = { viewModel.setTabIndex(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentRole == null) {
                AuthScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                when (currentRole) {
                    UserRole.ADMIN -> {
                        when (tabIndex) {
                            0 -> AdminDashboardScreen(viewModel = viewModel)
                            1 -> StaffOperationsScreen(viewModel = viewModel)
                            2 -> ShiftCalendarScreen(viewModel = viewModel)
                            3 -> ClientPortalScreen(viewModel = viewModel)
                            else -> AdminDashboardScreen(viewModel = viewModel)
                        }
                    }
                    UserRole.STAFF -> {
                        when (tabIndex) {
                            0 -> StaffOperationsScreen(viewModel = viewModel)
                            1 -> ShiftCalendarScreen(viewModel = viewModel)
                            2 -> ClientPortalScreen(viewModel = viewModel)
                            else -> StaffOperationsScreen(viewModel = viewModel)
                        }
                    }
                    UserRole.GUEST -> {
                        when (tabIndex) {
                            0 -> ClientPortalScreen(viewModel = viewModel)
                            1 -> StaffOperationsScreen(viewModel = viewModel)
                            2 -> ShiftCalendarScreen(viewModel = viewModel)
                            else -> ClientPortalScreen(viewModel = viewModel)
                        }
                    }
                    null -> {}
                }
            }
        }
    }

    if (showLogoutConfirm) {
        LogoutConfirmDialog(
            language = language,
            onDismiss = { showLogoutConfirm = false },
            onConfirm = {
                showLogoutConfirm = false
                viewModel.logout()
            }
        )
    }

    if (showMfa) {
        MfaDialog(
            onDismiss = { viewModel.cancelMfa() },
            onVerify = { otp -> viewModel.verifyMfaCode(otp) }
        )
    }
}

private data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
private fun SuperBottomNavigation(
    role: UserRole,
    currentIndex: Int,
    language: AppLanguage,
    onSelectTab: (Int) -> Unit
) {
    val items = when (role) {
        UserRole.ADMIN -> listOf(
            NavItem(
                label = AppStrings.get("tab_dashboard", language),
                selectedIcon = Icons.Default.AdminPanelSettings,
                unselectedIcon = Icons.Outlined.AdminPanelSettings,
                testTag = "nav_admin"
            ),
            NavItem(
                label = AppStrings.get("tab_tasks", language),
                selectedIcon = Icons.Default.Engineering,
                unselectedIcon = Icons.Outlined.Engineering,
                testTag = "nav_tasks"
            ),
            NavItem(
                label = AppStrings.get("tab_shifts", language),
                selectedIcon = Icons.Default.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                testTag = "nav_shifts"
            ),
            NavItem(
                label = AppStrings.get("tab_client", language),
                selectedIcon = Icons.Default.Business,
                unselectedIcon = Icons.Outlined.Business,
                testTag = "nav_client"
            )
        )
        UserRole.STAFF -> listOf(
            NavItem(
                label = AppStrings.get("tab_tasks", language),
                selectedIcon = Icons.Default.Engineering,
                unselectedIcon = Icons.Outlined.Engineering,
                testTag = "nav_tasks"
            ),
            NavItem(
                label = AppStrings.get("tab_shifts", language),
                selectedIcon = Icons.Default.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                testTag = "nav_shifts"
            ),
            NavItem(
                label = AppStrings.get("tab_client", language),
                selectedIcon = Icons.Default.Business,
                unselectedIcon = Icons.Outlined.Business,
                testTag = "nav_client"
            )
        )
        UserRole.GUEST -> listOf(
            NavItem(
                label = AppStrings.get("tab_client", language),
                selectedIcon = Icons.Default.Business,
                unselectedIcon = Icons.Outlined.Business,
                testTag = "nav_client"
            ),
            NavItem(
                label = "Public Projects",
                selectedIcon = Icons.Default.Engineering,
                unselectedIcon = Icons.Outlined.Engineering,
                testTag = "nav_projects"
            ),
            NavItem(
                label = AppStrings.get("tab_shifts", language),
                selectedIcon = Icons.Default.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                testTag = "nav_shifts"
            )
        )
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = currentIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
