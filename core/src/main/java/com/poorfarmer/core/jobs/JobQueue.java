package com.poorfarmer.core.jobs;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class JobQueue {

  public interface Job {
    Object run();
  }

  private static final class Pending {
    final Job job;
    final Consumer<Object> callback;
    volatile Object result;

    Pending(Job job, Consumer<Object> callback) {
      this.job = job;
      this.callback = callback;
    }
  }

  private final ArrayDeque<Pending> queued = new ArrayDeque<>();
  private final ArrayDeque<Pending> finished = new ArrayDeque<>();
  private final Thread worker;
  private volatile boolean shutdown;
  private int running;

  public JobQueue() {
    worker = new Thread(this::run, "poor-farmer-jobs");
    worker.setDaemon(true);
    worker.start();
  }

  public void submit(Job job) {
    submit(job, null);
  }

  public void submit(Job job, Consumer<Object> callback) {
    synchronized (queued) {
      if (shutdown) {
        return;
      }
      queued.add(new Pending(job, callback));
      queued.notifyAll();
    }
  }

  public void pollCallbacks() {
    Pending pending;
    synchronized (finished) {
      while ((pending = finished.pollFirst()) != null) {
        if (pending.callback != null) {
          pending.callback.accept(pending.result);
        }
      }
    }
  }

  public int queuedCount() {
    synchronized (queued) {
      return queued.size() + running;
    }
  }

  public boolean isIdle() {
    synchronized (queued) {
      return queued.isEmpty() && running == 0;
    }
  }

  public void shutdown() {
    synchronized (queued) {
      shutdown = true;
      queued.notifyAll();
    }
    try {
      worker.join(2000);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  private void run() {
    while (true) {
      Pending pending;
      synchronized (queued) {
        while (queued.isEmpty()) {
          if (shutdown) {
            return;
          }
          try {
            queued.wait(50);
          } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
          }
        }
        pending = queued.pollFirst();
        running++;
      }
      Object result;
      try {
        result = pending.job.run();
      } catch (RuntimeException error) {
        result = null;
      }
      synchronized (queued) {
        running--;
      }
      synchronized (finished) {
        pending.result = result;
        finished.addLast(pending);
      }
    }
  }
}
