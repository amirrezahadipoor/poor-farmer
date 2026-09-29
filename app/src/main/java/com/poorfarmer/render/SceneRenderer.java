package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.farm.FarmGrid;
import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.world.GameCamera;
import com.poorfarmer.core.world.SunState;
import com.poorfarmer.core.world.Terrain;

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

  private static final String LIT_FRAGMENT =
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

  private static final String FARM_VERTEX =
      "#version 300 es\n"
          + "layout(location=0) in vec3 aPosition;\n"
          + "layout(location=1) in vec3 aNormal;\n"
          + "layout(location=2) in vec2 aUV;\n"
          + "layout(location=3) in vec4 aColor;\n"
          + "uniform mat4 uView;\n"
          + "uniform mat4 uProj;\n"
          + "out vec3 vNormal;\n"
          + "out vec4 vColor;\n"
          + "out vec2 vUV;\n"
          + "out float vViewZ;\n"
          + "void main() {\n"
          + "  vNormal = aNormal;\n"
          + "  vColor = aColor;\n"
          + "  vUV = aUV;\n"
          + "  vec4 view = uView * vec4(aPosition, 1.0);\n"
          + "  vViewZ = view.z;\n"
          + "  gl_Position = uProj * view;\n"
          + "}\n";

  private static final String FARM_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "in vec3 vNormal;\n"
          + "in vec4 vColor;\n"
          + "in vec2 vUV;\n"
          + "in float vViewZ;\n"
          + "uniform vec3 uSunDir;\n"
          + "uniform vec3 uSunColor;\n"
          + "uniform float uSunIntensity;\n"
          + "uniform vec3 uAmbientColor;\n"
          + "uniform float uAmbientIntensity;\n"
          + "uniform vec3 uFogColor;\n"
          + "uniform float uFogStart;\n"
          + "uniform float uFogEnd;\n"
          + "uniform sampler2D uMoisture;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  float lambert = max(dot(normalize(vNormal), normalize(uSunDir)), 0.0);\n"
          + "  vec3 light = uSunColor * (uAmbientColor * uAmbientIntensity + uSunIntensity * lambert);\n"
          + "  float m = texture(uMoisture, vUV).r;\n"
          + "  vec3 c = vColor.rgb * (1.0 - 0.4 * vColor.a * m) * light;\n"
          + "  float fog = smoothstep(uFogStart, uFogEnd, -vViewZ);\n"
          + "  c = mix(c, uFogColor, fog);\n"
          + "  frag = vec4(c, 1.0);\n"
          + "}\n";

  private static final float[] WILD_GREEN = {0.38f, 0.55f, 0.26f};
  private static final float[] PLOWED_BROWN = {0.45f, 0.32f, 0.19f};
  private static final float[] DITCH_WATER = {0.20f, 0.42f, 0.70f};
  private static final float WILD_Y = Terrain.FARM_MAX_HEIGHT + 0.03f;
  private static final float DITCH_Y = Terrain.FARM_MAX_HEIGHT + 0.012f;

  private final Mat4 viewScratch = new Mat4();
  private final Mat4 projectionScratch = new Mat4();
  private final java.nio.FloatBuffer moistureBuffer =
      java.nio.ByteBuffer.allocateDirect(FarmGrid.CELLS * FarmGrid.CELLS * 4 * 4)
          .order(java.nio.ByteOrder.nativeOrder())
          .asFloatBuffer();
  private ShaderProgram terrainProgram;
  private ShaderProgram farmProgram;
  private Mesh terrain;
  private Mesh farmOverlay;
  private int farmTexture;
  private int farmVersion = -1;
  private int viewportWidth;
  private int viewportHeight;
  private boolean ready;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    close();
    terrainProgram = ShaderProgram.create(TERRAIN_VERTEX, LIT_FRAGMENT)
        .bindAttribute(Mesh.ATTR_POSITION, "aPosition")
        .bindAttribute(Mesh.ATTR_NORMAL, "aNormal")
        .bindAttribute(Mesh.ATTR_COLOR, "aColor")
        .link();
    farmProgram = ShaderProgram.create(FARM_VERTEX, FARM_FRAGMENT)
        .bindAttribute(Mesh.ATTR_POSITION, "aPosition")
        .bindAttribute(Mesh.ATTR_NORMAL, "aNormal")
        .bindAttribute(Mesh.ATTR_UV, "aUV")
        .bindAttribute(Mesh.ATTR_COLOR, "aColor")
        .link();
    int[] tex = {0};
    GLES30.glGenTextures(1, tex, 0);
    farmTexture = tex[0];
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, farmTexture);
    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA32F,
        FarmGrid.CELLS, FarmGrid.CELLS, 0, GLES30.GL_RGBA, GLES30.GL_FLOAT, null);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
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

  public void draw(GameCamera camera, SunState sun, FarmGrid farm) {
    if (!ready || terrain == null) {
      return;
    }
    if (farm != null && farm.stateVersion() != farmVersion) {
      rebuildFarmOverlay(farm);
    }
    float[] fog = sun.fogColor();
    GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
    GLES30.glClearColor(fog[0], fog[1], fog[2], 1f);
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    GLES30.glEnable(GLES30.GL_CULL_FACE);
    GLES30.glFrontFace(GLES30.GL_CCW);
    GLES30.glCullFace(GLES30.GL_BACK);
    float aspect = (float) viewportWidth / (float) viewportHeight;
    camera.fillView(viewScratch);
    camera.fillProjection(projectionScratch, aspect);
    terrainProgram.use();
    applySharedUniforms(terrainProgram, sun, fog);
    terrain.draw();
    if (farmOverlay != null) {
      uploadMoisture(farm);
      farmProgram.use();
      applySharedUniforms(farmProgram, sun, fog);
      GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, farmTexture);
      farmProgram.uniform1i("uMoisture", 0);
      farmOverlay.draw();
    }
  }

  private void applySharedUniforms(ShaderProgram program, SunState sun, float[] fog) {
    program.uniformMat4("uView", viewScratch.data);
    program.uniformMat4("uProj", projectionScratch.data);
    var dir = sun.direction();
    program.uniform3f("uSunDir", dir.x, dir.y, dir.z);
    float[] sunColor = sun.sunColor();
    program.uniform3f("uSunColor", sunColor[0], sunColor[1], sunColor[2]);
    program.uniform1f("uSunIntensity", sun.sunIntensity());
    float[] ambient = sun.ambientColor();
    program.uniform3f("uAmbientColor", ambient[0], ambient[1], ambient[2]);
    program.uniform1f("uAmbientIntensity", sun.ambientIntensity());
    program.uniform3f("uFogColor", fog[0], fog[1], fog[2]);
    program.uniform1f("uFogStart", sun.fogStart());
    program.uniform1f("uFogEnd", sun.fogEnd());
  }

  private void uploadMoisture(FarmGrid farm) {
    int i = 0;
    for (int y = 0; y < FarmGrid.CELLS; y++) {
      for (int x = 0; x < FarmGrid.CELLS; x++) {
        float m = farm.moistureAt(x, y);
        moistureBuffer.put(i++, m);
        moistureBuffer.put(i++, 0f);
        moistureBuffer.put(i++, 0f);
        moistureBuffer.put(i++, 1f);
      }
    }
    moistureBuffer.position(0);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, farmTexture);
    GLES30.glTexSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0,
        FarmGrid.CELLS, FarmGrid.CELLS, GLES30.GL_RGBA, GLES30.GL_FLOAT, moistureBuffer);
  }

  private void rebuildFarmOverlay(FarmGrid grid) {
    MeshGeometry g = new MeshGeometry();
    int cells = FarmGrid.CELLS;
    for (int y = 0; y < cells; y++) {
      for (int x = 0; x < cells; x++) {
        byte st = grid.stateAt(x, y);
        float cx = FarmGrid.cellCenterX(x);
        float cz = FarmGrid.cellCenterZ(y);
        float ty = st == FarmGrid.STATE_DITCH ? DITCH_Y : WILD_Y;
        float h = FarmGrid.CELL_SIZE / 2f;
        float r;
        float gg;
        float b;
        float wettable;
        if (st == FarmGrid.STATE_DITCH) {
          r = DITCH_WATER[0];
          gg = DITCH_WATER[1];
          b = DITCH_WATER[2];
          wettable = 0f;
        } else if (st == FarmGrid.STATE_PLOWED) {
          r = PLOWED_BROWN[0];
          gg = PLOWED_BROWN[1];
          b = PLOWED_BROWN[2];
          wettable = 1f;
        } else {
          float checker = ((x + y) & 1) == 0 ? 1f : 0.94f;
          r = WILD_GREEN[0] * checker;
          gg = WILD_GREEN[1] * checker;
          b = WILD_GREEN[2] * checker;
          wettable = 0.6f;
        }
        float u = (x + 0.5f) / cells;
        float v = (y + 0.5f) / cells;
        int a = g.vertexCount();
        g.appendVertex(cx - h, ty, cz - h, 0f, 1f, 0f, u, v);
        g.appendVertex(cx + h, ty, cz - h, 0f, 1f, 0f, u, v);
        g.appendVertex(cx + h, ty, cz + h, 0f, 1f, 0f, u, v);
        g.appendVertex(cx - h, ty, cz + h, 0f, 1f, 0f, u, v);
        g.setVertexColor(a, r, gg, b, wettable);
        g.setVertexColor(a + 1, r, gg, b, wettable);
        g.setVertexColor(a + 2, r, gg, b, wettable);
        g.setVertexColor(a + 3, r, gg, b, wettable);
        g.appendIndex(a);
        g.appendIndex(a + 2);
        g.appendIndex(a + 1);
        g.appendIndex(a + 1);
        g.appendIndex(a + 2);
        g.appendIndex(a + 3);
      }
    }
    g.trimToUsed();
    if (farmOverlay != null) {
      farmOverlay.close();
    }
    farmOverlay = Mesh.upload(g);
    farmVersion = grid.stateVersion();
  }

  @Override
  public void close() {
    if (terrain != null) {
      terrain.close();
      terrain = null;
    }
    if (farmOverlay != null) {
      farmOverlay.close();
      farmOverlay = null;
    }
    if (farmTexture != 0) {
      GLES30.glDeleteTextures(1, new int[]{farmTexture}, 0);
      farmTexture = 0;
    }
    if (terrainProgram != null) {
      terrainProgram.close();
      terrainProgram = null;
    }
    if (farmProgram != null) {
      farmProgram.close();
      farmProgram = null;
    }
    farmVersion = -1;
    ready = false;
  }
}
