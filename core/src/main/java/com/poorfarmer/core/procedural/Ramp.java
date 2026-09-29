package com.poorfarmer.core.procedural;

public final class Ramp {

  private final float[] positions;
  private final float[] reds;
  private final float[] greens;
  private final float[] blues;

  public Ramp(float[][] stops) {
    int n = stops.length;
    positions = new float[n];
    reds = new float[n];
    greens = new float[n];
    blues = new float[n];
    for (int i = 0; i < n; i++) {
      positions[i] = stops[i][0];
      reds[i] = stops[i][1];
      greens[i] = stops[i][2];
      blues[i] = stops[i][3];
    }
  }

  public void sample(float t, float[] out) {
    if (t <= positions[0]) {
      out[0] = reds[0];
      out[1] = greens[0];
      out[2] = blues[0];
      return;
    }
    int n = positions.length;
    if (t >= positions[n - 1]) {
      out[0] = reds[n - 1];
      out[1] = greens[n - 1];
      out[2] = blues[n - 1];
      return;
    }
    int i = 0;
    while (t > positions[i + 1]) {
      i++;
    }
    float span = positions[i + 1] - positions[i];
    float f = span > 0f ? (t - positions[i]) / span : 0f;
    out[0] = reds[i] + (reds[i + 1] - reds[i]) * f;
    out[1] = greens[i] + (greens[i + 1] - greens[i]) * f;
    out[2] = blues[i] + (blues[i + 1] - blues[i]) * f;
  }
}
