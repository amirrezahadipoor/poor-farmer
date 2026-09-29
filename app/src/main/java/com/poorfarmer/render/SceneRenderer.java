package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.world.GameCamera;
import com.poorfarmer.core.world.SunState;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class SceneRenderer implements AutoCloseable {

  private static final String TERRAIN_VERTEX =
      "#version 300 es\n"
          + "layout(location=0) in vec3 aPosition;\n"
          + "layout(location=1) in vec3 aNormal;\n"
          + "layout(location=3) in vec4 aColor;\n"
          + "uniform mat4 uView;\n"
          + "uniform mat4 uProj;\n"
          + "out vec3 vNormal;\n"
          + "out vec4 vColor;\n"
          + "out float vViewZ;\n"
          + "void main() {\n"
          + "  vNormal = aNormal;\n"
          + "  vColor = aColor;\n"
          + "  vec4 view = uView * vec4(aPosition, 1.0);\n"
          + "  vViewZ = view.z;\n"
          + "  gl_Position = uProj * view;\n"
          + "}\n";

  private static final String TERRAIN_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "in vec3 vNormal;\n"
          + "in vec4 vColor;\n"
          + "in float vViewZ;\n"
          + "uniform vec3 uSunDir;\n"
          + "uniform vec3 uSunColor;\n"
          + "uniform float uSunIntensity;\n"
          + "uniform vec3 uAmbientColor;\n"
          + "uniform float uAmbientIntensity;\n"
          + "uniform vec3 uFogColor;\n"
          + "uniform float uFogStart;\n"
          + "uniform float uFogEnd;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  float lambert = max(dot(normalize(vNormal), normalize(uSunDir)), 0.0);\n"
          + "  vec3 light = uSunColor * (uAmbientColor * uAmbientIntensity + uSunIntensity * lambert);\n"
          + "  vec3 c = vColor.rgb * light;\n"
          + "  float fog = smoothstep(uFogStart, uFogEnd, -vViewZ);\n"
          + "  c = mix(c, uFogColor, fog);\n"
          + "  frag = vec4(c, 1.0);\n"
          + "}\n";

  private final Mat4 viewScratch = new Mat4();
  private final Mat4 projectionScratch = new Mat4();
  private ShaderProgram terrainProgram;
  private Mesh terrain;
  private int viewportWidth;
  private int viewportHeight;
  private boolean ready;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    close();
    terrainProgram = ShaderProgram.create(TERRAIN_VERTEX, TERRAIN_FRAGMENT)
        .bindAttribute(Mesh.ATTR_POSITION, "aPosition")
        .bindAttribute(Mesh.ATTR_NORMAL, "aNormal")
        .bindAttribute(Mesh.ATTR_COLOR, "aColor")
        .link();
    ready = true;
  }

  public void onSurfaceChanged(GL10 gl, int width, int height) {
    viewportWidth = width;
    viewportHeight = height;
  }

  public void setTerrain(MeshGeometry geometry) {
    if (terrain != null) {
      terrain.close();
      terrain = null;
    }
    terrain = Mesh.upload(geometry);
  }

  public void draw(GameCamera camera, SunState sun) {
    if (!ready || terrain == null) {
      return;
    }
    float[] fog = sun.fogColor();
    GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
    GLES30.glClearColor(fog[0], fog[1], fog[2], 1f);
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    GLES30.glEnable(GLES30.GL_CULL_FACE);
    GLES30.glFrontFace(GLES30.GL_CCW);
    GLES30.glCullFace(GLES30.GL_BACK);
    terrainProgram.use();
    float aspect = (float) viewportWidth / (float) viewportHeight;
    camera.fillView(viewScratch);
    camera.fillProjection(projectionScratch, aspect);
    terrainProgram.uniformMat4("uView", viewScratch.data);
    terrainProgram.uniformMat4("uProj", projectionScratch.data);
    var dir = sun.direction();
    terrainProgram.uniform3f("uSunDir", dir.x, dir.y, dir.z);
    float[] sunColor = sun.sunColor();
    terrainProgram.uniform3f("uSunColor", sunColor[0], sunColor[1], sunColor[2]);
    terrainProgram.uniform1f("uSunIntensity", sun.sunIntensity());
    float[] ambient = sun.ambientColor();
    terrainProgram.uniform3f("uAmbientColor", ambient[0], ambient[1], ambient[2]);
    terrainProgram.uniform1f("uAmbientIntensity", sun.ambientIntensity());
    terrainProgram.uniform3f("uFogColor", fog[0], fog[1], fog[2]);
    terrainProgram.uniform1f("uFogStart", sun.fogStart());
    terrainProgram.uniform1f("uFogEnd", sun.fogEnd());
    terrain.draw();
  }

  public boolean hasTerrain() {
    return terrain != null;
  }

  @Override
  public void close() {
    if (terrain != null) {
      terrain.close();
      terrain = null;
    }
    if (terrainProgram != null) {
      terrainProgram.close();
      terrainProgram = null;
    }
    ready = false;
  }
}
