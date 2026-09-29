package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.model.MeshGeometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class Mesh implements AutoCloseable {

  public static final int ATTR_POSITION = 0;
  private static int generateBuffer() {
    int[] out = new int[1];
    GLES30.glGenBuffers(1, out, 0);
    return out[0];
  }

  private static int generateVertexArray() {
    int[] out = new int[1];
    GLES30.glGenVertexArrays(1, out, 0);
    return out[0];
  }
  public static final int ATTR_NORMAL = 1;
  public static final int ATTR_UV = 2;
  public static final int ATTR_COLOR = 3;

  private final int vao;
  private final int indexBuffer;
  private final int[] vertexBuffers;
  private final int indexCount;
  private final boolean hasColors;

  private Mesh(int vao, int indexBuffer, int[] vertexBuffers, int indexCount, boolean hasColors) {
    this.vao = vao;
    this.indexBuffer = indexBuffer;
    this.vertexBuffers = vertexBuffers;
    this.indexCount = indexCount;
    this.hasColors = hasColors;
  }

  public static Mesh upload(MeshGeometry geometry) {
    int vao = generateVertexArray();
    if (vao == 0) {
      throw new RenderException("glGenVertexArrays returned 0");
    }
    GLES30.glBindVertexArray(vao);
    int[] buffers = new int[4];
    buffers[ATTR_POSITION] = uploadStaticFloatBuffer(geometry.positions, 3, ATTR_POSITION);
    buffers[ATTR_NORMAL] = uploadStaticFloatBuffer(geometry.normals, 3, ATTR_NORMAL);
    buffers[ATTR_UV] = uploadStaticFloatBuffer(geometry.uvs, 2, ATTR_UV);
    boolean hasColors = geometry.hasColors();
    if (hasColors) {
      buffers[ATTR_COLOR] = uploadStaticFloatBuffer(geometry.colors, 4, ATTR_COLOR);
    } else {
      GLES30.glDisableVertexAttribArray(ATTR_COLOR);
    }
    ByteBuffer indexData = ByteBuffer.allocateDirect(geometry.indices.length * 4).order(ByteOrder.nativeOrder());
    for (int index : geometry.indices) {
      indexData.putInt(index);
    }
    indexData.flip();
    int indexBuffer = generateBuffer();
    if (indexBuffer == 0) {
      throw new RenderException("glGenBuffers for index buffer returned 0");
    }
    GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
    GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, geometry.indices.length * 4, indexData, GLES30.GL_STATIC_DRAW);
    GLES30.glBindVertexArray(0);
    return new Mesh(vao, indexBuffer, buffers, geometry.indexCount(), hasColors);
  }

  public static int uploadStaticFloatBuffer(float[] data, int components, int attribute) {
    FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    buffer.put(data).flip();
    int vbo = generateBuffer();
    if (vbo == 0) {
      throw new RenderException("glGenBuffers for attribute " + attribute + " returned 0");
    }
    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.length * 4, buffer, GLES30.GL_STATIC_DRAW);
    GLES30.glEnableVertexAttribArray(attribute);
    GLES30.glVertexAttribPointer(attribute, components, GLES30.GL_FLOAT, false, 0, 0);
    return vbo;
  }

  public void draw() {
    GLES30.glBindVertexArray(vao);
    GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_INT, 0);
    GLES30.glBindVertexArray(0);
  }

  public int indexCount() {
    return indexCount;
  }

  public boolean hasColors() {
    return hasColors;
  }

  @Override
  public void close() {
    GLES30.glDeleteVertexArrays(1, new int[]{vao}, 0);
    GLES30.glDeleteBuffers(vertexBuffers.length, vertexBuffers, 0);
    GLES30.glDeleteBuffers(1, new int[]{indexBuffer}, 0);
  }
}
