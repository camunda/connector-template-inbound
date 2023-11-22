package io.camunda.connector.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.inbound.subscription.MockSubscription;
import io.camunda.connector.inbound.subscription.MockSubscriptionEvent;

@InboundConnector(name = "My Inbound Connector", type = "io.camunda:my-inbound-connector:1")
public class MyConnectorExecutable implements InboundConnectorExecutable<InboundConnectorContext> {

  private MockSubscription subscription;

  private InboundConnectorContext context;

  @Override
  public void activate(InboundConnectorContext connectorContext) {
    this.context = connectorContext;
    var props = connectorContext.bindProperties(MyConnectorProperties.class);
    subscription = new MockSubscription(props.sender(), props.messagesPerMinute(), this::onEvent);
  }

  private void onEvent(MockSubscriptionEvent rawEvent) {
    context.correlate(new MyConnectorEvent(rawEvent));
  }

  @Override
  public void deactivate() {
    subscription.stop();
  }
}
