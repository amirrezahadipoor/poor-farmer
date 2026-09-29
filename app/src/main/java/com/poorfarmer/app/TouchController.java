package com.poorfarmer.app;

import android.view.MotionEvent;

import com.poorfarmer.core.EventBus;
import com.poorfarmer.core.GameEvents;
import com.poorfarmer.core.ui.HitTarget;
import com.poorfarmer.core.ui.HitTester;
import com.poorfarmer.core.ui.TouchInput;
import com.poorfarmer.core.world.GameCamera;

public final class TouchController implements TouchInput.Listener {

  private static final float PINCH_SENSITIVITY = 0.05f;

  private final TouchInput touch;
  private final GameCamera camera;
  private final EventBus bus;
  private final HitTester hitTester = new HitTester();
  private int screenWidth;
  private int screenHeight;

  public TouchController(GameCamera camera, EventBus bus) {
    this.camera = camera;
    this.bus = bus;
    this.touch = new TouchInput(this);
  }

  public void setScreenSize(int width, int height) {
    this.screenWidth = width;
    this.screenHeight = height;
  }

  public void addHitTarget(HitTarget target) {
    hitTester.add(target);
  }

  public void removeHitTarget(HitTarget target) {
    hitTester.remove(target);
  }

  public HitTarget hitTargetAt(float x, float z) {
    return hitTester.nearest(x, z);
  }

  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        touch.down(event.getPointerId(0), event.getX(0), event.getY(0), event.getEventTime());
        return true;
      case MotionEvent.ACTION_POINTER_DOWN:
        int index = event.getActionIndex();
        touch.down(event.getPointerId(index), event.getX(index), event.getY(index), event.getEventTime());
        return true;
      case MotionEvent.ACTION_MOVE:
        for (int i = 0; i < event.getPointerCount(); i++) {
          touch.move(event.getPointerId(i), event.getX(i), event.getY(i), event.getEventTime());
        }
        return true;
      case MotionEvent.ACTION_POINTER_UP:
        touch.up(event.getPointerId(event.getActionIndex()), event.getEventTime());
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
          touch.up(event.getPointerId(event.getPointerCount() - 1), event.getEventTime());
        } else {
          touch.cancel();
        }
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onTap(float x, float y) {
    float[] world = worldFromScreen(x, y);
    if (world == null) {
      return;
    }
    bus.publish(new GameEvents.Tapped(world[0], world[1]));
  }

  @Override
  public void onLongPress(float x, float y) {
    float[] world = worldFromScreen(x, y);
    if (world == null) {
      return;
    }
    bus.publish(new GameEvents.LongPressed(world[0], world[1]));
  }

  @Override
  public void onDrag(float x, float y, float dx, float dy) {
    camera.panByScreen(dx, dy, screenHeight);
  }

  @Override
  public void onPinch(float distanceDelta, float centerX, float centerY) {
    camera.zoomBy(-distanceDelta * PINCH_SENSITIVITY);
  }

  @Override
  public void onRotate(int steps) {
    camera.rotate(steps);
  }

  private float[] worldFromScreen(float sx, float sy) {
    if (screenWidth <= 0 || screenHeight <= 0) {
      return null;
    }
    float aspect = (float) screenWidth / (float) screenHeight;
    com.poorfarmer.core.math.Vec3 world = camera.worldFromScreen(
        camera.projectionMatrix(aspect), sx, sy, screenWidth, screenHeight, 0f);
    return new float[]{world.x, world.z};
  }
}
