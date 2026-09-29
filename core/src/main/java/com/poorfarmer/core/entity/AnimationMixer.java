package com.poorfarmer.core.entity;

public final class AnimationMixer {

  public static final int MAX_VOICES = 4;

  private static final class Voice {
    AnimationClip clip;
    float time;
    float weight;
    float targetWeight;
    float fadeTime;
    boolean active;
  }

  private final Skeleton skeleton;
  private final Pose rest;
  private final Voice[] voices = new Voice[MAX_VOICES];
  private final Pose[] scratch = new Pose[MAX_VOICES];
  private final Pose result;

  public AnimationMixer(Skeleton skeleton) {
    this.skeleton = skeleton;
    this.rest = skeleton.buildRestPose();
    for (int i = 0; i < MAX_VOICES; i++) {
      voices[i] = new Voice();
      scratch[i] = new Pose(skeleton.boneCount());
    }
    result = new Pose(skeleton.boneCount());
  }

  public void play(AnimationClip clip) {
    voices[0].clip = clip;
    voices[0].time = 0f;
    voices[0].weight = 1f;
    voices[0].targetWeight = 1f;
    voices[0].fadeTime = 0f;
    voices[0].active = true;
    for (int i = 1; i < MAX_VOICES; i++) {
      voices[i].active = false;
    }
  }

  public void crossFade(AnimationClip clip, float fadeTime) {
    for (int i = 0; i < MAX_VOICES; i++) {
      if (voices[i].active) {
        voices[i].targetWeight = 0f;
        voices[i].fadeTime = Math.max(fadeTime, 0.0001f);
      }
    }
    int slot = firstInactiveSlot();
    if (slot < 0) {
      slot = slotWithLowestActiveWeight();
    }
    voices[slot].clip = clip;
    voices[slot].time = 0f;
    voices[slot].weight = 0f;
    voices[slot].targetWeight = 1f;
    voices[slot].fadeTime = Math.max(fadeTime, 0.0001f);
    voices[slot].active = true;
  }

  public void update(float dt) {
    for (int i = 0; i < MAX_VOICES; i++) {
      Voice voice = voices[i];
      if (!voice.active) {
        continue;
      }
      float step = dt / voice.fadeTime;
      if (voice.weight < voice.targetWeight) {
        voice.weight = Math.min(voice.targetWeight, voice.weight + step);
      } else if (voice.weight > voice.targetWeight) {
        voice.weight = Math.max(voice.targetWeight, voice.weight - step);
      }
      if (voice.targetWeight <= 0f && voice.weight <= 0f) {
        voice.active = false;
        continue;
      }
      if (voice.clip.loop()) {
        voice.time = (voice.time + dt) % voice.clip.duration();
      } else {
        voice.time = Math.min(voice.time + dt, voice.clip.duration());
      }
    }
    float totalWeight = 0f;
    for (int i = 0; i < MAX_VOICES; i++) {
      if (voices[i].active) {
        totalWeight += voices[i].weight;
      }
    }
    for (int i = 0; i < result.positions.length; i++) {
      result.positions[i].set(0f, 0f, 0f);
    }
    result.copyRotationsOnly(rest);
    for (int i = 0; i < MAX_VOICES; i++) {
      Voice voice = voices[i];
      if (!voice.active || voice.weight <= 0f) {
        continue;
      }
      voice.clip.sample(voice.time, scratch[i], rest);
      float weight = voice.weight / totalWeight;
      for (int b = 0; b < result.positions.length; b++) {
        result.positions[b].x += scratch[i].positions[b].x * weight;
        result.positions[b].y += scratch[i].positions[b].y * weight;
        result.positions[b].z += scratch[i].positions[b].z * weight;
      }
      result.accumulateRotation(scratch[i].rotations, weight);
    }
    skeleton.applyPose(result);
  }

  public float weightOf(AnimationClip clip) {
    float weight = 0f;
    for (int i = 0; i < MAX_VOICES; i++) {
      if (voices[i].active && voices[i].clip == clip) {
        weight = Math.max(weight, voices[i].weight);
      }
    }
    return weight;
  }

  private int firstInactiveSlot() {
    for (int i = 0; i < MAX_VOICES; i++) {
      if (!voices[i].active) {
        return i;
      }
    }
    return -1;
  }

  private int slotWithLowestActiveWeight() {
    int best = 0;
    for (int i = 1; i < MAX_VOICES; i++) {
      if (voices[i].active && voices[i].weight < voices[best].weight) {
        best = i;
      }
    }
    return best;
  }
}
