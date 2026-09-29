package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.procedural.Noise;

public final class BackgroundMountains {

  public static final float INNER_RADIUS = 180f;
  public static final float OUTER_RADIUS = 420f;
  public static final int ANGULAR_SEGMENTS = 96;
  public static final float MIN_HEIGHT = 25f;
  public static final float MAX_HEIGHT = 150f;

  private static final float[] ROCK_COLOR = {0.34f, 0.40f, 0.52f};

  public static float heightAt(float angle, float radius, Noise noise) {
    float radial = (radius - INNER_RADIUS) / (OUTER_RADIUS - INNER_RADIUS);
    float ridge = noise.ridged2(angle * 1.8f + 3.1f, 0.7f, 3);
    float ridge2 = noise.ridged2(angle * 4.2f + 9.7f, 2.3f, 3) * 0.4f;
    return MIN_HEIGHT + (MAX_HEIGHT - MIN_HEIGHT) * clamp(ridge + ridge2, 0f, 1f) * (0.35f + 0.65f * radial);
  }

  public MeshGeometry toMesh(Noise noise) {
    MeshGeometry g = new MeshGeometry();
    for (int a = 0; a <= ANGULAR_SEGMENTS; a++) {
      float angle = a * 2f * (float) Math.PI / ANGULAR_SEGMENTS;
      float cos = (float) Math.cos(angle);
      float sin = (float) Math.sin(angle);
      for (int r = 0; r < 2; r++) {
        float radius = r == 0 ? INNER_RADIUS : OUTER_RADIUS;
        float h = heightAt(angle, radius, noise);
        g.appendVertex(cos * radius, h, sin * radius, 0f, 1f, 0f, 0f, 0f);
        int v = g.vertexCount() - 1;
        g.setVertexColor(v, ROCK_COLOR[0], ROCK_COLOR[1], ROCK_COLOR[2], 1f);
      }
    }
    for (int a = 0; a < ANGULAR_SEGMENTS; a++) {
      int in0 = 2 * a;
      int out0 = 2 * a + 1;
      int in1 = 2 * a + 2;
      int out1 = 2 * a + 3;
      g.appendIndex(in0);
      g.appendIndex(in1);
      g.appendIndex(out0);
      g.appendIndex(out0);
      g.appendIndex(in1);
      g.appendIndex(out1);
    }
    g.trimToUsed();
    return g;
  }

  private static float clamp(float v, float lo, float hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }
}
