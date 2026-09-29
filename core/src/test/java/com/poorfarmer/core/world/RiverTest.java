package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class RiverTest {

  private static final long SEED = 20260930L;

  @Test
  public void riverFollowsValleyCenterline() {
    for (float x = -120f; x <= 120f; x += 10f) {
      assertEquals(Terrain.valleyCenterZ(x), River.centerZ(x), 0f);
    }
  }

  @Test
  public void widthStaysInBand() {
    for (float x = -120f; x <= 120f; x += 5f) {
      float hw = River.halfWidth(x);
      assertTrue("hw=" + hw, hw > 2f && hw < 4f);
    }
  }

  @Test
  public void bedDepthIsZeroAwayAndMaxAtCenter() {
    for (float x = -100f; x <= 100f; x += 25f) {
      float cz = River.centerZ(x);
      assertEquals(River.BED_DEPTH, River.bedDepthAt(x, cz), 0.01f);
      assertEquals(0f, River.bedDepthAt(x, cz + 30f), 0.001f);
      float mid = River.bedDepthAt(x, cz + River.halfWidth(x) / 2f);
      assertEquals(0.5f * River.BED_DEPTH, mid, 0.01f);
    }
  }

  @Test
  public void waterSitsBelowTheBanks() {
    Terrain terrain = new Terrain(SEED);
    River river = new River();
    for (float x = -110f; x <= 110f; x += 15f) {
      float cz = River.centerZ(x);
      float hw = River.halfWidth(x);
      float water = river.surfaceY(terrain, x);
      float bankLeft = terrain.heightAt(x, cz - hw * 1.3f);
      float bankRight = terrain.heightAt(x, cz + hw * 1.3f);
      float bed = terrain.heightAt(x, cz);
      assertTrue("water above bed at x=" + x, water > bed);
      assertTrue("water above bank left at x=" + x, water < bankLeft);
      assertTrue("water above bank right at x=" + x, water < bankRight);
    }
  }

  @Test
  public void riverCarvesThroughFlattenedFarm() {
    Terrain terrain = new Terrain(SEED);
    float farmEdge = terrain.heightAt(0f, 8f);
    float farmRiver = terrain.heightAt(0f, River.centerZ(0f));
    assertTrue(farmRiver < farmEdge - 0.8f);
  }

  @Test
  public void meshCountsMatchRibbon() {
    Terrain terrain = new Terrain(SEED);
    MeshGeometry mesh = new River().toMesh(terrain, 2f);
    int cells = (int) (Terrain.WORLD_HALF * 2f / 2f);
    assertEquals(cells * 2 + 2, mesh.vertexCount());
    assertEquals(cells * 6, mesh.indexCount());
  }

  @Test
  public void windingFacesUp() {
    Terrain terrain = new Terrain(SEED);
    MeshGeometry mesh = new River().toMesh(terrain, 2f);
    float[] p = mesh.positions;
    int i0 = mesh.indices[0];
    int i1 = mesh.indices[1];
    int i2 = mesh.indices[2];
    float ex = p[i1 * 3] - p[i0 * 3];
    float ey = p[i1 * 3 + 1] - p[i0 * 3 + 1];
    float ez = p[i1 * 3 + 2] - p[i0 * 3 + 2];
    float fx = p[i2 * 3] - p[i0 * 3];
    float fy = p[i2 * 3 + 1] - p[i0 * 3 + 1];
    float fz = p[i2 * 3 + 2] - p[i0 * 3 + 2];
    float cy = ez * fx - ex * fz;
    assertTrue("river normal should point up, got y=" + cy, cy > 0f);
  }

  @Test
  public void meshIsDeterministic() {
    Terrain terrain = new Terrain(SEED);
    MeshGeometry a = new River().toMesh(terrain, 2f);
    MeshGeometry b = new River().toMesh(terrain, 2f);
    assertEquals(a.vertexCount(), b.vertexCount());
    for (int i = 0; i < a.positions.length; i++) {
      assertEquals(a.positions[i], b.positions[i], 0f);
    }
  }
}
