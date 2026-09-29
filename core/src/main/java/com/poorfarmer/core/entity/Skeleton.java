package com.poorfarmer.core.entity;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Skeleton {

  private final List<Bone> bones = new ArrayList<>();
  private final HashMap<String, Integer> nameIndex = new HashMap<>();
  private final Mat4 localScratch = new Mat4();
  private final Mat4 rotationScratch = new Mat4();
  private final Mat4 scaleScratch = new Mat4();

  public Bone addBone(String name, int parent, Vec3 localPosition, Quat localRotation) {
    if (nameIndex.containsKey(name)) {
      throw new IllegalArgumentException("duplicate bone name: " + name);
    }
    if (parent != -1 && parent >= bones.size()) {
      throw new IllegalArgumentException("parent must precede child: " + name);
    }
    Bone bone = new Bone(name, parent);
    bone.localPosition.set(localPosition);
    bone.localRotation.set(localRotation);
    bones.add(bone);
    nameIndex.put(name, bones.size() - 1);
    return bone;
  }

  public int boneCount() {
    return bones.size();
  }

  public Bone bone(int index) {
    return bones.get(index);
  }

  public int boneIndex(String name) {
    Integer index = nameIndex.get(name);
    if (index == null) {
      throw new IllegalArgumentException("unknown bone: " + name);
    }
    return index;
  }

  public void applyPose(Pose pose) {
    for (int i = 0; i < bones.size(); i++) {
      Bone bone = bones.get(i);
      bone.localPosition.set(pose.positions[i]);
      bone.localRotation.set(pose.rotations[i]);
    }
    updateWorldMatrices();
  }

  public void updateWorldMatrices() {
    for (int i = 0; i < bones.size(); i++) {
      Bone bone = bones.get(i);
      bone.localRotation.toMat4(rotationScratch);
      scaleScratch.identity();
      scaleScratch.data[0] = bone.localScale.x;
      scaleScratch.data[5] = bone.localScale.y;
      scaleScratch.data[10] = bone.localScale.z;
      localScratch.copyOf(rotationScratch);
      localScratch.multiply(scaleScratch);
      localScratch.data[12] = bone.localPosition.x;
      localScratch.data[13] = bone.localPosition.y;
      localScratch.data[14] = bone.localPosition.z;
      if (bone.parent < 0) {
        bone.worldMatrix.copyOf(localScratch);
      } else {
        bone.worldMatrix.copyOf(bones.get(bone.parent).worldMatrix);
        bone.worldMatrix.multiply(localScratch);
      }
    }
  }

  public Pose buildRestPose() {
    Pose pose = new Pose(bones.size());
    for (int i = 0; i < bones.size(); i++) {
      pose.positions[i].set(bones.get(i).localPosition);
      pose.rotations[i].set(bones.get(i).localRotation);
    }
    return pose;
  }
}
