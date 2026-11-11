package com.javard.eams.modules.employee.dto.req;

import com.javard.eams.modules.employee.entity.EmploymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EmployeeUpdateRequest {
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

    private LocalDate exitDate;

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;
}

