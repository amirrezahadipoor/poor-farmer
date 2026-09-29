package com.poorfarmer.core.world;

public final class SkyPalette {

  public static final int ZENITH = 0;
  public static final int HORIZON = 1;
  public static final int SUN_GLOW = 2;
  public static final int CLOUD_BASE = 3;
  public static final int CLOUD_SHADOW = 4;

  public final float[] colors = new float[5 * 3];
  public final float starAlpha;
  public final float moonAlpha;

  private static final float[][] DAY_ZENITH = {
      {0.23f, 0.44f, 0.85f},
      {0.20f, 0.42f, 0.90f},
      {0.26f, 0.42f, 0.80f},
      {0.30f, 0.45f, 0.85f}};
  private static final float[][] DAY_HORIZON = {
      {0.70f, 0.78f, 0.86f},
      {0.72f, 0.80f, 0.88f},
      {0.78f, 0.74f, 0.72f},
      {0.70f, 0.76f, 0.84f}};
  private static final float[] NIGHT_ZENITH = {0.015f, 0.02f, 0.05f};
  private static final float[] NIGHT_HORIZON = {0.05f, 0.07f, 0.12f};
  private static final float[] DUSK_HORIZON = {0.95f, 0.55f, 0.30f};
  private static final float[] DUSK_ZENITH = {0.15f, 0.20f, 0.40f};

  public SkyPalette(float sunElevationDeg, float sunIntensity, int season) {
    int s = Math.max(0, Math.min(3, season));
    float day = smoothstep(2f, 12f, sunElevationDeg);
    float dusk = 1f - Math.abs(sunElevationDeg) / 12f;
    dusk = clamp(dusk, 0f, 1f) * (1f - day) * (sunIntensity > 0f ? 1f : 0f);
    float night = 1f - smoothstep(-8f, 0f, sunElevationDeg);

    float[] zenith = mix3(DAY_ZENITH[s], NIGHT_ZENITH, night);
    float[] horizon = mix3(DAY_HORIZON[s], NIGHT_HORIZON, night);
    horizon = mix3(horizon, DUSK_HORIZON, dusk * 0.8f);
    zenith = mix3(zenith, DUSK_ZENITH, dusk * 0.5f);

    float[] sunGlow = new float[]{1f, 0.85f + 0.15f * (1f - day), 0.6f + 0.3f * day};
    float glowStrength = sunIntensity * smoothstep(-4f, 4f, sunElevationDeg);

    set(ZENITH, zenith);
    set(HORIZON, horizon);
    set(SUN_GLOW, sunGlow[0] * glowStrength, sunGlow[1] * glowStrength, sunGlow[2] * glowStrength);

    float cloudLight = 0.25f + 0.75f * clamp(sunIntensity, 0f, 1f);
    float[] cloudBase = {
        0.92f * cloudLight + 0.05f, 0.92f * cloudLight + 0.05f, 0.95f * cloudLight + 0.06f};
    float[] cloudShadow = {0.55f * cloudLight + 0.08f, 0.58f * cloudLight + 0.09f, 0.68f * cloudLight + 0.12f};
    set(CLOUD_BASE, cloudBase);
    set(CLOUD_SHADOW, cloudShadow);

    starAlpha = night * smoothstep(0.3f, 0.9f, 1f - sunIntensity);
    moonAlpha = night;
  }

  public float colorOf(int channel, int rgb) {
    return colors[channel * 3 + rgb];
  }

  private void set(int channel, float r, float g, float b) {
    colors[channel * 3] = r;
    colors[channel * 3 + 1] = g;
    colors[channel * 3 + 2] = b;
  }

  private void set(int channel, float[] c) {
    set(channel, c[0], c[1], c[2]);
  }

  private static float[] mix3(float[] a, float[] b, float t) {
    float[] out = new float[3];
    out[0] = lerp(a[0], b[0], t);
    out[1] = lerp(a[1], b[1], t);
    out[2] = lerp(a[2], b[2], t);
    return out;
  }

  private static float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }

  private static float smoothstep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
    return t * t * (3f - 2f * t);
  }

  private static float clamp(float v, float lo, float hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }
}
