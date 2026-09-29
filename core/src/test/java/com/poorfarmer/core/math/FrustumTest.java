package com.poorfarmer.core.math;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.world.GameCamera;
import org.junit.Test;

public class FrustumTest {

  @Test
  public void identityPerspectiveContainsPointsInFront() {
    Frustum frustum = new Frustum();
    frustum.extract(new Mat4().perspective((float) Math.toRadians(60f), 1.777f, 0.1f, 100f));
    assertTrue(frustum.containsPoint(0f, 0f, -1f));
    assertTrue(frustum.containsPoint(0f, 0f, -50f));
    assertFalse(frustum.containsPoint(0f, 0f, 1f));
    assertFalse(frustum.containsPoint(0f, 0f, -200f));
    assertFalse(frustum.containsPoint(50f, 0f, -1f));
    assertTrue(frustum.containsPoint(0.05f, 0f, -0.11f));
  }

  @Test
  public void rotatedCameraSeesItsForwardDirection() {
    GameCamera camera = new GameCamera(100f);
    camera.setZoom(20f);
    camera.snap();
    camera.rotate(1);
    camera.snap();
    Mat4 view = camera.viewMatrix();
    Mat4 proj = camera.projectionMatrix(1.777f);
    Mat4 vp = new Mat4();
    Mat4.multiply(proj, view, vp);
    Frustum frustum = new Frustum();
    frustum.extract(vp);
    Vec2 fwd = camera.forward();
    assertTrue(frustum.containsPoint(fwd.x * 5f, 0f, fwd.y * 5f));
    assertTrue(frustum.containsPoint(fwd.x * 25f, 0f, fwd.y * 25f));
    assertFalse(frustum.containsPoint(fwd.x * -15f, 0f, fwd.y * -15f));
  }

  @Test
  public void sphereStraddlingNearPlaneIsInside() {
    Frustum frustum = new Frustum();
    frustum.extract(new Mat4().perspective((float) Math.toRadians(60f), 1.777f, 0.1f, 100f));
    assertTrue(frustum.containsSphere(0f, 0f, -0.05f, 0.5f));
    assertFalse(frustum.containsSphere(0f, 0f, 10f, 0.1f));
    assertTrue(frustum.containsSphere(0f, 0f, -50f, 0.5f));
  }

  @Test
  public void boxCullingMatchesPointCulling() {
    Frustum frustum = new Frustum();
    frustum.extract(new Mat4().perspective((float) Math.toRadians(60f), 1.777f, 0.1f, 100f));
    assertTrue(frustum.containsBox(new Vec3(-1f, -1f, -5f), new Vec3(1f, 1f, -3f)));
    assertFalse(frustum.containsBox(new Vec3(-1f, -1f, 1f), new Vec3(1f, 1f, 3f)));
    assertTrue(frustum.containsBox(new Vec3(-1f, -1f, -0.15f), new Vec3(1f, 1f, -0.05f)));
    assertFalse(frustum.containsBox(new Vec3(10f, 0f, -5f), new Vec3(12f, 2f, -3f)));
  }

  @Test
  public void visibleGroundPointsPassAndFarBehindFail() {
    GameCamera camera = new GameCamera(120f);
    camera.setZoom(20f);
    camera.snap();
    Mat4 proj = camera.projectionMatrix(1.777f);
    Mat4 vp = new Mat4();
    Mat4.multiply(proj, camera.viewMatrix(), vp);
    Frustum frustum = new Frustum();
    frustum.extract(vp);
    Vec2 fwd = camera.forward();
    for (float dist = 2f; dist <= 24f; dist += 4f) {
      assertTrue(frustum.containsPoint(fwd.x * dist, 0f, fwd.y * dist));
    }
    assertFalse(frustum.containsPoint(fwd.x * -15f, 0f, fwd.y * -15f));
    Vec2 right = camera.rightDir();
    assertFalse(frustum.containsPoint(right.x * 60f, 0f, right.y * 60f));
  }
}
