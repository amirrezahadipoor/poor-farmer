package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class TerrainTest {

  private static final long SEED = 20260930L;

  @Test
  public void sameSeedIsDeterministic() {
    Terrain a = new Terrain(SEED);
    Terrain b = new Terrain(SEED);
    for (float x = -100f; x <= 100f; x += 7.3f) {
      for (float z = -100f; z <= 100f; z += 9.1f) {
        assertEquals(a.heightAt(x, z), b.heightAt(x, z), 0f);
      }
    }
  }

  @Test
  public void differentSeedsDiffer() {
    Terrain a = new Terrain(SEED);
    Terrain b = new Terrain(SEED + 1);
    boolean differs = false;
    for (float x = -100f; x <= 100f && !differs; x += 5f) {
      differs = a.heightAt(x, 20f) != b.heightAt(x, 20f);
    }
    assertTrue(differs);
  }

  @Test
  public void farmAreaIsFlattened() {
    Terrain terrain = new Terrain(SEED);
    for (float x = -25f; x <= 25f; x += 5f) {
      for (float z = -25f; z <= 25f; z += 5f) {
        float h = terrain.heightAt(x, z);
        float expected = Terrain.FARM_MAX_HEIGHT - River.bedDepthAt(x, z);
        assertEquals("at " + x + "," + z, expected, h, 0.001f);
      }
    }
  }

  @Test
  public void edgesRiseToFoothills() {
    Terrain terrain = new Terrain(SEED);
    float edge = terrain.heightAt(110f, 110f);
    float inner = terrain.heightAt(0f, 70f);
    assertTrue(edge > inner + 15f);
    assertTrue(edge > 20f);
  }

  @Test
  public void valleyIsLowerThanItsRim() {
    Terrain terrain = new Terrain(SEED);
    for (float x = -110f; x <= 110f; x += 20f) {
      if (Math.abs(x) < 45f) {
        continue;
      }
      float cz = Terrain.valleyCenterZ(x);
      float floor = terrain.heightAt(x, cz);
      float rim = terrain.heightAt(x, cz + 32f);
      assertTrue("x=" + x + " floor=" + floor + " rim=" + rim, floor < rim);
    }
  }

  @Test
  public void valleyCrossProfileIsGentle() {
    Terrain terrain = new Terrain(SEED);
    for (float x = -110f; x <= 110f; x += 25f) {
      if (Math.abs(x) < 45f) {
        continue;
      }
      float cz = Terrain.valleyCenterZ(x);
      float floor = smoothedHeight(terrain, x, cz, 0f);
      float rim = smoothedHeight(terrain, x, cz, 16f);
      assertTrue("floor should be lowest at x=" + x, floor < rim);
      float slope = (rim - floor) / 16f;
      assertTrue("cross-valley slope " + slope + " too steep at x=" + x, slope < 0.58f);
    }
  }

  private static float smoothedHeight(Terrain terrain, float x, float cz, float offset) {
    float sum = 0f;
    int count = 0;
    for (float sx = x - 12f; sx <= x + 12f; sx += 6f) {
      sum += terrain.heightAt(sx, cz + offset);
      count++;
    }
    return sum / count;
  }

  @Test
  public void heightRangeIsSane() {
    Terrain terrain = new Terrain(SEED);
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (float x = -120f; x <= 120f; x += 4f) {
      for (float z = -120f; z <= 120f; z += 4f) {
        float h = terrain.heightAt(x, z);
        if (h < min) {
          min = h;
        }
        if (h > max) {
          max = h;
        }
      }
    }
    assertTrue(min > -15f);
    assertTrue(max < 50f);
  }

  @Test
  public void normalsAreUnitAndUpwardOnFlatGround() {
    Terrain terrain = new Terrain(SEED);
    var n = terrain.normalAt(10f, 10f);
    assertEquals(1f, n.length(), 0.01f);
    assertTrue(n.y > 0.99f);
  }

  @Test
  public void colorsStayWithinUnitRange() {
    Terrain terrain = new Terrain(SEED);
    float[] c = new float[3];
    for (float x = -110f; x <= 110f; x += 11f) {
      for (float z = -110f; z <= 110f; z += 11f) {
        terrain.colorAt(x, z, c);
        for (float v : c) {
          assertTrue(v >= 0f && v <= 1f);
        }
      }
    }
  }

  @Test
  public void meshCountsMatchGrid() {
    Terrain terrain = new Terrain(SEED);
    MeshGeometry mesh = terrain.toMesh(4f);
    int cells = (int) (Terrain.WORLD_HALF * 2f / 4f);
    assertEquals((cells + 1) * (cells + 1), mesh.vertexCount());
    assertEquals(cells * cells * 6, mesh.indexCount());
    assertTrue(mesh.hasColors());
  }

  @Test
  public void windingIsCounterClockwiseUpward() {
    Terrain terrain = new Terrain(SEED);
    MeshGeometry mesh = terrain.toMesh(4f);
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
    assertTrue("triangle normal should point up, got y=" + cy, cy > 0f);
    assertTrue(mesh.normals[i0 * 3 + 1] > 0f);
  }
}
