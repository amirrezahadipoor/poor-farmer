package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshBuilder;
import com.poorfarmer.core.model.MeshGeometry;

public final class FarmBuildings {

  public enum BuildingKind {
    BARN, LIVESTOCK_SHED, CHICKEN_COOP, BEEHIVE, OVEN, MILL,
    COLD_STORAGE, ROSEWATER_STILL, CARPET_WORKSHOP, SAFFRON_DRYER, GREENHOUSE
  }

  public static final float GROUND_Y = 1.2f;

  private static final float[][] POSITIONS = {
      {-12f, 30f}, {-30f, 0f}, {-30f, 14f}, {-26f, 24f}, {-14f, -30f}, {30f, 12f},
      {30f, 0f}, {30f, -16f}, {10f, -30f}, {-2f, -30f}, {14f, 30f}};

  private static final float[] BARN_WALL = {0.66f, 0.55f, 0.40f};
  private static final float[] BARN_ROOF = {0.50f, 0.30f, 0.20f};
  private static final float[] SHED_WALL = {0.60f, 0.50f, 0.38f};
  private static final float[] SHED_ROOF = {0.45f, 0.42f, 0.40f};
  private static final float[] COOP_WALL = {0.62f, 0.52f, 0.40f};
  private static final float[] COOP_ROOF = {0.50f, 0.35f, 0.25f};
  private static final float[] HIVE = {0.80f, 0.65f, 0.35f};
  private static final float[] OVEN_CLAY = {0.55f, 0.42f, 0.30f};
  private static final float[] MILL_BASE = {0.62f, 0.50f, 0.36f};
  private static final float[] MILL_WOOD = {0.50f, 0.35f, 0.22f};
  private static final float[] MILL_CAP = {0.40f, 0.30f, 0.25f};
  private static final float[] COLD_BODY = {0.75f, 0.80f, 0.85f};
  private static final float[] COLD_DOOR = {0.40f, 0.50f, 0.60f};
  private static final float[] METAL = {0.70f, 0.70f, 0.72f};
  private static final float[] METAL_DARK = {0.50f, 0.40f, 0.30f};
  private static final float[] WORKSHOP_WALL = {0.60f, 0.45f, 0.35f};
  private static final float[] WORKSHOP_ROOF = {0.45f, 0.35f, 0.30f};
  private static final float[] DRYER_WALL = {0.70f, 0.55f, 0.35f};
  private static final float[] GLASS = {0.65f, 0.85f, 0.90f};
  private static final float[] CHIMNEY = {0.40f, 0.35f, 0.35f};
  private static final float[] GH_FRAME = {0.75f, 0.78f, 0.80f};
  private static final float[] GH_BASE = {0.60f, 0.50f, 0.40f};
  private static final float[] DOOR_WOOD = {0.40f, 0.28f, 0.18f};
  private static final float[] RAMP = {0.55f, 0.45f, 0.32f};

  private final MeshGeometry[] cache = new MeshGeometry[BuildingKind.values().length];

  public static float positionX(BuildingKind kind) {
    return POSITIONS[kind.ordinal()][0];
  }

  public static float positionZ(BuildingKind kind) {
    return POSITIONS[kind.ordinal()][1];
  }

  public MeshGeometry buildingMesh(BuildingKind kind) {
    int i = kind.ordinal();
    if (cache[i] == null) {
      MeshGeometry[] parts = buildKind(kind);
      MeshGeometry merged = MeshBuilder.merge(parts);
      merged.translate(positionX(kind), GROUND_Y, positionZ(kind));
      cache[i] = merged;
    }
    return cache[i];
  }

  public MeshGeometry allMesh() {
    MeshGeometry[] parts = new MeshGeometry[BuildingKind.values().length];
    for (BuildingKind kind : BuildingKind.values()) {
      parts[kind.ordinal()] = buildingMesh(kind);
    }
    return MeshBuilder.merge(parts);
  }

  private static MeshGeometry[] buildKind(BuildingKind kind) {
    switch (kind) {
      case BARN:
        return new MeshGeometry[]{
            part(MeshBuilder.box(5f, 2.4f, 4f), BARN_WALL, 0f, 1.2f, 0f),
            part(MeshBuilder.box(1f, 1.8f, 0.08f), DOOR_WOOD, 1.5f, 0.9f, 2.0f),
            part(rotatedRoof(5.4f, 2.6f, 0.65f), BARN_ROOF, 0f, 2.95f, 1.3f),
            part(rotatedRoof(5.4f, 2.6f, -0.65f), BARN_ROOF, 0f, 2.95f, -1.3f),
            part(MeshBuilder.box(5.4f, 0.1f, 0.1f), DOOR_WOOD, 0f, 3.6f, 0f)};
      case LIVESTOCK_SHED:
        return new MeshGeometry[]{
            part(MeshBuilder.box(4.5f, 2.2f, 0.2f), SHED_WALL, 0f, 1.1f, -1.65f),
            part(MeshBuilder.box(0.2f, 2.2f, 3.5f), SHED_WALL, -2.15f, 1.1f, 0f),
            part(MeshBuilder.box(0.2f, 2.2f, 3.5f), SHED_WALL, 2.15f, 1.1f, 0f),
            part(rotatedRoof(5.0f, 2.2f, 0.45f), SHED_ROOF, 0f, 2.6f, 0f),
            part(MeshBuilder.box(0.9f, 1.6f, 0.08f), DOOR_WOOD, -1.2f, 0.8f, -1.7f)};
      case CHICKEN_COOP:
        return new MeshGeometry[]{
            part(MeshBuilder.box(2f, 1.4f, 1.8f), COOP_WALL, 0f, 0.7f, 0f),
            part(MeshBuilder.box(0.4f, 0.5f, 0.06f), DOOR_WOOD, 0.4f, 0.25f, 0.9f),
            part(rotatedRoof(2.4f, 2.2f, 0.5f), COOP_ROOF, 0f, 1.95f, 0f),
            part(slantedRamp(1.0f, 0.8f, -0.5f), RAMP, 1.5f, 0.4f, 0f)};
      case BEEHIVE:
        return new MeshGeometry[]{
            part(MeshBuilder.box(1f, 0.7f, 1f), HIVE, 0f, 0.35f, 0f),
            part(MeshBuilder.box(0.85f, 0.6f, 0.85f), HIVE, 0f, 1.0f, 0f),
            part(MeshBuilder.box(0.7f, 0.5f, 0.7f), HIVE, 0f, 1.55f, 0f),
            part(MeshBuilder.box(0.95f, 0.08f, 0.95f), METAL_DARK, 0f, 1.85f, 0f),
            part(MeshBuilder.box(0.3f, 0.12f, 0.04f), METAL_DARK, 0f, 0.15f, 0.51f),
            part(MeshBuilder.box(0.6f, 0.1f, 0.6f), METAL_DARK, 0f, 0.05f, 0f)};
      case OVEN:
        return new MeshGeometry[]{
            part(MeshBuilder.lathe(new float[][]{{0.05f, 1.3f}, {0.45f, 1.2f}, {0.75f, 1.0f}, {0.9f, 0.7f}, {0.85f, 0.35f}, {0.8f, 0.05f}}, 16), OVEN_CLAY, 0f, 0f, 0f),
            part(MeshBuilder.box(0.5f, 0.5f, 0.5f), METAL_DARK, 0.9f, 0.25f, 0f),
            part(MeshBuilder.cylinder(0.08f, 0.9f, 8, false, false), METAL_DARK, 0f, 1.65f, 0f)};
      case MILL:
        return new MeshGeometry[]{
            part(MeshBuilder.box(2.2f, 3.2f, 2.2f), MILL_BASE, 0f, 1.6f, 0f),
            part(MeshBuilder.cone(1.5f, 1.1f, 12), MILL_CAP, 0f, 3.75f, 0f),
            part(MeshBuilder.box(0.1f, 1.0f, 0.3f), MILL_WOOD, 0f, 3.4f, 1.35f),
            part(rotatedBlade(0.1f, 1.6f, 0.7f, 0.5f), MILL_WOOD, 0f, 3.4f, 1.35f),
            part(rotatedBlade(0.1f, 1.6f, 0.7f, -0.5f), MILL_WOOD, 0f, 3.4f, 1.35f),
            part(MeshBuilder.box(0.9f, 1.4f, 0.08f), DOOR_WOOD, 0f, 0.7f, 1.1f)};
      case COLD_STORAGE:
        return new MeshGeometry[]{
            part(MeshBuilder.box(3f, 2.8f, 3f), COLD_BODY, 0f, 1.4f, 0f),
            part(MeshBuilder.box(3.2f, 0.12f, 3.2f), COLD_DOOR, 0f, 2.86f, 0f),
            part(MeshBuilder.box(0.9f, 2.0f, 0.08f), COLD_DOOR, 0.7f, 1.0f, 1.5f)};
      case ROSEWATER_STILL:
        return new MeshGeometry[]{
            part(MeshBuilder.cylinder(1.1f, 1.5f, 16, true, false), METAL, 0f, 0.95f, 0f),
            part(MeshBuilder.cylinder(0.45f, 0.5f, 12, true, false), METAL, 0f, 1.9f, 0f),
            part(MeshBuilder.cylinder(0.08f, 0.8f, 8, false, false), METAL, 0.5f, 2.4f, 0f),
            part(MeshBuilder.box(1.9f, 0.15f, 1.9f), METAL_DARK, 0f, 0.08f, 0f)};
      case CARPET_WORKSHOP:
        return new MeshGeometry[]{
            part(MeshBuilder.box(5f, 2.2f, 3f), WORKSHOP_WALL, 0f, 1.1f, 0f),
            part(MeshBuilder.box(5.4f, 0.1f, 3.4f), WORKSHOP_ROOF, 0f, 2.25f, 0f),
            part(MeshBuilder.box(1.2f, 1.6f, 0.08f), DOOR_WOOD, -1.5f, 0.8f, 1.5f),
            part(MeshBuilder.box(1.2f, 1.0f, 0.06f), GLASS, 1.3f, 1.3f, 1.5f)};
      case SAFFRON_DRYER:
        return new MeshGeometry[]{
            part(MeshBuilder.box(3f, 2.5f, 3f), DRYER_WALL, 0f, 1.25f, 0f),
            part(MeshBuilder.box(3.1f, 0.1f, 3.1f), WORKSHOP_ROOF, 0f, 2.55f, 0f),
            part(MeshBuilder.box(2.2f, 1.4f, 0.04f), GLASS, 0f, 1.3f, 1.51f),
            part(MeshBuilder.box(0.4f, 1.2f, 0.4f), CHIMNEY, 0.8f, 3.2f, -0.8f),
            part(MeshBuilder.box(1f, 1.5f, 0.08f), DOOR_WOOD, -0.9f, 0.75f, 1.5f)};
      case GREENHOUSE:
        return new MeshGeometry[]{
            part(MeshBuilder.box(4f, 1.6f, 3f), GH_BASE, 0f, 0.8f, 0f),
            part(MeshBuilder.box(3.8f, 1.4f, 0.04f), GLASS, 0f, 1.1f, 1.51f),
            part(MeshBuilder.box(3.8f, 1.4f, 0.04f), GLASS, 0f, 1.1f, -1.51f),
            part(MeshBuilder.box(0.04f, 1.4f, 2.9f), GLASS, 1.99f, 1.1f, 0f),
            part(MeshBuilder.box(0.04f, 1.4f, 2.9f), GLASS, -1.99f, 1.1f, 0f),
            part(ridgeRoof(4.4f, 3.2f), GH_FRAME, 0f, 2.3f, 0f),
            part(MeshBuilder.box(0.8f, 1.3f, 0.08f), DOOR_WOOD, 0.5f, 0.65f, 1.53f)};
      default:
        throw new IllegalArgumentException("unknown kind " + kind);
    }
  }

  private static MeshGeometry rotatedRoof(float lengthX, float lengthZ, float angle) {
    MeshGeometry roof = MeshBuilder.box(lengthX, 0.08f, lengthZ);
    roof.rotateX(angle);
    return roof;
  }

  private static MeshGeometry slantedRamp(float lengthZ, float height, float angle) {
    MeshGeometry ramp = MeshBuilder.box(1.0f, 0.08f, lengthZ);
    ramp.rotateX(angle);
    return ramp;
  }

  private static MeshGeometry rotatedBlade(float thickness, float length, float width, float angle) {
    MeshGeometry blade = MeshBuilder.box(thickness, length, width);
    blade.rotateZ(angle);
    return blade;
  }

  private static MeshGeometry ridgeRoof(float lengthX, float spanZ) {
    MeshGeometry roof = MeshBuilder.cylinder(spanZ / 2f, lengthX, 12, false, false);
    roof.rotateZ((float) Math.PI / 2f);
    return roof;
  }

  private static MeshGeometry part(MeshGeometry geometry, float[] color, float tx, float ty, float tz) {
    geometry.translate(tx, ty, tz);
    for (int i = 0; i < geometry.vertexCount(); i++) {
      geometry.setVertexColor(i, color[0], color[1], color[2], 1f);
    }
    return geometry;
  }
}
