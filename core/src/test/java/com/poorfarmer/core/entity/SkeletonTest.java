package com.poorfarmer.core.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public class SkeletonTest {

  private static Skeleton twoBone() {
    Skeleton skeleton = new Skeleton();
    skeleton.addBone("root", -1, new Vec3(0f, 0f, 0f), new Quat().identity());
    skeleton.addBone("arm", 0, new Vec3(0.5f, 0f, 0f), new Quat().identity());
    return skeleton;
  }

  @Test
  public void worldMatrixIsParentTimesLocal() {
    Skeleton skeleton = twoBone();
    Quat yaw = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, yaw);
    skeleton.bone(0).localRotation.set(yaw);
    skeleton.updateWorldMatrices();
    assertEquals(0f, skeleton.bone(1).worldMatrix.data[12], 0.01f);
    assertEquals(0f, skeleton.bone(1).worldMatrix.data[13], 0.01f);
    assertEquals(-0.5f, skeleton.bone(1).worldMatrix.data[14], 0.01f);
    Vec3 axisInWorld = new Vec3();
    skeleton.bone(1).worldMatrix.transformDirection(new Vec3(1f, 0f, 0f), axisInWorld);
    assertEquals(0f, axisInWorld.x, 0.01f);
    assertEquals(-1f, axisInWorld.z, 0.01f);
  }

  @Test
  public void duplicateBoneNameRejected() {
    Skeleton skeleton = new Skeleton();
    skeleton.addBone("root", -1, new Vec3(), new Quat().identity());
    try {
      skeleton.addBone("root", -1, new Vec3(), new Quat().identity());
      fail("expected duplicate name rejection");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void parentMustPrecedeChild() {
    Skeleton skeleton = new Skeleton();
    try {
      skeleton.addBone("first", 1, new Vec3(), new Quat().identity());
      fail("expected parent-before-child rejection");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void restPoseRoundTrip() {
    Skeleton skeleton = twoBone();
    Pose rest = skeleton.buildRestPose();
    assertEquals(0.5f, rest.positions[1].x, 0f);
    skeleton.applyPose(rest);
    assertEquals(0.5f, skeleton.bone(1).localPosition.x, 0f);
  }

  @Test
  public void boneIndexLookup() {
    Skeleton skeleton = twoBone();
    assertEquals(0, skeleton.boneIndex("root"));
    assertEquals(1, skeleton.boneIndex("arm"));
    try {
      skeleton.boneIndex("missing");
      fail("expected unknown bone rejection");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void poseBlendMidpoint() {
    Skeleton skeleton = twoBone();
    Pose a = new Pose(2);
    Pose b = new Pose(2);
    a.positions[1].set(0f, 1f, 0f);
    b.positions[1].set(0f, -1f, 0f);
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, b.rotations[1]);
    Pose mid = new Pose(2);
    mid.blendFrom(a, b, 0.5f);
    assertEquals(0f, mid.positions[1].y, 0.01f);
    Vec3 rotated = new Vec3();
    Quat.rotateVec3(mid.rotations[1], new Vec3(1f, 0f, 0f), rotated);
    assertTrue(Math.abs(rotated.x - (float) Math.sqrt(0.5)) < 0.05f);
    assertTrue(rotated.z < -0.5f);
  }
}
