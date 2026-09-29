package com.poorfarmer.core.world;

import com.poorfarmer.core.Quality;
import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshBuilder;
import com.poorfarmer.core.model.MeshGeometry;

public final class ShadowState {

  public static final float MAX_STRETCH = 14f;
  public static final float MIN_SUN_ELEVATION = 0.12f;
  public static final float BASE_OPACITY = 0.42f;
  public static final float GROUND_OFFSET = 0.02f;

  private ShadowState() {
  }

  public static float offsetAmount(SunState sun, float height) {
    Vec3 d = sun.direction();
    if (d.y < MIN_SUN_ELEVATION) {
      return 0f;
    }
    return clamp(height / d.y, -MAX_STRETCH, MAX_STRETCH);
  }

  public static Vec3 groundOffset(SunState sun, float height) {
    Vec3 d = sun.direction();
    float amount = offsetAmount(sun, height);
    return new Vec3(-amount * d.x, 0f, -amount * d.z);
  }

  public static float opacity(SunState sun) {
    if (sun.isNight()) {
      return 0f;
    }
    float elevation = (float) Math.toRadians(sun.elevationDegrees());
    float falloff = clamp(elevation / 0.5f, 0f, 1f);
    return BASE_OPACITY * (0.4f + 0.6f * falloff) * sun.dayFactor();
  }

  public static float shadowAzimuth(SunState sun) {
    Vec3 d = sun.direction();
    return (float) Math.atan2(d.z, -d.x);
  }

  public static MeshGeometry blob(float x, float groundY, float z, float radius, float height, SunState sun, Quality quality) {
    int segments = quality == Quality.LOW ? 10 : (quality == Quality.MEDIUM ? 16 : 24);
    MeshGeometry g = MeshBuilder.disc(radius, segments);
    float azimuth = 0f;
    float offsetX = 0f;
    float offsetZ = 0f;
    float stretchX = 1f;
    if (quality.atLeast(Quality.MEDIUM)) {
      Vec3 d = sun.direction();
      if (d.y > MIN_SUN_ELEVATION) {
        azimuth = shadowAzimuth(sun);
        Vec3 offset = groundOffset(sun, height);
        offsetX = offset.x;
        offsetZ = offset.z;
        float clamped = offsetAmount(sun, height);
        stretchX = 1f + Math.max(0f, clamped - radius) / (radius * 3f);
      }
    }
    g.scale(stretchX, 1f, 1f);
    g.rotateY(azimuth);
    g.translate(x + offsetX, groundY + GROUND_OFFSET, z + offsetZ);
    float alpha = opacity(sun);
    int vertexCount = g.vertexCount();
    float reachX = radius * stretchX;
    for (int v = 0; v < vertexCount; v++) {
      float px = g.positions[v * 3] - (x + offsetX);
      float pz = g.positions[v * 3 + 2] - (z + offsetZ);
      float dist = (float) Math.sqrt(px * px + pz * pz);
      float falloff = 1f - Math.min(1f, dist / (reachX + 0.001f));
      float a = quality == Quality.LOW ? alpha : alpha * (0.35f + 0.65f * falloff);
      g.setVertexColor(v, 0f, 0f, 0f, a);
    }
    return g;
  }

  private static float clamp(float v, float min, float max) {
    return v < min ? min : (v > max ? max : v);
  }
}
