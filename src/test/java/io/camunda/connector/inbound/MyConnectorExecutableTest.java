package io.camunda.connector.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.camunda.connector.api.inbound.CorrelationRequest;
import io.camunda.connector.api.inbound.CorrelationResult;
import io.camunda.connector.api.inbound.InboundConnectorContext;
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

  @Mock
  private CorrelationResult correlationResult;

  private MyConnectorExecutable connector;

  @BeforeEach
  void setUp() {
    connector = new MyConnectorExecutable();
  }

  @Test
  void shouldActivateConnectorWithProperties() {
    // given
    var properties = new MyConnectorProperties("test-sender", 10);
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);
    when(context.correlate(any(CorrelationRequest.class))).thenReturn(correlationResult);

    // when
    connector.activate(context);

    // then
    verify(context).bindProperties(MyConnectorProperties.class);
  }

  @Test
  void shouldCorrelateEventsSuccessfully() throws InterruptedException {
    // given
    var properties = new MyConnectorProperties("test-sender", 60); // 60 events per minute = 1 per second
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);
    when(context.correlate(any(CorrelationRequest.class))).thenReturn(correlationResult);

    // when
    connector.activate(context);

    // wait for at least one event to be generated
    Thread.sleep(1500);

    // then
    ArgumentCaptor<CorrelationRequest> captor = ArgumentCaptor.forClass(CorrelationRequest.class);
    verify(context, atLeastOnce()).correlate(captor.capture());

    CorrelationRequest request = captor.getValue();
    assertThat(request.getVariables()).isNotNull();
    assertThat(request.getVariables()).isInstanceOf(MyConnectorEvent.class);

    MyConnectorEvent event = (MyConnectorEvent) request.getVariables();
    assertThat(event.event()).isNotNull();
    assertThat(event.event().sender()).isEqualTo("test-sender");

    // cleanup
    connector.deactivate();
  }

  @Test
  void shouldHandleCorrelation() throws InterruptedException {
    // given
    var properties = new MyConnectorProperties("test-sender", 60);
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);
    when(context.correlate(any(CorrelationRequest.class))).thenReturn(correlationResult);

    // when
    connector.activate(context);

    // wait for at least one event to be generated
    Thread.sleep(1500);

    // then
    verify(context, atLeastOnce()).correlate(any(CorrelationRequest.class));

    // cleanup
    connector.deactivate();
  }

  @Test
  void shouldStopSubscriptionOnDeactivate() {
    // given
    var properties = new MyConnectorProperties("test-sender", 10);
    when(context.bindProperties(MyConnectorProperties.class)).thenReturn(properties);
    when(context.correlate(any(CorrelationRequest.class))).thenReturn(correlationResult);

    connector.activate(context);

    // when
    connector.deactivate();

    // then - no exception should be thrown
    // The subscription should be stopped
  }
}

