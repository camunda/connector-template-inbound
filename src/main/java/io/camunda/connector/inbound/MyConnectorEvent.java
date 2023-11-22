package io.camunda.connector.inbound;

import io.camunda.connector.inbound.subscription.MockSubscriptionEvent;
import java.util.Objects;

/**
 * Data model of an event produced by the inbound Connector
 *
 * @param event
 */
public record MyConnectorEvent(MockSubscriptionEvent event) {}
