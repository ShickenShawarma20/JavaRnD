package com.javard.eams.modules.employee.service;

import com.javard.eams.common.exception.BadRequestException;
import com.javard.eams.common.exception.ResourceNotFoundException;
import com.javard.eams.common.response.PageResponse;
import com.javard.eams.modules.auth.entity.Role;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.RoleRepository;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.dto.req.EmployeeCreateRequest;
import com.javard.eams.modules.employee.dto.req.EmployeeUpdateRequest;
import com.javard.eams.modules.employee.dto.res.EmployeeResponse;
import com.javard.eams.modules.employee.entity.Department;
import com.javard.eams.modules.employee.entity.Designation;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.entity.EmploymentStatus;
import com.javard.eams.modules.employee.repository.DepartmentRepository;
import com.javard.eams.modules.employee.repository.DesignationRepository;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

        Employee reportingManager = null;
        if (request.getReportingManagerId() != null) {
            reportingManager = employeeRepository.findById(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reporting Manager not found"));
        }

        // Assign Roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                roleRepository.findByRoleName(roleName).ifPresent(roles::add);
            }
        }
        if (roles.isEmpty()) {
            roleRepository.findByRoleName("ROLE_EMPLOYEE").ifPresent(roles::add);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .accountNonLocked(true)
                .roles(roles)
                .build();
        userRepository.save(user);

        // Auto-generate employee code if needed: EMP-XXXX
        String employeeCode = "EMP-" + String.format("%04d", employeeRepository.count() + 101);

        Employee employee = Employee.builder()
                .user(user)
                .employeeCode(employeeCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .department(department)
                .designation(designation)
                .reportingManager(reportingManager)
                .joiningDate(request.getJoiningDate())
                .employmentStatus(request.getEmploymentStatus())
                .build();

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(String search, UUID departmentId, EmploymentStatus status, Pageable pageable) {
        Page<Employee> page = employeeRepository.searchEmployees(search, departmentId, status, pageable);
        return PageResponse.from(page.map(EmployeeResponse::from));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeResponse.from(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getDirectReports(UUID managerId) {
        return employeeRepository.findByReportingManagerId(managerId).stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

        Employee reportingManager = null;
        if (request.getReportingManagerId() != null) {
            if (request.getReportingManagerId().equals(id)) {
                throw new BadRequestException("An employee cannot report to themselves");
            }
            reportingManager = employeeRepository.findById(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reporting Manager not found"));
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setReportingManager(reportingManager);
        employee.setJoiningDate(request.getJoiningDate());
        employee.setExitDate(request.getExitDate());
        employee.setEmploymentStatus(request.getEmploymentStatus());

        return EmployeeResponse.from(employeeRepository.save(employee));
    }
}

