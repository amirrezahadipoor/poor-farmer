package com.poorfarmer.core.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public class AnimationClipTest {

  private static final Skeleton SKELETON = makeSkeleton();

  private static Skeleton makeSkeleton() {
    Skeleton skeleton = new Skeleton();
    skeleton.addBone("root", -1, new Vec3(), new Quat().identity());
    skeleton.addBone("arm", 0, new Vec3(0.5f, 0f, 0f), new Quat().identity());
    return skeleton;
  }

  @Test
  public void staticKeyHoldsValue() {
    AnimationClip clip = AnimationClip
        .builder("static", 2f, true, 2)
        .key(0f, 1, new Vec3(0.5f, 1f, 0f), new Quat().identity())
        .build();
    Pose rest = SKELETON.buildRestPose();
    Pose pose = new Pose(2);
    clip.sample(1.3f, pose, rest);
    assertEquals(1f, pose.positions[1].y, 0.001f);
  }

  @Test
  public void interpolatesBetweenKeys() {
    Quat endRotation = new Quat();
    Quat.fromAxisAngle(Vec3.UP, (float) Math.PI / 2f, endRotation);
    AnimationClip clip = AnimationClip
        .builder("slide", 1f, true, 2)
        .key(0f, 1, new Vec3(0.5f, 0f, 0f), new Quat().identity())
        .key(1f, 1, new Vec3(0.5f, 2f, 0f), endRotation)
        .build();
    Pose rest = SKELETON.buildRestPose();
    Pose pose = new Pose(2);
    clip.sample(0.5f, pose, rest);
    assertEquals(1f, pose.positions[1].y, 0.01f);
    Vec3 rotated = new Vec3();
    Quat.rotateVec3(pose.rotations[1], new Vec3(1f, 0f, 0f), rotated);
    assertTrue(rotated.z < -0.3f);
    assertTrue(rotated.x > 0.3f);
  }

  @Test
  public void loopWrapsTime() {
    AnimationClip clip = AnimationClip
        .builder("loop", 1f, true, 2)
        .key(0f, 1, new Vec3(0.5f, 0f, 0f), new Quat().identity())
        .key(1f, 1, new Vec3(0.5f, 2f, 0f), new Quat().identity())
        .build();
    Pose rest = SKELETON.buildRestPose();
    Pose pose = new Pose(2);
    clip.sample(1.5f, pose, rest);
    assertEquals(1f, pose.positions[1].y, 0.01f);
  }

  @Test
  public void nonLoopClampsToEnd() {
    AnimationClip clip = AnimationClip
        .builder("once", 1f, false, 2)
        .key(0f, 1, new Vec3(0.5f, 0f, 0f), new Quat().identity())
        .key(1f, 1, new Vec3(0.5f, 2f, 0f), new Quat().identity())
        .build();
    Pose rest = SKELETON.buildRestPose();
    Pose pose = new Pose(2);
    clip.sample(5f, pose, rest);
    assertEquals(2f, pose.positions[1].y, 0.01f);
  }

  @Test
  public void unkeyedBonesKeepRest() {
    AnimationClip clip = AnimationClip
        .builder("armOnly", 1f, true, 2)
        .key(0f, 1, new Vec3(0.5f, 3f, 0f), new Quat().identity())
        .build();
    Pose rest = SKELETON.buildRestPose();
    Pose pose = new Pose(2);
    clip.sample(0.2f, pose, rest);
    assertEquals(rest.positions[0].x, pose.positions[0].x, 0f);
    assertEquals(3f, pose.positions[1].y, 0.01f);
  }

  @Test
  public void keyOutsideDurationRejected() {
    try {
      AnimationClip.builder("bad", 1f, true, 2)
          .key(2f, 1, new Vec3(), new Quat().identity());
      fail("expected out-of-range key rejection");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void sparseKeysPaddedAtEnd() {
    AnimationClip clip = AnimationClip
        .builder("sparse", 2f, true, 2)
        .key(1f, 1, new Vec3(0.5f, 4f, 0f), new Quat().identity())
        .build();
    Pose rest = SKELETON.buildRestPose();
    Pose pose = new Pose(2);
    clip.sample(0f, pose, rest);
    assertEquals(4f, pose.positions[1].y, 0.01f);
    clip.sample(1.9f, pose, rest);
    assertEquals(4f, pose.positions[1].y, 0.01f);
  }
}
