/**
 * Core domain model for the user module.
 *
 * <p>The {@code User} entity and its repository port. Pure identity model; carries
 * the credential hash but no knowledge of how it is produced or verified (that is
 * the {@code auth} module's job).
 */
package com.wafula.teza.user.domain;
