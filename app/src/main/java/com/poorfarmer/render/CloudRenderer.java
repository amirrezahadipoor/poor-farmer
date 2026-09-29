package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.world.SkyPalette;
import com.poorfarmer.core.world.SunState;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class CloudRenderer implements AutoCloseable {

  private static final String CLOUD_NOISE =
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
          + "float fbm2(vec2 p) {\n"
          + "  float sum = 0.0;\n"
          + "  float amp = 0.5;\n"
          + "  for (int i = 0; i < 3; i++) {\n"
          + "    sum += noise2(p) * amp;\n"
          + "    p = p * 2.1 + 7.3;\n"
          + "    amp *= 0.5;\n"
          + "  }\n"
          + "  return sum;\n"
          + "}\n";

  private static final String CLOUD_VERTEX =
      "#version 300 es\n"
          + "layout(location=0) in vec3 aPosition;\n"
          + "layout(location=2) in vec2 aUV;\n"
          + "uniform mat4 uView;\n"
          + "uniform mat4 uProj;\n"
          + "out vec2 vUV;\n"
          + "out float vViewZ;\n"
          + "void main() {\n"
          + "  vUV = aUV;\n"
          + "  vec4 view = uView * vec4(aPosition, 1.0);\n"
          + "  vViewZ = view.z;\n"
          + "  gl_Position = uProj * view;\n"
          + "}\n";

  private static final String CLOUD_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + CLOUD_NOISE
          + "in vec2 vUV;\n"
          + "in float vViewZ;\n"
          + "uniform float uTime;\n"
          + "uniform vec3 uFogColor;\n"
          + "uniform float uFogStart;\n"
          + "uniform float uFogEnd;\n"
          + "uniform vec3 uCloudBase;\n"
          + "uniform vec3 uCloudShadow;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  vec2 p = vUV * 3.2;\n"
          + "  float n = fbm2(p + vec2(uTime * 0.008, uTime * 0.002));\n"
          + "  n += fbm2(p * 2.3 - vec2(uTime * 0.012, uTime * 0.003)) * 0.5;\n"
          + "  float alpha = smoothstep(0.52, 0.82, n) * 0.88;\n"
          + "  if (alpha < 0.02) {\n"
          + "    discard;\n"
          + "  }\n"
          + "  vec3 c = mix(uCloudShadow, uCloudBase, 0.35 + 0.65 * clamp(n, 0.0, 1.0));\n"
          + "  float fog = smoothstep(uFogStart, uFogEnd, -vViewZ);\n"
          + "  c = mix(c, uFogColor, fog);\n"
          + "  frag = vec4(c, alpha * (1.0 - fog));\n"
          + "}\n";

  private ShaderProgram program;
  private Mesh cloudMesh;
  private boolean ready;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    close();
    program = ShaderProgram.create(CLOUD_VERTEX, CLOUD_FRAGMENT)
        .bindAttribute(Mesh.ATTR_POSITION, "aPosition")
        .bindAttribute(Mesh.ATTR_UV, "aUV")
        .link();
    ready = true;
  }

  public void setCloudLayer(MeshGeometry geometry) {
    if (cloudMesh != null) {
      cloudMesh.close();
      cloudMesh = null;
    }
    cloudMesh = Mesh.upload(geometry);
  }

  public void draw(Mat4 view, Mat4 projection, SunState sun, SkyPalette palette, float timeSeconds) {
    if (!ready || cloudMesh == null) {
      return;
    }
    float[] fog = sun.fogColor();
    program.use();
    program.uniformMat4("uView", view.data);
    program.uniformMat4("uProj", projection.data);
    program.uniform1f("uTime", timeSeconds);
    program.uniform3f("uFogColor", fog[0], fog[1], fog[2]);
    program.uniform1f("uFogStart", sun.fogStart());
    program.uniform1f("uFogEnd", sun.fogEnd());
    program.uniform3f("uCloudBase",
        palette.colorOf(SkyPalette.CLOUD_BASE, 0),
        palette.colorOf(SkyPalette.CLOUD_BASE, 1),
        palette.colorOf(SkyPalette.CLOUD_BASE, 2));
    program.uniform3f("uCloudShadow",
        palette.colorOf(SkyPalette.CLOUD_SHADOW, 0),
        palette.colorOf(SkyPalette.CLOUD_SHADOW, 1),
        palette.colorOf(SkyPalette.CLOUD_SHADOW, 2));
    GLES30.glEnable(GLES30.GL_BLEND);
    GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
    GLES30.glDepthMask(false);
    cloudMesh.draw();
    GLES30.glDepthMask(true);
    GLES30.glDisable(GLES30.GL_BLEND);
  }

  @Override
  public void close() {
    if (cloudMesh != null) {
      cloudMesh.close();
      cloudMesh = null;
    }
    if (program != null) {
      program.close();
      program = null;
    }
    ready = false;
  }
}
