package com.poorfarmer.core.model;

import java.util.List;

public final class MeshBuilder {

  public static MeshGeometry box(float sizeX, float sizeY, float sizeZ) {
    MeshGeometry g = new MeshGeometry();
    float x = sizeX / 2f;
    float y = sizeY / 2f;
    float z = sizeZ / 2f;
    face(g, -x, -y, -z, x, -y, -z, x, -y, z, -x, -y, z, 0f, -1f, 0f);
    face(g, -x, y, -z, -x, y, z, x, y, z, x, y, -z, 0f, 1f, 0f);
    face(g, -x, -y, -z, -x, y, -z, x, y, -z, x, -y, -z, 0f, 0f, -1f);
    face(g, x, -y, -z, x, y, -z, x, y, z, x, -y, z, 1f, 0f, 0f);
    face(g, x, -y, z, x, y, z, -x, y, z, -x, -y, z, 0f, 0f, 1f);
    face(g, -x, -y, z, -x, y, z, -x, y, -z, -x, -y, -z, -1f, 0f, 0f);
    g.trimToUsed();
    return g;
  }

  private static void face(MeshGeometry g,
                           float ax, float ay, float az,
                           float bx, float by, float bz,
                           float cx, float cy, float cz,
                           float dx, float dy, float dz,
                           float nx, float ny, float nz) {
    int a = g.vertexCount();
    g.appendVertex(ax, ay, az, nx, ny, nz, 0f, 1f);
    g.appendVertex(bx, by, bz, nx, ny, nz, 1f, 1f);
    g.appendVertex(cx, cy, cz, nx, ny, nz, 1f, 0f);
    g.appendVertex(dx, dy, dz, nx, ny, nz, 0f, 0f);
    g.appendIndex(a);
    g.appendIndex(a + 1);
    g.appendIndex(a + 2);
    g.appendIndex(a);
    g.appendIndex(a + 2);
    g.appendIndex(a + 3);
  }

  public static MeshGeometry cylinder(float radius, float height, int radialSegments, boolean capTop, boolean capBottom) {
    MeshGeometry g = new MeshGeometry();
    int base = g.vertexCount();
    for (int i = 0; i < radialSegments; i++) {
      float angle0 = (float) i / radialSegments * (float) Math.PI * 2f;
      float angle1 = (float) (i + 1) / radialSegments * (float) Math.PI * 2f;
      float c0 = (float) Math.cos(angle0);
      float s0 = (float) Math.sin(angle0);
      float c1 = (float) Math.cos(angle1);
      float s1 = (float) Math.sin(angle1);
      g.appendVertex(c0 * radius, -height / 2f, s0 * radius, c0, 0f, s0, (float) i / radialSegments, 0f);
      g.appendVertex(c0 * radius, height / 2f, s0 * radius, c0, 0f, s0, (float) i / radialSegments, 1f);
      g.appendVertex(c1 * radius, height / 2f, s1 * radius, c1, 0f, s1, (float) (i + 1) / radialSegments, 1f);
      g.appendVertex(c1 * radius, -height / 2f, s1 * radius, c1, 0f, s1, (float) (i + 1) / radialSegments, 0f);
      int v = base + i * 4;
      g.appendIndex(v);
      g.appendIndex(v + 1);
      g.appendIndex(v + 2);
      g.appendIndex(v);
      g.appendIndex(v + 2);
      g.appendIndex(v + 3);
    }
    if (capTop) {
      cap(g, radius, height / 2f, 1f);
    }
    if (capBottom) {
      cap(g, radius, -height / 2f, -1f);
    }
    g.trimToUsed();
    return g;
  }

  private static void cap(MeshGeometry g, float radius, float y, float normalY) {
    int center = g.vertexCount();
    g.appendVertex(0f, y, 0f, 0f, normalY, 0f, 0.5f, 0.5f);
    int ringStart = g.vertexCount();
    int segments = 32;
    for (int i = 0; i <= segments; i++) {
      float angle = (float) i / segments * (float) Math.PI * 2f;
      float c = (float) Math.cos(angle);
      float s = (float) Math.sin(angle);
      g.appendVertex(c * radius, y, s * radius, 0f, normalY, 0f, 0.5f + c * 0.5f, 0.5f + s * 0.5f);
    }
    for (int i = 0; i < segments; i++) {
      if (normalY > 0f) {
        g.appendIndex(center);
        g.appendIndex(ringStart + i + 1);
        g.appendIndex(ringStart + i);
      } else {
        g.appendIndex(center);
        g.appendIndex(ringStart + i);
        g.appendIndex(ringStart + i + 1);
      }
    }
  }

  public static MeshGeometry cone(float radius, float height, int radialSegments) {
    MeshGeometry g = new MeshGeometry();
    for (int i = 0; i < radialSegments; i++) {
      float angle0 = (float) i / radialSegments * (float) Math.PI * 2f;
      float angle1 = (float) (i + 1) / radialSegments * (float) Math.PI * 2f;
      float c0 = (float) Math.cos(angle0);
      float s0 = (float) Math.sin(angle0);
      float c1 = (float) Math.cos(angle1);
      float s1 = (float) Math.sin(angle1);
      float slope = (float) Math.sqrt(radius * radius + height * height);
      float ny = radius / slope;
      float nRadial = height / slope;
      g.appendVertex(c0 * radius, -height / 2f, s0 * radius, c0 * nRadial, ny, s0 * nRadial, (float) i / radialSegments, 0f);
      g.appendVertex(c1 * radius, -height / 2f, s1 * radius, c1 * nRadial, ny, s1 * nRadial, (float) (i + 1) / radialSegments, 0f);
      g.appendVertex(0f, height / 2f, 0f, 0f, 1f, 0f, 0.5f, 1f);
      int v = g.vertexCount() - 3;
      g.appendIndex(v);
      g.appendIndex(v + 2);
      g.appendIndex(v + 1);
    }
    g.trimToUsed();
    return g;
  }

  public static MeshGeometry sphere(float radius, int widthSegments, int heightSegments) {
    MeshGeometry g = new MeshGeometry();
    for (int iy = 0; iy <= heightSegments; iy++) {
      float v = (float) iy / heightSegments;
      float phi = v * (float) Math.PI;
      for (int ix = 0; ix <= widthSegments; ix++) {
        float u = (float) ix / widthSegments;
        float theta = u * (float) Math.PI * 2f;
        float x;
        float y;
        float z;
        if (iy == 0) {
          x = 0f;
          y = 1f;
          z = 0f;
        } else if (iy == heightSegments) {
          x = 0f;
          y = -1f;
          z = 0f;
        } else {
          x = (float) Math.sin(phi) * (float) Math.cos(theta);
          y = (float) Math.cos(phi);
          z = (float) Math.sin(phi) * (float) Math.sin(theta);
        }
        g.appendVertex(x * radius, y * radius, z * radius, x, y, z, u, v);
      }
    }
    for (int iy = 0; iy < heightSegments; iy++) {
      for (int ix = 0; ix < widthSegments; ix++) {
        int a = iy * (widthSegments + 1) + ix;
        int b = a + widthSegments + 1;
        g.appendIndex(a);
        g.appendIndex(a + 1);
        g.appendIndex(b);
        g.appendIndex(a + 1);
        g.appendIndex(b + 1);
        g.appendIndex(b);
      }
    }
    g.trimToUsed();
    return g;
  }

  public static MeshGeometry lathe(float[][] profile, int segments) {
    MeshGeometry g = new MeshGeometry();
    int points = profile.length;
    int columns = segments + 1;
    for (int i = 0; i < columns; i++) {
      float angle = (float) i / segments * (float) Math.PI * 2f;
      float c = (float) Math.cos(angle);
      float s = (float) Math.sin(angle);
      for (int j = 0; j < points; j++) {
        float r = profile[j][0];
        float y = profile[j][1];
        float dr;
        float dy;
        if (points == 2) {
          dr = profile[1][0] - profile[0][0];
          dy = profile[1][1] - profile[0][1];
        } else if (j == points - 1) {
          dr = profile[j][0] - profile[j - 1][0];
          dy = profile[j][1] - profile[j - 1][1];
        } else if (j == 0) {
          dr = profile[1][0] - profile[0][0];
          dy = profile[1][1] - profile[0][1];
        } else {
          dr = profile[j + 1][0] - profile[j - 1][0];
          dy = profile[j + 1][1] - profile[j - 1][1];
        }
        float nx = -c * dy;
        float ny = dr;
        float nz = -s * dy;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-8f) {
          nx = c;
          ny = 0f;
          nz = s;
          len = 1f;
        }
        g.appendVertex(r * c, y, r * s, nx / len, ny / len, nz / len, (float) i / segments, (float) j / (points - 1));
      }
    }
    for (int i = 0; i < segments; i++) {
      for (int j = 0; j < points - 1; j++) {
        int a = i * points + j;
        int b = a + 1;
        int c = a + points;
        int d = c + 1;
        g.appendIndex(a);
        g.appendIndex(c);
        g.appendIndex(b);
        g.appendIndex(c);
        g.appendIndex(d);
        g.appendIndex(b);
      }
    }
    g.trimToUsed();
    return g;
  }

  public static MeshGeometry extrude(float[][] outline, float height) {
    MeshGeometry g = new MeshGeometry();
    int n = outline.length;
    float[] xs = new float[n];
    float[] zs = new float[n];
    for (int i = 0; i < n; i++) {
      xs[i] = outline[i][0];
      zs[i] = outline[i][1];
    }
    if (PolygonTriangulator.signedArea(xs, zs) < 0f) {
      for (int i = 0; i < n / 2; i++) {
        int j = n - 1 - i;
        float tx = xs[i];
        xs[i] = xs[j];
        xs[j] = tx;
        float tz = zs[i];
        zs[i] = zs[j];
        zs[j] = tz;
      }
    }
    for (int i = 0; i < n; i++) {
      int j = (i + 1) % n;
      float dx = xs[j] - xs[i];
      float dz = zs[j] - zs[i];
      float len = (float) Math.sqrt(dx * dx + dz * dz);
      float nx = dz / len;
      float nz = -dx / len;
      int a = g.vertexCount();
      g.appendVertex(xs[i], 0f, zs[i], nx, 0f, nz, 0f, 0f);
      g.appendVertex(xs[j], 0f, zs[j], nx, 0f, nz, 1f, 0f);
      g.appendVertex(xs[j], height, zs[j], nx, 0f, nz, 1f, 1f);
      g.appendVertex(xs[i], height, zs[i], nx, 0f, nz, 0f, 1f);
      g.appendIndex(a);
      g.appendIndex(a + 2);
      g.appendIndex(a + 1);
      g.appendIndex(a);
      g.appendIndex(a + 3);
      g.appendIndex(a + 2);
    }
    List<int[]> triangles = PolygonTriangulator.triangulate(xs, zs);
    capFromOutline(g, xs, zs, height, 1f, triangles);
    capFromOutline(g, xs, zs, 0f, -1f, triangles);
    g.trimToUsed();
    return g;
  }

  private static void capFromOutline(MeshGeometry g, float[] xs, float[] zs, float y, float normalY, List<int[]> triangles) {
    int start = g.vertexCount();
    for (int i = 0; i < xs.length; i++) {
      g.appendVertex(xs[i], y, zs[i], 0f, normalY, 0f, xs[i], zs[i]);
    }
    for (int[] t : triangles) {
      if (normalY > 0f) {
        g.appendIndex(start + t[0]);
        g.appendIndex(start + t[2]);
        g.appendIndex(start + t[1]);
      } else {
        g.appendIndex(start + t[0]);
        g.appendIndex(start + t[1]);
        g.appendIndex(start + t[2]);
      }
    }
  }

  public static MeshGeometry merge(MeshGeometry[] parts) {
    MeshGeometry result = new MeshGeometry();
    for (MeshGeometry part : parts) {
      result.absorb(part);
    }
    result.trimToUsed();
    return result;
  }

  private MeshBuilder() {
  }
}
