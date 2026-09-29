package com.poorfarmer.core.world;

import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.procedural.Noise;

public final class Terrain {

  public static final float WORLD_HALF = 120f;
  public static final float FARM_FLAT_RADIUS = 36f;
  public static final float FARM_FLATTEN_RADIUS = FARM_FLAT_RADIUS + 12f;
  public static final float FARM_MAX_HEIGHT = 1.2f;
  public static final float VALLEY_DEPTH = 4f;
  public static final float VALLEY_HALF_WIDTH = 24f;

  private final long seed;
  private final float[] heights;
  private final int grid;

  public Terrain(long seed) {
    this.seed = seed;
    this.grid = (int) (WORLD_HALF * 2f) + 1;
    Noise noise = new Noise(seed);
    heights = new float[grid * grid];
    for (int iz = 0; iz < grid; iz++) {
      for (int ix = 0; ix < grid; ix++) {
        float x = -WORLD_HALF + ix;
        float z = -WORLD_HALF + iz;
        heights[iz * grid + ix] = sampleHeight(noise, x, z);
      }
    }
  }

  public long seed() {
    return seed;
  }

  public int gridResolution() {
    return grid;
  }

  public float heightAt(float x, float z) {
    float fx = clamp(x + WORLD_HALF, 0f, WORLD_HALF * 2f - 1f);
    float fz = clamp(z + WORLD_HALF, 0f, WORLD_HALF * 2f - 1f);
    int ix = (int) fx;
    int iz = (int) fz;
    float tx = fx - ix;
    float tz = fz - iz;
    float h00 = heights[iz * grid + ix];
    float h10 = heights[iz * grid + ix + 1];
    float h01 = heights[(iz + 1) * grid + ix];
    float h11 = heights[(iz + 1) * grid + ix + 1];
    float top = h00 + (h10 - h00) * tx;
    float bottom = h01 + (h11 - h01) * tx;
    return top + (bottom - top) * tz;
  }

  static float valleyCenterZ(float x) {
    return (float) Math.sin(x * 0.011f) * 10f;
  }

  static float sampleHeight(Noise noise, float x, float z) {
    float rolling = noise.fbm2(x * 0.012f, z * 0.012f, 4, 0.5f, 2f);
    float detail = noise.fbm2(x * 0.05f, z * 0.05f, 3, 0.5f, 2f);
    float dx = x / WORLD_HALF;
    float dz = z / WORLD_HALF;
    float edge = Math.max(Math.abs(dx), Math.abs(dz));
    float foothill = smoothstep(0.45f, 1f, edge) * 26f;
    float ridge = smoothstep(0.7f, 1f, edge) * noise.fbm2(x * 0.02f, z * 0.02f, 3, 0.5f, 2f) * 14f;
    float vz = Math.abs(z - valleyCenterZ(x));
    float vt = clamp(vz / VALLEY_HALF_WIDTH, 0f, 1f);
    float valley = -VALLEY_DEPTH * (1f - smoothstep(0f, 1f, vt));
    float base = rolling * 7f + detail * 1.2f;
    float height = base + foothill + ridge + valley;
    float dist = (float) Math.sqrt(x * x + z * z);
    float flatten = 1f - smoothstep(FARM_FLAT_RADIUS, FARM_FLATTEN_RADIUS, dist);
    height = height * (1f - flatten) + FARM_MAX_HEIGHT * flatten;
    height -= River.bedDepthAt(x, z);
    return height;
  }

  public Vec3 normalAt(float x, float z) {
    float e = 0.5f;
    float hL = heightAt(x - e, z);
    float hR = heightAt(x + e, z);
    float hD = heightAt(x, z - e);
    float hU = heightAt(x, z + e);
    Vec3 n = new Vec3(hL - hR, 2f * e, hD - hU);
    n.normalize();
    return n;
  }

  public float slopeDegreesAt(float x, float z) {
    Vec3 n = normalAt(x, z);
    return (float) (90.0 - Math.toDegrees(Math.acos(Math.max(-1f, Math.min(1f, n.y)))));
  }

  public void colorAt(float x, float z, float[] out) {
    float h = heightAt(x, z);
    float slope = slopeDegreesAt(x, z);
    float dryness = smoothstep(4f, 20f, h);
    float rock = smoothstep(18f, 34f, h);
    float rockySlope = smoothstep(28f, 42f, slope);
    float grassR = lerp(0.24f, 0.42f, dryness);
    float grassG = lerp(0.42f, 0.45f, dryness);
    float grassB = lerp(0.18f, 0.28f, dryness);
    float rockR = 0.45f;
    float rockG = 0.42f;
    float rockB = 0.38f;
    float dirtR = 0.42f;
    float dirtG = 0.32f;
    float dirtB = 0.22f;
    float r = grassR;
    float g = grassG;
    float b = grassB;
    r = lerp(r, dirtR, rockySlope * 0.6f);
    g = lerp(g, dirtG, rockySlope * 0.6f);
    b = lerp(b, dirtB, rockySlope * 0.6f);
    float rockMix = Math.max(rock, rockySlope * 0.8f);
    r = lerp(r, rockR, rockMix);
    g = lerp(g, rockG, rockMix);
    b = lerp(b, rockB, rockMix);
    out[0] = r;
    out[1] = g;
    out[2] = b;
  }

  public MeshGeometry toMesh(float step) {
    int cells = (int) (WORLD_HALF * 2f / step);
    MeshGeometry g = new MeshGeometry();
    for (int iz = 0; iz <= cells; iz++) {
      for (int ix = 0; ix <= cells; ix++) {
        float x = -WORLD_HALF + ix * step;
        float z = -WORLD_HALF + iz * step;
        float y = heightAt(x, z);
        Vec3 normal = normalAt(x, z);
        g.appendVertex(x, y, z, normal.x, normal.y, normal.z, x * 0.05f, z * 0.05f);
        float[] c = new float[3];
        colorAt(x, z, c);
        int vertex = g.vertexCount() - 1;
        g.setVertexColor(vertex, c[0], c[1], c[2], 1f);
      }
    }
    for (int iz = 0; iz < cells; iz++) {
      for (int ix = 0; ix < cells; ix++) {
        int a = iz * (cells + 1) + ix;
        int b = a + 1;
        int c = a + (cells + 1);
        int d = c + 1;
        g.appendIndex(a);
        g.appendIndex(c);
        g.appendIndex(b);
        g.appendIndex(b);
        g.appendIndex(c);
        g.appendIndex(d);
      }
    }
    g.trimToUsed();
    return g;
  }

  static float smoothstep(float a, float b, float x) {
    float f = clamp((x - a) / (b - a), 0f, 1f);
    return f * f * (3f - 2f * f);
  }

  static float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }

  private static float clamp(float v, float min, float max) {
    return v < min ? min : (v > max ? max : v);
  }
}
