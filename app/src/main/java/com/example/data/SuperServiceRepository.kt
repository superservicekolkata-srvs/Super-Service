package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SuperServiceRepository {

    private val _staffList = MutableStateFlow<List<StaffMember>>(initialStaff)
    val staffList: StateFlow<List<StaffMember>> = _staffList.asStateFlow()

    private val _taskList = MutableStateFlow<List<TaskItem>>(initialTasks)
    val taskList: StateFlow<List<TaskItem>> = _taskList.asStateFlow()

    private val _attendanceList = MutableStateFlow<List<AttendanceRecord>>(initialAttendance)
    val attendanceList: StateFlow<List<AttendanceRecord>> = _attendanceList.asStateFlow()

    private val _shiftList = MutableStateFlow<List<ShiftSchedule>>(initialShifts)
    val shiftList: StateFlow<List<ShiftSchedule>> = _shiftList.asStateFlow()

    private val _debtList = MutableStateFlow<List<DebtAccount>>(initialDebts)
    val debtList: StateFlow<List<DebtAccount>> = _debtList.asStateFlow()

    private val _clientBookings = MutableStateFlow<List<ClientBooking>>(initialBookings)
    val clientBookings: StateFlow<List<ClientBooking>> = _clientBookings.asStateFlow()

    private val _jobPostings = MutableStateFlow<List<JobPosting>>(initialJobPostings)
    val jobPostings: StateFlow<List<JobPosting>> = _jobPostings.asStateFlow()

    private val _jobApplications = MutableStateFlow<List<JobApplication>>(initialJobApplications)
    val jobApplications: StateFlow<List<JobApplication>> = _jobApplications.asStateFlow()

    // Offline Sync tracking
    private val _isOfflineSimulated = MutableStateFlow(false)
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    private val _unsyncedCount = MutableStateFlow(0)
    val unsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()

    fun toggleOfflineMode() {
        val next = !_isOfflineSimulated.value
        _isOfflineSimulated.value = next
        if (!next) {
            // Reconnected -> Sync all
            _attendanceList.update { list -> list.map { it.copy(isSynced = true) } }
            _unsyncedCount.value = 0
        }
    }

    // Staff Approvals
    fun approveStaff(staffId: String) {
        _staffList.update { list ->
            list.map { staff ->
                if (staff.id == staffId) {
                    staff.copy(
                        isApprovedByOwner = true,
                        approvalStatus = ApprovalStatus.APPROVED
                    )
                } else staff
            }
        }
    }

    fun rejectStaff(staffId: String) {
        _staffList.update { list ->
            list.map { staff ->
                if (staff.id == staffId) {
                    staff.copy(
                        isApprovedByOwner = false,
                        approvalStatus = ApprovalStatus.REJECTED
                    )
                } else staff
            }
        }
    }

    fun registerStaff(
        name: String,
        phone: String,
        email: String,
        department: String,
        facilitySite: String,
        pass: String
    ): StaffMember {
        val count = _staffList.value.size + 1
        val newId = "SS-EMP-${String.format("%02d", count + 15)}"
        val newStaff = StaffMember(
            id = newId,
            name = name,
            phone = phone,
            email = email,
            department = department,
            facilitySite = facilitySite,
            password = pass,
            isApprovedByOwner = false,
            approvalStatus = ApprovalStatus.PENDING_APPROVAL,
            performanceRating = 5.0f,
            onTimeRate = 100,
            attendanceRate = 100,
            totalHoursWorked = 0.0,
            baseSalary = 20000.0,
            registeredAt = "Today"
        )
        _staffList.update { listOf(newStaff) + it }
        return newStaff
    }

    // Task Operations
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        _taskList.update { list ->
            list.map { task ->
                if (task.id == taskId) task.copy(status = newStatus) else task
            }
        }
        if (_isOfflineSimulated.value) {
            _unsyncedCount.update { it + 1 }
        }
    }

    fun toggleTaskCheckpoint(taskId: String, index: Int) {
        _taskList.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val updated = task.completedCheckpoints.toMutableSet()
                    if (updated.contains(index)) updated.remove(index) else updated.add(index)
                    task.copy(completedCheckpoints = updated)
                } else task
            }
        }
    }

    fun createTask(
        title: String,
        facilitySite: String,
        department: String,
        assignedStaffId: String,
        assignedStaffName: String,
        priority: TaskPriority,
        deadline: String,
        checkpoints: List<String>
    ) {
        val newTask = TaskItem(
            id = "TSK-${System.currentTimeMillis() % 10000}",
            title = title,
            facilitySite = facilitySite,
            department = department,
            assignedStaffId = assignedStaffId,
            assignedStaffName = assignedStaffName,
            priority = priority,
            status = TaskStatus.PENDING,
            deadline = deadline,
            checkpoints = checkpoints
        )
        _taskList.update { listOf(newTask) + it }
        if (_isOfflineSimulated.value) {
            _unsyncedCount.update { it + 1 }
        }
    }

    fun confirmProjectCompletion(taskId: String, rating: Float, feedback: String) {
        _taskList.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        status = TaskStatus.COMPLETED,
                        isClientConfirmed = true,
                        clientRating = rating,
                        clientFeedback = feedback
                    )
                } else task
            }
        }
    }

    // Attendance
    fun recordClockIn(staff: StaffMember, facility: String, lat: Double, lon: Double): AttendanceRecord {
        val record = AttendanceRecord(
            id = "ATT-${System.currentTimeMillis() % 10000}",
            staffId = staff.id,
            staffName = staff.name,
            facilitySite = facility,
            clockInTime = "09:00 AM",
            latitude = lat,
            longitude = lon,
            dutyHours = 0.0,
            isSynced = !_isOfflineSimulated.value
        )
        _attendanceList.update { listOf(record) + it }
        if (_isOfflineSimulated.value) {
            _unsyncedCount.update { it + 1 }
        }
        return record
    }

    fun recordClockOut(recordId: String, dutyHours: Double) {
        _attendanceList.update { list ->
            list.map { rec ->
                if (rec.id == recordId) {
                    rec.copy(clockOutTime = "06:00 PM", dutyHours = dutyHours)
                } else rec
            }
        }
        // Update staff member total hours
        val record = _attendanceList.value.find { it.id == recordId }
        if (record != null) {
            _staffList.update { list ->
                list.map { staff ->
                    if (staff.id == record.staffId) {
                        staff.copy(totalHoursWorked = staff.totalHoursWorked + dutyHours)
                    } else staff
                }
            }
        }
    }

    // Client Booking
    fun addClientBooking(
        clientName: String,
        clientPhone: String,
        serviceType: String,
        facilityLocation: String,
        date: String,
        timeSlot: String,
        notes: String
    ): ClientBooking {
        val booking = ClientBooking(
            id = "BK-${System.currentTimeMillis() % 10000}",
            clientName = clientName,
            clientPhone = clientPhone,
            serviceType = serviceType,
            facilityLocation = facilityLocation,
            preferredDate = date,
            preferredTimeSlot = timeSlot,
            specialInstructions = notes,
            estimatedQuote = when (serviceType) {
                "HVAC Audit & Descaling" -> 18500.0
                "Generator Substation Check" -> 14000.0
                "Deep Sanitization" -> 12000.0
                "Fire & Hydrant Certification" -> 16500.0
                else -> 10000.0
            },
            status = BookingStatus.REQUESTED
        )
        _clientBookings.update { listOf(booking) + it }
        return booking
    }

    // Debt & Billing
    fun recordDebtPayment(debtId: String, amountPaid: Double) {
        _debtList.update { list ->
            list.map { debt ->
                if (debt.id == debtId) {
                    val newPaid = debt.paidAmount + amountPaid
                    val newBalance = (debt.invoicedAmount - newPaid).coerceAtLeast(0.0)
                    val newStatus = if (newBalance <= 0) DebtStatus.CURRENT else debt.status
                    debt.copy(
                        paidAmount = newPaid,
                        debtBalance = newBalance,
                        status = newStatus,
                        lastPaymentDate = "Today"
                    )
                } else debt
            }
        }
    }

    // Job application
    fun submitJobApplication(
        jobId: String,
        jobTitle: String,
        name: String,
        phone: String,
        email: String,
        exp: Int
    ) {
        val app = JobApplication(
            id = "APP-${System.currentTimeMillis() % 10000}",
            jobId = jobId,
            jobTitle = jobTitle,
            candidateName = name,
            phone = phone,
            email = email,
            experienceYears = exp,
            appliedDate = "Today"
        )
        _jobApplications.update { listOf(app) + it }
    }

    companion object {
        val initialStaff = listOf(
            StaffMember(
                id = "SS-EMP-22",
                name = "Bikram Halder",
                phone = "9830112233",
                email = "bikram.halder@superservice.in",
                department = "HVAC & Aircon Maintenance",
                facilitySite = "Sector V Tech Park",
                password = "pass",
                isApprovedByOwner = false,
                approvalStatus = ApprovalStatus.PENDING_APPROVAL,
                performanceRating = 4.6f,
                onTimeRate = 92,
                attendanceRate = 94,
                totalHoursWorked = 142.0,
                baseSalary = 22000.0,
                registeredAt = "2026-11-10"
            ),
            StaffMember(
                id = "SS-EMP-04",
                name = "Subhashis Roy",
                phone = "9830224466",
                email = "subhashis.roy@superservice.in",
                department = "Electrical & Generator Substation",
                facilitySite = "New Town Eco Hub",
                password = "pass",
                isApprovedByOwner = true,
                approvalStatus = ApprovalStatus.APPROVED,
                performanceRating = 4.9f,
                onTimeRate = 98,
                attendanceRate = 99,
                totalHoursWorked = 184.0,
                baseSalary = 26000.0,
                registeredAt = "2026-08-15"
            ),
            StaffMember(
                id = "SS-EMP-09",
                name = "Ananya Sengupta",
                phone = "9830335577",
                email = "ananya.s@superservice.in",
                department = "Deep Housekeeping & Sanitation",
                facilitySite = "Park Street Commercial Tower",
                password = "pass",
                isApprovedByOwner = true,
                approvalStatus = ApprovalStatus.APPROVED,
                performanceRating = 4.8f,
                onTimeRate = 96,
                attendanceRate = 97,
                totalHoursWorked = 176.0,
                baseSalary = 23000.0,
                registeredAt = "2026-09-01"
            ),
            StaffMember(
                id = "SS-EMP-15",
                name = "Debjit Mukherjee",
                phone = "9830446688",
                email = "debjit.m@superservice.in",
                department = "Fire & Safety Systems",
                facilitySite = "Rajarhat Logistics Park",
                password = "pass",
                isApprovedByOwner = true,
                approvalStatus = ApprovalStatus.APPROVED,
                performanceRating = 4.7f,
                onTimeRate = 94,
                attendanceRate = 95,
                totalHoursWorked = 168.0,
                baseSalary = 24000.0,
                registeredAt = "2026-09-20"
            ),
            StaffMember(
                id = "SS-EMP-18",
                name = "Pratik Ghosh",
                phone = "9830557799",
                email = "pratik.g@superservice.in",
                department = "Elevator & Lift Operations",
                facilitySite = "Alipore Corporate Complex",
                password = "pass",
                isApprovedByOwner = true,
                approvalStatus = ApprovalStatus.APPROVED,
                performanceRating = 4.9f,
                onTimeRate = 99,
                attendanceRate = 98,
                totalHoursWorked = 190.0,
                baseSalary = 27000.0,
                registeredAt = "2026-07-10"
            )
        )

        val initialTasks = listOf(
            TaskItem(
                id = "TSK-101",
                title = "Central Chiller Annual Chemical Descaling & Pressure Check",
                facilitySite = "Sector V Tech Park",
                department = "HVAC & Aircon Maintenance",
                assignedStaffId = "SS-EMP-22",
                assignedStaffName = "Bikram Halder",
                priority = TaskPriority.HIGH,
                status = TaskStatus.IN_PROGRESS,
                deadline = "Today, 05:00 PM",
                checkpoints = listOf(
                    "Check condenser refrigerant pressure gauges",
                    "Flush chemical descaling circulation pump",
                    "Inspect compressor oil level & seals",
                    "Verify automated cutoff sensors"
                ),
                completedCheckpoints = setOf(0, 1)
            ),
            TaskItem(
                id = "TSK-102",
                title = "500kVA Diesel Generator Load Bank & Auto-Mains Failure Testing",
                facilitySite = "New Town Eco Hub",
                department = "Electrical & Generator Substation",
                assignedStaffId = "SS-EMP-04",
                assignedStaffName = "Subhashis Roy",
                priority = TaskPriority.HIGH,
                status = TaskStatus.PENDING,
                deadline = "Tomorrow, 11:30 AM",
                checkpoints = listOf(
                    "Check fuel reserve line & water separator",
                    "Test AMF panel switchover latency (<8s)",
                    "Verify battery bank voltage (>25.4V)",
                    "Inspect earth pit resistance (<1 ohm)"
                )
            ),
            TaskItem(
                id = "TSK-103",
                title = "HVAC Duct Microbial Sanitization & Air Filter Replacement",
                facilitySite = "Park Street Commercial Tower",
                department = "Deep Housekeeping & Sanitation",
                assignedStaffId = "SS-EMP-09",
                assignedStaffName = "Ananya Sengupta",
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.UNDER_INSPECTION,
                deadline = "Today, 03:00 PM",
                checkpoints = listOf(
                    "Seal duct sections 4A & 4B",
                    "Inject food-grade antimicrobial mist",
                    "Replace primary HEPA pre-filters"
                ),
                completedCheckpoints = setOf(0, 1, 2)
            ),
            TaskItem(
                id = "TSK-104",
                title = "Fire Hydrant Flow Rate & Smoke Sensor Calibration",
                facilitySite = "Rajarhat Logistics Park",
                department = "Fire & Safety Systems",
                assignedStaffId = "SS-EMP-15",
                assignedStaffName = "Debjit Mukherjee",
                priority = TaskPriority.HIGH,
                status = TaskStatus.COMPLETED,
                deadline = "Yesterday, 04:00 PM",
                checkpoints = listOf(
                    "Pressurize jockey pump to 8 bar",
                    "Test strobe sounders on floors 1-3",
                    "Calibrate optical smoke detectors"
                ),
                completedCheckpoints = setOf(0, 1, 2),
                isClientConfirmed = true,
                clientRating = 5.0f,
                clientFeedback = "Excellent prompt test! Complete compliance report delivered."
            ),
            TaskItem(
                id = "TSK-105",
                title = "Smart BMS & Substation Power Quality Audit",
                facilitySite = "Alipore Corporate Complex",
                department = "Elevator & Lift Operations",
                assignedStaffId = "SS-EMP-18",
                assignedStaffName = "Pratik Ghosh",
                priority = TaskPriority.NORMAL,
                status = TaskStatus.PENDING,
                deadline = "Nov 15, 02:00 PM",
                checkpoints = listOf(
                    "Check elevator gearless motor temperature",
                    "Test emergency car braking safety clamp",
                    "Inspect landing door interlocks"
                )
            )
        )

        val initialAttendance = listOf(
            AttendanceRecord(
                id = "ATT-901",
                staffId = "SS-EMP-04",
                staffName = "Subhashis Roy",
                facilitySite = "New Town Eco Hub",
                clockInTime = "08:55 AM",
                clockOutTime = "05:45 PM",
                dutyHours = 8.8,
                isSynced = true
            ),
            AttendanceRecord(
                id = "ATT-902",
                staffId = "SS-EMP-09",
                staffName = "Ananya Sengupta",
                facilitySite = "Park Street Commercial Tower",
                clockInTime = "09:05 AM",
                clockOutTime = "06:10 PM",
                dutyHours = 9.0,
                isSynced = true
            )
        )

        val initialShifts = listOf(
            ShiftSchedule(
                id = "SH-1",
                staffId = "SS-EMP-04",
                staffName = "Subhashis Roy",
                facilitySite = "New Town Eco Hub",
                shiftType = ShiftType.MORNING,
                timing = "06:00 AM - 02:00 PM",
                dayOfWeek = "Today",
                department = "Electrical"
            ),
            ShiftSchedule(
                id = "SH-2",
                staffId = "SS-EMP-22",
                staffName = "Bikram Halder",
                facilitySite = "Sector V Tech Park",
                shiftType = ShiftType.GENERAL,
                timing = "09:00 AM - 06:00 PM",
                dayOfWeek = "Today",
                department = "HVAC"
            ),
            ShiftSchedule(
                id = "SH-3",
                staffId = "SS-EMP-09",
                staffName = "Ananya Sengupta",
                facilitySite = "Park Street Commercial Tower",
                shiftType = ShiftType.GENERAL,
                timing = "09:00 AM - 06:00 PM",
                dayOfWeek = "Tomorrow",
                department = "Sanitation"
            ),
            ShiftSchedule(
                id = "SH-4",
                staffId = "SS-EMP-15",
                staffName = "Debjit Mukherjee",
                facilitySite = "Rajarhat Logistics Park",
                shiftType = ShiftType.NIGHT_VIGIL,
                timing = "10:00 PM - 06:00 AM",
                dayOfWeek = "Tomorrow",
                department = "Fire & Safety"
            ),
            ShiftSchedule(
                id = "SH-5",
                staffId = "SS-EMP-18",
                staffName = "Pratik Ghosh",
                facilitySite = "Alipore Corporate Complex",
                shiftType = ShiftType.STANDBY,
                timing = "On-Call 24/7",
                dayOfWeek = "Friday",
                department = "Elevators"
            )
        )

        val initialDebts = listOf(
            DebtAccount(
                id = "DB-101",
                clientName = "Bengal IT Horizon Corp",
                facilityName = "Sector V Millennium Tower",
                invoicedAmount = 450000.0,
                paidAmount = 310000.0,
                debtBalance = 140000.0,
                ageingDays = 45,
                lastPaymentDate = "2026-10-02",
                status = DebtStatus.OVERDUE_30,
                contactPerson = "S. Banerjee (Facility VP)",
                contactPhone = "+91 98310 99881"
            ),
            DebtAccount(
                id = "DB-102",
                clientName = "Kolkata Mega Retail Hub Ltd",
                facilityName = "Salt Lake City Center Complex",
                invoicedAmount = 820000.0,
                paidAmount = 580000.0,
                debtBalance = 240000.0,
                ageingDays = 75,
                lastPaymentDate = "2026-09-12",
                status = DebtStatus.OVERDUE_60,
                contactPerson = "R. K. Agarwal (Finance Dir)",
                contactPhone = "+91 98305 77661"
            ),
            DebtAccount(
                id = "DB-103",
                clientName = "Eastern Logistics & Warehousing",
                facilityName = "Rajarhat Cargo Hub Block B",
                invoicedAmount = 290000.0,
                paidAmount = 110000.0,
                debtBalance = 180000.0,
                ageingDays = 98,
                lastPaymentDate = "2026-08-20",
                status = DebtStatus.CRITICAL_90,
                contactPerson = "Amitava Bose (Ops Head)",
                contactPhone = "+91 98311 44332"
            ),
            DebtAccount(
                id = "DB-104",
                clientName = "Park Street Premier Suites",
                facilityName = "Park Street Corporate Annex",
                invoicedAmount = 375000.0,
                paidAmount = 375000.0,
                debtBalance = 0.0,
                ageingDays = 12,
                lastPaymentDate = "2026-11-01",
                status = DebtStatus.CURRENT,
                contactPerson = "Debolina Sen (Admin)",
                contactPhone = "+91 98318 22110"
            )
        )

        val initialBookings = listOf(
            ClientBooking(
                id = "BK-501",
                clientName = "Tata Consultancy Services (Gitanjali)",
                clientPhone = "+91 98300 11990",
                serviceType = "HVAC Audit & Descaling",
                facilityLocation = "Sector V Tech Park Phase 2",
                preferredDate = "2026-11-16",
                preferredTimeSlot = "10:00 AM - 01:00 PM",
                specialInstructions = "Requires clean room protocols for server farm chillers.",
                estimatedQuote = 28500.0,
                status = BookingStatus.CONFIRMED
            ),
            ClientBooking(
                id = "BK-502",
                clientName = "ITC Infotech Park",
                clientPhone = "+91 98302 33441",
                serviceType = "Generator Substation Check",
                facilityLocation = "Rajarhat High Street",
                preferredDate = "2026-11-18",
                preferredTimeSlot = "02:00 PM - 05:00 PM",
                specialInstructions = "Annual preventative test before peak winter loading.",
                estimatedQuote = 16000.0,
                status = BookingStatus.REQUESTED
            )
        )

        val initialJobPostings = listOf(
            JobPosting(
                id = "JOB-01",
                title = "Senior HVAC Chiller Technician",
                department = "HVAC & Aircon Maintenance",
                facilitySite = "Sector V Tech Park",
                experienceRequired = "3+ Years in VRV/Chillers",
                openPositions = 3,
                monthlyCompensation = "₹28,000 - ₹35,000 + Night Allowance"
            ),
            JobPosting(
                id = "JOB-02",
                title = "Substation Electrical Supervisor",
                department = "Electrical & Generator Substation",
                facilitySite = "New Town Eco Hub",
                experienceRequired = "B.Tech/Diploma + Grade 1 License",
                openPositions = 2,
                monthlyCompensation = "₹32,000 - ₹42,000 + PF/ESI"
            ),
            JobPosting(
                id = "JOB-03",
                title = "Fire & Safety Compliance Officer",
                department = "Fire & Safety Systems",
                facilitySite = "Rajarhat Logistics Park",
                experienceRequired = "Disaster Mgmt / Fire Certification",
                openPositions = 1,
                monthlyCompensation = "₹26,000 - ₹32,000"
            )
        )

        val initialJobApplications = listOf(
            JobApplication(
                id = "APP-01",
                jobId = "JOB-01",
                jobTitle = "Senior HVAC Chiller Technician",
                candidateName = "Subir Karmakar",
                phone = "+91 98319 88776",
                email = "subir.karmakar@gmail.com",
                experienceYears = 4,
                appliedDate = "Yesterday"
            ),
            JobApplication(
                id = "APP-02",
                jobId = "JOB-02",
                jobTitle = "Substation Electrical Supervisor",
                candidateName = "Tanmoy Chakraborty",
                phone = "+91 98308 11223",
                email = "tanmoy.c@gmail.com",
                experienceYears = 5,
                appliedDate = "Today"
            )
        )
    }
}
