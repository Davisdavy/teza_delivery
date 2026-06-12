package com.wafula.teza.auth.config;

import com.wafula.teza.auth.infrastructure.JwtAuthenticationEntryPoint;
import com.wafula.teza.auth.infrastructure.JwtAuthenticationFilter;
import com.wafula.teza.auth.infrastructure.JwtService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central security wiring for the application, kept entirely out of the business
 * logic. Establishes stateless JWT authentication and the role-based access rules.
 *
 * <p>Lives in the {@code auth} module (not {@code shared}) because the filter chain
 * depends on this module's {@link JwtAuthenticationFilter}; the architecture forbids
 * {@code shared} from depending on a business module.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, BootstrapAdminProperties.class})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        return http
                // No browser sessions or cookies → CSRF and the stateful login mechanisms are off.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Representative role rules; modules add finer-grained @PreAuthorize later.
                        .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "SUPPORT_ADMIN")
                        .requestMatchers("/api/merchant/**").hasAnyRole("MERCHANT", "SUPER_ADMIN", "SUPPORT_ADMIN")
                        .requestMatchers("/api/rider/**").hasAnyRole("RIDER", "SUPER_ADMIN", "SUPPORT_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * The JWT filter, registered only inside the security chain. Defined here as a
     * {@code @Bean} rather than a {@code @Component} so the servlet container does not
     * also register it as a global filter (which would run it twice).
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the auto-configured {@link AuthenticationManager}, which Spring backs with
     * a DAO provider over our {@code UserDetailsService} + {@link PasswordEncoder} beans.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
