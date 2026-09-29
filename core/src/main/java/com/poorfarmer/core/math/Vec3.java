package com.poorfarmer.core.math;

public final class Vec3 {

  public static final Vec3 UP = new Vec3(0f, 1f, 0f);
  public static final Vec3 RIGHT = new Vec3(1f, 0f, 0f);
  public static final Vec3 FORWARD = new Vec3(0f, 0f, 1f);

  public float x;
  public float y;
  public float z;

  public Vec3() {
  }

  public Vec3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3(Vec3 other) {
    x = other.x;
    y = other.y;
    z = other.z;
  }

  public Vec3 set(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    return this;
  }

  public Vec3 set(Vec3 other) {
    this.x = other.x;
    this.y = other.y;
    this.z = other.z;
    return this;
  }

  public Vec3 add(Vec3 other) {
    x += other.x;
    y += other.y;
    z += other.z;
    return this;
  }

  public Vec3 sub(Vec3 other) {
    x -= other.x;
    y -= other.y;
    z -= other.z;
    return this;
  }

  public Vec3 scale(float s) {
    x *= s;
    y *= s;
    z *= s;
    return this;
  }

  public float dot(Vec3 other) {
    return x * other.x + y * other.y + z * other.z;
  }

  public float length() {
    return (float) Math.sqrt(x * x + y * y + z * z);
  }

  public Vec3 normalize() {
    float length = length();
    if (length > 1e-8f) {
      float inv = 1f / length;
      x *= inv;
      y *= inv;
      z *= inv;
    }
    return this;
  }

  public static void add(Vec3 a, Vec3 b, Vec3 out) {
    out.x = a.x + b.x;
    out.y = a.y + b.y;
    out.z = a.z + b.z;
  }

  public static void sub(Vec3 a, Vec3 b, Vec3 out) {
    out.x = a.x - b.x;
    out.y = a.y - b.y;
    out.z = a.z - b.z;
  }

  public static void cross(Vec3 a, Vec3 b, Vec3 out) {
    float ax = a.x, ay = a.y, az = a.z;
    out.x = ay * b.z - az * b.y;
    out.y = az * b.x - ax * b.z;
    out.z = ax * b.y - ay * b.x;
  }

  public static void lerp(Vec3 a, Vec3 b, float t, Vec3 out) {
    out.x = a.x + (b.x - a.x) * t;
    out.y = a.y + (b.y - a.y) * t;
    out.z = a.z + (b.z - a.z) * t;
  }

  public static float distance(Vec3 a, Vec3 b) {
    float dx = b.x - a.x;
    float dy = b.y - a.y;
    float dz = b.z - a.z;
    return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
  }
}
