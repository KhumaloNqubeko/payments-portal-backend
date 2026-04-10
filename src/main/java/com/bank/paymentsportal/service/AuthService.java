package com.bank.paymentsportal.service;

import com.bank.paymentsportal.dto.AuthRequest;
import com.bank.paymentsportal.dto.AuthResponse;
import com.bank.paymentsportal.dto.RegisterCustomerRequest;
import com.bank.paymentsportal.dto.UserSummaryDto;
import com.bank.paymentsportal.entity.User;
import com.bank.paymentsportal.entity.UserRole;
import com.bank.paymentsportal.exception.ApiException;
import com.bank.paymentsportal.repository.UserRepository;
import com.bank.paymentsportal.security.CustomUserPrincipal;
import com.bank.paymentsportal.security.JwtService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MaskingService maskingService;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       MaskingService maskingService,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.maskingService = maskingService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already in use");
        }
        if (userRepository.existsByAccountNumber(request.accountNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "Account number is already in use");
        }
        if (userRepository.existsBySouthAfricanIdNumber(request.southAfricanIdNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "South African ID number is already in use");
        }

        User user = userRepository.save(User.builder()
                .fullName(request.fullName().trim())
                .username(request.username().trim())
                .southAfricanIdNumber(request.southAfricanIdNumber())
                .accountNumber(request.accountNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.CUSTOMER)
                .build());
        auditLogService.log(user.getId(), "REGISTER_CUSTOMER", "USER", user.getId(), "role=CUSTOMER");
        return buildAuthResponse(user);
    }

    public AuthResponse loginCustomer(AuthRequest request) {
        User user = findByUsernameOrAccountNumber(request.usernameOrAccountNumber())
                .filter(existing -> existing.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        authenticate(user.getUsername(), request.password());
        auditLogService.log(user.getId(), "LOGIN_CUSTOMER", "USER", user.getId(), "customer-login");
        return buildAuthResponse(user);
    }

    public AuthResponse loginEmployee(AuthRequest request) {
        User user = findByUsernameOrAccountNumber(request.usernameOrAccountNumber())
                .filter(existing -> existing.getRole() == UserRole.EMPLOYEE || existing.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        authenticate(user.getUsername(), request.password());
        auditLogService.log(user.getId(), "LOGIN_EMPLOYEE", "USER", user.getId(), "employee-login");
        return buildAuthResponse(user);
    }

    public UserSummaryDto currentUser(CustomUserPrincipal principal) {
        return toUserSummary(principal.getUser());
    }

    public void logout(CustomUserPrincipal principal) {
        auditLogService.log(principal.getId(), "LOGOUT", "USER", principal.getId(), "logout");
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private Optional<User> findByUsernameOrAccountNumber(String input) {
        return input.matches("^\\d{8,12}$")
                ? userRepository.findByAccountNumber(input)
                : userRepository.findByUsername(input);
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        return new AuthResponse(jwtService.generateToken(principal), user.getRole(), toUserSummary(user));
    }

    private UserSummaryDto toUserSummary(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                maskingService.maskAccount(user.getAccountNumber()),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
