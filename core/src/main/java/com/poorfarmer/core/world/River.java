package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshGeometry;

public final class River {

  public static final float WIDTH_BASE = 5.5f;
  public static final float WIDTH_WAVE = 1.2f;
  public static final float BED_DEPTH = 1.2f;
  public static final float WATER_ABOVE_BED = 0.55f;

  private static final float[] RIVER_COLOR = {0.18f, 0.36f, 0.55f, 1f};

  public static float centerZ(float x) {
    return Terrain.valleyCenterZ(x);
  }

  public static float halfWidth(float x) {
    return (WIDTH_BASE + WIDTH_WAVE * (float) Math.sin(x * 0.05f + 1.7f)) / 2f;
  }

  public static float bedDepthAt(float x, float z) {
    float t = Math.min(1f, Math.abs(z - centerZ(x)) / halfWidth(x));
    return BED_DEPTH * (1f - smoothstep(0f, 1f, t));
  }

  public float surfaceY(Terrain terrain, float x) {
    return terrain.heightAt(x, centerZ(x)) + WATER_ABOVE_BED;
  }

  public MeshGeometry toMesh(Terrain terrain, float step) {
    int cells = (int) (Terrain.WORLD_HALF * 2f / step);
    MeshGeometry g = new MeshGeometry();
    for (int i = 0; i <= cells; i++) {
      float x = -Terrain.WORLD_HALF + i * step;
      float cz = centerZ(x);
      float hw = halfWidth(x);
      float y = surfaceY(terrain, x);
      float u = x * 0.08f;
      g.appendVertex(x, y, cz - hw, 0f, 1f, 0f, u, 0f);
      g.appendVertex(x, y, cz + hw, 0f, 1f, 0f, u, 1f);
      g.setVertexColor(2 * i, RIVER_COLOR[0], RIVER_COLOR[1], RIVER_COLOR[2], RIVER_COLOR[3]);
      g.setVertexColor(2 * i + 1, RIVER_COLOR[0], RIVER_COLOR[1], RIVER_COLOR[2], RIVER_COLOR[3]);
    }
    for (int i = 0; i < cells; i++) {
      int a = 2 * i;
      int b = 2 * i + 1;
      int c = 2 * i + 2;
      int d = 2 * i + 3;
      g.appendIndex(a);
      g.appendIndex(b);
      g.appendIndex(c);
      g.appendIndex(b);
      g.appendIndex(d);
      g.appendIndex(c);
    }
    g.trimToUsed();
    return g;
  }

  private static float smoothstep(float edge0, float edge1, float x) {
    float t = Math.min(1f, Math.max(0f, (x - edge0) / (edge1 - edge0)));
    return t * t * (3f - 2f * t);
  }
}
