package com.poorfarmer.core.profiler;

public final class FpsMeter {

  private final float[] samples;
  private final int capacity;
  private int head;
  private int count;
  private double sum;

  public FpsMeter(int capacity) {
    if (capacity < 2) {
      throw new IllegalArgumentException("capacity must be at least 2");
    }
    this.capacity = capacity;
    this.samples = new float[capacity];
  }

  public synchronized void addFrame(float frameSeconds) {
    if (frameSeconds <= 0f) {
      return;
    }
    if (count == capacity) {
      sum -= samples[head];
      samples[head] = frameSeconds;
      head = (head + 1) % capacity;
    } else {
      samples[count] = frameSeconds;
      count++;
      if (count == capacity) {
        head = 0;
      }
    }
    sum += frameSeconds;
  }

  public synchronized int fps() {
    if (count == 0) {
      return 0;
    }
    float average = (float) (sum / count);
    if (average <= 0f) {
      return 0;
    }
    return (int) Math.round(1f / average);
  }

  public synchronized float averageFrameMs() {
    if (count == 0) {
      return 0f;
    }
    return (float) ((sum / count) * 1000f);
  }

  public synchronized float worstFrameMs() {
    float worst = 0f;
    for (int i = 0; i < count; i++) {
      if (samples[i] > worst) {
        worst = samples[i];
      }
    }
    return worst * 1000f;
  }

  public synchronized boolean meetsTarget(int targetFps) {
    if (count < 2) {
      return false;
    }
    return fps() >= targetFps - 1;
  }

  public synchronized int sampleCount() {
    return count;
  }

  public synchronized void reset() {
    head = 0;
    count = 0;
    sum = 0;
  }
}
