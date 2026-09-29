package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public class FarmBuildingsTest {

  private static final long SEED = 20260930L;

  @Test
  public void allPositionsSitOnFlatGround() {
    Terrain terrain = new Terrain(SEED);
    for (FarmBuildings.BuildingKind kind : FarmBuildings.BuildingKind.values()) {
      float x = FarmBuildings.positionX(kind);
      float z = FarmBuildings.positionZ(kind);
      float h = terrain.heightAt(x, z);
      float expected = Terrain.FARM_MAX_HEIGHT - River.bedDepthAt(x, z);
      assertEquals("ground under " + kind, expected, h, 0.01f);
    }
  }

  @Test
  public void buildingsDoNotOverlap() {
    FarmBuildings buildings = new FarmBuildings();
    FarmBuildings.BuildingKind[] kinds = FarmBuildings.BuildingKind.values();
    for (int a = 0; a < kinds.length; a++) {
      for (int b = a + 1; b < kinds.length; b++) {
        Vec3[] bbA = buildings.buildingMesh(kinds[a]).boundingBox();
        Vec3[] bbB = buildings.buildingMesh(kinds[b]).boundingBox();
        boolean overlap = bbA[0].x < bbB[1].x && bbA[1].x > bbB[0].x
            && bbA[0].y < bbB[1].y && bbA[1].y > bbB[0].y
            && bbA[0].z < bbB[1].z && bbA[1].z > bbB[0].z;
        assertFalse("overlap between " + kinds[a] + " and " + kinds[b], overlap);
      }
    }
  }

  @Test
  public void allBuildingsRestOnGroundAndStayReasonablyLow() {
    FarmBuildings buildings = new FarmBuildings();
    for (FarmBuildings.BuildingKind kind : FarmBuildings.BuildingKind.values()) {
      Vec3[] bb = buildings.buildingMesh(kind).boundingBox();
      assertTrue(kind + " minY=" + bb[0].y, bb[0].y >= FarmBuildings.GROUND_Y - 0.05f);
      assertTrue(kind + " maxY=" + bb[1].y, bb[1].y <= FarmBuildings.GROUND_Y + 6.5f);
    }
  }

  @Test
  public void everyBuildingHasAnUpwardFacingSurface() {
    FarmBuildings buildings = new FarmBuildings();
    for (FarmBuildings.BuildingKind kind : FarmBuildings.BuildingKind.values()) {
      MeshGeometry mesh = buildings.buildingMesh(kind);
      float maxNy = -1f;
      for (int i = 0; i < mesh.vertexCount(); i++) {
        maxNy = Math.max(maxNy, mesh.normals[i * 3 + 1]);
      }
      assertTrue(kind + " maxNy=" + maxNy, maxNy > 0.3f);
    }
  }

  @Test
  public void allMeshHasEnoughDetail() {
    FarmBuildings buildings = new FarmBuildings();
    MeshGeometry all = buildings.allMesh();
    assertTrue("too few vertices: " + all.vertexCount(), all.vertexCount() > 1500);
    assertTrue("too few triangles: " + all.triangleCount(), all.triangleCount() > 800);
    assertTrue(all.hasColors());
  }

  @Test
  public void meshIsDeterministic() {
    FarmBuildings a = new FarmBuildings();
    FarmBuildings b = new FarmBuildings();
    MeshGeometry ma = a.allMesh();
    MeshGeometry mb = b.allMesh();
    assertEquals(ma.vertexCount(), mb.vertexCount());
    for (int i = 0; i < ma.positions.length; i++) {
      assertEquals(ma.positions[i], mb.positions[i], 0f);
    }
  }

  @Test
  public void noNanInAllBuildings() {
    FarmBuildings buildings = new FarmBuildings();
    MeshGeometry all = buildings.allMesh();
    for (float v : all.positions) {
      assertFalse("NaN position", Float.isNaN(v));
    }
    for (float v : all.colors) {
      assertFalse("NaN color", Float.isNaN(v));
    }
  }
}
