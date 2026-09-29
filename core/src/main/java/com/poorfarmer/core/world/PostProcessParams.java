package com.poorfarmer.core.world;

public final class PostProcessParams {

  private static final float[][] SEASON_TINT = {
      {1.02f, 1.00f, 0.98f},
      {1.04f, 1.01f, 0.95f},
      {1.08f, 0.99f, 0.90f},
      {0.94f, 0.98f, 1.06f},
  };
  private static final float[] SEASON_EXPOSURE = {1.00f, 1.06f, 0.97f, 0.92f};
  private static final float[] SEASON_VIGNETTE = {0.22f, 0.18f, 0.26f, 0.30f};

  public static final float NOON_BLOOM_THRESHOLD = 0.75f;
  public static final float DUSK_BLOOM_THRESHOLD = 0.45f;
  public static final float NOON_BLOOM_INTENSITY = 0.30f;
  public static final float DUSK_BLOOM_INTENSITY = 0.85f;
  public static final float NIGHT_BLOOM_INTENSITY = 0.05f;
  public static final float NIGHT_VIGNETTE_BONUS = 0.08f;

  public float tintR;
  public float tintG;
  public float tintB;
  public float exposure;
  public float vignetteStrength;
  public float bloomThreshold;
  public float bloomIntensity;
  public float warmth;

  private PostProcessParams() {
  }

  public static PostProcessParams resolve(int season, float sunElevationDegrees, float sunIntensity) {
    int s = ((season % 4) + 4) % 4;
    PostProcessParams params = new PostProcessParams();
    params.tintR = SEASON_TINT[s][0];
    params.tintG = SEASON_TINT[s][1];
    params.tintB = SEASON_TINT[s][2];
    params.exposure = SEASON_EXPOSURE[s];
    params.vignetteStrength = SEASON_VIGNETTE[s];

    if (sunIntensity <= 0.02f) {
      params.warmth = 0f;
      params.bloomThreshold = NOON_BLOOM_THRESHOLD;
      params.bloomIntensity = NIGHT_BLOOM_INTENSITY;
      params.vignetteStrength += NIGHT_VIGNETTE_BONUS;
      params.tintR = lerp(params.tintR, 0.98f, 0.5f);
      params.tintG = lerp(params.tintG, 0.98f, 0.5f);
      params.tintB = lerp(params.tintB, 1.04f, 0.5f);
      return params;
    }

    float dusk = 0f;
    if (sunElevationDegrees > 0f && sunElevationDegrees < 20f) {
      dusk = 1f - Math.min(1f, sunElevationDegrees / 20f);
    }
    params.warmth = dusk * (0.5f + 0.5f * Math.min(1f, sunIntensity));
    params.tintR += 0.18f * params.warmth;
    params.tintG += 0.04f * params.warmth;
    params.tintB -= 0.16f * params.warmth;
    params.bloomThreshold = lerp(NOON_BLOOM_THRESHOLD, DUSK_BLOOM_THRESHOLD, dusk);
    params.bloomIntensity = lerp(NOON_BLOOM_INTENSITY, DUSK_BLOOM_INTENSITY, dusk);
    return params;
  }

  private static float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }
}
