package com.poorfarmer.core.math;

public final class Frustum {

  private final float[] planes = new float[24];

  public void extract(Mat4 viewProjection) {
    float[] m = viewProjection.data;
    float[] r0 = {m[0], m[4], m[8], m[12]};
    float[] r1 = {m[1], m[5], m[9], m[13]};
    float[] r2 = {m[2], m[6], m[10], m[14]};
    float[] r3 = {m[3], m[7], m[11], m[15]};
    addPlane(r3, r0, 1f, 0);
    addPlane(r3, r0, -1f, 1);
    addPlane(r3, r1, 1f, 2);
    addPlane(r3, r1, -1f, 3);
    addPlane(r3, r2, 1f, 4);
    addPlane(r3, r2, -1f, 5);
  }

  private void addPlane(float[] a, float[] b, float sign, int plane) {
    planes[plane * 4] = a[0] + sign * b[0];
    planes[plane * 4 + 1] = a[1] + sign * b[1];
    planes[plane * 4 + 2] = a[2] + sign * b[2];
    planes[plane * 4 + 3] = a[3] + sign * b[3];
    normalize(plane);
  }

  private void normalize(int plane) {
    float nx = planes[plane * 4];
    float ny = planes[plane * 4 + 1];
    float nz = planes[plane * 4 + 2];
    float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
    if (length > 0f) {
      planes[plane * 4] /= length;
      planes[plane * 4 + 1] /= length;
      planes[plane * 4 + 2] /= length;
      planes[plane * 4 + 3] /= length;
    }
  }

  public boolean containsPoint(float x, float y, float z) {
    for (int p = 0; p < 6; p++) {
      float value = planes[p * 4] * x + planes[p * 4 + 1] * y + planes[p * 4 + 2] * z + planes[p * 4 + 3];
      if (value < 0f) {
        return false;
      }
    }
    return true;
  }

  public boolean containsSphere(float cx, float cy, float cz, float radius) {
    for (int p = 0; p < 6; p++) {
      float value = planes[p * 4] * cx + planes[p * 4 + 1] * cy + planes[p * 4 + 2] * cz + planes[p * 4 + 3];
      if (value < -radius) {
        return false;
      }
    }
    return true;
  }

  public boolean containsBox(Vec3 min, Vec3 max) {
    for (int p = 0; p < 6; p++) {
      float nx = planes[p * 4];
      float ny = planes[p * 4 + 1];
      float nz = planes[p * 4 + 2];
      float px = nx > 0f ? max.x : min.x;
      float py = ny > 0f ? max.y : min.y;
      float pz = nz > 0f ? max.z : min.z;
      float value = nx * px + ny * py + nz * pz + planes[p * 4 + 3];
      if (value < 0f) {
        return false;
      }
    }
    return true;
  }
}
