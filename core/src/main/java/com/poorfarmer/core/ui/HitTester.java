package com.poorfarmer.core.ui;

import java.util.ArrayList;
import java.util.List;

public final class HitTester {

  private final List<HitTarget> targets = new ArrayList<>();

  public void add(HitTarget target) {
    targets.add(target);
  }

  public void remove(HitTarget target) {
    targets.remove(target);
  }

  public void clear() {
    targets.clear();
  }

  public int size() {
    return targets.size();
  }

  public HitTarget nearest(float x, float z) {
    HitTarget best = null;
    float bestDistance = Float.MAX_VALUE;
    for (HitTarget target : targets) {
      float dx = target.position().x - x;
      float dz = target.position().z - z;
      float distance = (float) Math.sqrt(dx * dx + dz * dz);
      if (distance <= target.radius() && distance < bestDistance) {
        best = target;
        bestDistance = distance;
      }
    }
    return best;
  }

  public static int cellIndex(float x, float z, float originX, float originZ, float cellSize, int columns) {
    int col = (int) Math.floor((x - originX) / cellSize);
    int row = (int) Math.floor((z - originZ) / cellSize);
    if (col < 0 || row < 0 || col >= columns || row >= columns) {
      return -1;
    }
    return row * columns + col;
  }

  public static int cellCoordinate(float value, float origin, float cellSize) {
    return (int) Math.floor((value - origin) / cellSize);
  }
}
