package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.world.GameCamera;
import com.poorfarmer.core.world.SkyPalette;
import com.poorfarmer.core.world.SunState;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class SkyRenderer implements AutoCloseable {

  private static final String SKY_VERTEX =
      "#version 300 es\n"
          + "out vec2 vNdc;\n"
          + "void main() {\n"
          + "  vec2 pos[3] = vec2[3](vec2(-1.0, -1.0), vec2(3.0, -1.0), vec2(-1.0, 3.0));\n"
          + "  vNdc = pos[gl_VertexID];\n"
          + "  gl_Position = vec4(pos[gl_VertexID], 1.0, 1.0);\n"
          + "}\n";

  private static final String SKY_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "in vec2 vNdc;\n"
          + "uniform vec3 uRight;\n"
          + "uniform vec3 uUp;\n"
          + "uniform vec3 uForward;\n"
          + "uniform float uTanHalfFov;\n"
          + "uniform float uAspect;\n"
          + "uniform vec3 uSunDir;\n"
          + "uniform float uSunFactor;\n"
          + "uniform vec3 uZenith;\n"
          + "uniform vec3 uHorizon;\n"
          + "uniform vec3 uFogColor;\n"
          + "uniform vec3 uSunGlow;\n"
          + "uniform float uStarAlpha;\n"
          + "uniform float uMoonAlpha;\n"
          + "uniform float uTime;\n"
          + "out vec4 frag;\n"
          + "float hash31(vec3 p) {\n"
          + "  p = fract(p * 0.3183099 + 0.1);\n"
          + "  p *= 17.0;\n"
          + "  return fract(p.x * p.y * p.z * (p.x + p.y + p.z));\n"
          + "}\n"
          + "void main() {\n"
          + "  vec3 rayView = normalize(vec3(vNdc.x * uTanHalfFov * uAspect, vNdc.y * uTanHalfFov, -1.0));\n"
          + "  vec3 ray = normalize(uRight * rayView.x + uUp * rayView.y + uForward * -rayView.z);\n"
          + "  float grad = clamp(ray.y * 3.0, 0.0, 1.0);\n"
          + "  vec3 sky = mix(uHorizon, uZenith, grad);\n"
          + "  if (ray.y < 0.0) {\n"
          + "    sky = uFogColor;\n"
          + "  }\n"
          + "  float sd = dot(ray, uSunDir);\n"
          + "  float sunDisc = smoothstep(0.99955, 0.99985, sd);\n"
          + "  float sunGlow = pow(max(sd, 0.0), 6.0) * 0.35 + pow(max(sd, 0.0), 64.0) * 0.6;\n"
          + "  sky += uSunGlow * (sunGlow + sunDisc * 2.5) * uSunFactor;\n"
          + "  float md = dot(ray, -uSunDir);\n"
          + "  float moonDisc = smoothstep(0.99965, 0.99990, md);\n"
          + "  float moonHalo = pow(max(md, 0.0), 64.0) * 0.18;\n"
          + "  sky += vec3(0.88, 0.90, 0.95) * (moonDisc * 1.4 + moonHalo) * uMoonAlpha * smoothstep(-0.05, 0.15, ray.y);\n"
          + "  vec3 sp = ray * 220.0;\n"
          + "  float h = hash31(floor(sp));\n"
          + "  float star = step(0.9972, h) * uStarAlpha * smoothstep(0.02, 0.25, ray.y);\n"
          + "  star *= 0.75 + 0.25 * sin(uTime * 2.0 + h * 40.0);\n"
          + "  sky += vec3(0.9) * star;\n"
          + "  frag = vec4(sky, 1.0);\n"
          + "}\n";

  private final Vec3 rightScratch = new Vec3();
  private final Vec3 upScratch = new Vec3();
  private final Vec3 forwardScratch = new Vec3();
  private int skyVao;
  private ShaderProgram program;
  private int viewportWidth;
  private int viewportHeight;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    close();
    int[] vao = {0};
    GLES30.glGenVertexArrays(1, vao, 0);
    skyVao = vao[0];
    program = ShaderProgram.create(SKY_VERTEX, SKY_FRAGMENT).link();
  }

  public void draw(GameCamera camera, SunState sun, SkyPalette palette, float timeSeconds) {
    camera.fillRight(rightScratch);
    camera.fillForward(forwardScratch);
    Vec3.cross(rightScratch, forwardScratch, upScratch);
    var dir = sun.direction();
    program.use();
    GLES30.glBindVertexArray(skyVao);
    program.uniform3f("uRight", rightScratch.x, rightScratch.y, rightScratch.z);
    program.uniform3f("uUp", upScratch.x, upScratch.y, upScratch.z);
    program.uniform3f("uForward", forwardScratch.x, forwardScratch.y, forwardScratch.z);
    program.uniform1f("uTanHalfFov", GameCamera.TAN_HALF_FOV);
    program.uniform1f("uAspect", (float) viewportWidth / (float) viewportHeight);
    program.uniform3f("uSunDir", dir.x, dir.y, dir.z);
    program.uniform1f("uSunFactor", sun.sunIntensity() > 0f ? 1f : 0f);
    float[] fog = sun.fogColor();
    program.uniform3f("uZenith", palette.colorOf(SkyPalette.ZENITH, 0), palette.colorOf(SkyPalette.ZENITH, 1), palette.colorOf(SkyPalette.ZENITH, 2));
    program.uniform3f("uHorizon", palette.colorOf(SkyPalette.HORIZON, 0), palette.colorOf(SkyPalette.HORIZON, 1), palette.colorOf(SkyPalette.HORIZON, 2));
    program.uniform3f("uFogColor", fog[0], fog[1], fog[2]);
    program.uniform3f("uSunGlow", palette.colorOf(SkyPalette.SUN_GLOW, 0), palette.colorOf(SkyPalette.SUN_GLOW, 1), palette.colorOf(SkyPalette.SUN_GLOW, 2));
    program.uniform1f("uStarAlpha", palette.starAlpha);
    program.uniform1f("uMoonAlpha", palette.moonAlpha);
    program.uniform1f("uTime", timeSeconds);
    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    GLES30.glBindVertexArray(0);
  }

  public void setViewport(int width, int height) {
    this.viewportWidth = width;
    this.viewportHeight = height;
  }

  @Override
  public void close() {
    if (skyVao != 0) {
      GLES30.glDeleteVertexArrays(1, new int[]{skyVao}, 0);
      skyVao = 0;
    }
    if (program != null) {
      program.close();
      program = null;
    }
  }
}
