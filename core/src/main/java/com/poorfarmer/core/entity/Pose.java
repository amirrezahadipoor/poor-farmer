package com.poorfarmer.core.entity;

import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;

public final class Pose {

  public final Vec3[] positions;
  public final Quat[] rotations;

  public Pose(int boneCount) {
    positions = new Vec3[boneCount];
    rotations = new Quat[boneCount];
    for (int i = 0; i < boneCount; i++) {
      positions[i] = new Vec3();
      rotations[i] = new Quat().identity();
    }
  }

  public void copy(Pose other) {
    for (int i = 0; i < positions.length; i++) {
      positions[i].set(other.positions[i]);
      rotations[i].set(other.rotations[i]);
    }
  }

  public void blendFrom(Pose a, Pose b, float t) {
    for (int i = 0; i < positions.length; i++) {
      Vec3.lerp(a.positions[i], b.positions[i], t, positions[i]);
      Quat.slerp(a.rotations[i], b.rotations[i], t, rotations[i]);
    }
  }

  public void copyRotationsOnly(Pose other) {
    for (int i = 0; i < rotations.length; i++) {
      rotations[i].set(other.rotations[i]);
    }
  }

  public void accumulateRotation(Quat[] animatedRotations, float weight) {
    for (int i = 0; i < rotations.length; i++) {
      Quat.slerp(rotations[i], animatedRotations[i], weight, rotations[i]);
    }
  }
}
