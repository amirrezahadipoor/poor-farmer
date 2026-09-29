package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class TownTest {

  private static final long SEED = 20260930L;

  private static Town town(Terrain terrain) {
    return new Town(terrain);
  }

  @Test
  public void townSitsOutsideFlattenedFarmDisc() {
    for (Town.TownKind kind : Town.TownKind.values()) {
      float x = Town.positionX(kind);
      float z = Town.positionZ(kind);
      float dist = (float) Math.sqrt(x * x + z * z);
      assertTrue(kind + " dist=" + dist + " should be beyond " + Terrain.FARM_FLAT_RADIUS,
          dist > Terrain.FARM_FLAT_RADIUS);
    }
  }

  @Test
  public void buildingsRestOnTerrain() {
    Terrain terrain = new Terrain(SEED);
    Town town = town(terrain);
    for (Town.TownKind kind : Town.TownKind.values()) {
      Vec3[] bb = town.buildingMesh(kind).boundingBox();
      float ground = terrain.heightAt(Town.positionX(kind), Town.positionZ(kind));
      assertEquals(kind + " base should sit on terrain " + ground, ground, bb[0].y, 0.2f);
    }
  }

  @Test
  public void buildingsDoNotOverlap() {
    Terrain terrain = new Terrain(SEED);
    Town town = town(terrain);
    Town.TownKind[] kinds = Town.TownKind.values();
    for (int a = 0; a < kinds.length; a++) {
      for (int b = a + 1; b < kinds.length; b++) {
        Vec3[] bbA = town.buildingMesh(kinds[a]).boundingBox();
        Vec3[] bbB = town.buildingMesh(kinds[b]).boundingBox();
        boolean overlap = bbA[0].x < bbB[1].x && bbA[1].x > bbB[0].x
            && bbA[0].z < bbB[1].z && bbA[1].z > bbB[0].z;
        assertFalse("overlap between " + kinds[a] + " and " + kinds[b], overlap);
      }
    }
  }

  @Test
  public void townDoesNotOverlapVillage() {
    Terrain terrain = new Terrain(SEED);
    Town town = town(terrain);
    Village village = new Village(terrain);
    Vec3[] townBb = town.allMesh().boundingBox();
    Vec3[] villageBb = village.allMesh().boundingBox();
    boolean overlap = townBb[0].x < villageBb[1].x && townBb[1].x > villageBb[0].x
        && townBb[0].z < villageBb[1].z && townBb[1].z > villageBb[0].z;
    assertFalse("town and village footprints overlap", overlap);
  }

  @Test
  public void heightsStayReasonable() {
    Terrain terrain = new Terrain(SEED);
    Town town = town(terrain);
    for (Town.TownKind kind : Town.TownKind.values()) {
      Vec3[] bb = town.buildingMesh(kind).boundingBox();
      float ground = terrain.heightAt(Town.positionX(kind), Town.positionZ(kind));
      assertTrue(kind + " too tall: " + (bb[1].y - ground), bb[1].y - ground < 8f);
      assertTrue(kind + " too short: " + (bb[1].y - ground), bb[1].y - ground > 2f);
    }
  }

  @Test
  public void meshIsDeterministic() {
    Town a = town(new Terrain(SEED));
    Town b = town(new Terrain(SEED));
    MeshGeometry ma = a.allMesh();
    MeshGeometry mb = b.allMesh();
    assertEquals(ma.vertexCount(), mb.vertexCount());
    for (int i = 0; i < ma.positions.length; i++) {
      assertEquals(ma.positions[i], mb.positions[i], 0f);
    }
  }

  @Test
  public void noNanInTown() {
    Town town = town(new Terrain(SEED));
    MeshGeometry all = town.allMesh();
    assertTrue("too few vertices: " + all.vertexCount(), all.vertexCount() > 1500);
    for (float v : all.positions) {
      assertFalse("NaN position", Float.isNaN(v));
    }
    for (float v : all.colors) {
      assertFalse("NaN color", Float.isNaN(v));
    }
  }
}
