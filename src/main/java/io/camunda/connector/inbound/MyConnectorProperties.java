package io.camunda.connector.inbound;

import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
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
    @NotNull
    @TemplateProperty(
            id = "sender",
            label = "Sender",
            group = "properties",
            binding = @TemplateProperty.PropertyBinding(name = "sender"),
            feel = Property.FeelMode.optional)
    String sender,
    @TemplateProperty(
            id = "messagesPerMinute",
            label = "Message per minute",
            group = "properties",
            binding = @TemplateProperty.PropertyBinding(name = "messagesPerMinute"),
            feel = Property.FeelMode.optional)
    @Max(10) @Min(1) int messagesPerMinute
) {}
