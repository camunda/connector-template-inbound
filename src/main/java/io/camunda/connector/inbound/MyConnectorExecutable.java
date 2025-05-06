package io.camunda.connector.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.CorrelationRequest;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.inbound.subscription.MockSubscription;
import io.camunda.connector.inbound.subscription.MockSubscriptionEvent;

@InboundConnector(name = "My Inbound Connector", type = "io.camunda:my-inbound-connector:1")
@ElementTemplate(
        id = "io.camunda.connector.Template.v1",
        name = "Template connector",
        version = 1,
        description = "Describe this connector",
        icon = "icon.svg",
        documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/",
        propertyGroups = {
                @ElementTemplate.PropertyGroup(id = "properties", label = "Properties"),
        },
        inputDataClass = MyConnectorProperties.class)
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
    var event = new MyConnectorEvent(rawEvent);
    context.correlate(CorrelationRequest.builder().variables(event).build());
  }

  @Override
  public void deactivate() {
    subscription.stop();
  }
}
