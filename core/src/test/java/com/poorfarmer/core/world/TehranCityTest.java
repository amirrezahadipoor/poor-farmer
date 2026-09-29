package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class TehranCityTest {

  private static final long SEED = 20260930L;

  private static TehranCity tehran(Terrain terrain) {
    return new TehranCity(terrain);
  }

  @Test
  public void tehranSitsFarFromTheFarm() {
    for (TehranCity.PrologueKind kind : TehranCity.PrologueKind.values()) {
      float x = TehranCity.positionX(kind);
      float z = TehranCity.positionZ(kind);
      float dist = (float) Math.sqrt(x * x + z * z);
      assertTrue(kind + " dist=" + dist + " should be well beyond the farm", dist > 80f);
    }
  }

  @Test
  public void buildingsRestOnTerrain() {
    Terrain terrain = new Terrain(SEED);
    TehranCity tehran = tehran(terrain);
    for (TehranCity.PrologueKind kind : TehranCity.PrologueKind.values()) {
      Vec3[] bb = tehran.buildingMesh(kind).boundingBox();
      float ground = terrain.heightAt(TehranCity.positionX(kind), TehranCity.positionZ(kind));
      assertEquals(kind + " base should sit on terrain " + ground, ground, bb[0].y, 0.2f);
    }
  }

  @Test
  public void buildingsDoNotOverlap() {
    Terrain terrain = new Terrain(SEED);
    TehranCity tehran = tehran(terrain);
    TehranCity.PrologueKind[] kinds = TehranCity.PrologueKind.values();
    for (int a = 0; a < kinds.length; a++) {
      for (int b = a + 1; b < kinds.length; b++) {
        Vec3[] bbA = tehran.buildingMesh(kinds[a]).boundingBox();
        Vec3[] bbB = tehran.buildingMesh(kinds[b]).boundingBox();
        boolean overlap = bbA[0].x < bbB[1].x && bbA[1].x > bbB[0].x
            && bbA[0].z < bbB[1].z && bbA[1].z > bbB[0].z;
        assertFalse("overlap between " + kinds[a] + " and " + kinds[b], overlap);
      }
    }
  }

  @Test
  public void tehranDoesNotOverlapOtherLocations() {
    Terrain terrain = new Terrain(SEED);
    Vec3[] tehranBb = tehran(terrain).allMesh().boundingBox();
    Vec3[] townBb = new Town(terrain).allMesh().boundingBox();
    Vec3[] villageBb = new Village(terrain).allMesh().boundingBox();
    boolean townOverlap = tehranBb[0].x < townBb[1].x && tehranBb[1].x > townBb[0].x
        && tehranBb[0].z < townBb[1].z && tehranBb[1].z > townBb[0].z;
    boolean villageOverlap = tehranBb[0].x < villageBb[1].x && tehranBb[1].x > villageBb[0].x
        && tehranBb[0].z < villageBb[1].z && tehranBb[1].z > villageBb[0].z;
    assertFalse("tehran overlaps town", townOverlap);
    assertFalse("tehran overlaps village", villageOverlap);
  }

  @Test
  public void towersAreTallestStructures() {
    Terrain terrain = new Terrain(SEED);
    TehranCity tehran = tehran(terrain);
    float officeHeight = tehran.buildingMesh(TehranCity.PrologueKind.COMPANY_OFFICE).boundingBox()[1].y
        - terrain.heightAt(TehranCity.positionX(TehranCity.PrologueKind.COMPANY_OFFICE),
            TehranCity.positionZ(TehranCity.PrologueKind.COMPANY_OFFICE));
    for (TehranCity.PrologueKind kind : new TehranCity.PrologueKind[]{
        TehranCity.PrologueKind.TOWER_A, TehranCity.PrologueKind.TOWER_B, TehranCity.PrologueKind.TOWER_C}) {
      float h = tehran.buildingMesh(kind).boundingBox()[1].y
          - terrain.heightAt(TehranCity.positionX(kind), TehranCity.positionZ(kind));
      assertTrue(kind + " should be taller than the office (" + h + " vs " + officeHeight + ")",
          h > officeHeight);
      assertTrue(kind + " within world scale: " + h, h < 12f);
    }
  }

  @Test
  public void tehranFocusMatchesCluster() {
    float x = 0f;
    float z = 0f;
    int n = 0;
    for (TehranCity.PrologueKind kind : TehranCity.PrologueKind.values()) {
      x += TehranCity.positionX(kind);
      z += TehranCity.positionZ(kind);
      n++;
    }
    float cx = x / n;
    float cz = z / n;
    assertTrue("focus should be near the cluster centroid (" + cx + "," + cz + ")",
        Math.abs(WorldLocation.TEHRAN.focusX() - cx) < 12f
            && Math.abs(WorldLocation.TEHRAN.focusZ() - cz) < 12f);
  }

  @Test
  public void meshIsDeterministic() {
    TehranCity a = tehran(new Terrain(SEED));
    TehranCity b = tehran(new Terrain(SEED));
    MeshGeometry ma = a.allMesh();
    MeshGeometry mb = b.allMesh();
    assertEquals(ma.vertexCount(), mb.vertexCount());
    for (int i = 0; i < ma.positions.length; i++) {
      assertEquals(ma.positions[i], mb.positions[i], 0f);
    }
  }

  @Test
  public void noNanInTehran() {
    TehranCity tehran = tehran(new Terrain(SEED));
    MeshGeometry all = tehran.allMesh();
    assertTrue("too few vertices: " + all.vertexCount(), all.vertexCount() > 1200);
    for (float v : all.positions) {
      assertFalse("NaN position", Float.isNaN(v));
    }
    for (float v : all.colors) {
      assertFalse("NaN color", Float.isNaN(v));
    }
  }
}
