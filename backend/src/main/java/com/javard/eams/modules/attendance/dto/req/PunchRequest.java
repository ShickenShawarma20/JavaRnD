package com.javard.eams.modules.attendance.dto.req;

import com.javard.eams.modules.attendance.entity.PunchEventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PunchRequest {
    @NotNull(message = "Event type is required (CHECK_IN, CHECK_OUT, BREAK_START, BREAK_END)")
    private PunchEventType eventType;

    private String ipAddress;
    private String deviceInfo;
}

