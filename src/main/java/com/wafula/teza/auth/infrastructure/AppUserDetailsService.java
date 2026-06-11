package com.wafula.teza.auth.infrastructure;

import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapter between a {@link UserAccount} (owned by the user module) and Spring
 * Security's {@link UserDetails}.
 *
 * <p>The username is the user's email; the single {@link com.wafula.teza.shared.domain.Role}
 * is exposed as a {@code ROLE_<name>} authority.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserAccountService userAccountService;

    public AppUserDetailsService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount account = userAccountService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));
        return org.springframework.security.core.userdetails.User.builder()
                .username(account.email())
                .password(account.passwordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + account.role().name())))
                .disabled(!account.enabled())
                .build();
    }
}
