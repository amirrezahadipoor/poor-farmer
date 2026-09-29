package com.poorfarmer.core.procedural;

public final class ProceduralTextures {

  public static final int DEFAULT_SIZE = 256;

  private ProceduralTextures() {
  }

  public static Texture dirt(Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    Ramp ramp = new Ramp(new float[][]{
        {0f, 0.30f, 0.21f, 0.14f},
        {0.5f, 0.44f, 0.33f, 0.22f},
        {1f, 0.58f, 0.47f, 0.33f}
    });
    float[] c = new float[3];
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float n = noise.fbm2(x * 0.04f, y * 0.04f, 4, 0.5f, 2f);
        float speckle = noise.fbm2(x * 0.25f, y * 0.25f, 2, 0.5f, 2f);
        float w = noise.worley2(x * 0.05f, y * 0.05f);
        float pebble = smoothstep(0.10f, 0.16f, w);
        ramp.sample(n, c);
        float light = 1f + (speckle - 0.5f) * 0.35f + (pebble - 0.5f) * 0.25f;
        t.setPixel(x, y, c[0] * light, c[1] * light, c[2] * light, 1f);
      }
    }
    return t;
  }

  public static Texture grass(Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    Ramp ramp = new Ramp(new float[][]{
        {0f, 0.10f, 0.22f, 0.08f},
        {0.6f, 0.20f, 0.38f, 0.13f},
        {1f, 0.33f, 0.52f, 0.20f}
    });
    float[] c = new float[3];
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float patch = noise.fbm2(x * 0.02f, y * 0.02f, 4, 0.5f, 2f);
        float blades = noise.fbm2(x * 0.12f, y * 0.45f, 3, 0.5f, 2f);
        float wet = noise.worley2(x * 0.04f, y * 0.04f);
        ramp.sample(patch, c);
        float light = 0.8f + blades * 0.5f - (1f - smoothstep(0.06f, 0.14f, wet)) * 0.2f;
        t.setPixel(x, y, c[0] * light, c[1] * light, c[2] * light, 1f);
      }
    }
    return t;
  }

  public static Texture stone(Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    Ramp ramp = new Ramp(new float[][]{
        {0f, 0.33f, 0.34f, 0.36f},
        {0.5f, 0.46f, 0.47f, 0.49f},
        {1f, 0.60f, 0.61f, 0.63f}
    });
    float[] c = new float[3];
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float strata = noise.fbm2(x * 0.015f, y * 0.06f, 3, 0.5f, 2f);
        float detail = noise.fbm2(x * 0.09f, y * 0.09f, 5, 0.5f, 2f);
        float veins = 1f - noise.ridged2(x * 0.05f, y * 0.05f, 3);
        float w = noise.worley2(x * 0.06f, y * 0.06f);
        float chips = smoothstep(0.05f, 0.12f, w);
        ramp.sample(strata * 0.4f + detail * 0.6f, c);
        float light = 1f - veins * 0.25f - (1f - chips) * 0.3f;
        t.setPixel(x, y, c[0] * light, c[1] * light, c[2] * light, 1f);
      }
    }
    return t;
  }

  public static Texture wood(Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    Ramp ramp = new Ramp(new float[][]{
        {0f, 0.24f, 0.14f, 0.07f},
        {0.5f, 0.40f, 0.26f, 0.14f},
        {1f, 0.55f, 0.40f, 0.24f}
    });
    float[] c = new float[3];
    float half = size / 2f;
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float dx = (x - half) / size;
        float dy = (y - half) / size;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float warp = noise.fbm2(x * 0.03f, y * 0.03f, 3, 0.5f, 2f);
        float ring = (float) Math.sin(dist * 42f + warp * 7f) * 0.5f + 0.5f;
        float grain = noise.fbm2(x * 0.02f, y * 0.2f, 2, 0.5f, 2f);
        ramp.sample(ring * 0.8f + grain * 0.2f, c);
        t.setPixel(x, y, c[0], c[1], c[2], 1f);
      }
    }
    return t;
  }

  public static Texture cloth(float baseR, float baseG, float baseB, Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float weaveX = (float) Math.sin(x * Math.PI);
        float weaveY = (float) Math.sin(y * Math.PI);
        float weave = weaveX * weaveY * 0.5f + 0.5f;
        float n = noise.fbm2(x * 0.3f, y * 0.3f, 2, 0.5f, 2f);
        float light = 0.82f + weave * 0.18f + (n - 0.5f) * 0.15f;
        t.setPixel(x, y, baseR * light, baseG * light, baseB * light, 1f);
      }
    }
    return t;
  }

  public static Texture water(Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    Ramp ramp = new Ramp(new float[][]{
        {0f, 0.07f, 0.22f, 0.36f},
        {0.6f, 0.14f, 0.36f, 0.52f},
        {1f, 0.28f, 0.52f, 0.64f}
    });
    float[] c = new float[3];
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float big = noise.fbm2(x * 0.03f, y * 0.03f, 4, 0.5f, 2f);
        float ripple = (float) Math.sin(x * 0.12f + y * 0.08f + big * 9f) * 0.5f + 0.5f;
        float sparkle = noise.fbm2(x * 0.3f, y * 0.3f, 2, 0.5f, 2f);
        ramp.sample(big * 0.5f + ripple * 0.5f, c);
        float light = 1f + Math.max(0f, sparkle - 0.78f) * 2f;
        t.setPixel(x, y, c[0] * light, c[1] * light, c[2] * light, 1f);
      }
    }
    return t;
  }

  public static Texture sky(float topR, float topG, float topB, float horizonR, float horizonG, float horizonB, Noise noise, int size) {
    int height = size * 2;
    Texture t = Texture.ofSize(size, height);
    for (int y = 0; y < height; y++) {
      float f = (float) y / (height - 1);
      for (int x = 0; x < size; x++) {
        float n = noise.fbm2(x * 0.05f, y * 0.05f, 2, 0.5f, 2f);
        float light = 1f + (n - 0.5f) * 0.06f;
        float r = (topR + (horizonR - topR) * f) * light;
        float g = (topG + (horizonG - topG) * f) * light;
        float b = (topB + (horizonB - topB) * f) * light;
        t.setPixel(x, y, r, g, b, 1f);
      }
    }
    return t;
  }

  public static Texture sand(Noise noise, int size) {
    Texture t = Texture.ofSize(size, size);
    Ramp ramp = new Ramp(new float[][]{
        {0f, 0.62f, 0.54f, 0.39f},
        {0.5f, 0.74f, 0.66f, 0.49f},
        {1f, 0.84f, 0.76f, 0.58f}
    });
    float[] c = new float[3];
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float n = noise.fbm2(x * 0.05f, y * 0.05f, 4, 0.5f, 2f);
        float speckle = noise.fbm2(x * 0.3f, y * 0.3f, 2, 0.5f, 2f);
        ramp.sample(n, c);
        float light = 1f + (speckle - 0.5f) * 0.2f;
        t.setPixel(x, y, c[0] * light, c[1] * light, c[2] * light, 1f);
      }
    }
    return t;
  }

  private static float smoothstep(float a, float b, float x) {
    float f = Math.max(0f, Math.min(1f, (x - a) / (b - a)));
    return f * f * (3f - 2f * f);
  }
}
