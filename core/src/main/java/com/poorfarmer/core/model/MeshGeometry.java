package com.poorfarmer.core.model;

import com.poorfarmer.core.math.Quat;
import com.poorfarmer.core.math.Vec3;

public final class MeshGeometry {

  public float[] positions = new float[0];
  public float[] normals = new float[0];
  public float[] uvs = new float[0];
  public float[] colors = new float[0];
  public int[] indices = new int[0];
  private int usedVertexCount;
  private int usedIndexCount;

  public int vertexCount() {
    return usedVertexCount;
  }

  public int indexCount() {
    return usedIndexCount;
  }

  public int triangleCount() {
    return usedIndexCount / 3;
  }

  public MeshGeometry translate(float x, float y, float z) {
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      positions[i] += x;
      positions[i + 1] += y;
      positions[i + 2] += z;
    }
    return this;
  }

  public MeshGeometry scale(float x, float y, float z) {
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      positions[i] *= x;
      positions[i + 1] *= y;
      positions[i + 2] *= z;
    }
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      normals[i] *= x < 0f ? -1f : 1f;
      normals[i + 1] *= y < 0f ? -1f : 1f;
      normals[i + 2] *= z < 0f ? -1f : 1f;
    }
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      float nx = normals[i], ny = normals[i + 1], nz = normals[i + 2];
      float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
      if (len > 1e-8f) {
        float inv = 1f / len;
        normals[i] *= inv;
        normals[i + 1] *= inv;
        normals[i + 2] *= inv;
      }
    }
    return this;
  }

  public MeshGeometry rotateZ(float radians) {
    float c = (float) Math.cos(radians);
    float s = (float) Math.sin(radians);
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      float x = positions[i];
      float y = positions[i + 1];
      positions[i] = x * c - y * s;
      positions[i + 1] = x * s + y * c;
    }
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      float nx = normals[i];
      float ny = normals[i + 1];
      normals[i] = nx * c - ny * s;
      normals[i + 1] = nx * s + ny * c;
    }
    return this;
  }

  public MeshGeometry rotateX(float radians) {
    float c = (float) Math.cos(radians);
    float s = (float) Math.sin(radians);
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      float y = positions[i + 1];
      float z = positions[i + 2];
      positions[i + 1] = y * c - z * s;
      positions[i + 2] = y * s + z * c;
    }
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      float ny = normals[i + 1];
      float nz = normals[i + 2];
      normals[i + 1] = ny * c - nz * s;
      normals[i + 2] = ny * s + nz * c;
    }
    return this;
  }

  public MeshGeometry rotateY(float radians) {
    Quat q = new Quat();
    Quat.fromAxisAngle(Vec3.UP, radians, q);
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      Vec3 p = new Vec3(positions[i], positions[i + 1], positions[i + 2]);
      Vec3 out = new Vec3();
      Quat.rotateVec3(q, p, out);
      positions[i] = out.x;
      positions[i + 1] = out.y;
      positions[i + 2] = out.z;
    }
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      Vec3 n = new Vec3(normals[i], normals[i + 1], normals[i + 2]);
      Vec3 out = new Vec3();
      Quat.rotateVec3(q, n, out);
      normals[i] = out.x;
      normals[i + 1] = out.y;
      normals[i + 2] = out.z;
    }
    return this;
  }

  public Vec3[] boundingBox() {
    Vec3 min = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    Vec3 max = new Vec3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
    for (int i = 0; i < usedVertexCount * 3; i += 3) {
      min.x = Math.min(min.x, positions[i]);
      min.y = Math.min(min.y, positions[i + 1]);
      min.z = Math.min(min.z, positions[i + 2]);
      max.x = Math.max(max.x, positions[i]);
      max.y = Math.max(max.y, positions[i + 1]);
      max.z = Math.max(max.z, positions[i + 2]);
    }
    return new Vec3[]{min, max};
  }

  public void appendVertex(float x, float y, float z, float nx, float ny, float nz, float u, float v) {
    int vertex = usedVertexCount;
    positions = growFloat(positions, (vertex + 1) * 3);
    normals = growFloat(normals, (vertex + 1) * 3);
    uvs = growFloat(uvs, (vertex + 1) * 2);
    int base = vertex * 3;
    positions[base] = x;
    positions[base + 1] = y;
    positions[base + 2] = z;
    normals[base] = nx;
    normals[base + 1] = ny;
    normals[base + 2] = nz;
    uvs[vertex * 2] = u;
    uvs[vertex * 2 + 1] = v;
    usedVertexCount++;
  }

  public void appendIndex(int index) {
    if (usedIndexCount >= indices.length) {
      indices = growInt(indices, usedIndexCount + 1);
    }
    indices[usedIndexCount] = index;
    usedIndexCount++;
  }

  public boolean hasColors() {
    return colors.length >= usedVertexCount * 4;
  }

  public void setVertexColor(int vertex, float r, float g, float b, float a) {
    if (colors.length < (vertex + 1) * 4) {
      colors = growFloat(colors, (vertex + 1) * 4);
    }
    int base = vertex * 4;
    colors[base] = r;
    colors[base + 1] = g;
    colors[base + 2] = b;
    colors[base + 3] = a;
  }

  public void trimToUsed() {
    positions = java.util.Arrays.copyOf(positions, usedVertexCount * 3);
    normals = java.util.Arrays.copyOf(normals, usedVertexCount * 3);
    uvs = java.util.Arrays.copyOf(uvs, usedVertexCount * 2);
    if (colors.length > 0) {
      colors = java.util.Arrays.copyOf(colors, usedVertexCount * 4);
    }
    indices = java.util.Arrays.copyOf(indices, usedIndexCount);
  }

  public void absorb(MeshGeometry part) {
    int offset = usedVertexCount;
    positions = growFloat(positions, (offset + part.usedVertexCount) * 3);
    normals = growFloat(normals, (offset + part.usedVertexCount) * 3);
    uvs = growFloat(uvs, (offset + part.usedVertexCount) * 2);
    indices = growInt(indices, usedIndexCount + part.usedIndexCount);
    System.arraycopy(part.positions, 0, positions, offset * 3, part.usedVertexCount * 3);
    System.arraycopy(part.normals, 0, normals, offset * 3, part.usedVertexCount * 3);
    System.arraycopy(part.uvs, 0, uvs, offset * 2, part.usedVertexCount * 2);
    if (part.colors.length > 0) {
      colors = growFloat(colors, (offset + part.usedVertexCount) * 4);
      System.arraycopy(part.colors, 0, colors, offset * 4, part.usedVertexCount * 4);
    }
    for (int i = 0; i < part.usedIndexCount; i++) {
      indices[usedIndexCount + i] = part.indices[i] + offset;
    }
    usedVertexCount += part.usedVertexCount;
    usedIndexCount += part.usedIndexCount;
  }

  static float[] growFloat(float[] array, int required) {
    if (array.length >= required) {
      return array;
    }
    int newLength = Math.max(array.length * 2, required);
    float[] grown = new float[newLength];
    System.arraycopy(array, 0, grown, 0, array.length);
    return grown;
  }

  private static int[] growInt(int[] array, int required) {
    if (array.length >= required) {
      return array;
    }
    int newLength = Math.max(array.length * 2, required);
    int[] grown = new int[newLength];
    System.arraycopy(array, 0, grown, 0, array.length);
    return grown;
  }
}
