package com.poorfarmer.core.world;

import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.procedural.Texture;

public final class BootAssets {

  public final Terrain terrain;
  public final MeshGeometry terrainMesh;
  public final MeshGeometry riverMesh;
  public final MeshGeometry mountainMesh;
  public final MeshGeometry cloudMesh;
  public final MeshGeometry buildingsMesh;
  public final MeshGeometry villageMesh;
  public final Texture grass;
  public final Texture dirt;
  public final Texture stone;

  public BootAssets(Terrain terrain, MeshGeometry terrainMesh, MeshGeometry riverMesh,
                    MeshGeometry mountainMesh, MeshGeometry cloudMesh, MeshGeometry buildingsMesh,
                    MeshGeometry villageMesh,
                    Texture grass, Texture dirt, Texture stone) {
    this.terrain = terrain;
    this.terrainMesh = terrainMesh;
    this.riverMesh = riverMesh;
    this.mountainMesh = mountainMesh;
    this.cloudMesh = cloudMesh;
    this.buildingsMesh = buildingsMesh;
    this.villageMesh = villageMesh;
    this.grass = grass;
    this.dirt = dirt;
    this.stone = stone;
  }
}
