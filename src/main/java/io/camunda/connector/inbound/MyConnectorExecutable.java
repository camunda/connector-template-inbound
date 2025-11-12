package io.camunda.connector.inbound;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.CorrelationFailureHandlingStrategy;
import io.camunda.connector.api.inbound.CorrelationRequest;
import io.camunda.connector.api.inbound.CorrelationResult;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.inbound.subscription.MockSubscription;
import io.camunda.connector.inbound.subscription.MockSubscriptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOG = LoggerFactory.getLogger(MyConnectorExecutable.class);

  private MockSubscription subscription;

  private InboundConnectorContext context;

  /**
   * Called when the connector is activated (e.g., when a process definition is deployed).
   *
   * <p>This method is called <b>synchronously</b>, so it's recommended to start any long-running
   * operations (like message consumers) asynchronously to avoid blocking the deployment.
   *
   * <p>Example: The Kafka Connector starts consumers in a separate thread. See:
   * <a href="https://github.com/camunda/connectors/blob/main/connectors/kafka/src/main/java/io/camunda/connector/kafka/inbound/KafkaExecutable.java">Kafka Connector</a>
   *
   * @param connectorContext the context providing access to connector properties and correlation methods
   */
  @Override
  public void activate(InboundConnectorContext connectorContext) {
    this.context = connectorContext;
    var props = connectorContext.bindProperties(MyConnectorProperties.class);
    subscription = new MockSubscription(props.sender(), props.messagesPerMinute(), this::onEvent);
    LOG.info("Connector activated for sender: {}", props.sender());
  }

  private void onEvent(MockSubscriptionEvent rawEvent) {
    var event = new MyConnectorEvent(rawEvent);
    var correlationRequest = CorrelationRequest.builder().variables(event).build();

    // Correlate the event with the process instance
    // The correlation result provides feedback about what happened with the event
    var result = context.correlate(correlationRequest);
    handleResult(result);
  }

  private void handleResult(CorrelationResult result) {
    switch (result) {
      case CorrelationResult.Success ignored -> LOG.debug("Message correlated successfully");
      case CorrelationResult.Failure failure -> {
        switch (failure.handlingStrategy()) {
          case CorrelationFailureHandlingStrategy.ForwardErrorToUpstream ignored -> {
            LOG.error("Correlation failed, reason: {}", failure.message());
            // forward error to upstream
          }
          case CorrelationFailureHandlingStrategy.Ignore ignored -> {
            LOG.debug("Correlation failed but no action required, reason: {}", failure.message());
            // ignore
          }
        }
      }
    }
  }

  /**
   * Called when the connector is deactivated.
   *
   * <p>This happens when:
   * <ul>
   *   <li>A process definition is deleted</li>
   *   <li>A new version of the process definition is deployed</li>
   *   <li>The connector runtime is shutting down</li>
   * </ul>
   *
   * <p>Use this method to clean up resources like closing connections, stopping consumers, etc.
   */
  @Override
  public void deactivate() {
    subscription.stop();
    LOG.info("Connector deactivated");
  }
}
