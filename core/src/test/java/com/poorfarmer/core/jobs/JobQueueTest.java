package com.poorfarmer.core.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;

public class JobQueueTest {

  private JobQueue queue;

  @After
  public void tearDown() {
    if (queue != null) {
      queue.shutdown();
      queue = null;
    }
  }

  private JobQueue newQueue() {
    queue = new JobQueue();
    return queue;
  }

  private static void awaitUntil(JobQueue q, java.util.function.BooleanSupplier condition, long timeoutMs)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (!condition.getAsBoolean()) {
      if (System.currentTimeMillis() > deadline) {
        throw new AssertionError("timed out waiting for condition");
      }
      q.pollCallbacks();
      Thread.sleep(5);
    }
    q.pollCallbacks();
  }

  @Test
  public void jobRunsAndCallbackReceivesResult() throws Exception {
    JobQueue q = newQueue();
    AtomicReference<Object> delivered = new AtomicReference<>();
    q.submit(() -> 42, delivered::set);
    awaitUntil(q, () -> delivered.get() != null, 2000);
    assertEquals(42, delivered.get());
    awaitUntil(q, q::isIdle, 2000);
  }

  @Test
  public void jobsRunInFifoOrder() throws Exception {
    JobQueue q = newQueue();
    List<Integer> results = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      final int value = i;
      q.submit(() -> value, result -> results.add((Integer) result));
    }
    awaitUntil(q, () -> results.size() == 6, 2000);
    assertEquals(List.of(0, 1, 2, 3, 4, 5), results);
  }

  @Test
  public void callbackWithoutSubmitIsNoOp() throws Exception {
    JobQueue q = newQueue();
    q.pollCallbacks();
    q.submit(() -> null);
    awaitUntil(q, q::isIdle, 2000);
  }

  @Test
  public void queuedCountTracksPendingWork() throws Exception {
    JobQueue q = newQueue();
    CountDownLatch firstEntered = new CountDownLatch(1);
    AtomicReference<CountDownLatch> release = new AtomicReference<>();
    release.set(new CountDownLatch(1));
    q.submit(() -> {
      firstEntered.countDown();
      try {
        release.get().await(2, TimeUnit.SECONDS);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      return null;
    });
    firstEntered.await(2, TimeUnit.SECONDS);
    assertEquals(1, q.queuedCount());
    release.get().countDown();
    awaitUntil(q, q::isIdle, 2000);
  }

  @Test
  public void exceptionInJobDoesNotStopQueue() throws Exception {
    JobQueue q = newQueue();
    AtomicReference<Object> second = new AtomicReference<>();
    q.submit(() -> {
      throw new IllegalStateException("boom");
    }, null);
    q.submit(() -> "recovered", second::set);
    awaitUntil(q, () -> second.get() != null, 2000);
    assertEquals("recovered", second.get());
  }

  @Test
  public void shutdownStopsAcceptingWork() throws Exception {
    JobQueue q = new JobQueue();
    CountDownLatch entered = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    q.submit(() -> {
      entered.countDown();
      try {
        release.await(2, TimeUnit.SECONDS);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      return null;
    });
    entered.await(2, TimeUnit.SECONDS);
    release.countDown();
    awaitUntil(q, q::isIdle, 2000);
    q.shutdown();
    AtomicReference<Object> after = new AtomicReference<>();
    q.submit(() -> "late", after::set);
    q.pollCallbacks();
    Thread.sleep(100);
    q.pollCallbacks();
    assertNull(after.get());
  }
}
