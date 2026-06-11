/**
 * Application / use-case layer for the user module.
 *
 * <p>Exposes {@code UserAccountService} and the {@code UserAccount} record — the
 * module's public contract. Other modules (notably {@code auth}) depend on this
 * layer only, never on the JPA entity or repository in {@code domain}.
 */
package com.wafula.teza.user.application;
