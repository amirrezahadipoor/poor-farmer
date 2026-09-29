package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.model.MeshGeometry;
import org.junit.Test;

public class CloudLayerTest {

  @Test
  public void meshCountsMatchGrid() {
    MeshGeometry mesh = new CloudLayer().toMesh();
    assertEquals((CloudLayer.SEGMENTS + 1) * (CloudLayer.SEGMENTS + 1), mesh.vertexCount());
    assertEquals(CloudLayer.SEGMENTS * CloudLayer.SEGMENTS * 6, mesh.indexCount());
  }

  @Test
  public void layerIsFlatAtAltitude() {
    MeshGeometry mesh = new CloudLayer().toMesh();
    float[] p = mesh.positions;
    for (int i = 0; i < mesh.vertexCount(); i++) {
      assertEquals(CloudLayer.HEIGHT, p[i * 3 + 1], 0.001f);
    }
    assertTrue(mesh.vertexCount() > 0);
  }

  @Test
  public void windingFacesUp() {
    MeshGeometry mesh = new CloudLayer().toMesh();
    float[] p = mesh.positions;
    int i0 = mesh.indices[0];
    int i1 = mesh.indices[1];
    int i2 = mesh.indices[2];
    float ex = p[i1 * 3] - p[i0 * 3];
    float ez = p[i1 * 3 + 2] - p[i0 * 3 + 2];
    float fx = p[i2 * 3] - p[i0 * 3];
    float fz = p[i2 * 3 + 2] - p[i0 * 3 + 2];
    float cy = ez * fx - ex * fz;
    assertTrue("cloud layer should face up, got " + cy, cy > 0f);
  }
}
