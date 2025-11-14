package io.camunda.connector.inbound.integration;

import io.camunda.client.CamundaClient;
import io.camunda.connector.inbound.MyConnectorEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {TestConnectorRuntimeApplication.class},
    properties = {
      "spring.main.allow-bean-definition-overriding=true",
      "camunda.connector.webhook.enabled=true",
      "camunda.connector.polling.enabled=true"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CamundaSpringProcessTest
public class MyConnectorIntegrationTest {
  private static final String SENDER_ID = "theSenderId";

  @Autowired private CamundaClient client;

  @BeforeAll
  static void setup() {
    // Increase the default timeout for assertions to 30 seconds
    CamundaAssert.setAssertionTimeout(Duration.ofSeconds(30));
  }

  @Test
  void testMyConnectorFunctionality() {
    // given - create a process instance with a correlation variable
    final var processInstance =
        client
            .newCreateInstanceCommand()
            // processes in resources/bpmn are automatically deployed
            .bpmnProcessId("intermediate-event-test-process")
            .latestVersion()
            .variable("expectedSender", SENDER_ID)
            .send()
            .join();

    // then - verify the process instance is waiting at the connector
    assertThatProcessInstance(processInstance).hasActiveElements(byName("MyConnector"));

    // The connector will automatically generate events and correlate them
    // Verify the process completes
    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byName("MyConnector"))
        .hasVariable("sender", SENDER_ID)
        .hasVariableSatisfies(
            "allResult",
            MyConnectorEvent.class,
            myConnectorEvent -> {
              var event = myConnectorEvent.event();
              assertThat(event.sender()).isEqualTo(SENDER_ID);
              assertThat(event.message()).isNotEmpty();
              assertThat(event.code()).isGreaterThanOrEqualTo(0);
            })
        .isCompleted();
  }
}
