package io.camunda.connector.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.camunda.connector.api.inbound.CorrelationRequest;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyConnectorExecutableTest {

  @Mock
  private InboundConnectorContext context;

  private MyConnectorExecutable connector;

  @BeforeEach
  void setUp() {
    connector = new MyConnectorExecutable();
  }

  @AfterEach
  void tearDown() {
    if (connector != null) {
      connector.deactivate();
    }
  }

  @Test
  void shouldActivateConnectorWithProperties() {
    // given
    var properties = new MyConnectorProperties("test-sender", 10);
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);

    // when
    connector.activate(context);

    // then
    verify(context).bindProperties(MyConnectorProperties.class);
  }

  @Test
  void shouldCorrelateEventsSuccessfully() {
    // given
    var properties = new MyConnectorProperties("test-sender", 60); // 60 events per minute
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);

    // when
    connector.activate(context);

    // then - wait for at least one event to be generated (initial delay is 5 seconds)
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> verify(context, atLeastOnce()).correlate(any(CorrelationRequest.class)));

    ArgumentCaptor<CorrelationRequest> captor = ArgumentCaptor.forClass(CorrelationRequest.class);
    verify(context, atLeastOnce()).correlate(captor.capture());

    CorrelationRequest request = captor.getValue();
    assertThat(request.getVariables()).isNotNull();
    assertThat(request.getVariables()).isInstanceOf(MyConnectorEvent.class);

    MyConnectorEvent event = (MyConnectorEvent) request.getVariables();
    assertThat(event.event()).isNotNull();
    assertThat(event.event().sender()).isEqualTo("test-sender");
  }

  @Test
  void shouldHandleCorrelation() {
    // given
    var properties = new MyConnectorProperties("test-sender", 60);
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);

    // when
    connector.activate(context);

    // then - wait for at least one event to be correlated
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> verify(context, atLeastOnce()).correlate(any(CorrelationRequest.class)));
  }

  @Test
  void shouldStopSubscriptionOnDeactivate() {
    // given
    var properties = new MyConnectorProperties("test-sender", 10);
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);

    connector.activate(context);

    // when
    connector.deactivate();

    // then - no exception should be thrown
    // The subscription should be stopped
  }
}

