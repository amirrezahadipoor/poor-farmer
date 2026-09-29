package com.poorfarmer.core.profiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class FpsMeterTest {

  @Test
  public void stableSixtyFpsWindow() {
    FpsMeter meter = new FpsMeter(60);
    for (int i = 0; i < 60; i++) {
      meter.addFrame(1f / 60f);
    }
    assertEquals(60, meter.fps());
    assertEquals(16.667f, meter.averageFrameMs(), 0.1f);
  }

  @Test
  public void slowFramesLowerFps() {
    FpsMeter meter = new FpsMeter(30);
    for (int i = 0; i < 30; i++) {
      meter.addFrame(1f / 30f);
    }
    assertEquals(30, meter.fps());
    assertTrue(meter.averageFrameMs() > 30f);
  }

  @Test
  public void ringBufferWraps() {
    FpsMeter meter = new FpsMeter(10);
    for (int i = 0; i < 5; i++) {
      meter.addFrame(1f / 1000f);
    }
    for (int i = 0; i < 100; i++) {
      meter.addFrame(1f / 60f);
    }
    assertEquals(10, meter.sampleCount());
    assertEquals(60, meter.fps());
  }

  @Test
  public void worstFrameTracksSpike() {
    FpsMeter meter = new FpsMeter(100);
    for (int i = 0; i < 50; i++) {
      meter.addFrame(1f / 60f);
    }
    meter.addFrame(0.12f);
    assertEquals(120f, meter.worstFrameMs(), 0.5f);
    assertTrue(meter.averageFrameMs() > 16.7f);
  }

  @Test
  public void zeroAndNegativeFramesIgnored() {
    FpsMeter meter = new FpsMeter(10);
    meter.addFrame(0f);
    meter.addFrame(-1f);
    assertEquals(0, meter.sampleCount());
    assertEquals(0, meter.fps());
  }

  @Test
  public void meetsTargetLogic() {
    FpsMeter meter = new FpsMeter(60);
    for (int i = 0; i < 60; i++) {
      meter.addFrame(1f / 60f);
    }
    assertTrue(meter.meetsTarget(60));
    assertFalse(meter.meetsTarget(120));
    meter.reset();
    assertFalse(meter.meetsTarget(60));
    assertEquals(0, meter.sampleCount());
  }

  @Test
  public void tinyCapacityRejected() {
    try {
      new FpsMeter(1);
      fail("expected capacity rejection");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void memoryProbeInterfacesReportPositiveValues() {
    MemoryProbe probe = new JvmMemoryProbe();
    assertTrue(probe.usedBytes() > 0);
    assertTrue(probe.maxBytes() > probe.usedBytes() / 2);
  }
}
