package com.poorfarmer.core.procedural;

public final class Noise {

  private final long seed;

  public Noise(long seed) {
    this.seed = seed;
  }

  public float noise2(float x, float y) {
    int x0 = (int) Math.floor(x);
    int y0 = (int) Math.floor(y);
    float fx = x - x0;
    float fy = y - y0;
    float sx = fx * fx * (3f - 2f * fx);
    float sy = fy * fy * (3f - 2f * fy);
    float v00 = hash2(x0, y0);
    float v10 = hash2(x0 + 1, y0);
    float v01 = hash2(x0, y0 + 1);
    float v11 = hash2(x0 + 1, y0 + 1);
    float a = v00 + (v10 - v00) * sx;
    float b = v01 + (v11 - v01) * sx;
    return a + (b - a) * sy;
  }

  public float fbm2(float x, float y, int octaves, float persistence, float lacunarity) {
    float sum = 0f;
    float amp = 1f;
    float freq = 1f;
    float norm = 0f;
    for (int i = 0; i < octaves; i++) {
      sum += noise2(x * freq, y * freq) * amp;
      norm += amp;
      amp *= persistence;
      freq *= lacunarity;
    }
    return sum / norm;
  }

  public float ridged2(float x, float y, int octaves) {
    float sum = 0f;
    float amp = 0.5f;
    float freq = 1f;
    for (int i = 0; i < octaves; i++) {
      float n = 1f - Math.abs(2f * noise2(x * freq, y * freq) - 1f);
      sum += n * n * amp;
      amp *= 0.5f;
      freq *= 2f;
    }
    return sum;
  }

  public float worley2(float x, float y) {
    int x0 = (int) Math.floor(x);
    int y0 = (int) Math.floor(y);
    float best = Float.MAX_VALUE;
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        int cx = x0 + dx;
        int cy = y0 + dy;
        float jx = cx + hash2(cx, cy);
        float jy = cy + hash2(cx + 1013, cy + 1013);
        float ddx = x - jx;
        float ddy = y - jy;
        float d = ddx * ddx + ddy * ddy;
        if (d < best) {
          best = d;
        }
      }
    }
    return (float) Math.sqrt(best);
  }

  private float hash2(int x, int y) {
    long h = seed;
    h = h * 6364136223846793005L + x * 1442695040888963407L;
    h = Long.rotateLeft(h, 17);
    h = h * 6364136223846793005L + y * 7467137109482818001L;
    h = Long.rotateLeft(h, 31);
    h ^= h >>> 33;
    h *= 0xff51afd7ed558ccdL;
    h ^= h >>> 33;
    return (h & 0xffffffL) / 16777216f;
  }
}
