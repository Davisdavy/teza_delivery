package com.wafula.teza.delivery.domain;

/**
 * Status lifecycle of a delivery order.
 */
public enum DeliveryStatus {
    PENDING,
    SEARCHING,
    ASSIGNED,
    ARRIVED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
