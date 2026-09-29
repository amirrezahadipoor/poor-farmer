package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.math.Vec2;
import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.math.Vec4;
import org.junit.Test;

public class GameCameraTest {

  private static final float EYE_TOLERANCE = 0.01f;

  @Test
  public void eyeSitsAboveAndBehindCenterAtDefaultYaw() {
    GameCamera camera = new GameCamera(100f);
    camera.setZoom(20f);
    camera.snap();
    Vec3 eye = camera.eyePosition();
    float expectedVert = 20f * (float) Math.sin(Math.toRadians(GameCamera.PITCH_DEG));
    float expectedHoriz = 20f * (float) Math.cos(Math.toRadians(GameCamera.PITCH_DEG));
    assertEquals(0f, eye.x, EYE_TOLERANCE);
    assertEquals(expectedVert, eye.y, EYE_TOLERANCE);
    assertEquals(expectedHoriz, eye.z, EYE_TOLERANCE);
  }

  @Test
  public void viewMatrixMapsEyeToOriginAndCenterDownZ() {
    GameCamera camera = new GameCamera(100f);
    camera.setZoom(20f);
    camera.snap();
    Mat4 view = camera.viewMatrix();
    Vec3 eye = camera.eyePosition();
    Vec4 eyeView = new Vec4();
    view.transformPoint(eye, eyeView);
    assertEquals(0f, eyeView.x, EYE_TOLERANCE);
    assertEquals(0f, eyeView.y, EYE_TOLERANCE);
    assertEquals(0f, eyeView.z, EYE_TOLERANCE);
    Vec4 centerView = new Vec4();
    view.transformPoint(new Vec3(0f, 0f, 0f), centerView);
    assertEquals(0f, centerView.x, EYE_TOLERANCE);
    assertTrue(centerView.z < -1f);
  }

  @Test
  public void panClampsToWorldBounds() {
    GameCamera camera = new GameCamera(50f);
    for (int i = 0; i < 200; i++) {
      camera.panByScreen(50f, 50f, 800f);
    }
    assertTrue(camera.targetCenter().x >= -50.01f);
    assertTrue(camera.targetCenter().x <= 50.01f);
    assertTrue(camera.targetCenter().y >= -50.01f);
    assertTrue(camera.targetCenter().y <= 50.01f);
  }

  @Test
  public void panRightMovesCenterLeftAtDefaultYaw() {
    GameCamera camera = new GameCamera(100f);
    float before = camera.targetCenter().x;
    camera.panByScreen(100f, 0f, 800f);
    assertTrue(camera.targetCenter().x < before);
  }

  @Test
  public void panDownMovesCenterForward() {
    GameCamera camera = new GameCamera(100f);
    float beforeZ = camera.targetCenter().y;
    camera.panByScreen(0f, 100f, 800f);
    assertTrue(camera.targetCenter().y < beforeZ);
  }

  @Test
  public void rotateStepsBy90Degrees() {
    GameCamera camera = new GameCamera(100f);
    camera.rotate(1);
    camera.snap();
    assertEquals(90f, camera.yawDegrees(), 0.01f);
    Vec2 fwd = camera.forward();
    assertEquals(-1f, fwd.x, 0.01f);
    assertEquals(0f, fwd.y, 0.01f);
    camera.rotate(1);
    camera.snap();
    assertEquals(180f, camera.yawDegrees(), 0.01f);
    camera.rotate(2);
    camera.snap();
    assertEquals(0f, camera.yawDegrees(), 0.01f);
  }

  @Test
  public void zoomStaysWithinLimits() {
    GameCamera camera = new GameCamera(100f);
    camera.zoomBy(-1000f);
    assertEquals(GameCamera.MIN_ZOOM, camera.targetZoom(), 0.001f);
    camera.zoomBy(1000f);
    assertEquals(GameCamera.MAX_ZOOM, camera.targetZoom(), 0.001f);
  }

  @Test
  public void updateSmoothsTowardTargets() {
    GameCamera camera = new GameCamera(100f);
    camera.setCenter(30f, -20f);
    camera.setZoom(30f);
    for (int i = 0; i < 600; i++) {
      camera.update(1f / 60f);
    }
    assertEquals(30f, camera.center().x, 0.01f);
    assertEquals(-20f, camera.center().y, 0.01f);
    assertEquals(30f, camera.zoom(), 0.01f);
  }

  @Test
  public void screenToWorldRoundTrip() {
    GameCamera camera = new GameCamera(100f);
    camera.setZoom(20f);
    camera.snap();
    float w = 1600f;
    float h = 900f;
    Mat4 proj = camera.projectionMatrix(w / h);
    Vec3 world = new Vec3(3f, 0f, -4f);
    Vec4 screen = camera.screenFromWorld(proj, world, w, h);
    Vec3 back = camera.worldFromScreen(proj, screen.x, screen.y, w, h, 0f);
    assertEquals(3f, back.x, 0.05f);
    assertEquals(-4f, back.z, 0.05f);
  }

  @Test
  public void screenFromWorldPutsCenterNearScreenMiddle() {
    GameCamera camera = new GameCamera(100f);
    camera.setZoom(20f);
    camera.snap();
    float w = 1600f;
    float h = 900f;
    Mat4 proj = camera.projectionMatrix(w / h);
    Vec4 screen = camera.screenFromWorld(proj, new Vec3(0f, 0f, 0f), w, h);
    assertEquals(w / 2f, screen.x, 1f);
    assertTrue(Math.abs(screen.y - h / 2f) < h * 0.08f);
  }
}
