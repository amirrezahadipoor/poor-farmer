package com.poorfarmer.core.entity;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;

public final class Bone {

  public final String name;
  public final int parent;
  public Vec3 localPosition = new Vec3();
  public Quat localRotation = new Quat().identity();
  public Vec3 localScale = new Vec3(1f, 1f, 1f);
  public final Mat4 worldMatrix = new Mat4();

  public Bone(String name, int parent) {
    this.name = name;
    this.parent = parent;
  }
}
