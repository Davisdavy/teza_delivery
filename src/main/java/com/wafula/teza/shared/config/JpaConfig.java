package com.wafula.teza.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * App-wide JPA configuration. Enables auditing so {@code @CreatedDate} /
 * {@code @LastModifiedDate} fields are populated across all modules' entities.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
