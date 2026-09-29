package com.poorfarmer.core.math;

public final class Quat {

  public float x;
  public float y;
  public float z;
  public float w = 1f;

  public Quat() {
  }

  public Quat(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Quat(Quat other) {
    x = other.x;
    y = other.y;
    z = other.z;
    w = other.w;
  }

  public Quat identity() {
    x = 0f;
    y = 0f;
    z = 0f;
    w = 1f;
    return this;
  }

  public Quat set(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
    return this;
  }

  public Quat set(Quat other) {
    this.x = other.x;
    this.y = other.y;
    this.z = other.z;
    this.w = other.w;
    return this;
  }

  public Quat normalize() {
    float length = (float) Math.sqrt(x * x + y * y + z * z + w * w);
    if (length > 1e-8f) {
      float inv = 1f / length;
      x *= inv;
      y *= inv;
      z *= inv;
      w *= inv;
    }
    return this;
  }

  public Quat conjugate() {
    x = -x;
    y = -y;
    z = -z;
    return this;
  }

  public static void multiply(Quat a, Quat b, Quat out) {
    float ax = a.x, ay = a.y, az = a.z, aw = a.w;
    float bx = b.x, by = b.y, bz = b.z, bw = b.w;
    out.x = aw * bx + ax * bw + ay * bz - az * by;
    out.y = aw * by - ax * bz + ay * bw + az * bx;
    out.z = aw * bz + ax * by - ay * bx + az * bw;
    out.w = aw * bw - ax * bx - ay * by - az * bz;
  }

  public Quat multiply(Quat other) {
    Quat.multiply(this, other, this);
    return this;
  }

  public static void fromAxisAngle(Vec3 axis, float angleRadians, Quat out) {
    Vec3 normalized = new Vec3(axis).normalize();
    float half = angleRadians / 2f;
    float s = (float) Math.sin(half);
    out.x = normalized.x * s;
    out.y = normalized.y * s;
    out.z = normalized.z * s;
    out.w = (float) Math.cos(half);
  }

  public static void slerp(Quat a, Quat b, float t, Quat out) {
    float cosTheta = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    Quat target = b;
    if (cosTheta < 0f) {
      cosTheta = -cosTheta;
      target = new Quat(-b.x, -b.y, -b.z, -b.w);
    }
    if (cosTheta > 0.9995f) {
      out.x = a.x + t * (target.x - a.x);
      out.y = a.y + t * (target.y - a.y);
      out.z = a.z + t * (target.z - a.z);
      out.w = a.w + t * (target.w - a.w);
      out.normalize();
      return;
    }
    float theta = (float) Math.acos(Math.min(1f, cosTheta));
    float sinTheta = (float) Math.sin(theta);
    float wa = (float) (Math.sin((1f - t) * theta) / sinTheta);
    float wb = (float) (Math.sin(t * theta) / sinTheta);
    out.x = wa * a.x + wb * target.x;
    out.y = wa * a.y + wb * target.y;
    out.z = wa * a.z + wb * target.z;
    out.w = wa * a.w + wb * target.w;
  }

  public static void rotateVec3(Quat q, Vec3 v, Vec3 out) {
    Quat conjugate = new Quat(q).conjugate();
    Quat vQuat = new Quat(v.x, v.y, v.z, 0f);
    Quat product = new Quat();
    multiply(q, vQuat, product);
    multiply(product, conjugate, product);
    out.x = product.x;
    out.y = product.y;
    out.z = product.z;
  }

  public void toMat4(Mat4 out) {
    float xx = x * x, yy = y * y, zz = z * z;
    float xy = x * y, xz = x * z, yz = y * z;
    float wx = w * x, wy = w * y, wz = w * z;
    out.data[0] = 1f - 2f * (yy + zz);
    out.data[1] = 2f * (xy + wz);
    out.data[2] = 2f * (xz - wy);
    out.data[3] = 0f;
    out.data[4] = 2f * (xy - wz);
    out.data[5] = 1f - 2f * (xx + zz);
    out.data[6] = 2f * (yz + wx);
    out.data[7] = 0f;
    out.data[8] = 2f * (xz + wy);
    out.data[9] = 2f * (yz - wx);
    out.data[10] = 1f - 2f * (xx + yy);
    out.data[11] = 0f;
    out.data[12] = 0f;
    out.data[13] = 0f;
    out.data[14] = 0f;
    out.data[15] = 1f;
  }
}
