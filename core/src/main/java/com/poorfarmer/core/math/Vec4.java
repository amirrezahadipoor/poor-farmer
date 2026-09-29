package com.poorfarmer.core.math;

public final class Vec4 {

  public float x;
  public float y;
  public float z;
  public float w;

  public Vec4() {
  }

  public Vec4(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vec4 set(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
    return this;
  }

  public Vec4 set(Vec3 v, float w) {
    this.x = v.x;
    this.y = v.y;
    this.z = v.z;
    this.w = w;
    return this;
  }

  public Vec4 scale(float s) {
    x *= s;
    y *= s;
    z *= s;
    w *= s;
    return this;
  }
}
