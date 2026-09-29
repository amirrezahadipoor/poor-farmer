package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.procedural.Noise;
import org.junit.Test;

public class BackgroundMountainsTest {

  private static final long SEED = 20260930L;

  @Test
  public void heightsStayInBand() {
    Noise noise = new Noise(SEED);
    for (int a = 0; a < 72; a++) {
      float angle = a * (float) Math.PI / 36f;
      for (float radius = BackgroundMountains.INNER_RADIUS; radius <= BackgroundMountains.OUTER_RADIUS; radius += 40f) {
        float h = BackgroundMountains.heightAt(angle, radius, noise);
        assertTrue("h=" + h, h >= BackgroundMountains.MIN_HEIGHT && h <= BackgroundMountains.MAX_HEIGHT);
      }
    }
  }

  @Test
  public void mountainsRiseWithDistance() {
    Noise noise = new Noise(SEED);
    float near = 0f;
    float far = 0f;
    int samples = 0;
    for (int a = 0; a < 36; a++) {
      float angle = a * (float) Math.PI / 18f;
      near += BackgroundMountains.heightAt(angle, BackgroundMountains.INNER_RADIUS, noise);
      far += BackgroundMountains.heightAt(angle, BackgroundMountains.OUTER_RADIUS, noise);
      samples++;
    }
    assertTrue(far / samples > near / samples);
  }

  @Test
  public void ringFacesUpTowardCenter() {
    Noise noise = new Noise(SEED);
    MeshGeometry mesh = new BackgroundMountains().toMesh(noise);
    float[] p = mesh.positions;
    int i0 = mesh.indices[0];
    int i1 = mesh.indices[1];
    int i2 = mesh.indices[2];
    float ax = p[i0 * 3];
    float az = p[i0 * 3 + 2];
    float ex = p[i1 * 3] - ax;
    float ey = p[i1 * 3 + 1] - p[i0 * 3 + 1];
    float ez = p[i1 * 3 + 2] - az;
    float fx = p[i2 * 3] - ax;
    float fy = p[i2 * 3 + 1] - p[i0 * 3 + 1];
    float fz = p[i2 * 3 + 2] - az;
    float ny = ez * fx - ex * fz;
    assertTrue("inner wall should face up, got ny=" + ny, ny > 0.5f);
  }

  @Test
  public void meshCountsMatchRing() {
    Noise noise = new Noise(SEED);
    MeshGeometry mesh = new BackgroundMountains().toMesh(noise);
    assertEquals((BackgroundMountains.ANGULAR_SEGMENTS + 1) * 2, mesh.vertexCount());
    assertEquals(BackgroundMountains.ANGULAR_SEGMENTS * 6, mesh.indexCount());
  }

  @Test
  public void meshIsDeterministic() {
    MeshGeometry a = new BackgroundMountains().toMesh(new Noise(SEED));
    MeshGeometry b = new BackgroundMountains().toMesh(new Noise(SEED));
    assertEquals(a.vertexCount(), b.vertexCount());
    for (int i = 0; i < a.positions.length; i++) {
      assertEquals(a.positions[i], b.positions[i], 0f);
    }
  }
}
