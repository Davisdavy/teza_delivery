package com.wafula.teza.user.application;

import com.wafula.teza.shared.domain.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use-case entry point for user accounts. This is the user module's public surface;
 * other modules depend on this interface, never on the JPA entity or repository.
 */
public interface UserAccountService {

    /**
     * Create a new account with an already-encoded password hash.
     *
     * @throws com.wafula.teza.shared.exception.ApiException if the email is taken.
     */
    UserAccount create(String email, String passwordHash, Role role);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(UUID id);

    boolean existsByEmail(String email);

    List<UserAccount> findAll();

    UserAccount update(UUID id, String email, Boolean enabled, Role role, UUID updaterId);

    void delete(UUID id);

    void changePassword(UUID userId, String oldPassword, String newPassword);

    String generateResetToken(String email);

    void resetPasswordWithToken(String token, String newPassword);
}
