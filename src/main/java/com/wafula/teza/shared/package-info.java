/**
 * Shared kernel and cross-cutting concerns reused by every business module.
 *
 * <p>Dependency rule: business modules may depend on {@code shared}, but
 * {@code shared} must never depend on a business module. This keeps the
 * coupling pointing inward and prevents the shared kernel from leaking
 * domain-specific knowledge.
 */
package com.wafula.teza.shared;
