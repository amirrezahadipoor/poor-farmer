package com.poorfarmer.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class EventBusTest {

  public static final class Ping {
    public final int value;

    public Ping(int value) {
      this.value = value;
    }
  }

  public static final class Pong {
    public final int value;

    public Pong(int value) {
      this.value = value;
    }
  }

  @Test
  public void subscriberReceivesPublishedEvent() {
    EventBus bus = new EventBus();
    List<Integer> received = new ArrayList<>();
    bus.subscribe(Ping.class, event -> received.add(event.value));
    bus.publish(new Ping(7));
    assertEquals(List.of(7), received);
  }

  @Test
  public void multipleSubscribersAllReceive() {
    EventBus bus = new EventBus();
    int[] first = {0};
    int[] second = {0};
    bus.subscribe(Ping.class, event -> first[0] = event.value);
    bus.subscribe(Ping.class, event -> second[0] = event.value);
    bus.publish(new Ping(3));
    assertEquals(3, first[0]);
    assertEquals(3, second[0]);
  }

  @Test
  public void unsubscribeStopsDelivery() {
    EventBus bus = new EventBus();
    int[] count = {0};
    java.util.function.Consumer<GameEvents.DayChanged> listener = event -> count[0] += 1;
    bus.subscribe(GameEvents.DayChanged.class, listener);
    bus.publish(new GameEvents.DayChanged(1, 0, 2));
    bus.unsubscribe(GameEvents.DayChanged.class, listener);
    bus.publish(new GameEvents.DayChanged(1, 0, 3));
    assertEquals(1, count[0]);
  }

  @Test
  public void publishingWithNoSubscribersIsSafe() {
    EventBus bus = new EventBus();
    bus.publish(new Pong(1));
    bus.publish(new Ping(2));
    assertTrue(true);
  }

  @Test
  public void eventTypesDoNotLeakIntoEachOther() {
    EventBus bus = new EventBus();
    int[] pings = {0};
    bus.subscribe(Ping.class, event -> pings[0] = event.value);
    bus.publish(new Pong(99));
    assertEquals(0, pings[0]);
  }
}
