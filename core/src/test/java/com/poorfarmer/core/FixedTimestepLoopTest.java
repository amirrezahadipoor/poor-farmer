package com.poorfarmer.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class FixedTimestepLoopTest {

  private static final class StepCounter implements FixedStepConsumer {
    int count;
    float total;

    @Override
    public void accept(float fixedSeconds) {
      count++;
      total += fixedSeconds;
    }
  }

  @Test
  public void exactFrameRunsExactlyOneStep() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    int steps = loop.accumulateAndStep(FixedTimestepLoop.FIXED_STEP_SECONDS, counter);
    assertEquals(1, steps);
    assertEquals(1, counter.count);
    assertEquals(0f, loop.interpolationAlpha(), 1e-4f);
  }

  @Test
  public void largeFrameRunsManySteps() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    int steps = loop.accumulateAndStep(0.2f, counter);
    assertEquals(12, steps);
    assertEquals(12f * FixedTimestepLoop.FIXED_STEP_SECONDS, counter.total, 1e-4f);
  }

  @Test
  public void partialFramesAccumulateUntilFullStep() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    int first = loop.accumulateAndStep(0.008f, counter);
    int second = loop.accumulateAndStep(0.008f, counter);
    int third = loop.accumulateAndStep(0.008f, counter);
    assertEquals(0, first);
    assertEquals(0, second);
    assertEquals(1, third);
    assertEquals(1, counter.count);
  }

  @Test
  public void hugeFrameIsClampedToAvoidSpiralOfDeath() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    int steps = loop.accumulateAndStep(10f, counter);
    float maxExpected = FixedTimestepLoop.MAX_FRAME_SECONDS / FixedTimestepLoop.FIXED_STEP_SECONDS;
    assertTrue(steps <= (int) maxExpected + 1);
  }

  @Test
  public void interpolationAlphaStaysWithinUnitRange() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    for (int i = 0; i < 1000; i++) {
      loop.accumulateAndStep(0.001f + (i % 7) * 0.003f, counter);
      float alpha = loop.interpolationAlpha();
      assertTrue(alpha >= 0f && alpha < 1f);
    }
  }

  @Test
  public void negativeFrameIsIgnored() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    int steps = loop.accumulateAndStep(-0.5f, counter);
    assertEquals(0, steps);
    assertEquals(0f, loop.interpolationAlpha(), 1e-6f);
  }

  @Test
  public void resetClearsAccumulator() {
    FixedTimestepLoop loop = new FixedTimestepLoop();
    StepCounter counter = new StepCounter();
    loop.accumulateAndStep(0.03f, counter);
    loop.reset();
    assertEquals(0f, loop.interpolationAlpha(), 1e-6f);
  }
}
