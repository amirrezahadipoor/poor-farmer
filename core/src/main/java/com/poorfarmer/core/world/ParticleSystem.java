package com.poorfarmer.core.world;

import java.util.Random;

public final class ParticleSystem {

  public static final int STRIDE = 20;
  public static final int RENDER_STRIDE = 8;
  public static final float GRAVITY = 5f;

  private static final int X = 0;
  private static final int Y = 1;
  private static final int Z = 2;
  private static final int VX = 3;
  private static final int VY = 4;
  private static final int VZ = 5;
  private static final int AGE = 6;
  private static final int LIFE = 7;
  private static final int SIZE = 8;
  private static final int GROW = 9;
  private static final int R = 10;
  private static final int G = 11;
  private static final int B = 12;
  private static final int ALPHA = 13;
  private static final int GRAVITY_FACTOR = 14;
  private static final int SWAY_AMP = 15;
  private static final int SWAY_FREQ = 16;
  private static final int PHASE = 17;
  private static final int KILL_AT_GROUND = 18;
  private static final int DRAG = 19;

  private final int capacity;
  private final float[] data;
  private final int[] freeList;
  private int freeTop;
  private int activeCount;
  private final Random random;
  private final float[] renderData;

  public ParticleSystem(int capacity, long seed) {
    this.capacity = capacity;
    this.data = new float[capacity * STRIDE];
    this.freeList = new int[capacity];
    for (int i = 0; i < capacity; i++) {
      freeList[i] = capacity - 1 - i;
    }
    freeTop = capacity;
    random = new Random(seed);
    renderData = new float[capacity * RENDER_STRIDE];
  }

  public int capacity() {
    return capacity;
  }

  public int activeCount() {
    return activeCount;
  }

  public boolean emit(ParticleType type, float x, float y, float z, int count, int season) {
    boolean allSpawned = true;
    for (int n = 0; n < count; n++) {
      if (freeTop == 0) {
        allSpawned = false;
        break;
      }
      int index = freeList[--freeTop] * STRIDE;
      activeCount++;
      applyPreset(index, type, x, y, z, season);
    }
    return allSpawned;
  }

  public void update(float dt) {
    for (int p = 0; p < capacity; p++) {
      int base = p * STRIDE;
      if (data[base + AGE] >= data[base + LIFE]) {
        continue;
      }
      float age = data[base + AGE] + dt;
      data[base + AGE] = age;
      if (age >= data[base + LIFE]) {
        kill(p);
        continue;
      }
      float drag = 1f - data[base + DRAG] * dt;
      if (drag < 0f) {
        drag = 0f;
      }
      data[base + VX] *= drag;
      data[base + VY] *= drag;
      data[base + VZ] *= drag;
      data[base + VY] -= GRAVITY * data[base + GRAVITY_FACTOR] * dt;
      if (data[base + SWAY_AMP] > 0f) {
        data[base + VX] += (float) Math.cos(age * data[base + SWAY_FREQ] + data[base + PHASE])
            * data[base + SWAY_AMP] * dt;
        data[base + VZ] += (float) Math.sin(age * data[base + SWAY_FREQ] + data[base + PHASE])
            * data[base + SWAY_AMP] * dt * 0.6f;
      }
      data[base + X] += data[base + VX] * dt;
      data[base + Y] += data[base + VY] * dt;
      data[base + Z] += data[base + VZ] * dt;
      data[base + SIZE] += data[base + GROW] * dt;
      if (data[base + SIZE] < 0.001f) {
        data[base + SIZE] = 0.001f;
      }
      if (data[base + KILL_AT_GROUND] > 0.5f && data[base + Y] <= 0f) {
        kill(p);
      }
    }
  }

  public void killAll() {
    for (int p = activeCount - 1; p >= 0; p--) {
      kill(p);
    }
  }

  private void kill(int p) {
    int base = p * STRIDE;
    data[base + AGE] = data[base + LIFE];
    activeCount--;
    freeList[freeTop++] = p;
  }

  public void fillRenderData() {
    int out = 0;
    for (int p = 0; p < capacity; p++) {
      int base = p * STRIDE;
      float age = data[base + AGE];
      float life = data[base + LIFE];
      if (age >= life) {
        continue;
      }
      float fadeIn = Math.min(1f, age / (0.1f * life + 0.0001f));
      float fadeOut = Math.min(1f, (life - age) / (0.3f * life + 0.0001f));
      float alpha = data[base + ALPHA] * fadeIn * fadeOut;
      renderData[out++] = data[base + X];
      renderData[out++] = data[base + Y];
      renderData[out++] = data[base + Z];
      renderData[out++] = data[base + SIZE];
      renderData[out++] = data[base + R];
      renderData[out++] = data[base + G];
      renderData[out++] = data[base + B];
      renderData[out++] = alpha;
    }
  }

  public float[] renderData() {
    return renderData;
  }

  public float particleX(int p) {
    return data[p * STRIDE + X];
  }

  public float particleY(int p) {
    return data[p * STRIDE + Y];
  }

  public float particleSize(int p) {
    return data[p * STRIDE + SIZE];
  }

  public float particleAlpha(int p) {
    int base = p * STRIDE;
    float age = data[base + AGE];
    float life = data[base + LIFE];
    if (age >= life) {
      return 0f;
    }
    float fadeIn = Math.min(1f, age / (0.1f * life + 0.0001f));
    float fadeOut = Math.min(1f, (life - age) / (0.3f * life + 0.0001f));
    return data[base + ALPHA] * fadeIn * fadeOut;
  }

  private void applyPreset(int i, ParticleType type, float x, float y, float z, int season) {
    float spread = 0.25f;
    switch (type) {
      case DUST:
        data[i + X] = x + rand(-spread, spread);
        data[i + Y] = y + rand(0f, 0.3f);
        data[i + Z] = z + rand(-spread, spread);
        data[i + VX] = rand(-0.2f, 0.2f);
        data[i + VY] = rand(0.1f, 0.4f);
        data[i + VZ] = rand(-0.2f, 0.2f);
        data[i + LIFE] = rand(2f, 4f);
        data[i + SIZE] = rand(0.05f, 0.12f);
        data[i + GROW] = 0.02f;
        data[i + R] = 0.72f;
        data[i + G] = 0.62f;
        data[i + B] = 0.45f;
        data[i + ALPHA] = 0.35f;
        data[i + GRAVITY_FACTOR] = 0f;
        data[i + SWAY_AMP] = 0.3f;
        data[i + SWAY_FREQ] = 1.2f;
        break;
      case WATER:
        data[i + X] = x;
        data[i + Y] = y;
        data[i + Z] = z;
        data[i + VX] = rand(-1.5f, 1.5f);
        data[i + VY] = rand(1.5f, 3.5f);
        data[i + VZ] = rand(-1.5f, 1.5f);
        data[i + LIFE] = rand(0.6f, 1f);
        data[i + SIZE] = rand(0.04f, 0.08f);
        data[i + GROW] = 0f;
        data[i + R] = 0.45f;
        data[i + G] = 0.65f;
        data[i + B] = 0.85f;
        data[i + ALPHA] = 0.8f;
        data[i + GRAVITY_FACTOR] = 1f;
        data[i + KILL_AT_GROUND] = 1f;
        break;
      case RAIN:
        data[i + X] = x + rand(-2f, 2f);
        data[i + Y] = y + rand(8f, 14f);
        data[i + Z] = z + rand(-2f, 2f);
        data[i + VX] = rand(-0.3f, 0.3f);
        data[i + VY] = rand(-12f, -9f);
        data[i + VZ] = rand(-0.3f, 0.3f);
        data[i + LIFE] = 3f;
        data[i + SIZE] = 0.03f;
        data[i + GROW] = 0f;
        data[i + R] = 0.5f;
        data[i + G] = 0.6f;
        data[i + B] = 0.8f;
        data[i + ALPHA] = 0.5f;
        data[i + GRAVITY_FACTOR] = 0.3f;
        data[i + KILL_AT_GROUND] = 1f;
        break;
      case SNOW:
        data[i + X] = x + rand(-2f, 2f);
        data[i + Y] = y + rand(6f, 12f);
        data[i + Z] = z + rand(-2f, 2f);
        data[i + VX] = rand(-0.15f, 0.15f);
        data[i + VY] = rand(-0.9f, -0.5f);
        data[i + VZ] = rand(-0.15f, 0.15f);
        data[i + LIFE] = rand(6f, 10f);
        data[i + SIZE] = rand(0.04f, 0.07f);
        data[i + GROW] = 0f;
        data[i + R] = 1f;
        data[i + G] = 1f;
        data[i + B] = 1f;
        data[i + ALPHA] = 0.9f;
        data[i + GRAVITY_FACTOR] = 0.05f;
        data[i + SWAY_AMP] = 0.6f;
        data[i + SWAY_FREQ] = 1.5f;
        data[i + KILL_AT_GROUND] = 1f;
        break;
      case LEAF:
        data[i + X] = x + rand(-1.5f, 1.5f);
        data[i + Y] = y + rand(2f, 6f);
        data[i + Z] = z + rand(-1.5f, 1.5f);
        data[i + VX] = rand(-0.3f, 0.3f);
        data[i + VY] = rand(-0.8f, -0.4f);
        data[i + VZ] = rand(-0.3f, 0.3f);
        data[i + LIFE] = rand(8f, 14f);
        data[i + SIZE] = rand(0.08f, 0.14f);
        data[i + GROW] = 0f;
        int palette = season == 2 ? 1 : (season == 3 ? 2 : 0);
        if (palette == 0) {
          data[i + R] = rand(0.2f, 0.35f);
          data[i + G] = rand(0.4f, 0.55f);
          data[i + B] = rand(0.15f, 0.2f);
        } else if (palette == 1) {
          data[i + R] = rand(0.7f, 0.85f);
          data[i + G] = rand(0.55f, 0.7f);
          data[i + B] = rand(0.15f, 0.25f);
        } else {
          data[i + R] = rand(0.45f, 0.55f);
          data[i + G] = rand(0.3f, 0.38f);
          data[i + B] = rand(0.15f, 0.2f);
        }
        data[i + ALPHA] = 0.95f;
        data[i + GRAVITY_FACTOR] = 0.1f;
        data[i + SWAY_AMP] = 1.2f;
        data[i + SWAY_FREQ] = 2f;
        data[i + KILL_AT_GROUND] = 1f;
        break;
      case SPARK:
        data[i + X] = x;
        data[i + Y] = y;
        data[i + Z] = z;
        float angle = (float) (random.nextDouble() * Math.PI * 2f);
        float speed = rand(2f, 5f);
        data[i + VX] = (float) Math.cos(angle) * speed;
        data[i + VY] = rand(1f, 3f) + speed * 0.4f;
        data[i + VZ] = (float) Math.sin(angle) * speed;
        data[i + LIFE] = rand(0.3f, 0.8f);
        data[i + SIZE] = rand(0.02f, 0.04f);
        data[i + GROW] = -0.02f;
        data[i + R] = 1f;
        data[i + G] = rand(0.55f, 0.85f);
        data[i + B] = 0.2f;
        data[i + ALPHA] = 1f;
        data[i + GRAVITY_FACTOR] = 1f;
        break;
      case COIN:
        data[i + X] = x + rand(-0.1f, 0.1f);
        data[i + Y] = y;
        data[i + Z] = z + rand(-0.1f, 0.1f);
        data[i + VX] = rand(-0.4f, 0.4f);
        data[i + VY] = rand(3.5f, 5f);
        data[i + VZ] = rand(-0.4f, 0.4f);
        data[i + LIFE] = rand(1f, 1.4f);
        data[i + SIZE] = 0.09f;
        data[i + GROW] = 0f;
        data[i + R] = 1f;
        data[i + G] = 0.85f;
        data[i + B] = 0.25f;
        data[i + ALPHA] = 1f;
        data[i + GRAVITY_FACTOR] = 1.2f;
        data[i + DRAG] = 0.1f;
        break;
      case SMOKE:
      default:
        data[i + X] = x + rand(-0.1f, 0.1f);
        data[i + Y] = y;
        data[i + Z] = z + rand(-0.1f, 0.1f);
        data[i + VX] = rand(-0.15f, 0.15f);
        data[i + VY] = rand(0.5f, 1f);
        data[i + VZ] = rand(-0.15f, 0.15f);
        data[i + LIFE] = rand(2f, 4f);
        data[i + SIZE] = 0.15f;
        data[i + GROW] = 0.25f;
        data[i + R] = 0.5f;
        data[i + G] = 0.5f;
        data[i + B] = 0.52f;
        data[i + ALPHA] = 0.5f;
        data[i + GRAVITY_FACTOR] = -0.1f;
        data[i + SWAY_AMP] = 0.4f;
        data[i + SWAY_FREQ] = 0.8f;
        break;
    }
    data[i + AGE] = 0f;
    data[i + PHASE] = (float) (random.nextDouble() * Math.PI * 2f);
  }

  private float rand(float min, float max) {
    return min + (float) random.nextDouble() * (max - min);
  }
}
