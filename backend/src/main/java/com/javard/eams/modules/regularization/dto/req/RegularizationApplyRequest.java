package com.javard.eams.modules.regularization.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegularizationApplyRequest {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    private Instant requestedCheckIn;
    private Instant requestedCheckOut;

    @NotBlank(message = "Reason is required")
    private String reason;
}

