/**
 * Global Spring configuration applied application-wide.
 *
 * <p>Holds beans that are not owned by any single module, e.g. JPA/auditing
 * setup, web/CORS, and API documentation. Beans that belong to one module live
 * in that module instead — notably the security filter chain, which lives in
 * {@code auth.config} because it depends on the auth module's JWT filter and
 * {@code shared} must never depend on a business module.
 */
package com.wafula.teza.shared.config;
