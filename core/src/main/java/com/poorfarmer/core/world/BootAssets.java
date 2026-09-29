package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.procedural.Texture;

public final class BootAssets {

  public final Terrain terrain;
  public final MeshGeometry terrainMesh;
  public final Texture grass;
  public final Texture dirt;
  public final Texture stone;

  public BootAssets(Terrain terrain, MeshGeometry terrainMesh, Texture grass, Texture dirt, Texture stone) {
    this.terrain = terrain;
    this.terrainMesh = terrainMesh;
    this.grass = grass;
    this.dirt = dirt;
    this.stone = stone;
  }
}
