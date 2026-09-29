package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.world.InstanceBatch;

public final class InstancedBatchRenderer {

  private static final String VERTEX_SOURCE =
      "#version 300 es\n"
          + "layout(location = 0) in vec3 aPosition;\n"
          + "layout(location = 1) in vec3 aNormal;\n"
          + "layout(location = 2) in vec2 aUv;\n"
          + "layout(location = 4) in vec4 aInstance0;\n"
          + "layout(location = 5) in vec4 aInstance1;\n"
          + "layout(location = 6) in vec4 aInstance2;\n"
          + "layout(location = 7) in vec4 aInstance3;\n"
          + "uniform mat4 uMvp;\n"
          + "out vec3 vNormal;\n"
          + "out vec2 vUv;\n"
          + "void main() {\n"
          + "  mat4 instance = mat4(aInstance0, aInstance1, aInstance2, aInstance3);\n"
          + "  vec4 world = instance * vec4(aPosition, 1.0);\n"
          + "  vNormal = mat3(instance) * aNormal;\n"
          + "  vUv = aUv;\n"
          + "  gl_Position = uMvp * world;\n"
          + "}\n";

  private static final String FRAGMENT_SOURCE =
      "#version 300 es\n"
          + "precision mediump float;\n"
          + "in vec3 vNormal;\n"
          + "in vec2 vUv;\n"
          + "uniform vec3 uColor;\n"
          + "uniform vec3 uLightDirection;\n"
          + "out vec4 fragColor;\n"
          + "void main() {\n"
          + "  float diffuse = max(dot(normalize(vNormal), uLightDirection), 0.0);\n"
          + "  float light = 0.55 + 0.45 * diffuse;\n"
          + "  fragColor = vec4(uColor * light, 1.0);\n"
          + "}\n";

  private ShaderProgram program;
  private InstancedMesh mesh;
  private int instanceCount;

  public void onSurfaceCreated() {
    program = ShaderProgram.compile(VERTEX_SOURCE, FRAGMENT_SOURCE);
  }

  public void upload(MeshGeometry geometry, InstanceBatch batch) {
    if (mesh != null) {
      mesh.close();
    }
    mesh = InstancedMesh.upload(geometry, batch);
    instanceCount = batch.instanceCount();
  }

  public void draw(Mat4 viewProjection, float r, float g, float b, float lightX, float lightY, float lightZ) {
    if (mesh == null) {
      return;
    }
    program.use();
    program.uniformMat4("uMvp", viewProjection.data);
    program.uniform3f("uColor", r, g, b);
    program.uniform3f("uLightDirection", lightX, lightY, lightZ);
    GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    mesh.draw(instanceCount);
  }

  public void onSurfaceDestroyed() {
    if (mesh != null) {
      mesh.close();
      mesh = null;
    }
    if (program != null) {
      program.close();
      program = null;
    }
  }
}
