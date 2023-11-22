package io.camunda.connector.inbound;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for inbound Connector
 *
 * @param sender
 * @param messagesPerMinute
 */
public record MyConnectorProperties(
    @NotNull String sender,
    @Max(10) @Min(1) int messagesPerMinute
) {}
