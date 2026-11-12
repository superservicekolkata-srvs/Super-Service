package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SuperServiceRepository
import com.example.localization.AppLanguage
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SuperServiceViewModel(
    val repository: SuperServiceRepository = SuperServiceRepository()
) : ViewModel() {

    // Auth & User State
    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

    private val _currentStaff = MutableStateFlow<StaffMember?>(null)
    val currentStaff: StateFlow<StaffMember?> = _currentStaff.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // 2FA / MFA simulation
    private val _showMfaPrompt = MutableStateFlow(false)
    val showMfaPrompt: StateFlow<Boolean> = _showMfaPrompt.asStateFlow()
    private val _pendingAdminLogin = MutableStateFlow(false)

    // Language
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Active bottom navigation tab index
    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    // Data streams from repository
    val staffList: StateFlow<List<StaffMember>> = repository.staffList
    val taskList: StateFlow<List<TaskItem>> = repository.taskList
    val attendanceList: StateFlow<List<AttendanceRecord>> = repository.attendanceList
    val shiftList: StateFlow<List<ShiftSchedule>> = repository.shiftList
    val debtList: StateFlow<List<DebtAccount>> = repository.debtList
    val clientBookings: StateFlow<List<ClientBooking>> = repository.clientBookings
    val jobPostings: StateFlow<List<JobPosting>> = repository.jobPostings
    val jobApplications: StateFlow<List<JobApplication>> = repository.jobApplications
    val isOfflineSimulated: StateFlow<Boolean> = repository.isOfflineSimulated
    val unsyncedCount: StateFlow<Int> = repository.unsyncedCount

    // Duty & Clock-In state
    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _activeAttendanceRecord = MutableStateFlow<AttendanceRecord?>(null)
    val activeAttendanceRecord: StateFlow<AttendanceRecord?> = _activeAttendanceRecord.asStateFlow()

    private val _dutySeconds = MutableStateFlow(0L)
    val dutySeconds: StateFlow<Long> = _dutySeconds.asStateFlow()

    init {
        // Run timer while clocked in
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isClockedIn.value) {
                    _dutySeconds.update { it + 1 }
                }
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun setTabIndex(index: Int) {
        _currentTabIndex.value = index
    }

    fun clearAuthMessages() {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
    }

    // --- AUTHENTICATION FLOWS ---

    fun loginAsAdmin(password: String, requireMfa: Boolean = false) {
        clearAuthMessages()
        if (password.isBlank()) {
            _authErrorMessage.value = "Please enter Admin password."
            return
        }
        if (password == "admin" || password == "admin123" || password == "super123") {
            if (requireMfa) {
                _pendingAdminLogin.value = true
                _showMfaPrompt.value = true
            } else {
                _currentUserRole.value = UserRole.ADMIN
                _currentStaff.value = null
                _currentTabIndex.value = 0
            }
        } else {
            _authErrorMessage.value = "Incorrect Admin credentials. Default demo password is: admin123"
        }
    }

    fun verifyMfaCode(otp: String) {
        if (otp.length == 6) {
            _showMfaPrompt.value = false
            if (_pendingAdminLogin.value) {
                _currentUserRole.value = UserRole.ADMIN
                _currentStaff.value = null
                _pendingAdminLogin.value = false
                _currentTabIndex.value = 0
            }
        } else {
            _authErrorMessage.value = "Invalid MFA code. Please enter 6-digit verification OTP."
        }
    }

    fun cancelMfa() {
        _showMfaPrompt.value = false
        _pendingAdminLogin.value = false
    }

    /**
     * Mandatory Staff Approval Check:
     * If pending approval (e.g. SS-EMP-22 Bikram Halder), login is strictly blocked with:
     * "Staff login rejected: Your account is pending approval by the Admin. Please contact Admin."
     */
    fun loginAsStaff(identifier: String, pass: String) {
        clearAuthMessages()
        val trimmed = identifier.trim()
        if (trimmed.isBlank() || pass.isBlank()) {
            _authErrorMessage.value = "Please enter your Staff ID, phone, or email, and password."
            return
        }

        val allStaff = repository.staffList.value
        val matched = allStaff.find {
            it.id.equals(trimmed, ignoreCase = true) ||
                    it.phone == trimmed ||
                    it.email.equals(trimmed, ignoreCase = true)
        }

        if (matched == null) {
            _authErrorMessage.value = "Staff account not found. Please verify your Staff ID or Sign Up below."
            return
        }

        // Check Admin Approval
        if (!matched.isApprovedByOwner || matched.approvalStatus == ApprovalStatus.PENDING_APPROVAL) {
            _authErrorMessage.value = "Staff login rejected: Your account is pending approval by the Admin. Please contact Admin."
            return
        }

        if (matched.approvalStatus == ApprovalStatus.REJECTED) {
            _authErrorMessage.value = "Staff login rejected: Your account application was declined by the Admin. Please contact Owner."
            return
        }

        // Approved - proceed with login
        _currentUserRole.value = UserRole.STAFF
        _currentStaff.value = matched
        _currentTabIndex.value = 0
    }

    fun loginAsGuest() {
        clearAuthMessages()
        _currentUserRole.value = UserRole.GUEST
        _currentStaff.value = null
        _currentTabIndex.value = 2 // Client portal tab
    }

    fun logout() {
        _currentUserRole.value = null
        _currentStaff.value = null
        _currentTabIndex.value = 0
        clearAuthMessages()
    }

    fun submitStaffRegistration(
        name: String,
        phone: String,
        email: String,
        department: String,
        facilitySite: String,
        pass: String
    ) {
        clearAuthMessages()
        if (name.isBlank() || phone.isBlank() || email.isBlank()) {
            _authErrorMessage.value = "All fields are required for technical staff registration."
            return
        }
        val registered = repository.registerStaff(name, phone, email, department, facilitySite, pass)
        _authSuccessMessage.value = "Registration submitted successfully! Assigned ID: ${registered.id}. Status: Pending Admin Approval. The Admin will review your profile shortly."
    }

    // --- ADMIN APPROVAL ACTIONS ---

    fun approveStaff(staffId: String) {
        repository.approveStaff(staffId)
    }

    fun rejectStaff(staffId: String) {
        repository.rejectStaff(staffId)
    }

    // --- ATTENDANCE & GPS CLOCK-IN ---

    fun clockIn(facility: String) {
        val staff = _currentStaff.value
        if (staff != null) {
            val record = repository.recordClockIn(
                staff = staff,
                facility = facility,
                lat = 22.5804,
                lon = 88.4378
            )
            _activeAttendanceRecord.value = record
            _isClockedIn.value = true
            _dutySeconds.value = 0L
        }
    }

    fun clockOut() {
        val record = _activeAttendanceRecord.value
        if (record != null) {
            val hours = (_dutySeconds.value / 3600.0).coerceAtLeast(0.1)
            repository.recordClockOut(record.id, (hours * 10).toInt() / 10.0)
            _isClockedIn.value = false
            _activeAttendanceRecord.value = null
            _dutySeconds.value = 0L
        }
    }

    // --- TASK & OPS ACTIONS ---

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        repository.updateTaskStatus(taskId, newStatus)
    }

    fun toggleTaskCheckpoint(taskId: String, index: Int) {
        repository.toggleTaskCheckpoint(taskId, index)
    }

    fun createNewTask(
        title: String,
        facility: String,
        dept: String,
        staffId: String,
        staffName: String,
        priority: TaskPriority,
        deadline: String,
        checkpoints: List<String>
    ) {
        repository.createTask(title, facility, dept, staffId, staffName, priority, deadline, checkpoints)
    }

    fun confirmProjectCompletion(taskId: String, rating: Float, feedback: String) {
        repository.confirmProjectCompletion(taskId, rating, feedback)
    }

    // --- CLIENT BOOKINGS ---

    fun createBooking(
        clientName: String,
        phone: String,
        serviceType: String,
        facility: String,
        date: String,
        slot: String,
        notes: String
    ) {
        repository.addClientBooking(clientName, phone, serviceType, facility, date, slot, notes)
    }

    // --- DEBT RECOVERY & BILLING ---

    fun recordDebtPayment(debtId: String, amount: Double) {
        repository.recordDebtPayment(debtId, amount)
    }

    // --- JOBS & APPLICANTS ---

    fun applyForJob(jobId: String, title: String, name: String, phone: String, email: String, exp: Int) {
        repository.submitJobApplication(jobId, title, name, phone, email, exp)
    }

    // --- OFFLINE SYNC TOGGLE ---

    fun toggleOfflineMode() {
        repository.toggleOfflineMode()
    }
}
