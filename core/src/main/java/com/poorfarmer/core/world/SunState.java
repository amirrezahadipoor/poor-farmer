package com.poorfarmer.core.world;

import com.poorfarmer.core.math.Vec3;

public final class SunState {

  private static final float[][] SUNRISE_MINUTE = {{390f}, {345f}, {405f}, {450f}};
  private static final float[][] SUNSET_MINUTE = {{1110f}, {1155f}, {1065f}, {1005f}};
  private static final float[][] MAX_ELEVATION_DEG = {{55f}, {65f}, {52f}, {40f}};

  private final float hour;
  private final int season;
  private final float dayFraction;
  private final float elevation;
  private final Vec3 direction;
  private final float[] sunColor;
  private final float sunIntensity;
  private final float[] ambientColor;
  private final float ambientIntensity;
  private final float[] fogColor;
  private final float fogStart;
  private final float fogEnd;

  public SunState(float hour, int season) {
    this.hour = hour;
    this.season = season;
    float sunrise = SUNRISE_MINUTE[season][0] / 60f;
    float sunset = SUNSET_MINUTE[season][0] / 60f;
    float span = sunset - sunrise;
    dayFraction = (hour - sunrise) / span;
    boolean isDay = dayFraction >= 0f && dayFraction <= 1f;
    if (isDay) {
      float maxElevation = (float) Math.toRadians(MAX_ELEVATION_DEG[season][0]);
      elevation = (float) Math.sin(Math.PI * dayFraction) * maxElevation;
      float cosE = (float) Math.cos(elevation);
      float sinE = (float) Math.sin(elevation);
      direction = new Vec3(
          cosE * (float) Math.cos(Math.PI * dayFraction),
          sinE,
          -cosE * (float) Math.sin(Math.PI * dayFraction));
    } else {
      elevation = -0.2f;
      direction = new Vec3(0f, -1f, 0f);
    }
    sunColor = new float[3];
    if (isDay) {
      float warmth = 1f - (float) Math.min(1f, elevation / 0.35f);
      sunColor[0] = 1f;
      sunColor[1] = 0.92f + 0.08f * (1f - warmth);
      sunColor[2] = 0.86f + 0.14f * (1f - warmth);
      float arc = (float) Math.sin(Math.PI * dayFraction);
      sunIntensity = 0.15f + 0.85f * arc * arc;
      float ambientWarmth = 1f - (float) Math.min(1f, elevation / 0.5f);
      ambientColor = new float[]{
          0.42f + 0.1f * (1f - ambientWarmth),
          0.48f + 0.06f * (1f - ambientWarmth),
          0.56f + 0.05f * (1f - ambientWarmth)};
      ambientIntensity = 0.55f + 0.35f * (float) Math.sin(Math.PI * dayFraction);
    } else {
      sunIntensity = 0f;
      sunColor[0] = 0.55f;
      sunColor[1] = 0.6f;
      sunColor[2] = 0.75f;
      ambientColor = new float[]{0.1f, 0.12f, 0.18f};
      ambientIntensity = 0.45f;
    }
    float dayness = isDay ? (float) Math.min(1f, (float) Math.sin(Math.PI * dayFraction) / 0.5f) : 0f;
    fogColor = new float[]{
        lerp(0.06f, 0.72f, dayness),
        lerp(0.08f, 0.76f, dayness),
        lerp(0.12f, 0.82f, dayness)};
    float duskBoost = isDay ? (1f - (float) Math.min(1f, elevation / 0.25f)) : 0f;
    fogStart = lerp(70f, 35f, duskBoost);
    fogEnd = lerp(320f, 180f, duskBoost);
  }

  public float hour() {
    return hour;
  }

  public int season() {
    return season;
  }

  public float elevationDegrees() {
    return (float) Math.toDegrees(elevation);
  }

  public Vec3 direction() {
    return direction;
  }

  public float[] sunColor() {
    return sunColor;
  }

  public float sunIntensity() {
    return sunIntensity;
  }

  public float[] ambientColor() {
    return ambientColor;
  }

  public float ambientIntensity() {
    return ambientIntensity;
  }

  public float[] fogColor() {
    return fogColor;
  }

  public float fogStart() {
    return fogStart;
  }

  public float fogEnd() {
    return fogEnd;
  }

  public boolean isNight() {
    return sunIntensity == 0f;
  }

  public float dayFactor() {
    return isNight() ? 0f : (float) Math.min(1f, sunIntensity);
  }

  private static float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }
}
