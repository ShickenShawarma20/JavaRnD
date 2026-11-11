package com.javard.eams.modules.employee.dto.req;

import com.javard.eams.modules.employee.entity.EmploymentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCreateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phoneNumber;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Designation ID is required")
    private UUID designationId;

    private UUID reportingManagerId;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @Builder.Default
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    private List<String> roles; // e.g. ["ROLE_EMPLOYEE", "ROLE_MANAGER"]
}

