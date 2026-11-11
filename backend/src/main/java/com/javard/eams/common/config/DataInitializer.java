package com.javard.eams.common.config;

import com.javard.eams.modules.auth.entity.Role;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.RoleRepository;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.entity.Department;
import com.javard.eams.modules.employee.entity.Designation;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.entity.EmploymentStatus;
import com.javard.eams.modules.employee.repository.DepartmentRepository;
import com.javard.eams.modules.employee.repository.DesignationRepository;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.leave.entity.LeaveType;
import com.javard.eams.modules.leave.repository.LeaveTypeRepository;
import com.javard.eams.modules.shift.entity.Shift;
import com.javard.eams.modules.shift.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing system reference data for JAVA R&D PVT. LTD...");

        // 1. Roles
        Role superAdminRole = roleRepository.findByRoleName("ROLE_SUPER_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().roleName("ROLE_SUPER_ADMIN").description("Super Admin").build()));
        Role hrAdminRole = roleRepository.findByRoleName("ROLE_HR_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().roleName("ROLE_HR_ADMIN").description("HR Admin").build()));
        Role managerRole = roleRepository.findByRoleName("ROLE_MANAGER").orElseGet(() ->
                roleRepository.save(Role.builder().roleName("ROLE_MANAGER").description("Team Manager").build()));
        Role empRole = roleRepository.findByRoleName("ROLE_EMPLOYEE").orElseGet(() ->
                roleRepository.save(Role.builder().roleName("ROLE_EMPLOYEE").description("Employee").build()));

        // 2. Default Shifts
        if (shiftRepository.count() == 0) {
            shiftRepository.save(Shift.builder()
                    .shiftCode("GEN-0918")
                    .shiftName("General Shift (09:00 - 18:00)")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .gracePeriodMinutes(15)
                    .halfDayThresholdMinutes(240)
                    .fullDayThresholdMinutes(480)
                    .overnight(false)
                    .active(true)
                    .build());

            shiftRepository.save(Shift.builder()
                    .shiftCode("MRN-0716")
                    .shiftName("Morning Shift (07:00 - 16:00)")
                    .startTime(LocalTime.of(7, 0))
                    .endTime(LocalTime.of(16, 0))
                    .gracePeriodMinutes(15)
                    .halfDayThresholdMinutes(240)
                    .fullDayThresholdMinutes(480)
                    .overnight(false)
                    .active(true)
                    .build());

            shiftRepository.save(Shift.builder()
                    .shiftCode("NGT-2106")
                    .shiftName("Night Shift (21:00 - 06:00)")
                    .startTime(LocalTime.of(21, 0))
                    .endTime(LocalTime.of(6, 0))
                    .gracePeriodMinutes(15)
                    .halfDayThresholdMinutes(240)
                    .fullDayThresholdMinutes(480)
                    .overnight(true)
                    .active(true)
                    .build());
        }

        // 3. Leave Types
        if (leaveTypeRepository.count() == 0) {
            leaveTypeRepository.save(LeaveType.builder().typeCode("CL").typeName("Casual Leave").annualQuota(BigDecimal.valueOf(12)).paid(true).carryForwardLimit(0).build());
            leaveTypeRepository.save(LeaveType.builder().typeCode("SL").typeName("Sick Leave").annualQuota(BigDecimal.valueOf(10)).paid(true).carryForwardLimit(5).build());
            leaveTypeRepository.save(LeaveType.builder().typeCode("EL").typeName("Earned / Privilege Leave").annualQuota(BigDecimal.valueOf(15)).paid(true).carryForwardLimit(30).build());
            leaveTypeRepository.save(LeaveType.builder().typeCode("UL").typeName("Unpaid Leave").annualQuota(BigDecimal.valueOf(365)).paid(false).carryForwardLimit(0).build());
        }

        // 4. Organization Setup
        Department engineering = departmentRepository.findByDepartmentCode("ENG").orElseGet(() ->
                departmentRepository.save(Department.builder().departmentCode("ENG").departmentName("Engineering & R&D").build()));
        Department hr = departmentRepository.findByDepartmentCode("HR").orElseGet(() ->
                departmentRepository.save(Department.builder().departmentCode("HR").departmentName("Human Resources").build()));

        Designation arch = designationRepository.findByDesignationCode("ARCH").orElseGet(() ->
                designationRepository.save(Designation.builder().designationCode("ARCH").designationName("Principal Architect").build()));
        Designation sde = designationRepository.findByDesignationCode("SDE2").orElseGet(() ->
                designationRepository.save(Designation.builder().designationCode("SDE2").designationName("Senior Software Engineer").build()));

        // 5. Seed Initial Admin & Test Employees
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@javard.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .active(true)
                    .accountNonLocked(true)
                    .roles(Set.of(superAdminRole, hrAdminRole))
                    .build();
            userRepository.save(adminUser);

            Employee adminEmp = Employee.builder()
                    .user(adminUser)
                    .employeeCode("EMP-0001")
                    .firstName("Super")
                    .lastName("Administrator")
                    .phoneNumber("+91-9876543210")
                    .department(engineering)
                    .designation(arch)
                    .joiningDate(LocalDate.of(2022, 1, 1))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .build();
            employeeRepository.save(adminEmp);

            // Create Manager
            User mgrUser = User.builder()
                    .username("manager")
                    .email("manager@javard.com")
                    .passwordHash(passwordEncoder.encode("Manager@123"))
                    .active(true)
                    .roles(Set.of(managerRole))
                    .build();
            userRepository.save(mgrUser);

            Employee mgrEmp = Employee.builder()
                    .user(mgrUser)
                    .employeeCode("EMP-0002")
                    .firstName("Aakash")
                    .lastName("Verma")
                    .phoneNumber("+91-9876543211")
                    .department(engineering)
                    .designation(arch)
                    .reportingManager(adminEmp)
                    .joiningDate(LocalDate.of(2023, 3, 1))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .build();
            employeeRepository.save(mgrEmp);

            // Create Employee
            User devUser = User.builder()
                    .username("john")
                    .email("john.doe@javard.com")
                    .passwordHash(passwordEncoder.encode("John@123"))
                    .active(true)
                    .roles(Set.of(empRole))
                    .build();
            userRepository.save(devUser);

            Employee devEmp = Employee.builder()
                    .user(devUser)
                    .employeeCode("EMP-0003")
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("+91-9876543212")
                    .department(engineering)
                    .designation(sde)
                    .reportingManager(mgrEmp)
                    .joiningDate(LocalDate.of(2024, 1, 15))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .build();
            employeeRepository.save(devEmp);
        }

        log.info("System reference data initialized successfully.");
    }
}

