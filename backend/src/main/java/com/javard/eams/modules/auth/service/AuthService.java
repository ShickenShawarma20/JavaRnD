package com.javard.eams.modules.auth.service;

import com.javard.eams.common.exception.BadRequestException;
import com.javard.eams.common.exception.ResourceNotFoundException;
import com.javard.eams.modules.auth.dto.req.LoginRequest;
import com.javard.eams.modules.auth.dto.req.PasswordResetRequest;
import com.javard.eams.modules.auth.dto.req.RefreshTokenRequest;
import com.javard.eams.modules.auth.dto.res.AuthResponse;
import com.javard.eams.modules.auth.entity.Permission;
import com.javard.eams.modules.auth.entity.Role;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateAccessToken(authentication);

        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLoginAt(Instant.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromJwt(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of());
        String newAccessToken = tokenProvider.generateAccessToken(auth);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("No active session");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));

        return buildAuthResponse(user, null, null);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getPermissionName)
                .distinct()
                .collect(Collectors.toList());

        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .employeeId(empOpt.map(Employee::getId).orElse(null))
                .employeeCode(empOpt.map(Employee::getEmployeeCode).orElse(null))
                .fullName(empOpt.map(Employee::getFullName).orElse(user.getUsername()))
                .build();
    }
}

