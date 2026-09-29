package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshBuilder;
import com.poorfarmer.core.model.MeshGeometry;

public final class Town {

  public enum TownKind {
    BAZAAR, BANK_OFTAB, PARS_PACK, LAW_OFFICE, SHOP_A, SHOP_B
  }

  private static final float[][] POSITIONS = {
      {-78f, 18f}, {-60f, -4f}, {-88f, -12f}, {-58f, 26f}, {-70f, 40f}, {-56f, 44f}};

  private static final float[] PAD = {0.45f, 0.38f, 0.30f};
  private static final float[] STALL_WOOD = {0.55f, 0.42f, 0.28f};
  private static final float[] CANOPY_RED = {0.70f, 0.25f, 0.20f};
  private static final float[] CANOPY_WHITE = {0.85f, 0.83f, 0.78f};
  private static final float[] STONE = {0.62f, 0.60f, 0.56f};
  private static final float[] STONE_DARK = {0.45f, 0.44f, 0.42f};
  private static final float[] BANK_SIGN = {0.20f, 0.35f, 0.50f};
  private static final float[] CONCRETE = {0.58f, 0.56f, 0.52f};
  private static final float[] ROOF_DARK = {0.35f, 0.36f, 0.38f};
  private static final float[] PACK_SIGN = {0.30f, 0.50f, 0.30f};
  private static final float[] PLASTER = {0.80f, 0.76f, 0.66f};
  private static final float[] LAW_AWNING = {0.40f, 0.45f, 0.50f};
  private static final float[] SHOP_A_WALL = {0.68f, 0.58f, 0.45f};
  private static final float[] SHOP_B_WALL = {0.62f, 0.55f, 0.50f};
  private static final float[] AWNING_TEAL = {0.30f, 0.50f, 0.45f};
  private static final float[] DOOR = {0.35f, 0.25f, 0.16f};
  private static final float[] GLASS = {0.55f, 0.72f, 0.85f};

  private final float[] baseHeights = new float[TownKind.values().length];
  private final MeshGeometry[] cache = new MeshGeometry[TownKind.values().length];

  public Town(Terrain terrain) {
    for (TownKind kind : TownKind.values()) {
      baseHeights[kind.ordinal()] = terrain.heightAt(positionX(kind), positionZ(kind));
    }
  }

  public static float positionX(TownKind kind) {
    return POSITIONS[kind.ordinal()][0];
  }

  public static float positionZ(TownKind kind) {
    return POSITIONS[kind.ordinal()][1];
  }

  public float baseHeight(TownKind kind) {
    return baseHeights[kind.ordinal()];
  }

  public MeshGeometry buildingMesh(TownKind kind) {
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
    MeshGeometry[] parts = new MeshGeometry[TownKind.values().length];
    for (TownKind kind : TownKind.values()) {
      parts[kind.ordinal()] = buildingMesh(kind);
    }
    return MeshBuilder.merge(parts);
  }

  private static MeshGeometry[] buildKind(TownKind kind) {
    switch (kind) {
      case BAZAAR: {
        MeshGeometry[] parts = new MeshGeometry[40];
        int n = 0;
        parts[n++] = pad(7f);
        for (int s = 0; s < 5; s++) {
          float sx = (s - 2) * 3.2f;
          float sz = s < 3 ? 2.4f : (s == 3 ? -1.2f : -4.8f);
          float[] canopy = s % 2 == 0 ? CANOPY_RED : CANOPY_WHITE;
          parts[n++] = part(MeshBuilder.box(1.6f, 0.9f, 0.9f), STALL_WOOD, sx, 0.45f, sz);
          parts[n++] = part(MeshBuilder.cylinder(0.04f, 2.3f, 6, false, false), STALL_WOOD, sx - 0.7f, 1.15f, sz - 0.5f);
          parts[n++] = part(MeshBuilder.cylinder(0.04f, 2.3f, 6, false, false), STALL_WOOD, sx + 0.7f, 1.15f, sz - 0.5f);
          parts[n++] = part(MeshBuilder.cylinder(0.04f, 2.3f, 6, false, false), STALL_WOOD, sx - 0.7f, 1.15f, sz + 0.5f);
          parts[n++] = part(MeshBuilder.cylinder(0.04f, 2.3f, 6, false, false), STALL_WOOD, sx + 0.7f, 1.15f, sz + 0.5f);
          parts[n++] = part(MeshBuilder.box(1.9f, 0.05f, 1.3f), canopy, sx, 2.35f, sz);
        }
        parts[n++] = part(MeshBuilder.cylinder(0.08f, 2.7f, 8, false, false), STONE_DARK, -3.2f, 1.35f, -8.2f);
        parts[n++] = part(MeshBuilder.cylinder(0.08f, 2.7f, 8, false, false), STONE_DARK, 3.2f, 1.35f, -8.2f);
        parts[n++] = part(MeshBuilder.cylinder(0.08f, 2.7f, 8, false, false), STONE_DARK, -3.2f, 1.35f, 5.8f);
        parts[n++] = part(MeshBuilder.cylinder(0.08f, 2.7f, 8, false, false), STONE_DARK, 3.2f, 1.35f, 5.8f);
        parts[n++] = part(MeshBuilder.box(7f, 0.08f, 15f), CANOPY_WHITE, 0f, 2.75f, -1.2f);
        return java.util.Arrays.copyOf(parts, n);
      }
      case BANK_OFTAB: {
        MeshGeometry pediment = MeshBuilder.extrude(
            new float[][]{{-4.1f, 2.7f}, {4.1f, 2.7f}, {0f, 3.9f}}, 0.3f);
        pediment.rotateX(-(float) Math.PI / 2f);
        return new MeshGeometry[]{
            pad(5.5f),
            part(MeshBuilder.box(8f, 3.2f, 5f), STONE, 0f, 1.6f, 0f),
            part(pediment, STONE_DARK, 0f, 0f, 2.5f),
            part(MeshBuilder.cylinder(0.22f, 2.6f, 10, true, false), STONE, -2.2f, 1.3f, 2.35f),
            part(MeshBuilder.cylinder(0.22f, 2.6f, 10, true, false), STONE, -0.8f, 1.3f, 2.35f),
            part(MeshBuilder.cylinder(0.22f, 2.6f, 10, true, false), STONE, 0.8f, 1.3f, 2.35f),
            part(MeshBuilder.cylinder(0.22f, 2.6f, 10, true, false), STONE, 2.2f, 1.3f, 2.35f),
            part(MeshBuilder.box(3f, 0.5f, 0.1f), BANK_SIGN, 0f, 3.0f, 2.55f),
            part(MeshBuilder.box(1.6f, 2.2f, 0.08f), DOOR, 0f, 1.1f, 2.5f),
            part(MeshBuilder.box(1.2f, 1.0f, 0.06f), GLASS, -3f, 1.8f, 2.5f),
            part(MeshBuilder.box(1.2f, 1.0f, 0.06f), GLASS, 3f, 1.8f, 2.5f)};
      }
      case PARS_PACK:
        return new MeshGeometry[]{
            pad(5f),
            part(MeshBuilder.box(7f, 3f, 4f), CONCRETE, 0f, 1.5f, 0f),
            part(MeshBuilder.box(7.3f, 0.1f, 4.3f), ROOF_DARK, 0f, 3.05f, 0f),
            part(MeshBuilder.box(2.4f, 2.2f, 0.1f), ROOF_DARK, 0f, 1.1f, 2.0f),
            part(MeshBuilder.box(3.4f, 0.5f, 0.1f), PACK_SIGN, 0f, 2.5f, 2.05f),
            part(MeshBuilder.box(1f, 1.6f, 0.08f), DOOR, -2.4f, 0.8f, 2.0f)};
      case LAW_OFFICE:
        return new MeshGeometry[]{
            pad(4.2f),
            part(MeshBuilder.box(5f, 2.8f, 4f), PLASTER, 0f, 1.4f, 0f),
            part(MeshBuilder.box(5.2f, 0.1f, 4.2f), STONE_DARK, 0f, 2.85f, 0f),
            part(MeshBuilder.box(5.4f, 0.05f, 1.6f), LAW_AWNING, 0f, 3.1f, 2.5f).rotateX(0.2f),
            part(MeshBuilder.box(2.6f, 0.45f, 0.1f), BANK_SIGN, 0f, 2.45f, 2.05f),
            part(MeshBuilder.box(1.2f, 1.9f, 0.08f), DOOR, 1f, 0.95f, 2.0f),
            part(MeshBuilder.box(1.2f, 1.0f, 0.06f), GLASS, -1.3f, 1.6f, 2.0f)};
      case SHOP_A:
        return shop(5f, 2.6f, 4f, SHOP_A_WALL, AWNING_TEAL);
      case SHOP_B:
        return shop(4.4f, 2.4f, 3.6f, SHOP_B_WALL, CANOPY_RED);
      default:
        throw new IllegalArgumentException("unknown kind " + kind);
    }
  }

  private static MeshGeometry[] shop(float width, float height, float depth, float[] wall, float[] awning) {
    return new MeshGeometry[]{
        pad(width / 2f + 1.4f),
        part(MeshBuilder.box(width, height, depth), wall, 0f, height / 2f, 0f),
        part(MeshBuilder.box(width + 0.2f, 0.1f, depth + 0.2f), STONE_DARK, 0f, height + 0.05f, 0f),
        part(MeshBuilder.box(width + 0.4f, 0.05f, 1.6f), awning, 0f, height + 0.4f, depth / 2f + 0.5f).rotateX(0.2f),
        part(MeshBuilder.box(1.2f, 1.7f, 0.08f), DOOR, 0.8f, 0.85f, depth / 2f),
        part(MeshBuilder.box(1.3f, 1.0f, 0.06f), GLASS, -1f, 1.4f, depth / 2f)};
  }

  private static MeshGeometry pad(float radius) {
    return part(MeshBuilder.cylinder(radius, 0.3f, 14, true, true), PAD, 0f, 0.15f, 0f);
  }

  private static MeshGeometry part(MeshGeometry geometry, float[] color, float tx, float ty, float tz) {
    geometry.translate(tx, ty, tz);
    for (int i = 0; i < geometry.vertexCount(); i++) {
      geometry.setVertexColor(i, color[0], color[1], color[2], 1f);
    }
    return geometry;
  }
}
