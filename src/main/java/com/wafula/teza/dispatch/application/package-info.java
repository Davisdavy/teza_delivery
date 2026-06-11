/**
 * Application / use-case layer for the dispatch module.
 *
 * <p>Hosts the matching workflow: listens for delivery/rider events, invokes
 * the domain matching strategy, defines transaction boundaries, and publishes
 * assignment results. The only layer other modules may depend on.
 */
package com.wafula.teza.dispatch.application;
