package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class VillageTest {

  private static final long SEED = 20260930L;

  private static Village village(Terrain terrain) {
    return new Village(terrain);
  }

  @Test
  public void villageSitsOutsideFlattenedFarmDisc() {
    for (Village.VillageKind kind : Village.VillageKind.values()) {
      float x = Village.positionX(kind);
      float z = Village.positionZ(kind);
      float dist = (float) Math.sqrt(x * x + z * z);
      assertTrue(kind + " dist=" + dist + " should be beyond " + Terrain.FARM_FLAT_RADIUS,
          dist > Terrain.FARM_FLAT_RADIUS);
    }
  }

  @Test
  public void buildingsRestOnTerrain() {
    Terrain terrain = new Terrain(SEED);
    Village village = village(terrain);
    for (Village.VillageKind kind : Village.VillageKind.values()) {
      Vec3[] bb = village.buildingMesh(kind).boundingBox();
      float ground = terrain.heightAt(Village.positionX(kind), Village.positionZ(kind));
      assertEquals(kind + " base should sit on terrain " + ground, ground, bb[0].y, 0.2f);
    }
  }

  @Test
  public void buildingsDoNotOverlap() {
    Terrain terrain = new Terrain(SEED);
    Village village = village(terrain);
    Village.VillageKind[] kinds = Village.VillageKind.values();
    for (int a = 0; a < kinds.length; a++) {
      for (int b = a + 1; b < kinds.length; b++) {
        Vec3[] bbA = village.buildingMesh(kinds[a]).boundingBox();
        Vec3[] bbB = village.buildingMesh(kinds[b]).boundingBox();
        boolean overlap = bbA[0].x < bbB[1].x && bbA[1].x > bbB[0].x
            && bbA[0].z < bbB[1].z && bbA[1].z > bbB[0].z;
        assertFalse("overlap between " + kinds[a] + " and " + kinds[b], overlap);
      }
    }
  }

  @Test
  public void heightsStayReasonable() {
    Terrain terrain = new Terrain(SEED);
    Village village = village(terrain);
    for (Village.VillageKind kind : Village.VillageKind.values()) {
      Vec3[] bb = village.buildingMesh(kind).boundingBox();
      float ground = terrain.heightAt(Village.positionX(kind), Village.positionZ(kind));
      assertTrue(kind + " too tall: " + (bb[1].y - ground), bb[1].y - ground < 9f);
      assertTrue(kind + " too short: " + (bb[1].y - ground), bb[1].y - ground > 2f);
    }
  }

  @Test
  public void meshIsDeterministic() {
    Village a = village(new Terrain(SEED));
    Village b = village(new Terrain(SEED));
    MeshGeometry ma = a.allMesh();
    MeshGeometry mb = b.allMesh();
    assertEquals(ma.vertexCount(), mb.vertexCount());
    for (int i = 0; i < ma.positions.length; i++) {
      assertEquals(ma.positions[i], mb.positions[i], 0f);
    }
  }

  @Test
  public void noNanInVillage() {
    Village village = village(new Terrain(SEED));
    MeshGeometry all = village.allMesh();
    assertTrue("too few vertices: " + all.vertexCount(), all.vertexCount() > 2000);
    for (float v : all.positions) {
      assertFalse("NaN position", Float.isNaN(v));
    }
    for (float v : all.colors) {
      assertFalse("NaN color", Float.isNaN(v));
    }
  }
}
