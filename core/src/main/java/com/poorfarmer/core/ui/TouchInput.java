package com.poorfarmer.core.ui;

public final class TouchInput {

  public interface Listener {
    void onTap(float x, float y);

    void onLongPress(float x, float y);

    void onDrag(float x, float y, float dx, float dy);

    void onPinch(float distanceDelta, float centerX, float centerY);

    void onRotate(int steps);
  }

  public static final float TAP_MAX_DISTANCE = 24f;
  public static final float TAP_MAX_TIME_MS = 250f;
  public static final float DRAG_THRESHOLD = 12f;
  public static final float LONG_PRESS_TIME_MS = 500f;
  public static final float ROTATE_SNAP_DEG = 45f;

  private final Listener listener;
  private final float[] pointerX = new float[2];
  private final float[] pointerY = new float[2];
  private final int[] pointerId = new int[2];
  private int pointerCount;
  private float startX;
  private float startY;
  private float startMs;
  private boolean dragActive;
  private boolean longPressFired;
  private boolean pinchActive;
  private float lastPinchDistance;
  private float lastPinchAngleDegrees;
  private float lastDragX;
  private float lastDragY;
  private float twistAccumulator;

  public TouchInput(Listener listener) {
    this.listener = listener;
  }

  public void down(int id, float x, float y, float timeMs) {
    if (pointerCount >= 2) {
      return;
    }
    int slot = pointerCount;
    pointerId[slot] = id;
    pointerX[slot] = x;
    pointerY[slot] = y;
    pointerCount++;
    if (pointerCount == 1) {
      startX = x;
      startY = y;
      lastDragX = x;
      lastDragY = y;
      startMs = timeMs;
      dragActive = false;
      longPressFired = false;
    } else {
      dragActive = true;
      pinchActive = true;
      lastPinchDistance = distance();
      lastPinchAngleDegrees = angleDegrees();
      twistAccumulator = 0f;
    }
  }

  public void move(int id, float x, float y, float timeMs) {
    int slot = slotOf(id);
    if (slot < 0) {
      return;
    }
    pointerX[slot] = x;
    pointerY[slot] = y;
    if (pointerCount == 1) {
      float moved = (float) Math.hypot(x - startX, y - startY);
      if (!dragActive && moved > DRAG_THRESHOLD) {
        dragActive = true;
      }
      if (dragActive) {
        listener.onDrag(x, y, x - lastDragX, y - lastDragY);
      } else if (!longPressFired && timeMs - startMs >= LONG_PRESS_TIME_MS) {
        longPressFired = true;
        listener.onLongPress(startX, startY);
      }
      lastDragX = x;
      lastDragY = y;
    } else if (pinchActive) {
      float distance = distance();
      float angle = angle();
      listener.onPinch(distance - lastPinchDistance, centerOfMassX(), centerOfMassY());
      twistAccumulator += normalizeDegrees(angleDegrees() - lastPinchAngleDegrees);
      lastPinchDistance = distance;
      lastPinchAngleDegrees = angleDegrees();
      while (twistAccumulator >= ROTATE_SNAP_DEG) {
        listener.onRotate(1);
        twistAccumulator -= 90f;
      }
      while (twistAccumulator <= -ROTATE_SNAP_DEG) {
        listener.onRotate(-1);
        twistAccumulator += 90f;
      }
    }
  }

  public void up(int id, float timeMs) {
    int slot = slotOf(id);
    if (slot < 0) {
      return;
    }
    float x = pointerX[slot];
    float y = pointerY[slot];
    if (pointerCount == 1) {
      float moved = (float) Math.hypot(x - startX, y - startY);
      if (!dragActive && !longPressFired
          && moved <= TAP_MAX_DISTANCE
          && timeMs - startMs <= TAP_MAX_TIME_MS) {
        listener.onTap(x, y);
      }
      reset();
    } else {
      int remaining = 1 - slot;
      pointerId[slot] = pointerId[remaining];
      pointerX[slot] = pointerX[remaining];
      pointerY[slot] = pointerY[remaining];
      pointerCount--;
      if (pointerCount == 1) {
        dragActive = true;
        pinchActive = false;
        startX = pointerX[0];
        startY = pointerY[0];
        lastDragX = pointerX[0];
        lastDragY = pointerY[0];
      }
    }
  }

  public void cancel() {
    reset();
  }

  public boolean isTouching() {
    return pointerCount > 0;
  }

  private void reset() {
    pointerCount = 0;
    dragActive = false;
    longPressFired = false;
    pinchActive = false;
    twistAccumulator = 0f;
  }

  private int slotOf(int id) {
    for (int i = 0; i < pointerCount; i++) {
      if (pointerId[i] == id) {
        return i;
      }
    }
    return -1;
  }

  private float distance() {
    return (float) Math.hypot(pointerX[1] - pointerX[0], pointerY[1] - pointerY[0]);
  }

  private float angle() {
    return (float) Math.atan2(pointerY[1] - pointerY[0], pointerX[1] - pointerX[0]);
  }

  private float angleDegrees() {
    return (float) Math.toDegrees(angle());
  }

  private float centerOfMassX() {
    return (pointerX[0] + pointerX[1]) / 2f;
  }

  private float centerOfMassY() {
    return (pointerY[0] + pointerY[1]) / 2f;
  }

  private static float normalizeDegrees(float a) {
    while (a >= 180f) {
      a -= 360f;
    }
    while (a < -180f) {
      a += 360f;
    }
    return a;
  }
}
