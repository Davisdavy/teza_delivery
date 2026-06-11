/**
 * Internal event-publishing infrastructure for inter-module communication.
 *
 * <p>Modules stay loosely coupled by publishing and handling application
 * events here (e.g. via Spring's {@code ApplicationEventPublisher}) instead of
 * calling each other's services directly. This is the seam that supports the
 * "event-driven internal services" principle from the README.
 */
package com.wafula.teza.shared.event;
