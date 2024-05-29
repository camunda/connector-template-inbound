package io.camunda.connector.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.CorrelationFailureHandlingStrategy.ForwardErrorToUpstream;
import io.camunda.connector.api.inbound.CorrelationFailureHandlingStrategy.Ignore;
import io.camunda.connector.api.inbound.CorrelationResult;
import io.camunda.connector.api.inbound.CorrelationResult.Failure;
import io.camunda.connector.api.inbound.CorrelationResult.Success;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.inbound.subscription.MockSubscription;
import io.camunda.connector.inbound.subscription.MockSubscriptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InboundConnector(name = "My Inbound Connector", type = "io.camunda:my-inbound-connector:1")
public class MyConnectorExecutable implements InboundConnectorExecutable<InboundConnectorContext> {

  private MockSubscription subscription;

  private InboundConnectorContext context;

  private final static Logger LOG = LoggerFactory.getLogger(MyConnectorExecutable.class);

  @Override
  public void activate(InboundConnectorContext connectorContext) {
    this.context = connectorContext;
    var props = connectorContext.bindProperties(MyConnectorProperties.class);
    subscription = new MockSubscription(props.sender(), props.messagesPerMinute(), this::onEvent);
  }

  private void onEvent(MockSubscriptionEvent rawEvent) {
    var result = context.correlateWithResult(new MyConnectorEvent(rawEvent));
    handleResult(result);
  }

  private void handleResult(CorrelationResult result) {
    switch (result) {
      case Success ignored -> LOG.debug("Message correlated successfully");
      case Failure failure -> {
        switch (failure.handlingStrategy()) {
          case ForwardErrorToUpstream ignored -> {
            LOG.error("Correlation failed, reason: {}", failure.message());
            // forward error to upstream
          }
          case Ignore ignored -> {
            LOG.debug("Correlation failed but no action required, reason: {}", failure.message());
            // ignore
          }
        }
      }
    }
  }

  @Override
  public void deactivate() {
    subscription.stop();
  }
}
