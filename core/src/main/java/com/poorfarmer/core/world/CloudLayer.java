package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshGeometry;

public final class CloudLayer {

  public static final float SIZE = 500f;
  public static final float HEIGHT = 115f;
  public static final int SEGMENTS = 24;

  public MeshGeometry toMesh() {
    float half = SIZE / 2f;
    float step = SIZE / SEGMENTS;
    MeshGeometry g = new MeshGeometry();
    for (int iz = 0; iz <= SEGMENTS; iz++) {
      for (int ix = 0; ix <= SEGMENTS; ix++) {
        float x = -half + ix * step;
        float z = -half + iz * step;
        g.appendVertex(x, HEIGHT, z, 0f, 1f, 0f, ix / (float) SEGMENTS, iz / (float) SEGMENTS);
      }
    }
    for (int iz = 0; iz < SEGMENTS; iz++) {
      for (int ix = 0; ix < SEGMENTS; ix++) {
        int a = iz * (SEGMENTS + 1) + ix;
        int b = a + 1;
        int c = a + (SEGMENTS + 1);
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
}
