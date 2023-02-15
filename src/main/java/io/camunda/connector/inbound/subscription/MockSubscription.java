package io.camunda.connector.inbound.subscription;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSubscription {
  private final static Logger LOG = LoggerFactory.getLogger(MockSubscription.class);

  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private final EventGenerator generator = new EventGenerator();

  public MockSubscription(Consumer<MockSubscriptionEvent> callback) {
    LOG.info("Activating mock subscription");
    executor.scheduleAtFixedRate(() -> produceEvent(callback), 5, 10, TimeUnit.SECONDS);
  }

  public void stop() {
    LOG.info("Deactivating mock subscription");
    executor.shutdownNow();
  }

  private void produceEvent(Consumer<MockSubscriptionEvent> callback) {
    MockSubscriptionEvent event = generator.getRandomEvent();
    LOG.info("Emulating subscription event: " + event);
    callback.accept(event);
  }

  private static class EventGenerator {
    private final String[] SENDERS = {"Alice", "Bob", "Charlie", "Dave"};
    private final int MAX_CODE = 10;

    private final Random random = new Random();

    public MockSubscriptionEvent getRandomEvent() {
      String sender = SENDERS[random.nextInt(SENDERS.length)];
      int code = random.nextInt(MAX_CODE);
      String message = UUID.randomUUID().toString();
      return new MockSubscriptionEvent(sender, code, message);
    }
  }
}
