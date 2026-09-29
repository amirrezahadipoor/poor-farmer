package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.world.InstanceBatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class InstancedMesh implements AutoCloseable {

  public static final int INSTANCE_ATTRIBUTE_BASE = 4;

  private final int vao;
  private final int instanceBuffer;
  private final int indexBuffer;
  private final int[] vertexBuffers;
  private final int indexCount;

  private InstancedMesh(int vao, int instanceBuffer, int indexBuffer, int[] vertexBuffers, int indexCount) {
    this.vao = vao;
    this.instanceBuffer = instanceBuffer;
    this.indexBuffer = indexBuffer;
    this.vertexBuffers = vertexBuffers;
    this.indexCount = indexCount;
  }

  public static InstancedMesh upload(MeshGeometry geometry, InstanceBatch batch) {
    int vao = glGenVertexArray();
    GLES30.glBindVertexArray(vao);
    int[] buffers = new int[4];
    buffers[0] = Mesh.uploadStaticFloatBuffer(geometry.positions, 3, 0);
    buffers[1] = Mesh.uploadStaticFloatBuffer(geometry.normals, 3, 1);
    buffers[2] = Mesh.uploadStaticFloatBuffer(geometry.uvs, 2, 2);
    GLES30.glDisableVertexAttribArray(3);
    int indexBuffer = uploadIndices(geometry.indices);
    float[] instanceData = batch.matrixData();
    int instanceBuffer = glGenBuffer();
    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, instanceBuffer);
    FloatBuffer instanceFloats = ByteBuffer.allocateDirect(instanceData.length * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
    instanceFloats.put(instanceData).flip();
    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, instanceData.length * 4, instanceFloats, GLES30.GL_STATIC_DRAW);
    for (int i = 0; i < 4; i++) {
      int attribute = INSTANCE_ATTRIBUTE_BASE + i;
      GLES30.glEnableVertexAttribArray(attribute);
      GLES30.glVertexAttribPointer(attribute, 4, GLES30.GL_FLOAT, false, 16, i * 16);
      GLES30.glVertexAttribDivisor(attribute, 1);
    }
    GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
    GLES30.glBindVertexArray(0);
    return new InstancedMesh(vao, instanceBuffer, indexBuffer, buffers, geometry.indexCount());
  }

  public static int uploadIndices(int[] indices) {
    int indexBuffer = glGenBuffer();
    GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
    ByteBuffer indexData = ByteBuffer.allocateDirect(indices.length * 4).order(ByteOrder.nativeOrder());
    for (int index : indices) {
      indexData.putInt(index);
    }
    indexData.flip();
    GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, indexData, GLES30.GL_STATIC_DRAW);
    return indexBuffer;
  }

  public void draw(int instanceCount) {
    GLES30.glBindVertexArray(vao);
    GLES30.glDrawElementsInstanced(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_INT, 0, instanceCount);
    GLES30.glBindVertexArray(0);
  }

  @Override
  public void close() {
    GLES30.glBindVertexArray(vao);
    GLES30.glDisableVertexAttribArray(4);
    GLES30.glDisableVertexAttribArray(5);
    GLES30.glDisableVertexAttribArray(6);
    GLES30.glDisableVertexAttribArray(7);
    for (int i = 4; i < 8; i++) {
      GLES30.glVertexAttribDivisor(i, 0);
    }
    GLES30.glBindVertexArray(0);
    int count = 0;
    int[] delete = new int[vertexBuffers.length + 2];
    for (int buffer : vertexBuffers) {
      if (buffer != 0) {
        delete[count++] = buffer;
      }
    }
    delete[count++] = instanceBuffer;
    delete[count++] = indexBuffer;
    GLES30.glDeleteVertexArrays(1, new int[]{vao}, 0);
    GLES30.glDeleteBuffers(count, delete, 0);
  }

  private static int glGenBuffer() {
    int[] out = new int[1];
    GLES30.glGenBuffers(1, out, 0);
    return out[0];
  }

  private static int glGenVertexArray() {
    int[] out = new int[1];
    GLES30.glGenVertexArrays(1, out, 0);
    return out[0];
  }
}
