package com.poorfarmer.app;

import android.view.MotionEvent;

import com.poorfarmer.core.world.GameCamera;

public final class CameraTouchController {

  private static final float PINCH_SENSITIVITY = 0.05f;
  private static final float TWIST_SNAP_RADIANS = (float) Math.toRadians(45f);

  private final GameCamera camera;
  private float screenH;
  private boolean singleTouch;
  private boolean pinching;
  private float lastX;
  private float lastY;
  private float lastPinchDistance;
  private float lastPinchAngle;
  private float twistAccumulator;

  public CameraTouchController(GameCamera camera) {
    this.camera = camera;
  }

  public void setScreenSize(int width, int height) {
    this.screenH = height;
  }

  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        singleTouch = true;
        pinching = false;
        lastX = event.getX(0);
        lastY = event.getY(0);
        return true;
      case MotionEvent.ACTION_POINTER_DOWN:
        singleTouch = false;
        pinching = true;
        lastPinchDistance = pinchDistance(event);
        lastPinchAngle = pinchAngle(event);
        twistAccumulator = 0f;
        return true;
      case MotionEvent.ACTION_MOVE:
        if (pinching && event.getPointerCount() >= 2) {
          float distance = pinchDistance(event);
          float angle = pinchAngle(event);
          camera.zoomBy((lastPinchDistance - distance) * PINCH_SENSITIVITY);
          float twist = normalizeAngle(angle - lastPinchAngle);
          twistAccumulator += twist;
          if (twistAccumulator > TWIST_SNAP_RADIANS) {
            camera.rotate(1);
            twistAccumulator = 0f;
          } else if (twistAccumulator < -TWIST_SNAP_RADIANS) {
            camera.rotate(-1);
            twistAccumulator = 0f;
          }
          lastPinchDistance = distance;
          lastPinchAngle = angle;
        } else if (singleTouch) {
          float x = event.getX(0);
          float y = event.getY(0);
          camera.panByScreen(x - lastX, y - lastY, screenH);
          lastX = x;
          lastY = y;
        }
        return true;
      case MotionEvent.ACTION_POINTER_UP:
        pinching = false;
        int remaining = event.getActionIndex() == 0 ? 1 : 0;
        lastX = event.getX(remaining);
        lastY = event.getY(remaining);
        singleTouch = event.getPointerCount() - 1 == 1;
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        singleTouch = false;
        pinching = false;
        return true;
      default:
        return false;
    }
  }

  private static float pinchDistance(MotionEvent event) {
    float dx = event.getX(1) - event.getX(0);
    float dy = event.getY(1) - event.getY(0);
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  private static float pinchAngle(MotionEvent event) {
    return (float) Math.atan2(event.getY(1) - event.getY(0), event.getX(1) - event.getX(0));
  }

  private static float normalizeAngle(float angle) {
    while (angle > (float) Math.PI) {
      angle -= (float) (2 * Math.PI);
    }
    while (angle < -(float) Math.PI) {
      angle += (float) (2 * Math.PI);
    }
    return angle;
  }
}
