package com.wafula.teza.auth.config;

import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.user.application.UserAccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Seeds a single {@code ADMIN} account on startup so the system is reachable
 * before any users exist. The public registration endpoint cannot create admins,
 * so this bootstrap is the only path to the first one.
 *
 * <p>No-op when disabled, when credentials are blank, or when the account already
 * exists — making it safe to run on every boot.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminBootstrap.class);

    private final BootstrapAdminProperties properties;
    private final UserAccountService userAccountService;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(BootstrapAdminProperties properties,
            UserAccountService userAccountService,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
            log.warn("Bootstrap admin enabled but email/password not set; skipping seed.");
            return;
        }
        if (userAccountService.existsByEmail(properties.email())) {
            return;
        }
        userAccountService.create(properties.email(), passwordEncoder.encode(properties.password()), Role.SUPER_ADMIN);
        log.info("Seeded bootstrap SUPER_ADMIN account: {}", properties.email());
    }
}
