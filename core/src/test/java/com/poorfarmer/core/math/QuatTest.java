package com.poorfarmer.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class QuatTest {

  private static final float TOL = 1e-4f;

  @Test
  public void identityQuatRotatesNothing() {
    Quat q = new Quat().identity();
    Vec3 out = new Vec3();
    Quat.rotateVec3(q, new Vec3(1f, 2f, 3f), out);
    assertEquals(1f, out.x, TOL);
    assertEquals(2f, out.y, TOL);
    assertEquals(3f, out.z, TOL);
  }

  @Test
  public void axisAngleQuarterTurnAroundY() {
    Quat q = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, q);
    Vec3 out = new Vec3();
    Quat.rotateVec3(q, new Vec3(1f, 0f, 0f), out);
    assertEquals(0f, out.x, TOL);
    assertEquals(0f, out.y, TOL);
    assertEquals(-1f, out.z, TOL);
  }

  @Test
  public void slerpEndpointsMatchInputs() {
    Quat a = new Quat(0f, 0f, 0f, 1f);
    Quat b = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, b);
    Quat out = new Quat();
    Quat.slerp(a, b, 0f, out);
    assertEquals(a.x, out.x, TOL);
    assertEquals(a.w, out.w, TOL);
    Quat.slerp(a, b, 1f, out);
    assertEquals(b.x, out.x, TOL);
    assertEquals(b.w, out.w, TOL);
  }

  @Test
  public void slerpHalfwayGivesHalfAngle() {
    Quat a = new Quat(0f, 0f, 0f, 1f);
    Quat b = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, b);
    Quat out = new Quat();
    Quat.slerp(a, b, 0.5f, out);
    Vec3 rotated = new Vec3();
    Quat.rotateVec3(out, new Vec3(1f, 0f, 0f), rotated);
    float cosHalf = (float) Math.cos(Math.PI / 4f);
    assertEquals(cosHalf, rotated.x, TOL);
    assertEquals(0f, rotated.y, TOL);
    assertEquals(-cosHalf, rotated.z, TOL);
  }

  @Test
  public void toMat4MatchesAxisAngleRotation() {
    Quat q = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, q);
    Mat4 m = new Mat4();
    q.toMat4(m);
    Vec3 out = new Vec3();
    m.transformDirection(new Vec3(1f, 0f, 0f), out);
    assertEquals(0f, out.x, TOL);
    assertEquals(0f, out.y, TOL);
    assertEquals(-1f, out.z, TOL);
  }

  @Test
  public void normalizeRestoresUnitLength() {
    Quat q = new Quat(1f, 2f, 3f, 4f).normalize();
    float length = (float) Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
    assertEquals(1f, length, 1e-5f);
  }

  @Test
  public void conjugateRotatesBackward() {
    Quat q = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 4f, q);
    Vec3 out = new Vec3();
    Quat.rotateVec3(q, new Vec3(1f, 0f, 0f), out);
    Vec3 back = new Vec3();
    Quat.rotateVec3(new Quat(q).conjugate(), out, back);
    assertEquals(1f, back.x, TOL);
    assertEquals(0f, back.y, TOL);
    assertEquals(0f, back.z, TOL);
  }
}
