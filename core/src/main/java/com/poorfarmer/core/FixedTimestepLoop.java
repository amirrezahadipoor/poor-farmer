package com.poorfarmer.core;

public final class FixedTimestepLoop {

  public static final float FIXED_STEP_SECONDS = 1f / 60f;
  public static final float MAX_FRAME_SECONDS = 0.25f;

  private float accumulator;

  public int accumulateAndStep(float frameSeconds, FixedStepConsumer step) {
    float clamped = Math.min(Math.max(frameSeconds, 0f), MAX_FRAME_SECONDS);
    accumulator += clamped;
    int steps = 0;
    while (accumulator >= FIXED_STEP_SECONDS) {
      step.accept(FIXED_STEP_SECONDS);
      accumulator -= FIXED_STEP_SECONDS;
      steps++;
    }
    return steps;
  }

  public float interpolationAlpha() {
    return accumulator / FIXED_STEP_SECONDS;
  }

  public void reset() {
    accumulator = 0f;
  }
}
