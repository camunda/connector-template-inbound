package io.camunda.connector.inbound.subscription;

/**
 * Data model of an event consumed by inbound Connector (e.g. originating from an external system)
 *
 * @param sender
 * @param code
 * @param message
 */
public record MockSubscriptionEvent(String sender, int code, String message) {}
