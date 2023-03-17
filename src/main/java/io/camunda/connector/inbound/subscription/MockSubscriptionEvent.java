package io.camunda.connector.inbound.subscription;

import java.util.Objects;

/**
 * Data model of an event consumed by inbound Connector (e.g. originating from an external system)
 */
public class MockSubscriptionEvent {
  private final String sender;
  private final int code;
  private final String message;

  public MockSubscriptionEvent(String sender, int code, String message) {
    this.sender = sender;
    this.code = code;
    this.message = message;
  }

  public String getSender() {
    return sender;
  }

  public String getMessage() {
    return message;
  }

  public int getCode() {
    return code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MockSubscriptionEvent that = (MockSubscriptionEvent) o;
    return code == that.code && Objects.equals(sender, that.sender)
        && Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sender, code, message);
  }

  @Override
  public String toString() {
    return "MockSubscriptionEvent{" +
        "sender='" + sender + '\'' +
        ", code=" + code +
        ", message='" + message + '\'' +
        '}';
  }
}
