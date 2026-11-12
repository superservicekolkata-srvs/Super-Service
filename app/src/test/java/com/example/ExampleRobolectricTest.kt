package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SuperServiceRepository
import com.example.model.ApprovalStatus
import com.example.model.UserRole
import com.example.viewmodel.SuperServiceViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Super Service`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Super Service", appName)
    }

    @Test
    fun `pending staff login is blocked by mandatory approval guard`() {
        val repo = SuperServiceRepository()
        val viewModel = SuperServiceViewModel(repo)

        // Attempt login with pending staff Bikram Halder (SS-EMP-22)
        viewModel.loginAsStaff("SS-EMP-22", "pass")

        // Role should remain null
        assertNull(viewModel.currentUserRole.value)
        assertEquals(
            "Staff login rejected: Your account is pending approval by the Admin. Please contact Admin.",
            viewModel.authErrorMessage.value
        )

        // Admin approves staff
        viewModel.approveStaff("SS-EMP-22")

        // Login should now succeed immediately
        viewModel.loginAsStaff("SS-EMP-22", "pass")
        assertEquals(UserRole.STAFF, viewModel.currentUserRole.value)
        assertEquals("SS-EMP-22", viewModel.currentStaff.value?.id)
        assertNull(viewModel.authErrorMessage.value)
    }

    @Test
    fun `offline support caches attendance and syncs upon reconnection`() {
        val repo = SuperServiceRepository()
        val viewModel = SuperServiceViewModel(repo)

        // Login as approved staff
        viewModel.loginAsStaff("SS-EMP-04", "pass")

        // Toggle offline mode
        viewModel.toggleOfflineMode()
        assertTrue(viewModel.isOfflineSimulated.value)

        // Clock in while offline
        viewModel.clockIn("Sector V Tech Park")
        assertTrue(viewModel.isClockedIn.value)

        // Attendance record should be saved locally and marked unsynced
        val active = viewModel.activeAttendanceRecord.value
        assertNotNull(active)
        assertFalse(active!!.isSynced)
        assertTrue(viewModel.unsyncedCount.value > 0)

        // Reconnect online -> auto syncs
        viewModel.toggleOfflineMode()
        assertFalse(viewModel.isOfflineSimulated.value)
        assertEquals(0, viewModel.unsyncedCount.value)
    }
}
