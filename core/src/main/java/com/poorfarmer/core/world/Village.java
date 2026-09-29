package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshBuilder;
import com.poorfarmer.core.model.MeshGeometry;

public final class Village {

  public enum VillageKind {
    BETUL_SHOP, COOPERATIVE, TRACTOR_REPAIR, MOSQUE, SCHOOL, TEAHOUSE,
    HOUSE_A, HOUSE_B, HOUSE_C, HOUSE_D
  }

  private static final float[][] POSITIONS = {
      {52f, 16f}, {58f, 6f}, {66f, 16f}, {72f, 4f}, {62f, 26f}, {44f, 25f},
      {44f, 4f}, {44f, 15f}, {72f, 24f}, {79f, 20f}};

  private static final float[] PAD = {0.45f, 0.38f, 0.30f};
  private static final float[] SHOP_WALL = {0.72f, 0.62f, 0.48f};
  private static final float[] SHOP_AWNING = {0.72f, 0.28f, 0.22f};
  private static final float[] COOP_WALL = {0.60f, 0.55f, 0.45f};
  private static final float[] COOP_ROOF = {0.45f, 0.35f, 0.28f};
  private static final float[] REPAIR_WALL = {0.55f, 0.50f, 0.45f};
  private static final float[] REPAIR_ROOF = {0.42f, 0.38f, 0.36f};
  private static final float[] TRACTOR_RED = {0.70f, 0.20f, 0.15f};
  private static final float[] TRACTOR_DARK = {0.15f, 0.15f, 0.16f};
  private static final float[] MOSQUE_PLASTER = {0.85f, 0.85f, 0.80f};
  private static final float[] MOSQUE_DOME = {0.20f, 0.50f, 0.55f};
  private static final float[] MOSQUE_DOOR = {0.35f, 0.28f, 0.20f};
  private static final float[] SCHOOL_WALL = {0.80f, 0.78f, 0.70f};
  private static final float[] SCHOOL_ROOF = {0.40f, 0.42f, 0.45f};
  private static final float[] FLAG_RED = {0.78f, 0.12f, 0.12f};
  private static final float[] TEA_WALL = {0.68f, 0.55f, 0.40f};
  private static final float[] TEA_ROOF = {0.30f, 0.45f, 0.45f};
  private static final float[] HOUSE_A = {0.75f, 0.60f, 0.45f};
  private static final float[] HOUSE_B = {0.68f, 0.58f, 0.50f};
  private static final float[] HOUSE_C = {0.70f, 0.52f, 0.40f};
  private static final float[] HOUSE_D = {0.65f, 0.60f, 0.55f};
  private static final float[] HOUSE_ROOF = {0.50f, 0.35f, 0.25f};
  private static final float[] DOOR = {0.35f, 0.25f, 0.16f};
  private static final float[] WINDOW = {0.55f, 0.70f, 0.80f};
  private static final float[] GLASS_PANE = {0.60f, 0.78f, 0.88f};

  private final float[] baseHeights = new float[VillageKind.values().length];
  private final MeshGeometry[] cache = new MeshGeometry[VillageKind.values().length];

  public Village(Terrain terrain) {
    for (VillageKind kind : VillageKind.values()) {
      baseHeights[kind.ordinal()] = terrain.heightAt(positionX(kind), positionZ(kind));
    }
  }

  public static float positionX(VillageKind kind) {
    return POSITIONS[kind.ordinal()][0];
  }

  public static float positionZ(VillageKind kind) {
    return POSITIONS[kind.ordinal()][1];
  }

  public float baseHeight(VillageKind kind) {
    return baseHeights[kind.ordinal()];
  }

  public MeshGeometry buildingMesh(VillageKind kind) {
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
    MeshGeometry[] parts = new MeshGeometry[VillageKind.values().length];
    for (VillageKind kind : VillageKind.values()) {
      parts[kind.ordinal()] = buildingMesh(kind);
    }
    return MeshBuilder.merge(parts);
  }

  private static MeshGeometry[] buildKind(VillageKind kind) {
    switch (kind) {
      case BETUL_SHOP:
        return new MeshGeometry[]{
            pad(4.2f),
            part(MeshBuilder.box(6f, 2.6f, 4f), SHOP_WALL, 0f, 1.3f, 0f),
            part(MeshBuilder.box(6.1f, 0.12f, 4.1f), COOP_ROOF, 0f, 2.66f, 0f),
            part(MeshBuilder.box(6.4f, 0.05f, 1.8f), SHOP_AWNING, 0f, 3.0f, 2.6f).rotateX(0.25f),
            part(MeshBuilder.box(0.9f, 1.8f, 0.08f), DOOR, 1.6f, 0.9f, 2.0f),
            part(MeshBuilder.box(1.4f, 1.0f, 0.06f), GLASS_PANE, -1.4f, 1.5f, 2.0f),
            part(MeshBuilder.box(1.4f, 1.0f, 0.06f), GLASS_PANE, 0f, 1.5f, 2.0f),
            part(MeshBuilder.box(0.5f, 0.5f, 0.5f), TEA_ROOF, 2.8f, 2.9f, 1.8f)};
      case COOPERATIVE: {
        MeshGeometry coopEastGable = MeshBuilder.extrude(
            new float[][]{{-2.98f, -2.5f}, {-2.98f, 2.5f}, {-4.38f, 0f}}, 0.25f);
        coopEastGable.rotateZ(-(float) Math.PI / 2f);
        MeshGeometry coopWestGable = MeshBuilder.extrude(
            new float[][]{{-2.98f, -2.5f}, {-2.98f, 2.5f}, {-4.38f, 0f}}, 0.25f);
        coopWestGable.rotateZ(-(float) Math.PI / 2f);
        return new MeshGeometry[]{
            pad(5.5f),
            part(MeshBuilder.box(8f, 3f, 5f), COOP_WALL, 0f, 1.5f, 0f),
            part(rotatedRoof(8.4f, 2.8f, 0.5f), COOP_ROOF, 0f, 3.7f, 1.4f),
            part(rotatedRoof(8.4f, 2.8f, -0.5f), COOP_ROOF, 0f, 3.7f, -1.4f),
            part(MeshBuilder.box(8.4f, 0.12f, 0.12f), DOOR, 0f, 4.5f, 0f),
            part(coopEastGable, COOP_WALL, 3.75f, 0f, 0f),
            part(coopWestGable, COOP_WALL, -3.75f, 0f, 0f),
            part(MeshBuilder.box(2f, 2.2f, 0.1f), DOOR, 0f, 1.1f, 2.5f),
            part(MeshBuilder.box(0.1f, 0.9f, 3f), WINDOW, -2.8f, 1.9f, 2.5f),
            part(MeshBuilder.box(0.1f, 0.9f, 3f), WINDOW, 2.8f, 1.9f, 2.5f)};
      }
      case TRACTOR_REPAIR:
        return new MeshGeometry[]{
            pad(4.5f),
            part(MeshBuilder.box(5f, 2.8f, 0.2f), REPAIR_WALL, 0f, 1.4f, -1.9f),
            part(MeshBuilder.box(0.2f, 2.8f, 3.8f), REPAIR_WALL, -2.4f, 1.4f, 0f),
            part(MeshBuilder.box(0.2f, 2.8f, 3.8f), REPAIR_WALL, 2.4f, 1.4f, 0f),
            part(MeshBuilder.box(5.2f, 0.1f, 4.2f), REPAIR_ROOF, 0f, 2.85f, 0f),
            part(MeshBuilder.box(2f, 0.8f, 1f), TRACTOR_RED, 0.8f, 0.5f, 0.6f),
            part(MeshBuilder.box(0.7f, 0.7f, 0.7f), TRACTOR_RED, 1.2f, 1.25f, 0.6f),
            part(MeshBuilder.cylinder(0.04f, 0.5f, 6, false, false), TRACTOR_DARK, 1.7f, 1.7f, 0.4f),
            part(wheel(0.5f, 0.28f), TRACTOR_DARK, 0.3f, 0.5f, 0.1f),
            part(wheel(0.5f, 0.28f), TRACTOR_DARK, 0.3f, 0.5f, 1.1f),
            part(wheel(0.35f, 0.22f), TRACTOR_DARK, 1.5f, 0.35f, 0.1f),
            part(wheel(0.35f, 0.22f), TRACTOR_DARK, 1.5f, 0.35f, 1.1f)};
      case MOSQUE:
        return new MeshGeometry[]{
            pad(4.8f),
            part(MeshBuilder.box(5f, 2.8f, 5f), MOSQUE_PLASTER, 0f, 1.4f, 0f),
            part(MeshBuilder.lathe(new float[][]{{0.05f, 4.5f}, {0.8f, 4.4f}, {1.3f, 4.15f}, {1.5f, 3.7f}, {1.5f, 2.9f}}, 16), MOSQUE_DOME, 0f, 0f, 0f),
            part(MeshBuilder.cylinder(0.18f, 3.4f, 10, true, false), MOSQUE_PLASTER, 2.6f, 1.7f, 2.6f),
            part(MeshBuilder.lathe(new float[][]{{0.02f, 4.0f}, {0.35f, 3.9f}, {0.45f, 3.65f}}, 10), MOSQUE_DOME, 2.6f, 0f, 2.6f),
            part(MeshBuilder.box(1.4f, 2.2f, 0.08f), MOSQUE_DOOR, 0f, 1.1f, 2.5f),
            part(MeshBuilder.box(0.8f, 1.2f, 0.06f), WINDOW, -1.6f, 1.8f, 2.5f),
            part(MeshBuilder.box(0.8f, 1.2f, 0.06f), WINDOW, 1.6f, 1.8f, 2.5f)};
      case SCHOOL:
        return new MeshGeometry[]{
            pad(5f),
            part(MeshBuilder.box(6f, 2.6f, 4f), SCHOOL_WALL, 0f, 1.3f, 0f),
            part(MeshBuilder.box(6.2f, 0.1f, 4.2f), SCHOOL_ROOF, 0f, 2.65f, 0f),
            part(MeshBuilder.box(1.4f, 1.8f, 0.08f), DOOR, 0f, 0.9f, 2.0f),
            part(MeshBuilder.box(0.9f, 0.9f, 0.06f), GLASS_PANE, -1.9f, 1.5f, 2.0f),
            part(MeshBuilder.box(0.9f, 0.9f, 0.06f), GLASS_PANE, 1.9f, 1.5f, 2.0f),
            part(MeshBuilder.cylinder(0.03f, 3.4f, 6, false, false), TRACTOR_DARK, 2.6f, 1.7f, 2.3f),
            part(MeshBuilder.box(0.9f, 0.55f, 0.02f), FLAG_RED, 3.05f, 3.2f, 2.3f)};
      case TEAHOUSE:
        return new MeshGeometry[]{
            pad(4f),
            part(MeshBuilder.box(5f, 2.4f, 4f), TEA_WALL, 0f, 1.2f, 0f),
            part(MeshBuilder.lathe(new float[][]{{0.05f, 3.6f}, {1.0f, 3.5f}, {1.9f, 3.2f}, {2.4f, 2.75f}, {2.4f, 2.4f}}, 16), TEA_ROOF, 0f, 0f, 0f),
            part(MeshBuilder.box(1.2f, 1.7f, 0.08f), DOOR, 1.4f, 0.85f, 2.0f),
            part(MeshBuilder.box(1.1f, 0.9f, 0.06f), GLASS_PANE, -1.2f, 1.3f, 2.0f),
            part(MeshBuilder.box(0.4f, 0.4f, 0.4f), PAD, 2.6f, 0.2f, 2.4f)};
      case HOUSE_A:
        return smallHouse(4f, 2.2f, 3.4f, HOUSE_A);
      case HOUSE_B:
        return smallHouse(3.6f, 2f, 3.2f, HOUSE_B);
      case HOUSE_C:
        return smallHouse(4.4f, 2.4f, 3.6f, HOUSE_C);
      case HOUSE_D:
        return smallHouse(3.8f, 2.1f, 3.4f, HOUSE_D);
      default:
        throw new IllegalArgumentException("unknown kind " + kind);
    }
  }

  private static MeshGeometry[] smallHouse(float width, float height, float depth, float[] wallColor) {
    float angle = 0.55f;
    float halfSpan = depth / 2f + 0.25f;
    float slopeLen = halfSpan / (float) Math.cos(angle);
    float centerY = height + (halfSpan / 2f) * (float) Math.tan(angle);
    float centerZ = halfSpan / 2f;
    return new MeshGeometry[]{
        pad(width / 2f + 1.2f),
        part(MeshBuilder.box(width, height, depth), wallColor, 0f, height / 2f, 0f),
        part(rotatedRoof(width + 0.4f, slopeLen, angle), HOUSE_ROOF, 0f, centerY, centerZ),
        part(rotatedRoof(width + 0.4f, slopeLen, -angle), HOUSE_ROOF, 0f, centerY, -centerZ),
        part(MeshBuilder.box(0.9f, 1.6f, 0.08f), DOOR, 0.7f, 0.8f, depth / 2f),
        part(MeshBuilder.box(0.9f, 0.8f, 0.06f), WINDOW, -0.9f, 1.3f, depth / 2f)};
  }

  private static MeshGeometry pad(float radius) {
    return part(MeshBuilder.cylinder(radius, 0.3f, 14, true, true), PAD, 0f, 0.15f, 0f);
  }

  private static MeshGeometry rotatedRoof(float lengthX, float lengthZ, float angle) {
    MeshGeometry roof = MeshBuilder.box(lengthX, 0.08f, lengthZ);
    roof.rotateX(angle);
    return roof;
  }

  private static MeshGeometry wheel(float radius, float width) {
    MeshGeometry wheel = MeshBuilder.cylinder(radius, width, 12, true, true);
    wheel.rotateZ((float) Math.PI / 2f);
    return wheel;
  }

  private static MeshGeometry part(MeshGeometry geometry, float[] color, float tx, float ty, float tz) {
    geometry.translate(tx, ty, tz);
    for (int i = 0; i < geometry.vertexCount(); i++) {
      geometry.setVertexColor(i, color[0], color[1], color[2], 1f);
    }
    return geometry;
  }
}
