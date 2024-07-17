package io.camunda.connector.inbound.subscription;

public record WatchServiceSubscriptionEvent(
    String monitoredEvent,
    String directory, 
    String fileName
){}
