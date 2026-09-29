package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshBuilder;
import com.poorfarmer.core.model.MeshGeometry;

public final class TehranCity {

  public enum PrologueKind {
    COMPANY_OFFICE, STREET, BACHELOR_APT, TOWER_A, TOWER_B, TOWER_C
  }

  private static final float[][] POSITIONS = {
      {60f, -66f}, {75f, -78f}, {52f, -82f}, {84f, -66f}, {88f, -84f}, {66f, -92f}};

  private static final float[] URBAN_PAD = {0.42f, 0.42f, 0.45f};
  private static final float[] ASPHALT = {0.22f, 0.22f, 0.24f};
  private static final float[] LINE_WHITE = {0.85f, 0.85f, 0.8f};
  private static final float[] SIDEWALK = {0.52f, 0.52f, 0.55f};
  private static final float[] CONCRETE = {0.55f, 0.55f, 0.58f};
  private static final float[] CONCRETE_DARK = {0.38f, 0.38f, 0.42f};
  private static final float[] OFFICE_GLASS = {0.45f, 0.65f, 0.8f};
  private static final float[] OFFICE_GLASS_DARK = {0.25f, 0.4f, 0.55f};
  private static final float[] COMPANY_SIGN = {0.15f, 0.3f, 0.55f};
  private static final float[] TOWER_WALL = {0.6f, 0.58f, 0.55f};
  private static final float[] TOWER_GLASS = {0.5f, 0.7f, 0.82f};
  private static final float[] TANK = {0.45f, 0.4f, 0.38f};
  private static final float[] APT_WALL = {0.72f, 0.68f, 0.6f};
  private static final float[] CAFE_WOOD = {0.55f, 0.42f, 0.28f};
  private static final float[] CAFE_CANOPY = {0.65f, 0.25f, 0.25f};
  private static final float[] DOOR = {0.35f, 0.25f, 0.16f};
  private static final float[] GLASS = {0.55f, 0.72f, 0.85f};

  private final float[] baseHeights = new float[PrologueKind.values().length];
  private final MeshGeometry[] cache = new MeshGeometry[PrologueKind.values().length];

  public TehranCity(Terrain terrain) {
    for (PrologueKind kind : PrologueKind.values()) {
      baseHeights[kind.ordinal()] = terrain.heightAt(positionX(kind), positionZ(kind));
    }
  }

  public static float positionX(PrologueKind kind) {
    return POSITIONS[kind.ordinal()][0];
  }

  public static float positionZ(PrologueKind kind) {
    return POSITIONS[kind.ordinal()][1];
  }

  public float baseHeight(PrologueKind kind) {
    return baseHeights[kind.ordinal()];
  }

  public MeshGeometry buildingMesh(PrologueKind kind) {
    int i = kind.ordinal();
    if (cache[i] == null) {
      MeshGeometry[] parts = buildKind(kind);
      MeshGeometry merged = MeshBuilder.merge(parts);
      merged.translate(positionX(kind), baseHeights[i] + 0.15f, positionZ(kind));
      cache[i] = merged;
    }
    return cache[i];
  }

  public MeshGeometry allMesh() {
    MeshGeometry[] parts = new MeshGeometry[PrologueKind.values().length];
    for (PrologueKind kind : PrologueKind.values()) {
      parts[kind.ordinal()] = buildingMesh(kind);
    }
    return MeshBuilder.merge(parts);
  }

  private static MeshGeometry[] buildKind(PrologueKind kind) {
    switch (kind) {
      case COMPANY_OFFICE:
        return new MeshGeometry[]{
            pad(6.5f),
            part(MeshBuilder.box(8f, 4.5f, 6f), CONCRETE, 0f, 2.25f, 0f),
            part(MeshBuilder.box(8.3f, 0.12f, 6.3f), CONCRETE_DARK, 0f, 4.56f, 0f),
            part(MeshBuilder.box(6.5f, 3.2f, 0.06f), OFFICE_GLASS, 0f, 2.0f, 3.0f),
            part(MeshBuilder.box(1.4f, 2.2f, 0.08f), OFFICE_GLASS_DARK, 0f, 1.1f, 3.03f),
            part(MeshBuilder.box(3.2f, 0.1f, 1.6f), CONCRETE_DARK, 0f, 2.7f, 3.7f),
            part(MeshBuilder.cylinder(0.08f, 2.7f, 8, false, false), CONCRETE_DARK, -1.3f, 1.35f, 4.3f),
            part(MeshBuilder.cylinder(0.08f, 2.7f, 8, false, false), CONCRETE_DARK, 1.3f, 1.35f, 4.3f),
            part(MeshBuilder.box(4f, 0.6f, 0.1f), COMPANY_SIGN, 0f, 4.0f, 3.05f),
            part(MeshBuilder.box(3.6f, 0.15f, 1.4f), SIDEWALK, 0f, 0.075f, 3.7f),
            part(MeshBuilder.box(3f, 0.15f, 1f), SIDEWALK, 0f, 0.225f, 3.5f)};
      case STREET: {
        MeshGeometry[] parts = new MeshGeometry[26];
        int n = 0;
        parts[n++] = part(MeshBuilder.box(17f, 0.12f, 10f), URBAN_PAD, 0f, 0.06f, 0f);
        parts[n++] = part(MeshBuilder.box(14f, 0.05f, 4f), ASPHALT, 0f, 0.145f, 0f);
        for (int d = 0; d < 4; d++) {
          parts[n++] = part(MeshBuilder.box(0.6f, 0.02f, 0.12f), LINE_WHITE, (d - 1.5f) * 3f, 0.175f, 0f);
        }
        parts[n++] = part(MeshBuilder.box(14f, 0.08f, 1.2f), SIDEWALK, 0f, 0.16f, 2.6f);
        parts[n++] = part(MeshBuilder.box(14f, 0.08f, 1.2f), SIDEWALK, 0f, 0.16f, -2.6f);
        parts[n++] = part(MeshBuilder.box(4.5f, 5.5f, 4f), TOWER_WALL, -3f, 2.875f, 4.2f);
        parts[n++] = part(MeshBuilder.box(4.7f, 0.1f, 4.2f), CONCRETE_DARK, -3f, 5.75f, 4.2f);
        parts[n++] = part(MeshBuilder.box(0.9f, 1.8f, 0.08f), DOOR, -3f, 0.9f, 2.2f);
        for (int row = 0; row < 3; row++) {
          for (int col = 0; col < 2; col++) {
            parts[n++] = part(MeshBuilder.box(0.8f, 0.9f, 0.05f), TOWER_GLASS,
                -4.4f + col * 2.8f, 1.7f + row * 1.5f, 2.21f);
          }
        }
        parts[n++] = part(MeshBuilder.box(2f, 1f, 1f), CAFE_WOOD, 3f, 0.6f, 3.6f);
        parts[n++] = part(MeshBuilder.cylinder(0.04f, 2.1f, 6, false, false), CAFE_WOOD, 2.2f, 1.05f, 3.1f);
        parts[n++] = part(MeshBuilder.cylinder(0.04f, 2.1f, 6, false, false), CAFE_WOOD, 3.8f, 1.05f, 3.1f);
        parts[n++] = part(MeshBuilder.box(2.4f, 0.05f, 1.4f), CAFE_CANOPY, 3f, 2.1f, 3.5f);
        return java.util.Arrays.copyOf(parts, n);
      }
      case BACHELOR_APT:
        return new MeshGeometry[]{
            pad(4.5f),
            part(MeshBuilder.box(4f, 4.6f, 4f), APT_WALL, 0f, 2.3f, 0f),
            part(MeshBuilder.box(4.2f, 0.1f, 4.2f), CONCRETE_DARK, 0f, 4.65f, 0f),
            part(MeshBuilder.cylinder(0.45f, 0.7f, 10, true, false), TANK, 1f, 5.05f, -0.8f),
            part(MeshBuilder.box(1.5f, 0.07f, 0.9f), CONCRETE_DARK, -1f, 2.5f, 2.45f),
            part(MeshBuilder.box(1.5f, 0.05f, 0.05f), CONCRETE_DARK, -1f, 3.0f, 2.85f),
            part(MeshBuilder.box(0.05f, 0.5f, 0.05f), CONCRETE_DARK, -1.6f, 2.75f, 2.85f),
            part(MeshBuilder.box(0.05f, 0.5f, 0.05f), CONCRETE_DARK, -1f, 2.75f, 2.85f),
            part(MeshBuilder.box(0.05f, 0.5f, 0.05f), CONCRETE_DARK, -0.4f, 2.75f, 2.85f),
            part(MeshBuilder.box(0.8f, 1.6f, 0.05f), GLASS, -1f, 1.7f, 2.01f),
            part(MeshBuilder.box(0.9f, 1.7f, 0.08f), DOOR, 1.1f, 0.85f, 2.0f),
            part(MeshBuilder.box(0.9f, 0.9f, 0.05f), GLASS, 1.1f, 3.2f, 2.01f),
            part(MeshBuilder.box(0.9f, 0.9f, 0.05f), GLASS, -2.01f, 1.5f, 0f),
            part(MeshBuilder.box(0.9f, 0.9f, 0.05f), GLASS, -2.01f, 3.3f, 0f)};
      case TOWER_A:
        return tower(4f, 9f);
      case TOWER_B:
        return tower(4f, 8f);
      case TOWER_C:
        return tower(4f, 10f);
      default:
        throw new IllegalArgumentException("unknown kind " + kind);
    }
  }

  private static MeshGeometry[] tower(float size, float height) {
    int floors = (int) Math.floor(height / 1.8f);
    MeshGeometry[] parts = new MeshGeometry[8 + floors * 4];
    int n = 0;
    parts[n++] = pad(4f);
    parts[n++] = part(MeshBuilder.box(size, height, size), TOWER_WALL, 0f, height / 2f, 0f);
    parts[n++] = part(MeshBuilder.box(size + 0.2f, 0.1f, size + 0.2f), CONCRETE_DARK, 0f, height + 0.05f, 0f);
    parts[n++] = part(MeshBuilder.cylinder(0.4f, 0.6f, 10, true, false), TANK, 0.9f, height + 0.4f, 0.9f);
    parts[n++] = part(MeshBuilder.box(0.9f, 1.8f, 0.08f), DOOR, 0f, 0.9f, size / 2f);
    for (int f = 0; f < floors; f++) {
      for (int col = 0; col < 2; col++) {
        parts[n++] = part(MeshBuilder.box(0.8f, 0.9f, 0.05f), TOWER_GLASS,
            -0.9f + col * 1.8f, 1.4f + f * 1.8f, size / 2f + 0.01f);
        parts[n++] = part(MeshBuilder.box(0.8f, 0.9f, 0.05f), TOWER_GLASS,
            -0.9f + col * 1.8f, 1.4f + f * 1.8f, -size / 2f - 0.01f);
      }
    }
    return java.util.Arrays.copyOf(parts, n);
  }

  private static MeshGeometry pad(float radius) {
    return part(MeshBuilder.cylinder(radius, 0.3f, 14, true, true), URBAN_PAD, 0f, 0.15f, 0f);
  }

  private static MeshGeometry part(MeshGeometry geometry, float[] color, float tx, float ty, float tz) {
    geometry.translate(tx, ty, tz);
    for (int i = 0; i < geometry.vertexCount(); i++) {
      geometry.setVertexColor(i, color[0], color[1], color[2], 1f);
    }
    return geometry;
  }
}
