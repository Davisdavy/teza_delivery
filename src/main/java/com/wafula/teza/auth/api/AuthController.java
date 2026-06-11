package com.wafula.teza.auth.api;

import com.wafula.teza.auth.api.dto.AuthResponse;
import com.wafula.teza.auth.api.dto.LoginRequest;
import com.wafula.teza.auth.api.dto.RefreshTokenRequest;
import com.wafula.teza.auth.api.dto.RegisterRequest;
import com.wafula.teza.auth.api.dto.ForgotPasswordRequest;
import com.wafula.teza.auth.api.dto.ForgotPasswordResponse;
import com.wafula.teza.auth.api.dto.ResetPasswordRequest;
import com.wafula.teza.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints. This thin web adapter validates input and
 * delegates to {@link AuthService}; it holds no authentication logic itself.
 * All routes here are permitted without a token (see the security configuration).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthResponse.from(
                authService.register(request.email(), request.password(), request.role()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return AuthResponse.from(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.generateResetToken(request.email());
        return new ForgotPasswordResponse(request.email(), token);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPasswordWithToken(request.token(), request.newPassword());
    }
}
