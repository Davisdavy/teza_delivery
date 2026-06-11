/**
 * Notification module.
 *
 * <p>Sends outbound notifications (push, SMS, email) in response to events from
 * other modules. Purely a consumer of domain events — no other module depends
 * on it — which keeps notification an optional, swappable concern.
 */
package com.wafula.teza.notification;
