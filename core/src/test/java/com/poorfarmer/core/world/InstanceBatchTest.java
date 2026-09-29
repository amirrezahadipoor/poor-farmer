package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InstanceBatchTest {

  @Test
  public void addGrowsAndCounts() {
    InstanceBatch batch = new InstanceBatch();
    for (int i = 0; i < 300; i++) {
      batch.add(i, 0f, -i, 0.3f, 1f);
    }
    assertEquals(300, batch.instanceCount());
    assertEquals(300 * InstanceBatch.FLOATS_PER_INSTANCE, batch.matrixData().length);
  }

  @Test
  public void growthPreservesEarlierInstances() {
    InstanceBatch batch = new InstanceBatch();
    batch.add(1f, 2f, 3f, 0.5f, 1.5f);
    float[] first = new float[16];
    System.arraycopy(batch.matrixData(), 0, first, 0, 16);
    for (int i = 0; i < 300; i++) {
      batch.add(0f, 0f, (float) i, 0f, 1f);
    }
    float[] after = batch.matrixData();
    for (int i = 0; i < 16; i++) {
      assertEquals(first[i], after[i], 0f);
    }
  }

  @Test
  public void clearResetsCount() {
    InstanceBatch batch = new InstanceBatch();
    batch.add(0f, 0f, 0f, 0f, 1f);
    batch.clear();
    assertEquals(0, batch.instanceCount());
    assertEquals(0, batch.matrixData().length);
  }

  @Test
  public void composeRotatesScalesAndTranslates() {
    float[] m = new float[16];
    InstanceBatch.compose(m, 0, 5f, 1f, 2f, (float) Math.PI / 2f, 2f);
    float[] world = transformPoint(m, 1f, 0f, 0f);
    assertEquals(5f, world[0], 0.01f);
    assertEquals(1f, world[1], 0.01f);
    assertEquals(0f, world[2], 0.01f);
  }

  @Test
  public void composeIdentityScalesFromOrigin() {
    float[] m = new float[16];
    InstanceBatch.compose(m, 0, 0f, 0f, 0f, 0f, 3f);
    float[] world = transformPoint(m, 1f, 2f, 1f);
    assertEquals(3f, world[0], 0.001f);
    assertEquals(6f, world[1], 0.001f);
    assertEquals(3f, world[2], 0.001f);
  }

  @Test
  public void composeTranslationOnly() {
    float[] m = new float[16];
    InstanceBatch.compose(m, 0, 5f, 1f, 2f, 0f, 1f);
    float[] world = transformPoint(m, 1f, 0f, 0f);
    assertEquals(6f, world[0], 0.001f);
    assertEquals(1f, world[1], 0.001f);
    assertEquals(2f, world[2], 0.001f);
  }

  private static float[] transformPoint(float[] m, float x, float y, float z) {
    float[] out = new float[4];
    out[0] = m[0] * x + m[4] * y + m[8] * z + m[12];
    out[1] = m[1] * x + m[5] * y + m[9] * z + m[13];
    out[2] = m[2] * x + m[6] * y + m[10] * z + m[14];
    out[3] = m[3] * x + m[7] * y + m[11] * z + m[15];
    return out;
  }
}
