package com.poorfarmer.core.math;

public final class Vec2 {

  public float x;
  public float y;

  public Vec2() {
  }

  public Vec2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vec2 set(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Vec2 set(Vec2 other) {
    this.x = other.x;
    this.y = other.y;
    return this;
  }

  public Vec2 add(Vec2 other) {
    x += other.x;
    y += other.y;
    return this;
  }

  public Vec2 sub(Vec2 other) {
    x -= other.x;
    y -= other.y;
    return this;
  }

  public Vec2 scale(float s) {
    x *= s;
    y *= s;
    return this;
  }

  public float dot(Vec2 other) {
    return x * other.x + y * other.y;
  }

  public float length() {
    return (float) Math.sqrt(x * x + y * y);
  }

  public static void lerp(Vec2 a, Vec2 b, float t, Vec2 out) {
    out.x = a.x + (b.x - a.x) * t;
    out.y = a.y + (b.y - a.y) * t;
  }

  public static float distance(Vec2 a, Vec2 b) {
    float dx = b.x - a.x;
    float dy = b.y - a.y;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }
}
