package com.poorfarmer.core.model;

import java.util.ArrayList;
import java.util.List;

public final class PolygonTriangulator {

  public static List<int[]> triangulate(float[] xs, float[] zs) {
    int n = xs.length;
    List<Integer> remaining = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      remaining.add(i);
    }
    List<int[]> triangles = new ArrayList<>();
    int guard = n * n;
    while (remaining.size() > 3 && guard-- > 0) {
      boolean earFound = false;
      int m = remaining.size();
      for (int i = 0; i < m && !earFound; i++) {
        int prev = remaining.get((i - 1 + m) % m);
        int current = remaining.get(i);
        int next = remaining.get((i + 1) % m);
        if (!isEar(xs, zs, prev, current, next, remaining)) {
          continue;
        }
        triangles.add(new int[]{prev, current, next});
        remaining.remove(i);
        earFound = true;
      }
      if (!earFound) {
        break;
      }
    }
    if (remaining.size() == 3) {
      triangles.add(new int[]{remaining.get(0), remaining.get(1), remaining.get(2)});
    }
    return triangles;
  }

  private static boolean isEar(float[] xs, float[] zs, int a, int b, int c, List<Integer> remaining) {
    float cross = (xs[b] - xs[a]) * (zs[c] - zs[a]) - (zs[b] - zs[a]) * (xs[c] - xs[a]);
    if (cross <= 0f) {
      return false;
    }
    for (int index : remaining) {
      if (index == a || index == b || index == c) {
        continue;
      }
      if (pointInTriangle(xs[index], zs[index], xs[a], zs[a], xs[b], zs[b], xs[c], zs[c])) {
        return false;
      }
    }
    return true;
  }

  private static boolean pointInTriangle(float px, float pz, float ax, float az, float bx, float bz, float cx, float cz) {
    float d1 = sign(px, pz, ax, az, bx, bz);
    float d2 = sign(px, pz, bx, bz, cx, cz);
    float d3 = sign(px, pz, cx, cz, ax, az);
    boolean hasNegative = d1 < 0f || d2 < 0f || d3 < 0f;
    boolean hasPositive = d1 > 0f || d2 > 0f || d3 > 0f;
    return !(hasNegative && hasPositive);
  }

  private static float sign(float px, float pz, float ax, float az, float bx, float bz) {
    return (px - bx) * (az - bz) - (ax - bx) * (pz - bz);
  }

  public static float signedArea(float[] xs, float[] zs) {
    float area = 0f;
    int n = xs.length;
    for (int i = 0; i < n; i++) {
      int j = (i + 1) % n;
      area += xs[i] * zs[j] - xs[j] * zs[i];
    }
    return area / 2f;
  }

  private PolygonTriangulator() {
  }
}
