package com.poorfarmer.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TouchInputTest {

  private static final class Recorder implements TouchInput.Listener {
    final List<String> taps = new ArrayList<>();
    final List<String> longPresses = new ArrayList<>();
    final List<String> drags = new ArrayList<>();
    final List<String> pinches = new ArrayList<>();
    final List<Integer> rotations = new ArrayList<>();

    @Override
    public void onTap(float x, float y) {
      taps.add(format(x, y));
    }

    @Override
    public void onLongPress(float x, float y) {
      longPresses.add(format(x, y));
    }

    @Override
    public void onDrag(float x, float y, float dx, float dy) {
      drags.add(format(x, y) + "|" + format(dx, dy));
    }

    @Override
    public void onPinch(float distanceDelta, float centerX, float centerY) {
      pinches.add(format(distanceDelta, centerX));
    }

    @Override
    public void onRotate(int steps) {
      rotations.add(steps);
    }

    private static String format(float a, float b) {
      return String.format("%.2f,%.2f", a, b);
    }
  }

  private static float px(float x, float y) {
    return (float) Math.sqrt(x * x + y * y);
  }

  @Test
  public void quickSmallMoveIsTap() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.up(0, 110f);
    assertEquals(1, r.taps.size());
    assertEquals("100.00,100.00", r.taps.get(0));
  }

  @Test
  public void largeMoveIsNotTap() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.move(0, 140, 100, 50f);
    t.up(0, 80f);
    assertTrue(r.taps.isEmpty());
    assertFalse(r.drags.isEmpty());
  }

  @Test
  public void slowReleaseIsNotTap() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.up(0, 300f);
    assertTrue(r.taps.isEmpty());
    assertTrue(r.longPresses.isEmpty());
  }

  @Test
  public void heldPointerFiresLongPressOnce() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.move(0, 102, 101, 600f);
    t.move(0, 103, 101, 900f);
    assertEquals(1, r.longPresses.size());
    t.up(0, 1000f);
    assertEquals(1, r.longPresses.size());
    assertTrue(r.taps.isEmpty());
  }

  @Test
  public void dragAfterLongPressContinues() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.move(0, 101, 100, 600f);
    t.move(0, 130, 100, 700f);
    assertEquals(1, r.longPresses.size());
    assertEquals(1, r.drags.size());
    assertEquals("130.00,100.00|29.00,0.00", r.drags.get(0));
  }

  @Test
  public void dragReportsDeltas() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.move(0, 105, 100, 10f);
    t.move(0, 120, 100, 20f);
    t.move(0, 125, 110, 30f);
    assertEquals(2, r.drags.size());
    assertEquals("120.00,100.00|15.00,0.00", r.drags.get(0));
    assertEquals("125.00,110.00|5.00,10.00", r.drags.get(1));
  }

  @Test
  public void twoPointersPinch() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.down(1, 180, 100, 0f);
    t.move(0, 90, 100, 30f);
    t.move(1, 190, 100, 30f);
    assertEquals(2, r.pinches.size());
    float totalDelta = 0f;
    for (String pinch : r.pinches) {
      totalDelta += Float.parseFloat(pinch.split(",")[0]);
    }
    assertEquals(px(100, 0) - px(80, 0), totalDelta, 0.01f);
    t.up(1, 40f);
    t.up(0, 50f);
    assertTrue(r.taps.isEmpty());
  }

  @Test
  public void twistSnapsToRotate() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.down(1, 180, 100, 0f);
    float r1 = px(80, 0) / 2f;
    float theta = (float) Math.toRadians(60f);
    t.move(0, 100 + (float) Math.cos(Math.PI + theta) * r1, 100 + (float) Math.sin(Math.PI + theta) * r1, 30f);
    t.move(1, 100 + (float) Math.cos(theta) * r1, 100 + (float) Math.sin(theta) * r1, 30f);
    assertEquals(1, r.rotations.size());
    assertEquals(1, (int) r.rotations.get(0));
  }

  @Test
  public void smallTwistDoesNotRotate() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.down(1, 180, 100, 0f);
    float r1 = px(80, 0) / 2f;
    float theta = (float) Math.toRadians(30f);
    t.move(0, 100 + (float) Math.cos(Math.PI + theta) * r1, 100 + (float) Math.sin(Math.PI + theta) * r1, 30f);
    t.move(1, 100 + (float) Math.cos(theta) * r1, 100 + (float) Math.sin(theta) * r1, 30f);
    assertTrue(r.rotations.isEmpty());
  }

  @Test
  public void secondPointerCancelsTap() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.down(1, 120, 110, 20f);
    t.up(1, 40f);
    t.up(0, 60f);
    assertTrue(r.taps.isEmpty());
  }

  @Test
  public void dragResumesAfterPinchRelease() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.down(1, 200, 100, 0f);
    t.up(1, 30f);
    t.move(0, 130, 100, 60f);
    assertEquals(1, r.drags.size());
    assertEquals("130.00,100.00|30.00,0.00", r.drags.get(0));
    t.up(0, 80f);
    assertTrue(r.taps.isEmpty());
  }

  @Test
  public void cancelClearsState() {
    Recorder r = new Recorder();
    TouchInput t = new TouchInput(r);
    t.down(0, 100, 100, 0f);
    t.cancel();
    assertFalse(t.isTouching());
    t.up(0, 50f);
    assertTrue(r.taps.isEmpty());
  }
}
