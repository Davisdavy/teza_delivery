/**
 * User module — owns user accounts and identity.
 *
 * <p>The system of record for who a user is (email, role, enabled state) and their
 * stored credential hash. Authentication mechanics live in {@code auth}, which
 * depends on this module's {@code application} layer for identity; this module
 * depends on no other business module.
 */
package com.wafula.teza.user;
