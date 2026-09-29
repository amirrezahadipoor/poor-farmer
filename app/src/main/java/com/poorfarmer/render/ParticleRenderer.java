package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.world.ParticleSystem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ParticleRenderer {

  private static final String VERTEX_SOURCE =
      "#version 300 es\n"
          + "layout(location = 0) in vec4 aParticle;\n"
          + "layout(location = 1) in vec4 aColor;\n"
          + "uniform mat4 uMvp;\n"
          + "uniform float uSizeScale;\n"
          + "out vec4 vColor;\n"
          + "void main() {\n"
          + "  vColor = aColor;\n"
          + "  gl_Position = uMvp * vec4(aParticle.xyz, 1.0);\n"
          + "  gl_PointSize = max(1.0, aParticle.w * uSizeScale / gl_Position.w);\n"
          + "}\n";

  private static final String FRAGMENT_SOURCE =
      "#version 300 es\n"
          + "precision mediump float;\n"
          + "in vec4 vColor;\n"
          + "out vec4 fragColor;\n"
          + "void main() {\n"
          + "  vec2 d = gl_PointCoord - vec2(0.5);\n"
          + "  if (dot(d, d) > 0.25) {\n"
          + "    discard;\n"
          + "  }\n"
          + "  fragColor = vColor;\n"
          + "}\n";

  private ShaderProgram program;
  private int vao;
  private int vbo;
  private int maxParticles;
  private int particleCount;

  public void onSurfaceCreated(int maxParticles) {
    this.maxParticles = maxParticles;
    program = ShaderProgram.compile(VERTEX_SOURCE, FRAGMENT_SOURCE);
    int[] out = new int[1];
    GLES30.glGenVertexArrays(1, out, 0);
    vao = out[0];
    GLES30.glBindVertexArray(vao);
    GLES30.glGenBuffers(1, out, 0);
    vbo = out[0];
    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, maxParticles * 32, null, GLES30.GL_DYNAMIC_DRAW);
    GLES30.glEnableVertexAttribArray(0);
    GLES30.glVertexAttribPointer(0, 4, GLES30.GL_FLOAT, false, 32, 0);
    GLES30.glEnableVertexAttribArray(1);
    GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 32, 16);
    GLES30.glBindVertexArray(0);
  }

  public void upload(ParticleSystem particles) {
    particles.fillRenderData();
    particleCount = particles.activeCount();
    float[] data = particles.renderData();
    FloatBuffer buffer = ByteBuffer.allocateDirect(particleCount * 32)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
    buffer.put(data, 0, particleCount * 8).flip();
    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, particleCount * 32, buffer, GLES30.GL_DYNAMIC_DRAW);
  }

  public void draw(Mat4 viewProjection) {
    if (particleCount == 0) {
      return;
    }
    program.use();
    program.uniformMat4("uMvp", viewProjection.data);
    program.uniform1f("uSizeScale", 500f);
    GLES30.glBindVertexArray(vao);
    GLES30.glEnable(GLES30.GL_BLEND);
    GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
    GLES30.glDepthMask(false);
    GLES30.glDrawArrays(GLES30.GL_POINTS, 0, particleCount);
    GLES30.glDepthMask(true);
    GLES30.glDisable(GLES30.GL_BLEND);
    GLES30.glBindVertexArray(0);
  }

  public void onSurfaceDestroyed() {
    if (vbo != 0) {
      GLES30.glDeleteBuffers(1, new int[]{vbo}, 0);
      vbo = 0;
    }
    if (vao != 0) {
      GLES30.glDeleteVertexArrays(1, new int[]{vao}, 0);
      vao = 0;
    }
    if (program != null) {
      program.close();
      program = null;
    }
  }
}
