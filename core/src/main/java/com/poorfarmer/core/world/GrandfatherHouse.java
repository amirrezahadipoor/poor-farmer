package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshBuilder;
import com.poorfarmer.core.model.MeshGeometry;

public final class GrandfatherHouse {

  public enum Stage {
    RUINED, REPAIRED, FULL
  }

  public static final float WORLD_X = -16f;
  public static final float WORLD_Z = -14f;
  public static final float GROUND_Y = 1.2f;
  public static final float FLOOR_Y = GROUND_Y + 0.05f;

  private static final float[] MUD_BRICK = {0.62f, 0.50f, 0.36f};
  private static final float[] RUINED_MUD = {0.55f, 0.45f, 0.33f};
  private static final float[] RUBBLE = {0.50f, 0.48f, 0.45f};
  private static final float[] WOOD = {0.40f, 0.28f, 0.18f};
  private static final float[] ROOF_PLANK = {0.45f, 0.42f, 0.40f};
  private static final float[] ROOF_TILE = {0.60f, 0.32f, 0.22f};
  private static final float[] GLASS = {0.65f, 0.78f, 0.85f};
  private static final float[] FLOOR_DARK = {0.52f, 0.42f, 0.30f};
  private static final float[] CHIMNEY = {0.55f, 0.40f, 0.30f};
  private static final float[] RUG = {0.65f, 0.25f, 0.20f};
  private static final float[] CEILING = {0.68f, 0.60f, 0.50f};
  private static final float[] MATTRESS = {0.85f, 0.82f, 0.75f};
  private static final float[] PILLOW = {0.90f, 0.88f, 0.85f};
  private static final float[] STOVE = {0.25f, 0.25f, 0.28f};
  private static final float[] PIPE = {0.35f, 0.35f, 0.35f};
  private static final float[] LAMP = {0.90f, 0.80f, 0.30f};
  private static final float[] PAINTING = {0.55f, 0.65f, 0.50f};
  private static final float[] RADIO = {0.30f, 0.22f, 0.18f};

  private static final float ROOF_ANGLE = (float) Math.atan2(1.8f, 2.5f);

  private final MeshGeometry[] exteriorCache = new MeshGeometry[3];
  private MeshGeometry interiorCache;
  private Stage stage = Stage.RUINED;
  private int version;

  public Stage stage() {
    return stage;
  }

  public int version() {
    return version;
  }

  public void setStage(Stage newStage) {
    if (newStage == stage) {
      return;
    }
    stage = newStage;
    version++;
  }

  public MeshGeometry exteriorMesh() {
    int i = stage.ordinal();
    if (exteriorCache[i] == null) {
      exteriorCache[i] = buildExterior(stage);
    }
    return exteriorCache[i];
  }

  public MeshGeometry interiorMesh() {
    if (interiorCache == null) {
      interiorCache = buildInterior();
    }
    return interiorCache;
  }

  private static MeshGeometry buildExterior(Stage stage) {
    MeshGeometry[] parts;
    if (stage == Stage.RUINED) {
      MeshGeometry rubbleA = MeshBuilder.box(0.5f, 0.4f, 0.45f);
      rubbleA.rotateY(0.5f);
      MeshGeometry rubbleB = MeshBuilder.box(0.4f, 0.3f, 0.4f);
      rubbleB.rotateY(1.2f);
      MeshGeometry rubbleC = MeshBuilder.box(0.35f, 0.25f, 0.35f);
      rubbleC.rotateY(0.3f);
      MeshGeometry rubbleD = MeshBuilder.box(0.3f, 0.3f, 0.3f);
      rubbleD.rotateY(0.9f);
      parts = new MeshGeometry[]{
          part(MeshBuilder.box(6f, 1.4f, 0.25f), RUINED_MUD, 0f, 1.9f, -2.375f),
          part(MeshBuilder.box(0.25f, 1.8f, 5f), RUINED_MUD, -2.875f, 2.1f, 0f),
          part(MeshBuilder.box(0.25f, 1.2f, 5f), RUINED_MUD, 2.875f, 1.8f, 0f),
          part(MeshBuilder.box(2.4f, 1.0f, 0.25f), RUINED_MUD, -1.8f, 1.7f, 2.375f),
          part(MeshBuilder.box(1.0f, 0.6f, 0.25f), RUINED_MUD, 2.0f, 1.5f, 2.375f),
          part(MeshBuilder.box(0.12f, 0.12f, 5.6f), WOOD, -1.4f, 2.62f, 0f),
          part(MeshBuilder.box(0.12f, 0.12f, 5.6f), WOOD, 1.4f, 2.62f, 0f),
          part(MeshBuilder.box(5.9f, 0.1f, 4.9f), FLOOR_DARK, 0f, 1.25f, 0f),
          part(rubbleA, RUBBLE, 0.6f, 1.4f, 0.8f),
          part(rubbleB, RUBBLE, -1.2f, 1.35f, -0.6f),
          part(rubbleC, RUBBLE, 1.5f, 1.325f, -1.2f),
          part(rubbleD, RUBBLE, -0.4f, 1.35f, 1.4f)};
    } else {
      MeshGeometry southRoof = MeshBuilder.box(6.5f, 0.08f, 3.2f);
      southRoof.rotateX(ROOF_ANGLE);
      MeshGeometry northRoof = MeshBuilder.box(6.5f, 0.08f, 3.2f);
      northRoof.rotateX(-ROOF_ANGLE);
      MeshGeometry eastGable = MeshBuilder.extrude(
          new float[][]{{-3.78f, -2.5f}, {-3.78f, 2.5f}, {-5.64f, 0f}}, 0.25f);
      eastGable.rotateZ(-(float) Math.PI / 2f);
      MeshGeometry westGable = MeshBuilder.extrude(
          new float[][]{{-3.78f, -2.5f}, {-3.78f, 2.5f}, {-5.64f, 0f}}, 0.25f);
      westGable.rotateZ(-(float) Math.PI / 2f);
      float[] roofColor = stage == Stage.FULL ? ROOF_TILE : ROOF_PLANK;
      parts = new MeshGeometry[]{
          part(MeshBuilder.box(6f, 2.6f, 0.25f), MUD_BRICK, 0f, 2.5f, -2.375f),
          part(MeshBuilder.box(2.2f, 2.6f, 0.25f), MUD_BRICK, -1.9f, 2.5f, 2.375f),
          part(MeshBuilder.box(2.2f, 2.6f, 0.25f), MUD_BRICK, 1.9f, 2.5f, 2.375f),
          part(MeshBuilder.box(0.25f, 2.6f, 5f), MUD_BRICK, -2.875f, 2.5f, 0f),
          part(MeshBuilder.box(0.25f, 2.6f, 5f), MUD_BRICK, 2.875f, 2.5f, 0f),
          part(MeshBuilder.box(1.05f, 2.0f, 0.08f), WOOD, 0f, 2.2f, 2.37f),
          part(MeshBuilder.box(0.12f, 2.1f, 0.14f), WOOD, -0.62f, 2.25f, 2.37f),
          part(MeshBuilder.box(0.12f, 2.1f, 0.14f), WOOD, 0.62f, 2.25f, 2.37f),
          part(MeshBuilder.box(1.36f, 0.14f, 0.14f), WOOD, 0f, 3.35f, 2.37f),
          part(southRoof, roofColor, 0f, 4.7f, 1.25f),
          part(northRoof, roofColor, 0f, 4.7f, -1.25f),
          part(MeshBuilder.box(6.5f, 0.12f, 0.12f), WOOD, 0f, 5.64f, 0f),
          part(eastGable, MUD_BRICK, 2.75f, 0f, 0f),
          part(westGable, MUD_BRICK, -2.75f, 0f, 0f),
          part(MeshBuilder.box(5.9f, 0.1f, 4.9f), FLOOR_DARK, 0f, 1.25f, 0f)};
      if (stage == Stage.FULL) {
        MeshGeometry[] withExtras = new MeshGeometry[parts.length + 8];
        System.arraycopy(parts, 0, withExtras, 0, parts.length);
        withExtras[parts.length] = part(MeshBuilder.box(0.5f, 1.6f, 0.5f), CHIMNEY, 1.6f, 5.35f, -0.8f);
        withExtras[parts.length + 1] = part(MeshBuilder.box(0.62f, 0.12f, 0.62f), CHIMNEY, 1.6f, 6.2f, -0.8f);
        withExtras[parts.length + 2] = part(MeshBuilder.box(1.1f, 1.1f, 0.1f), WOOD, -1.5f, 2.6f, -2.37f);
        withExtras[parts.length + 3] = part(MeshBuilder.box(0.9f, 0.9f, 0.02f), GLASS, -1.5f, 2.6f, -2.39f);
        withExtras[parts.length + 4] = part(MeshBuilder.box(1.1f, 1.1f, 0.1f), WOOD, 1.5f, 2.6f, 2.37f);
        withExtras[parts.length + 5] = part(MeshBuilder.box(0.9f, 0.9f, 0.02f), GLASS, 1.5f, 2.6f, 2.39f);
        withExtras[parts.length + 6] = part(MeshBuilder.box(1.3f, 0.06f, 0.4f), RUBBLE, 0f, 1.23f, 2.6f);
        withExtras[parts.length + 7] = part(MeshBuilder.box(0.7f, 0.04f, 0.5f), WOOD, -1.5f, 1.5f, 2.62f);
        parts = withExtras;
      }
    }
    MeshGeometry merged = MeshBuilder.merge(parts);
    merged.translate(WORLD_X, 0f, WORLD_Z);
    return merged;
  }

  private static MeshGeometry buildInterior() {
    MeshGeometry[] parts = new MeshGeometry[]{
        part(MeshBuilder.box(5.7f, 0.08f, 4.7f), FLOOR_DARK, 0f, 1.24f, 0f),
        part(MeshBuilder.box(2.2f, 0.02f, 1.6f), RUG, 0.3f, 1.29f, 0.4f),
        part(MeshBuilder.box(5.7f, 2.6f, 0.2f), MUD_BRICK, 0f, 2.5f, -2.2f),
        part(MeshBuilder.box(5.7f, 2.6f, 0.2f), MUD_BRICK, 0f, 2.5f, 2.2f),
        part(MeshBuilder.box(0.2f, 2.6f, 4.7f), MUD_BRICK, -2.7f, 2.5f, 0f),
        part(MeshBuilder.box(0.2f, 2.6f, 4.7f), MUD_BRICK, 2.7f, 2.5f, 0f),
        part(MeshBuilder.box(5.7f, 0.1f, 4.7f), CEILING, 0f, 3.85f, 0f),
        part(MeshBuilder.box(2.0f, 0.45f, 1.3f), WOOD, -1.7f, 1.47f, -1.3f),
        part(MeshBuilder.box(1.9f, 0.15f, 1.2f), MATTRESS, -1.7f, 1.78f, -1.3f),
        part(MeshBuilder.box(0.5f, 0.12f, 0.9f), PILLOW, -2.35f, 1.90f, -1.3f),
        part(MeshBuilder.box(1.3f, 0.08f, 0.8f), WOOD, 1.4f, 1.95f, 1.0f),
        part(MeshBuilder.box(0.1f, 0.75f, 0.1f), WOOD, 1.05f, 1.56f, 0.7f),
        part(MeshBuilder.box(0.1f, 0.75f, 0.1f), WOOD, 1.75f, 1.56f, 0.7f),
        part(MeshBuilder.box(0.1f, 0.75f, 0.1f), WOOD, 1.05f, 1.56f, 1.3f),
        part(MeshBuilder.box(0.1f, 0.75f, 0.1f), WOOD, 1.75f, 1.56f, 1.3f),
        part(MeshBuilder.box(0.7f, 1.0f, 0.7f), STOVE, 2.2f, 1.7f, -1.6f),
        part(MeshBuilder.cylinder(0.06f, 2.0f, 8, false, false), PIPE, 2.2f, 3.1f, -1.6f),
        part(MeshBuilder.box(1.6f, 0.08f, 0.35f), WOOD, 0.5f, 2.6f, -2.0f),
        part(MeshBuilder.box(0.18f, 0.22f, 0.18f), new float[]{0.75f, 0.35f, 0.25f}, -0.05f, 2.77f, -2.0f),
        part(MeshBuilder.box(0.18f, 0.22f, 0.18f), new float[]{0.25f, 0.45f, 0.70f}, 0.25f, 2.77f, -2.0f),
        part(MeshBuilder.box(0.18f, 0.22f, 0.18f), new float[]{0.80f, 0.70f, 0.25f}, 0.55f, 2.77f, -2.0f),
        part(MeshBuilder.box(0.18f, 0.22f, 0.18f), new float[]{0.35f, 0.70f, 0.40f}, 0.85f, 2.77f, -2.0f),
        part(MeshBuilder.box(0.35f, 0.18f, 0.15f), RADIO, 1.2f, 2.83f, -2.0f),
        part(MeshBuilder.box(0.15f, 0.2f, 0.15f), LAMP, 1.4f, 2.06f, 1.0f),
        part(MeshBuilder.box(0.04f, 0.8f, 1.0f), WOOD, 2.68f, 2.4f, 0.5f),
        part(MeshBuilder.box(0.02f, 0.6f, 0.8f), PAINTING, 2.66f, 2.4f, 0.5f)};
    MeshGeometry merged = MeshBuilder.merge(parts);
    merged.translate(WORLD_X, 0f, WORLD_Z);
    return merged;
  }

  private static MeshGeometry part(MeshGeometry geometry, float[] color, float tx, float ty, float tz) {
    geometry.translate(tx, ty, tz);
    for (int i = 0; i < geometry.vertexCount(); i++) {
      geometry.setVertexColor(i, color[0], color[1], color[2], 1f);
    }
    return geometry;
  }
}
