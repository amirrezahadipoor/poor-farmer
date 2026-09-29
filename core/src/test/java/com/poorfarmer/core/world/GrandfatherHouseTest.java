package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class GrandfatherHouseTest {

  @Test
  public void stagesGainDetailInOrder() {
    GrandfatherHouse house = new GrandfatherHouse();
    house.setStage(GrandfatherHouse.Stage.RUINED);
    int ruined = house.exteriorMesh().triangleCount();
    house.setStage(GrandfatherHouse.Stage.REPAIRED);
    int repaired = house.exteriorMesh().triangleCount();
    house.setStage(GrandfatherHouse.Stage.FULL);
    int full = house.exteriorMesh().triangleCount();
    assertTrue("ruined=" + ruined, ruined > 0);
    assertTrue("ruined should be less built than repaired", repaired > ruined);
    assertTrue("full should have more than repaired", full > repaired);
  }

  @Test
  public void houseSitsOnFarmGround() {
    GrandfatherHouse house = new GrandfatherHouse();
    house.setStage(GrandfatherHouse.Stage.FULL);
    MeshGeometry mesh = house.exteriorMesh();
    float[] p = mesh.positions;
    for (int i = 0; i < mesh.vertexCount(); i++) {
      float x = p[i * 3];
      float y = p[i * 3 + 1];
      float z = p[i * 3 + 2];
      assertTrue("y below ground: " + y, y > GrandfatherHouse.GROUND_Y - 0.15f);
      assertTrue("x out of range: " + x, x > GrandfatherHouse.WORLD_X - 3.6f && x < GrandfatherHouse.WORLD_X + 3.6f);
      assertTrue("z out of range: " + z, z > GrandfatherHouse.WORLD_Z - 3.4f && z < GrandfatherHouse.WORLD_Z + 3.4f);
    }
  }

  @Test
  public void roofRisesAboveWalls() {
    GrandfatherHouse house = new GrandfatherHouse();
    house.setStage(GrandfatherHouse.Stage.FULL);
    MeshGeometry mesh = house.exteriorMesh();
    float maxY = -1f;
    for (int i = 0; i < mesh.vertexCount(); i++) {
      maxY = Math.max(maxY, mesh.positions[i * 3 + 1]);
    }
    assertTrue("roof should reach ~4.5m, got " + maxY, maxY > 4.4f);
  }

  @Test
  public void northWallFacesOutward() {
    GrandfatherHouse house = new GrandfatherHouse();
    house.setStage(GrandfatherHouse.Stage.REPAIRED);
    MeshGeometry mesh = house.exteriorMesh();
    boolean outerFaceFound = false;
    for (int i = 0; i < mesh.vertexCount(); i++) {
      float z = mesh.positions[i * 3 + 2];
      float ny = mesh.normals[i * 3 + 1];
      float nz = mesh.normals[i * 3 + 2];
      if (Math.abs(z - (GrandfatherHouse.WORLD_Z - 2.5f)) < 0.05f && Math.abs(ny) < 0.1f) {
        outerFaceFound |= nz < -0.9f;
      }
    }
    assertTrue("north wall outer face should point -z", outerFaceFound);
  }

  @Test
  public void stageChangesBumpVersion() {
    GrandfatherHouse house = new GrandfatherHouse();
    int v0 = house.version();
    house.setStage(GrandfatherHouse.Stage.RUINED);
    assertEquals(v0, house.version());
    house.setStage(GrandfatherHouse.Stage.REPAIRED);
    assertEquals(v0 + 1, house.version());
    house.setStage(GrandfatherHouse.Stage.FULL);
    assertEquals(v0 + 2, house.version());
  }

  @Test
  public void exteriorMeshIsDeterministic() {
    GrandfatherHouse a = new GrandfatherHouse();
    GrandfatherHouse b = new GrandfatherHouse();
    a.setStage(GrandfatherHouse.Stage.FULL);
    b.setStage(GrandfatherHouse.Stage.FULL);
    MeshGeometry ma = a.exteriorMesh();
    MeshGeometry mb = b.exteriorMesh();
    assertEquals(ma.vertexCount(), mb.vertexCount());
    for (int i = 0; i < ma.positions.length; i++) {
      assertEquals(ma.positions[i], mb.positions[i], 0f);
    }
  }

  @Test
  public void interiorHasFloorWallsAndFurniture() {
    GrandfatherHouse house = new GrandfatherHouse();
    MeshGeometry mesh = house.interiorMesh();
    assertTrue(mesh.vertexCount() > 400);
    float minY = Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    for (int i = 0; i < mesh.vertexCount(); i++) {
      float y = mesh.positions[i * 3 + 1];
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
    }
    assertTrue("floor near ground, got " + minY, Math.abs(minY - 1.2f) < 0.15f);
    assertTrue("ceiling ~3.9m with stove pipe above, got " + maxY, maxY > 3.8f && maxY < 4.2f);
  }

  @Test
  public void noNanInAnyStage() {
    GrandfatherHouse house = new GrandfatherHouse();
    for (GrandfatherHouse.Stage s : GrandfatherHouse.Stage.values()) {
      house.setStage(s);
      checkNoNan(house.exteriorMesh());
    }
    checkNoNan(house.interiorMesh());
  }

  private static void checkNoNan(MeshGeometry mesh) {
    for (float v : mesh.positions) {
      assertFalse("NaN position", Float.isNaN(v));
    }
    for (float v : mesh.colors) {
      assertFalse("NaN color", Float.isNaN(v));
    }
  }
}
