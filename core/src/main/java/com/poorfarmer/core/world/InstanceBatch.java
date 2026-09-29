package com.poorfarmer.core.world;

public final class InstanceBatch {

  public static final int FLOATS_PER_INSTANCE = 16;

  private float[] matrices = new float[0];
  private int count;

  public void add(float x, float y, float z, float rotationY, float scale) {
    if (matrices.length < (count + 1) * FLOATS_PER_INSTANCE) {
      int newLength = matrices.length == 0 ? 64 * FLOATS_PER_INSTANCE : matrices.length * 2;
      float[] grown = new float[newLength];
      System.arraycopy(matrices, 0, grown, 0, matrices.length);
      matrices = grown;
    }
    compose(matrices, count * FLOATS_PER_INSTANCE, x, y, z, rotationY, scale);
    count++;
  }

  public int instanceCount() {
    return count;
  }

  public float[] matrixData() {
    float[] copy = new float[count * FLOATS_PER_INSTANCE];
    System.arraycopy(matrices, 0, copy, 0, copy.length);
    return copy;
  }

  public void clear() {
    count = 0;
  }

  public static void compose(float[] out, int offset, float x, float y, float z, float rotationY, float scale) {
    float c = (float) Math.cos(rotationY);
    float s = (float) Math.sin(rotationY);
    out[offset + 0] = c * scale;
    out[offset + 1] = 0f;
    out[offset + 2] = -s * scale;
    out[offset + 3] = 0f;
    out[offset + 4] = 0f;
    out[offset + 5] = scale;
    out[offset + 6] = 0f;
    out[offset + 7] = 0f;
    out[offset + 8] = s * scale;
    out[offset + 9] = 0f;
    out[offset + 10] = c * scale;
    out[offset + 11] = 0f;
    out[offset + 12] = x;
    out[offset + 13] = y;
    out[offset + 14] = z;
    out[offset + 15] = 1f;
  }
}
