package com.javard.eams.modules.employee.dto.res;

import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.entity.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private UUID userId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String username;
    private String phoneNumber;
    private UUID departmentId;
    private String departmentName;
    private UUID designationId;
    private String designationName;
    private UUID reportingManagerId;
    private String reportingManagerName;
    private LocalDate joiningDate;
    private LocalDate exitDate;
    private EmploymentStatus employmentStatus;

    public static EmployeeResponse from(Employee employee) {
        String mgrName = null;
        UUID mgrId = null;
        if (employee.getReportingManager() != null) {
            mgrId = employee.getReportingManager().getId();
            mgrName = employee.getReportingManager().getFullName();
        }

        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUser().getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .email(employee.getUser().getEmail())
                .username(employee.getUser().getUsername())
                .phoneNumber(employee.getPhoneNumber())
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getDepartmentName())
                .designationId(employee.getDesignation().getId())
                .designationName(employee.getDesignation().getDesignationName())
                .reportingManagerId(mgrId)
                .reportingManagerName(mgrName)
                .joiningDate(employee.getJoiningDate())
                .exitDate(employee.getExitDate())
                .employmentStatus(employee.getEmploymentStatus())
                .build();
    }
}

