package com.poorfarmer.core.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public class AnimationMixerTest {

  private static Skeleton skeleton() {
    Skeleton skeleton = new Skeleton();
    skeleton.addBone("root", -1, new Vec3(), new Quat().identity());
    skeleton.addBone("arm", 0, new Vec3(0.5f, 0f, 0f), new Quat().identity());
    return skeleton;
  }

  private static AnimationClip staticClip(String name, float armY) {
    return AnimationClip
        .builder(name, 1f, true, 2)
        .key(0f, 1, new Vec3(0.5f, armY, 0f), new Quat().identity())
        .build();
  }

  @Test
  public void playFollowsClip() {
    Skeleton skeleton = skeleton();
    AnimationMixer mixer = new AnimationMixer(skeleton);
    AnimationClip clip = staticClip("up", 1f);
    mixer.play(clip);
    for (int i = 0; i < 10; i++) {
      mixer.update(1f / 60f);
    }
    assertEquals(1f, skeleton.bone(1).localPosition.y, 0.01f);
    assertEquals(1f, mixer.weightOf(clip), 0.01f);
  }

  @Test
  public void crossFadeEndsOnTarget() {
    Skeleton skeleton = skeleton();
    AnimationMixer mixer = new AnimationMixer(skeleton);
    AnimationClip up = staticClip("up", 1f);
    AnimationClip down = staticClip("down", -1f);
    mixer.play(up);
    mixer.update(0.05f);
    mixer.crossFade(down, 1f);
    for (int i = 0; i < 70; i++) {
      mixer.update(1f / 60f);
    }
    assertEquals(-1f, skeleton.bone(1).localPosition.y, 0.05f);
    assertEquals(1f, mixer.weightOf(down), 0.05f);
    assertEquals(0f, mixer.weightOf(up), 0.05f);
  }

  @Test
  public void crossFadeMidpointBlends() {
    Skeleton skeleton = skeleton();
    AnimationMixer mixer = new AnimationMixer(skeleton);
    AnimationClip up = staticClip("up", 1f);
    AnimationClip down = staticClip("down", -1f);
    mixer.play(up);
    mixer.update(0.05f);
    mixer.crossFade(down, 1f);
    for (int i = 0; i < 30; i++) {
      mixer.update(1f / 60f);
    }
    float y = skeleton.bone(1).localPosition.y;
    assertTrue(y > -0.4f && y < 0.4f);
  }

  @Test
  public void nonLoopingClipHoldsAtEnd() {
    Skeleton skeleton = skeleton();
    AnimationMixer mixer = new AnimationMixer(skeleton);
    AnimationClip once = AnimationClip
        .builder("once", 0.5f, false, 2)
        .key(0f, 1, new Vec3(0.5f, 0f, 0f), new Quat().identity())
        .key(0.5f, 1, new Vec3(0.5f, 2f, 0f), new Quat().identity())
        .build();
    mixer.play(once);
    for (int i = 0; i < 120; i++) {
      mixer.update(1f / 60f);
    }
    assertEquals(2f, skeleton.bone(1).localPosition.y, 0.01f);
  }

  @Test
  public void playReplacesPrevious() {
    Skeleton skeleton = skeleton();
    AnimationMixer mixer = new AnimationMixer(skeleton);
    AnimationClip up = staticClip("up", 1f);
    AnimationClip down = staticClip("down", -1f);
    mixer.play(up);
    mixer.update(0.05f);
    mixer.play(down);
    mixer.update(0.05f);
    assertEquals(-1f, skeleton.bone(1).localPosition.y, 0.05f);
  }
}
