package com.wafula.teza.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccountService;
import com.wafula.teza.user.application.UserAccountServiceImpl;
import com.wafula.teza.user.domain.User;
import com.wafula.teza.user.domain.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userAccountService = new UserAccountServiceImpl(userRepository, passwordEncoder, eventPublisher);
    }

    @Test
    void testChangePasswordSuccess() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("old_encoded_hash")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old_password", "old_encoded_hash")).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("new_encoded_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userAccountService.changePassword(userId, "old_password", "new_password");

        assertEquals("new_encoded_hash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void testChangePasswordInvalidOldPasswordThrows() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("old_encoded_hash")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "old_encoded_hash")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                userAccountService.changePassword(userId, "wrong_password", "new_password")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invalid old password", ex.getMessage());
    }

    @Test
    void testGenerateResetTokenSuccess() {
        String email = "test@example.com";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(Role.MERCHANT)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = userAccountService.generateResetToken(email);

        assertNotNull(token);
        assertEquals(token, user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetTokenExpiresAt());
        verify(userRepository).save(user);
    }

    @Test
    void testResetPasswordWithTokenSuccess() {
        String token = "valid-reset-token";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordResetToken(token)
                .passwordResetTokenExpiresAt(Instant.now().plusSeconds(600))
                .role(Role.RIDER)
                .build();

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new_password")).thenReturn("new_encoded_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userAccountService.resetPasswordWithToken(token, "new_password");

        assertEquals("new_encoded_hash", user.getPasswordHash());
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetTokenExpiresAt());
        verify(userRepository).save(user);
    }

    @Test
    void testResetPasswordWithExpiredTokenThrows() {
        String token = "expired-reset-token";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordResetToken(token)
                .passwordResetTokenExpiresAt(Instant.now().minusSeconds(10))
                .role(Role.RIDER)
                .build();

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class, () ->
                userAccountService.resetPasswordWithToken(token, "new_password")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invalid or expired password reset token", ex.getMessage());
    }
}
