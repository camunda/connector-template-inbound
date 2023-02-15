package io.camunda.connector.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.impl.inbound.InboundConnectorProperties;
import io.camunda.connector.inbound.subscription.MockSubscription;
import io.camunda.connector.inbound.subscription.MockSubscriptionEvent;

@InboundConnector(name = "MYINBOUNDCONNECTOR", type = "io.camunda:template.inbound:1")
public class MyConnectorExecutable implements InboundConnectorExecutable {

  private MockSubscription subscription;
  private InboundConnectorContext connectorContext;
  private InboundConnectorProperties properties;

  @Override
  public void activate(InboundConnectorProperties properties,
      InboundConnectorContext connectorContext) {

    this.properties = properties;
    this.connectorContext = connectorContext;
    subscription = new MockSubscription(this::onEvent);
  }

  @Override
  public void deactivate(InboundConnectorProperties properties) {
    subscription.stop();
  }

  private void onEvent(MockSubscriptionEvent rawEvent) {
    MyConnectorEvent connectorEvent = new MyConnectorEvent(rawEvent);
    connectorContext.correlate(properties.getCorrelationPoint(), connectorEvent);
  }
}
