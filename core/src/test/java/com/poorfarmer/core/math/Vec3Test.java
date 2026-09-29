package com.poorfarmer.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class Vec3Test {

  @Test
  public void crossProductOfOrthogonalAxes() {
    Vec3 out = new Vec3();
    Vec3.cross(Vec3.RIGHT, Vec3.UP, out);
    assertEquals(0f, out.x, 1e-6f);
    assertEquals(0f, out.y, 1e-6f);
    assertEquals(1f, out.z, 1e-6f);
  }

  @Test
  public void crossProductIsAnticommutative() {
    Vec3 a = new Vec3(1f, 2f, 3f);
    Vec3 b = new Vec3(4f, -5f, 6f);
    Vec3 ab = new Vec3();
    Vec3 ba = new Vec3();
    Vec3.cross(a, b, ab);
    Vec3.cross(b, a, ba);
    assertEquals(ab.x, -ba.x, 1e-6f);
    assertEquals(ab.y, -ba.y, 1e-6f);
    assertEquals(ab.z, -ba.z, 1e-6f);
  }

  @Test
  public void dotProductOfPerpendicularVectorsIsZero() {
    assertEquals(0f, Vec3.RIGHT.dot(Vec3.UP), 1e-6f);
  }

  @Test
  public void normalizeGivesUnitLength() {
    Vec3 v = new Vec3(3f, 4f, 0f).normalize();
    assertEquals(1f, v.length(), 1e-5f);
  }

  @Test
  public void normalizeOfZeroVectorIsSafe() {
    Vec3 v = new Vec3(0f, 0f, 0f).normalize();
    assertEquals(0f, v.x, 0f);
    assertEquals(0f, v.length(), 0f);
  }

  @Test
  public void lerpEndpoints() {
    Vec3 a = new Vec3(0f, 0f, 0f);
    Vec3 b = new Vec3(10f, -2f, 4f);
    Vec3 out = new Vec3();
    Vec3.lerp(a, b, 0f, out);
    assertEquals(0f, out.x, 1e-6f);
    Vec3.lerp(a, b, 1f, out);
    assertEquals(10f, out.x, 1e-6f);
    assertEquals(-2f, out.y, 1e-6f);
    Vec3.lerp(a, b, 0.5f, out);
    assertEquals(5f, out.x, 1e-6f);
    assertEquals(-1f, out.y, 1e-6f);
  }

  @Test
  public void distanceIsSymmetric() {
    Vec3 a = new Vec3(1f, 2f, 3f);
    Vec3 b = new Vec3(4f, 6f, 3f);
    assertEquals(Vec3.distance(a, b), Vec3.distance(b, a), 1e-6f);
    assertEquals(5f, Vec3.distance(a, b), 1e-5f);
  }
}
