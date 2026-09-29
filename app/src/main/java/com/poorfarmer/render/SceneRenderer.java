package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.farm.FarmGrid;
import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.world.GameCamera;
import com.poorfarmer.core.world.GrandfatherHouse;
import com.poorfarmer.core.world.SkyPalette;
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

  private static final String WATER_COMMON =
      "float hash21(vec2 p) {\n"
          + "  p = fract(p * vec2(234.34, 435.345));\n"
          + "  p += dot(p, p + 34.23);\n"
          + "  return fract(p.x * p.y);\n"
          + "}\n"
          + "float noise2(vec2 p) {\n"
          + "  vec2 i = floor(p);\n"
          + "  vec2 f = fract(p);\n"
          + "  vec2 u = f * f * (3.0 - 2.0 * f);\n"
          + "  float a = hash21(i);\n"
          + "  float b = hash21(i + vec2(1.0, 0.0));\n"
          + "  float c = hash21(i + vec2(0.0, 1.0));\n"
          + "  float d = hash21(i + vec2(1.0, 1.0));\n"
          + "  return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);\n"
          + "}\n"
          + "vec3 waterWave(vec2 worldXZ, float time) {\n"
          + "  vec2 flow = vec2(worldXZ.x * 0.9 - time * 0.6, worldXZ.z * 2.2);\n"
          + "  float n1 = noise2(flow);\n"
          + "  float n2 = noise2(flow * 2.3 + 11.7);\n"
          + "  return vec3((n1 - 0.5) * 0.4, 1.0, (n2 - 0.5) * 0.4);\n"
          + "}\n";

  private static final String WATER_VERTEX =
      "#version 300 es\n"
          + "layout(location=0) in vec3 aPosition;\n"
          + "layout(location=1) in vec3 aNormal;\n"
          + "layout(location=3) in vec4 aColor;\n"
          + "uniform mat4 uView;\n"
          + "uniform mat4 uProj;\n"
          + "out vec3 vNormal;\n"
          + "out vec4 vColor;\n"
          + "out float vViewZ;\n"
          + "out vec3 vWorldPos;\n"
          + "void main() {\n"
          + "  vNormal = aNormal;\n"
          + "  vColor = aColor;\n"
          + "  vWorldPos = aPosition;\n"
          + "  vec4 view = uView * vec4(aPosition, 1.0);\n"
          + "  vViewZ = view.z;\n"
          + "  gl_Position = uProj * view;\n"
          + "}\n";

  private static final String WATER_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + WATER_COMMON
          + "in vec3 vNormal;\n"
          + "in vec4 vColor;\n"
          + "in float vViewZ;\n"
          + "in vec3 vWorldPos;\n"
          + "uniform vec3 uSunDir;\n"
          + "uniform vec3 uSunColor;\n"
          + "uniform float uSunIntensity;\n"
          + "uniform vec3 uAmbientColor;\n"
          + "uniform float uAmbientIntensity;\n"
          + "uniform vec3 uFogColor;\n"
          + "uniform float uFogStart;\n"
          + "uniform float uFogEnd;\n"
          + "uniform vec3 uCamPos;\n"
          + "uniform float uTime;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  vec3 n = normalize(waterWave(vWorldPos.xz, uTime));\n"
          + "  vec3 viewDir = normalize(uCamPos - vWorldPos);\n"
          + "  float lambert = max(dot(n, uSunDir), 0.0);\n"
          + "  vec3 light = uSunColor * (uAmbientColor * uAmbientIntensity + uSunIntensity * lambert);\n"
          + "  vec3 c = vColor.rgb * light;\n"
          + "  float spec = pow(max(dot(reflect(-uSunDir, n), viewDir), 0.0), 64.0);\n"
          + "  c += uSunColor * spec * uSunIntensity * 0.8;\n"
          + "  float fresnel = 0.3 + 0.7 * pow(1.0 - max(dot(n, viewDir), 0.0), 3.0);\n"
          + "  c = mix(c, uFogColor, fresnel * 0.45);\n"
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
          + "out vec3 vWorldPos;\n"
          + "void main() {\n"
          + "  vNormal = aNormal;\n"
          + "  vColor = aColor;\n"
          + "  vUV = aUV;\n"
          + "  vWorldPos = aPosition;\n"
          + "  vec4 view = uView * vec4(aPosition, 1.0);\n"
          + "  vViewZ = view.z;\n"
          + "  gl_Position = uProj * view;\n"
          + "}\n";

  private static final String FARM_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + WATER_COMMON
          + "in vec3 vNormal;\n"
          + "in vec4 vColor;\n"
          + "in vec2 vUV;\n"
          + "in float vViewZ;\n"
          + "in vec3 vWorldPos;\n"
          + "uniform vec3 uSunDir;\n"
          + "uniform vec3 uSunColor;\n"
          + "uniform float uSunIntensity;\n"
          + "uniform vec3 uAmbientColor;\n"
          + "uniform float uAmbientIntensity;\n"
          + "uniform vec3 uFogColor;\n"
          + "uniform float uFogStart;\n"
          + "uniform float uFogEnd;\n"
          + "uniform vec3 uCamPos;\n"
          + "uniform float uTime;\n"
          + "uniform sampler2D uMoisture;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  vec3 viewDir = normalize(uCamPos - vWorldPos);\n"
          + "  float lambert = max(dot(vNormal, uSunDir), 0.0);\n"
          + "  vec3 light = uSunColor * (uAmbientColor * uAmbientIntensity + uSunIntensity * lambert);\n"
          + "  vec3 c;\n"
          + "  if (vColor.a > 1.5) {\n"
          + "    vec3 n = normalize(waterWave(vWorldPos.xz, uTime));\n"
          + "    float spec = pow(max(dot(reflect(-uSunDir, n), viewDir), 0.0), 64.0);\n"
          + "    float fresnel = 0.3 + 0.7 * pow(1.0 - max(dot(n, viewDir), 0.0), 3.0);\n"
          + "    c = vColor.rgb * light;\n"
          + "    c += uSunColor * spec * uSunIntensity * 0.8;\n"
          + "    c = mix(c, uFogColor, fresnel * 0.45);\n"
          + "  } else {\n"
          + "    float m = texture(uMoisture, vUV).r;\n"
          + "    c = vColor.rgb * (1.0 - 0.4 * vColor.a * m) * light;\n"
          + "  }\n"
          + "  float fog = smoothstep(uFogStart, uFogEnd, -vViewZ);\n"
          + "  c = mix(c, uFogColor, fog);\n"
          + "  frag = vec4(c, 1.0);\n"
          + "}\n";

  private static final float[] WILD_GREEN = {0.38f, 0.55f, 0.26f};
  private static final float[] PLOWED_BROWN = {0.45f, 0.32f, 0.19f};
  private static final float[] DITCH_WATER = {0.20f, 0.42f, 0.70f};
  private static final float WILD_Y = Terrain.FARM_MAX_HEIGHT + 0.03f;
  private static final float DITCH_Y = Terrain.FARM_MAX_HEIGHT + 0.012f;
  private static final float DITCH_WATER_FLAG = 2f;

  private final Mat4 viewScratch = new Mat4();
  private final Mat4 projectionScratch = new Mat4();
  private final Vec3 eyeScratch = new Vec3();
  private final java.nio.FloatBuffer moistureBuffer =
      java.nio.ByteBuffer.allocateDirect(FarmGrid.CELLS * FarmGrid.CELLS * 4 * 4)
          .order(java.nio.ByteOrder.nativeOrder())
          .asFloatBuffer();
  private ShaderProgram terrainProgram;
  private ShaderProgram farmProgram;
  private ShaderProgram waterProgram;
  private Mesh terrain;
  private Mesh farmOverlay;
  private Mesh river;
  private Mesh mountains;
  private GrandfatherHouse house;
  private Mesh houseMesh;
  private Mesh houseInteriorMesh;
  private int houseVersion = -1;
  private final SkyRenderer skyRenderer = new SkyRenderer();
  private final CloudRenderer cloudRenderer = new CloudRenderer();
  private int farmTexture;
  private int farmVersion = -1;
  private int viewportWidth;
  private int viewportHeight;
  private boolean cloudsEnabled = true;
  private boolean ready;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    close();
    skyRenderer.onSurfaceCreated(gl, config);
    cloudRenderer.onSurfaceCreated(gl, config);
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
    waterProgram = ShaderProgram.create(WATER_VERTEX, WATER_FRAGMENT)
        .bindAttribute(Mesh.ATTR_POSITION, "aPosition")
        .bindAttribute(Mesh.ATTR_NORMAL, "aNormal")
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
    skyRenderer.setViewport(width, height);
  }

  public void setCloudsEnabled(boolean enabled) {
    cloudsEnabled = enabled;
  }

  public void setMountains(MeshGeometry geometry) {
    if (mountains != null) {
      mountains.close();
      mountains = null;
    }
    mountains = Mesh.upload(geometry);
  }

  public void setCloudLayer(MeshGeometry geometry) {
    cloudRenderer.setCloudLayer(geometry);
  }

  public void setHouse(GrandfatherHouse grandfatherHouse) {
    house = grandfatherHouse;
  }

  public void setTerrain(MeshGeometry geometry) {
    if (terrain != null) {
      terrain.close();
      terrain = null;
    }
    terrain = Mesh.upload(geometry);
  }

  public void setRiver(MeshGeometry geometry) {
    if (river != null) {
      river.close();
      river = null;
    }
    river = Mesh.upload(geometry);
  }

  public void draw(GameCamera camera, SunState sun, SkyPalette palette, FarmGrid farm,
                   boolean houseInterior, float timeSeconds) {
    if (!ready || terrain == null) {
      return;
    }
    if (farm != null && farm.stateVersion() != farmVersion) {
      rebuildFarmOverlay(farm);
    }
    if (house != null && house.version() != houseVersion) {
      rebuildHouseMeshes();
    }
    float[] fog = sun.fogColor();
    GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
    GLES30.glClearColor(fog[0], fog[1], fog[2], 1f);
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    float aspect = (float) viewportWidth / (float) viewportHeight;
    camera.fillView(viewScratch);
    camera.fillProjection(projectionScratch, aspect);
    camera.fillEye(eyeScratch);
    GLES30.glDisable(GLES30.GL_DEPTH_TEST);
    GLES30.glDepthMask(false);
    skyRenderer.draw(camera, sun, palette, timeSeconds);
    GLES30.glDepthMask(true);
    GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    GLES30.glEnable(GLES30.GL_CULL_FACE);
    GLES30.glFrontFace(GLES30.GL_CCW);
    GLES30.glCullFace(GLES30.GL_BACK);
    terrainProgram.use();
    applySharedUniforms(terrainProgram, sun, fog, null);
    if (mountains != null) {
      mountains.draw();
    }
    terrain.draw();
    if (houseInteriorMesh != null && houseInterior) {
      houseInteriorMesh.draw();
    } else if (houseMesh != null) {
      houseMesh.draw();
    }
    if (farmOverlay != null) {
      uploadMoisture(farm);
      farmProgram.use();
      applySharedUniforms(farmProgram, sun, fog, timeSeconds);
      GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, farmTexture);
      farmProgram.uniform1i("uMoisture", 0);
      farmOverlay.draw();
    }
    if (river != null) {
      waterProgram.use();
      applySharedUniforms(waterProgram, sun, fog, timeSeconds);
      river.draw();
    }
    if (cloudsEnabled) {
      cloudRenderer.draw(viewScratch, projectionScratch, sun, palette, timeSeconds);
    }
  }

  private void applySharedUniforms(ShaderProgram program, SunState sun, float[] fog, Float timeSeconds) {
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
    if (timeSeconds != null) {
      program.uniform3f("uCamPos", eyeScratch.x, eyeScratch.y, eyeScratch.z);
      program.uniform1f("uTime", timeSeconds);
    }
  }

  private void rebuildHouseMeshes() {
    if (houseMesh != null) {
      houseMesh.close();
      houseMesh = null;
    }
    if (houseInteriorMesh != null) {
      houseInteriorMesh.close();
      houseInteriorMesh = null;
    }
    houseMesh = Mesh.upload(house.exteriorMesh());
    houseInteriorMesh = Mesh.upload(house.interiorMesh());
    houseVersion = house.version();
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
        float flag;
        if (st == FarmGrid.STATE_DITCH) {
          r = DITCH_WATER[0];
          gg = DITCH_WATER[1];
          b = DITCH_WATER[2];
          flag = DITCH_WATER_FLAG;
        } else if (st == FarmGrid.STATE_PLOWED) {
          r = PLOWED_BROWN[0];
          gg = PLOWED_BROWN[1];
          b = PLOWED_BROWN[2];
          flag = 1f;
        } else {
          float checker = ((x + y) & 1) == 0 ? 1f : 0.94f;
          r = WILD_GREEN[0] * checker;
          gg = WILD_GREEN[1] * checker;
          b = WILD_GREEN[2] * checker;
          flag = 0.6f;
        }
        float u = (x + 0.5f) / cells;
        float v = (y + 0.5f) / cells;
        int a = g.vertexCount();
        g.appendVertex(cx - h, ty, cz - h, 0f, 1f, 0f, u, v);
        g.appendVertex(cx + h, ty, cz - h, 0f, 1f, 0f, u, v);
        g.appendVertex(cx + h, ty, cz + h, 0f, 1f, 0f, u, v);
        g.appendVertex(cx - h, ty, cz + h, 0f, 1f, 0f, u, v);
        g.setVertexColor(a, r, gg, b, flag);
        g.setVertexColor(a + 1, r, gg, b, flag);
        g.setVertexColor(a + 2, r, gg, b, flag);
        g.setVertexColor(a + 3, r, gg, b, flag);
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
    if (river != null) {
      river.close();
      river = null;
    }
    if (mountains != null) {
      mountains.close();
      mountains = null;
    }
    if (houseMesh != null) {
      houseMesh.close();
      houseMesh = null;
    }
    if (houseInteriorMesh != null) {
      houseInteriorMesh.close();
      houseInteriorMesh = null;
    }
    house = null;
    houseVersion = -1;
    cloudRenderer.close();
    skyRenderer.close();
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
    if (waterProgram != null) {
      waterProgram.close();
      waterProgram = null;
    }
    farmVersion = -1;
    ready = false;
  }
}
