package com.poorfarmer.core.entity;

import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class AnimationClip {

  private final String name;
  private final float duration;
  private final boolean loop;
  private final List<BoneTrack>[] tracks;

  @SuppressWarnings("unchecked")
  private AnimationClip(String name, float duration, boolean loop, List<BoneTrack>[] tracks) {
    this.name = name;
    this.duration = duration;
    this.loop = loop;
    this.tracks = tracks;
  }

  public String name() {
    return name;
  }

  public float duration() {
    return duration;
  }

  public boolean loop() {
    return loop;
  }

  public void sample(float time, Pose out, Pose rest) {
    out.copy(rest);
    float t = loop ? (time % duration + duration) % duration : Math.max(0f, Math.min(time, duration));
    for (List<BoneTrack> track : tracks) {
      if (track.isEmpty()) {
        continue;
      }
      int index = 0;
      while (index < track.size() - 1 && track.get(index + 1).time < t) {
        index++;
      }
      BoneTrack a = track.get(index);
      BoneTrack b = track.get(Math.min(index + 1, track.size() - 1));
      float span = b.time - a.time;
      float f = span > 1e-5f ? (t - a.time) / span : 0f;
      Vec3.lerp(a.position, b.position, f, out.positions[a.boneIndex]);
      Quat.slerp(a.rotation, b.rotation, f, out.rotations[a.boneIndex]);
    }
  }

  public static final class Builder {

    private final String name;
    private final float duration;
    private final boolean loop;
    private final int boneCount;
    private final List<List<Key>> perBone = new ArrayList<>();

    private Builder(String name, float duration, boolean loop, int boneCount) {
      this.name = name;
      this.duration = duration;
      this.loop = loop;
      this.boneCount = boneCount;
      for (int i = 0; i < boneCount; i++) {
        perBone.add(new ArrayList<Key>());
      }
    }

    public Builder key(float time, int boneIndex, Vec3 position, Quat rotation) {
      if (time < -1e-5f || time > duration + 1e-5f) {
        throw new IllegalArgumentException("key time outside clip: " + time);
      }
      Key key = new Key();
      key.time = Math.max(0f, Math.min(time, duration));
      key.boneIndex = boneIndex;
      key.position = new Vec3(position);
      key.rotation = new Quat(rotation);
      perBone.get(boneIndex).add(key);
      return this;
    }

    public AnimationClip build() {
      List<BoneTrack>[] tracks = new List[boneCount];
      for (int i = 0; i < boneCount; i++) {
        List<Key> keys = perBone.get(i);
        keys.sort((k1, k2) -> Float.compare(k1.time, k2.time));
        List<BoneTrack> track = new ArrayList<>();
        float first = keys.isEmpty() ? 0f : keys.get(0).time;
        float last = keys.isEmpty() ? duration : keys.get(keys.size() - 1).time;
        for (Key key : keys) {
          track.add(new BoneTrack(key));
        }
        if (!keys.isEmpty()) {
          if (first > 1e-5f) {
            track.add(0, new BoneTrack(new Key(first, keys.get(0).boneIndex, keys.get(0).position, keys.get(0).rotation)));
            track.get(0).time = 0f;
          }
          if (last < duration - 1e-5f) {
            Key tail = new Key(duration, keys.get(keys.size() - 1).boneIndex,
                keys.get(keys.size() - 1).position, keys.get(keys.size() - 1).rotation);
            track.add(new BoneTrack(tail));
          }
        }
        tracks[i] = track;
      }
      return new AnimationClip(name, duration, loop, tracks);
    }
  }

  public static Builder builder(String name, float duration, boolean loop, int boneCount) {
    return new Builder(name, duration, loop, boneCount);
  }

  private static final class Key {
    float time;
    int boneIndex;
    Vec3 position;
    Quat rotation;

    Key() {
    }

    Key(float time, int boneIndex, Vec3 position, Quat rotation) {
      this.time = time;
      this.boneIndex = boneIndex;
      this.position = position;
      this.rotation = rotation;
    }
  }

  private static final class BoneTrack {
    float time;
    final int boneIndex;
    final Vec3 position;
    final Quat rotation;

    BoneTrack(Key key) {
      this.time = key.time;
      this.boneIndex = key.boneIndex;
      this.position = key.position;
      this.rotation = key.rotation;
    }
  }
}
