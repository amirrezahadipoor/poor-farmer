package com.poorfarmer.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class Mat4Test {

  private static final float TOL = 1e-4f;

  @Test
  public void identityLeavesPointsUnchanged() {
    Mat4 m = new Mat4();
    Vec4 out = new Vec4();
    m.transformPoint(new Vec3(3f, -2f, 7f), out);
    assertEquals(3f, out.x, TOL);
    assertEquals(-2f, out.y, TOL);
    assertEquals(7f, out.z, TOL);
    assertEquals(1f, out.w, TOL);
  }

  @Test
  public void translationMovesPoints() {
    Mat4 m = new Mat4().translation(1f, 2f, 3f);
    Vec4 out = new Vec4();
    m.transformPoint(new Vec3(10f, 10f, 10f), out);
    assertEquals(11f, out.x, TOL);
    assertEquals(12f, out.y, TOL);
    assertEquals(13f, out.z, TOL);
  }

  @Test
  public void scalingScalesPoints() {
    Mat4 m = new Mat4().scale(2f, 3f, 4f);
    Vec4 out = new Vec4();
    m.transformPoint(new Vec3(1f, 1f, 1f), out);
    assertEquals(2f, out.x, TOL);
    assertEquals(3f, out.y, TOL);
    assertEquals(4f, out.z, TOL);
  }

  @Test
  public void rotationYQuarterTurnSwapsAxes() {
    Mat4 m = new Mat4().rotationY((float) Math.PI / 2f);
    Vec3 out = new Vec3();
    m.transformDirection(new Vec3(1f, 0f, 0f), out);
    assertEquals(0f, out.x, TOL);
    assertEquals(0f, out.y, TOL);
    assertEquals(-1f, out.z, TOL);
  }

  @Test
  public void multiplyByIdentityIsIdentity() {
    Mat4 m = new Mat4().translation(1f, 2f, 3f).rotationY(0.5f);
    Mat4 result = new Mat4();
    Mat4.multiply(m, new Mat4(), result);
    for (int i = 0; i < 16; i++) {
      assertEquals(m.data[i], result.data[i], TOL);
    }
  }

  @Test
  public void inverseRoundTrips() {
    Mat4 m = new Mat4().translation(4f, -2f, 6f);
    m.multiply(new Mat4().rotationY(0.7f));
    m.multiply(new Mat4().scale(1.5f, 2f, 0.5f));
    Mat4 inverse = new Mat4();
    m.invert(inverse);
    Mat4 product = new Mat4();
    Mat4.multiply(m, inverse, product);
    for (int i = 0; i < 16; i++) {
      float expected = (i % 5) == 0 ? 1f : 0f;
      assertEquals(expected, product.data[i], 1e-3f);
    }
  }

  @Test
  public void perspectiveMapsNearFarPlane() {
    Mat4 p = new Mat4().perspective((float) Math.toRadians(60f), 16f / 9f, 0.1f, 100f);
    Vec4 near = new Vec4();
    p.transformPoint(new Vec3(0f, 0f, -0.1f), near);
    assertEquals(near.z / near.w, -1f, 1e-3f);
    Vec4 far = new Vec4();
    p.transformPoint(new Vec3(0f, 0f, -100f), far);
    assertEquals(far.z / far.w, 1f, 1e-3f);
  }

  @Test
  public void lookAtPutsEyeAtOriginAndTargetOnNegativeZ() {
    Vec3 eye = new Vec3(5f, 3f, 5f);
    Vec3 center = new Vec3(0f, 0f, 0f);
    Mat4 view = new Mat4().lookAt(eye, center, Vec3.UP);
    Vec4 out = new Vec4();
    view.transformPoint(eye, out);
    assertEquals(0f, out.x, 1e-4f);
    assertEquals(0f, out.y, 1e-4f);
    assertEquals(0f, out.z, 1e-4f);
    view.transformPoint(center, out);
    float expectedDistance = new Vec3(eye).sub(center).length();
    assertEquals(0f, out.x, 1e-4f);
    assertEquals(0f, out.y, 1e-4f);
    assertEquals(-expectedDistance, out.z, 1e-4f);
  }

  @Test
  public void transposeOfRotationIsItsInverse() {
    Mat4 r = new Mat4().rotationX(0.3f).rotationY(0.7f);
    Mat4 rt = new Mat4();
    r.transposed(rt);
    Mat4 product = new Mat4();
    Mat4.multiply(r, rt, product);
    for (int i = 0; i < 16; i++) {
      float expected = (i % 5) == 0 ? 1f : 0f;
      assertEquals(expected, product.data[i], 1e-4f);
    }
  }

  @Test
  public void transposeSwapsRowsAndColumns() {
    Mat4 m = new Mat4().translation(1f, 2f, 3f);
    Mat4 rt = new Mat4();
    m.transposed(rt);
    assertEquals(1f, rt.data[3], TOL);
    assertEquals(2f, rt.data[7], TOL);
    assertEquals(3f, rt.data[11], TOL);
    assertEquals(0f, rt.data[12], TOL);
  }
}
