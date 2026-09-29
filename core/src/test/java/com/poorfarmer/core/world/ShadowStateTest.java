package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.Quality;
import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class ShadowStateTest {

  private static final SunState NOON = new SunState(12.5f, 1);
  private static final SunState DUSK = new SunState(18.9f, 1);
  private static final SunState NIGHT = new SunState(25f, 1);

  @Test
  public void shadowFallsOppositeToSunAtNoon() {
    Vec3 offset = ShadowState.groundOffset(NOON, 2f);
    assertTrue(Math.abs(offset.x) < 0.01f);
    assertTrue(offset.z > 0.3f);
  }

  @Test
  public void shadowFallsEastWhenSunIsWest() {
    SunState sunset = new SunState(18.5f, 1);
    Vec3 offset = ShadowState.groundOffset(sunset, 2f);
    assertTrue(offset.x > 0.5f);
  }

  @Test
  public void offsetIsClampedForLowSun() {
    SunState dusk = new SunState(19.2f, 1);
    float amount = ShadowState.offsetAmount(dusk, 10f);
    assertTrue(amount <= ShadowState.MAX_STRETCH + 0.01f);
  }

  @Test
  public void offsetIsZeroAtNight() {
    Vec3 offset = ShadowState.groundOffset(NIGHT, 2f);
    assertEquals(0f, offset.x, 0f);
    assertEquals(0f, offset.z, 0f);
    assertEquals(0f, ShadowState.opacity(NIGHT), 0f);
  }

  @Test
  public void opacityStrongerAtNoonThanDusk() {
    assertTrue(ShadowState.opacity(NOON) > ShadowState.opacity(DUSK));
    assertTrue(ShadowState.opacity(NOON) > 0f);
  }

  @Test
  public void blobLowTierSitsUnderObject() {
    MeshGeometry g = ShadowState.blob(5f, 0f, -3f, 1f, 2f, NOON, Quality.LOW);
    assertTrue(g.vertexCount() >= 10);
    Vec3[] box = g.boundingBox();
    assertEquals(5f, (box[0].x + box[1].x) / 2f, 0.05f);
    assertEquals(-3f, (box[0].z + box[1].z) / 2f, 0.05f);
    assertEquals(0.02f, (box[0].y + box[1].y) / 2f, 0.001f);
    assertTrue(g.hasColors());
    assertTrue(g.colors[3] > 0f);
  }

  @Test
  public void blobMediumTierShiftsAwayFromSun() {
    MeshGeometry g = ShadowState.blob(0f, 0f, 0f, 1f, 2f, NOON, Quality.MEDIUM);
    Vec3[] box = g.boundingBox();
    float centerX = (box[0].x + box[1].x) / 2f;
    float centerZ = (box[0].z + box[1].z) / 2f;
    assertTrue(centerZ > 0.2f);
    assertTrue(Math.abs(centerX) < 0.1f);
  }

  @Test
  public void blobHighTierHasMoreVerticesThanMedium() {
    MeshGeometry medium = ShadowState.blob(0f, 0f, 0f, 1f, 2f, NOON, Quality.MEDIUM);
    MeshGeometry high = ShadowState.blob(0f, 0f, 0f, 1f, 2f, NOON, Quality.HIGH);
    assertTrue(high.vertexCount() > medium.vertexCount());
  }

  @Test
  public void blobAlphaFadesToZeroAtEdge() {
    MeshGeometry g = ShadowState.blob(0f, 0f, 0f, 1f, 2f, NOON, Quality.HIGH);
    float centerAlpha = g.colors[3];
    float edgeAlpha = g.colors[g.vertexCount() * 4 - 1];
    assertTrue(centerAlpha > 0.05f);
    assertTrue(edgeAlpha < centerAlpha * 0.6f);
  }

  @Test
  public void blobWindingFacesUp() {
    MeshGeometry g = ShadowState.blob(0f, 0f, 0f, 1f, 2f, NOON, Quality.MEDIUM);
    int[] indices = g.indices;
    float[] positions = g.positions;
    for (int i = 0; i + 2 < indices.length; i += 3) {
      int a = indices[i];
      int b = indices[i + 1];
      int c = indices[i + 2];
      float ax = positions[a * 3];
      float ay = positions[a * 3 + 1];
      float az = positions[a * 3 + 2];
      float bx = positions[b * 3] - ax;
      float by = positions[b * 3 + 1] - ay;
      float bz = positions[b * 3 + 2] - az;
      float cx = positions[c * 3] - ax;
      float cy = positions[c * 3 + 1] - ay;
      float cz = positions[c * 3 + 2] - az;
      float ny = bz * cx - bx * cz;
      assertTrue(ny > -1e-6f);
    }
  }
}
