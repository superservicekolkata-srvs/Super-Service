package com.example.model

enum class UserRole {
    ADMIN,
    STAFF,
    GUEST
}

enum class ApprovalStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

enum class TaskPriority {
    HIGH,
    MEDIUM,
    NORMAL
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    UNDER_INSPECTION,
    COMPLETED
}

enum class ShiftType {
    MORNING,       // 06:00 - 14:00
    GENERAL,       // 09:00 - 18:00
    NIGHT_VIGIL,   // 22:00 - 06:00
    STANDBY        // Emergency On-Call
}

enum class DebtStatus {
    CURRENT,
    OVERDUE_30,
    OVERDUE_60,
    CRITICAL_90
}

enum class BookingStatus {
    REQUESTED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED
}

data class StaffMember(
    val id: String,                    // e.g. "SS-EMP-22", "SS-EMP-04"
    val name: String,                  // e.g. "Bikram Halder"
    val phone: String,                 // e.g. "9830112233"
    val email: String,                 // e.g. "bikram.halder@superservice.in"
    val department: String,            // e.g. "HVAC & Aircon", "Electrical & Generator"
    val facilitySite: String,          // e.g. "Sector V Tech Park"
    val password: String = "pass123",
    val isApprovedByOwner: Boolean,    // Admin approval flag
    val approvalStatus: ApprovalStatus,
    val performanceRating: Float = 4.7f,
    val onTimeRate: Int = 95,          // %
    val attendanceRate: Int = 96,      // %
    val totalHoursWorked: Double = 168.0,
    val baseSalary: Double = 22000.0,
    val registeredAt: String = "2026-11-10"
)

data class TaskItem(
    val id: String,
    val title: String,
    val facilitySite: String,
    val department: String,
    val assignedStaffId: String,
    val assignedStaffName: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val deadline: String,
    val reminderMinutesBefore: Int = 30,
    val checkpoints: List<String> = emptyList(),
    val completedCheckpoints: Set<Int> = emptySet(),
    val isClientConfirmed: Boolean = false,
    val clientFeedback: String = "",
    val clientRating: Float? = null
)

data class AttendanceRecord(
    val id: String,
    val staffId: String,
    val staffName: String,
    val facilitySite: String,
    val clockInTime: String,
    val clockOutTime: String? = null,
    val latitude: Double = 22.5804,    // Kolkata Sector V approx
    val longitude: Double = 88.4378,
    val dutyHours: Double = 8.0,
    val isSynced: Boolean = true
)

data class ShiftSchedule(
    val id: String,
    val staffId: String,
    val staffName: String,
    val facilitySite: String,
    val shiftType: ShiftType,
    val timing: String,
    val dayOfWeek: String,
    val department: String
)

data class DebtAccount(
    val id: String,
    val clientName: String,
    val facilityName: String,
    val invoicedAmount: Double,
    val paidAmount: Double,
    val debtBalance: Double,
    val ageingDays: Int,
    val lastPaymentDate: String,
    val status: DebtStatus,
    val contactPerson: String,
    val contactPhone: String
)

data class ClientBooking(
    val id: String,
    val clientName: String,
    val clientPhone: String,
    val serviceType: String,
    val facilityLocation: String,
    val preferredDate: String,
    val preferredTimeSlot: String,
    val specialInstructions: String,
    val estimatedQuote: Double,
    val status: BookingStatus
)

data class JobPosting(
    val id: String,
    val title: String,
    val department: String,
    val facilitySite: String,
    val experienceRequired: String,
    val openPositions: Int,
    val monthlyCompensation: String,
    val isActive: Boolean = true
)

data class JobApplication(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val candidateName: String,
    val phone: String,
    val email: String,
    val experienceYears: Int,
    val appliedDate: String,
    val status: String = "Under Review"
)
