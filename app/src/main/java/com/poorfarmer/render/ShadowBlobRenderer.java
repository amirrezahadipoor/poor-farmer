package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.model.MeshGeometry;

public final class ShadowBlobRenderer {

  private static final String VERTEX_SOURCE =
      "#version 300 es\n"
          + "layout(location = 0) in vec3 aPosition;\n"
          + "layout(location = 3) in vec4 aColor;\n"
          + "uniform mat4 uMvp;\n"
          + "out vec4 vColor;\n"
          + "void main() {\n"
          + "  vColor = aColor;\n"
          + "  gl_Position = uMvp * vec4(aPosition, 1.0);\n"
          + "}\n";

  private static final String FRAGMENT_SOURCE =
      "#version 300 es\n"
          + "precision mediump float;\n"
          + "in vec4 vColor;\n"
          + "out vec4 fragColor;\n"
          + "void main() {\n"
          + "  fragColor = vColor;\n"
          + "}\n";

  private ShaderProgram program;
  private Mesh mesh;

  public void onSurfaceCreated() {
    program = ShaderProgram.compile(VERTEX_SOURCE, FRAGMENT_SOURCE);
  }

  public void upload(MeshGeometry geometry) {
    if (mesh != null) {
      mesh.close();
    }
    mesh = Mesh.upload(geometry);
  }

  public void draw(Mat4 viewProjection) {
    if (mesh == null || !mesh.hasColors()) {
      return;
    }
    program.use();
    program.uniformMat4("uMvp", viewProjection.data);
    GLES30.glEnable(GLES30.GL_BLEND);
    GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
    GLES30.glDepthMask(false);
    mesh.draw();
    GLES30.glDepthMask(true);
    GLES30.glDisable(GLES30.GL_BLEND);
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
