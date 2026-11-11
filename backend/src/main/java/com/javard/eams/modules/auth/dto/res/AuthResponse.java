package com.javard.eams.modules.auth.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UUID userId;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;
    private UUID employeeId;
    private String employeeCode;
    private String fullName;
}

