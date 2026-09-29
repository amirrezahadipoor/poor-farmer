package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.Quality;
import com.poorfarmer.core.world.PostProcessParams;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class PostProcessor implements AutoCloseable {

  private static final int BLOOM_DIVISOR = 4;

  private static final String QUAD_VERTEX =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "out vec2 vUv;\n"
          + "void main() {\n"
          + "  vec2 corner = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));\n"
          + "  gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);\n"
          + "  vUv = corner;\n"
          + "}\n";

  private static final String EXTRACT_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "uniform sampler2D uScene;\n"
          + "uniform float uThreshold;\n"
          + "in vec2 vUv;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  vec3 c = texture(uScene, vUv).rgb;\n"
          + "  float luma = max(max(c.r, c.g), c.b);\n"
          + "  float mask = smoothstep(uThreshold, uThreshold + 0.2, luma);\n"
          + "  frag = vec4(c * mask, 1.0);\n"
          + "}\n";

  private static final String BLUR_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "uniform sampler2D uSource;\n"
          + "uniform vec2 uTexel;\n"
          + "uniform vec2 uDirection;\n"
          + "in vec2 vUv;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  vec3 sum = texture(uSource, vUv).rgb * 0.227027;\n"
          + "  sum += (texture(uSource, vUv + uDirection * uTexel).rgb\n"
          + "      + texture(uSource, vUv - uDirection * uTexel).rgb) * 0.1945946;\n"
          + "  sum += (texture(uSource, vUv + uDirection * uTexel * 2.0).rgb\n"
          + "      + texture(uSource, vUv - uDirection * uTexel * 2.0).rgb) * 0.1216216;\n"
          + "  sum += (texture(uSource, vUv + uDirection * uTexel * 3.0).rgb\n"
          + "      + texture(uSource, vUv - uDirection * uTexel * 3.0).rgb) * 0.054054;\n"
          + "  sum += (texture(uSource, vUv + uDirection * uTexel * 4.0).rgb\n"
          + "      + texture(uSource, vUv - uDirection * uTexel * 4.0).rgb) * 0.016216;\n"
          + "  frag = vec4(sum, 1.0);\n"
          + "}\n";

  private static final String COMPOSITE_FRAGMENT =
      "#version 300 es\n"
          + "precision highp float;\n"
          + "uniform sampler2D uScene;\n"
          + "uniform sampler2D uBloom;\n"
          + "uniform vec3 uTint;\n"
          + "uniform float uExposure;\n"
          + "uniform float uVignette;\n"
          + "uniform float uBloomIntensity;\n"
          + "uniform float uWarmth;\n"
          + "in vec2 vUv;\n"
          + "out vec4 frag;\n"
          + "void main() {\n"
          + "  vec3 c = texture(uScene, vUv).rgb;\n"
          + "  vec3 bloom = texture(uBloom, vUv).rgb * uBloomIntensity;\n"
          + "  c += bloom;\n"
          + "  c *= uTint;\n"
          + "  c += vec3(0.10, 0.05, 0.015) * uWarmth;\n"
          + "  c = clamp(c * uExposure, 0.0, 1.0);\n"
          + "  float d = distance(vUv, vec2(0.5));\n"
          + "  c *= 1.0 - uVignette * smoothstep(0.35, 0.85, d);\n"
          + "  frag = vec4(c, 1.0);\n"
          + "}\n";

  private ShaderProgram extractProgram;
  private ShaderProgram blurProgram;
  private ShaderProgram compositeProgram;
  private int quadVao;
  private FrameBuffer scene;
  private FrameBuffer bloomA;
  private FrameBuffer bloomB;
  private boolean ready;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    close();
    extractProgram = ShaderProgram.compile(QUAD_VERTEX, EXTRACT_FRAGMENT);
    blurProgram = ShaderProgram.compile(QUAD_VERTEX, BLUR_FRAGMENT);
    compositeProgram = ShaderProgram.compile(QUAD_VERTEX, COMPOSITE_FRAGMENT);
    int[] vao = new int[1];
    GLES30.glGenVertexArrays(1, vao, 0);
    quadVao = vao[0];
    if (quadVao == 0) {
      throw new RenderException("quad VAO allocation failed");
    }
    ready = true;
  }

  public void onSurfaceChanged(GL10 gl, int width, int height) {
    int bloomWidth = Math.max(4, width / BLOOM_DIVISOR);
    int bloomHeight = Math.max(4, height / BLOOM_DIVISOR);
    if (scene == null) {
      scene = new FrameBuffer(width, height);
      bloomA = new FrameBuffer(bloomWidth, bloomHeight);
      bloomB = new FrameBuffer(bloomWidth, bloomHeight);
    } else {
      scene.resize(width, height);
      bloomA.resize(bloomWidth, bloomHeight);
      bloomB.resize(bloomWidth, bloomHeight);
    }
  }

  public void beginScene() {
    if (scene == null) {
      return;
    }
    scene.bind();
    GLES30.glViewport(0, 0, scene.width, scene.height);
  }

  public void endScene() {
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
  }

  public void composite(Quality quality, PostProcessParams params) {
    if (scene == null || compositeProgram == null) {
      return;
    }
    boolean bloomEnabled = quality.atLeast(Quality.MEDIUM);
    if (bloomEnabled) {
      runExtract(params.bloomThreshold);
      runBlurPass(1f, 0f);
      runBlurPass(0f, 1f);
    }
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    GLES30.glViewport(0, 0, scene.width, scene.height);
    GLES30.glDisable(GLES30.GL_DEPTH_TEST);
    compositeProgram.use();
    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.texture);
    compositeProgram.uniform1i("uScene", 0);
    GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, bloomEnabled ? bloomB.texture : scene.texture);
    compositeProgram.uniform1i("uBloom", 1);
    compositeProgram.uniform3f("uTint", params.tintR, params.tintG, params.tintB);
    compositeProgram.uniform1f("uExposure", params.exposure);
    compositeProgram.uniform1f("uVignette", params.vignetteStrength);
    compositeProgram.uniform1f("uBloomIntensity", bloomEnabled ? params.bloomIntensity : 0f);
    compositeProgram.uniform1f("uWarmth", params.warmth);
    GLES30.glBindVertexArray(quadVao);
    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    GLES30.glBindVertexArray(0);
  }

  private void runExtract(float threshold) {
    bloomA.bind();
    GLES30.glViewport(0, 0, bloomA.width, bloomA.height);
    extractProgram.use();
    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.texture);
    extractProgram.uniform1i("uScene", 0);
    extractProgram.uniform1f("uThreshold", threshold);
    GLES30.glBindVertexArray(quadVao);
    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    GLES30.glBindVertexArray(0);
  }

  private void runBlurPass(float dirX, float dirY) {
    FrameBuffer source = (dirX != 0f) ? bloomA : bloomB;
    FrameBuffer target = (dirX != 0f) ? bloomB : bloomA;
    target.bind();
    GLES30.glViewport(0, 0, target.width, target.height);
    blurProgram.use();
    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, source.texture);
    blurProgram.uniform1i("uSource", 0);
    blurProgram.uniform2f("uTexel", 1f / target.width, 1f / target.height);
    blurProgram.uniform2f("uDirection", dirX, dirY);
    GLES30.glBindVertexArray(quadVao);
    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    GLES30.glBindVertexArray(0);
  }

  public boolean isReady() {
    return ready;
  }

  @Override
  public void close() {
    if (scene != null) {
      scene.close();
      scene = null;
    }
    if (bloomA != null) {
      bloomA.close();
      bloomA = null;
    }
    if (bloomB != null) {
      bloomB.close();
      bloomB = null;
    }
    if (quadVao != 0) {
      GLES30.glDeleteVertexArrays(1, new int[]{quadVao}, 0);
      quadVao = 0;
    }
    if (extractProgram != null) {
      extractProgram.close();
      extractProgram = null;
    }
    if (blurProgram != null) {
      blurProgram.close();
      blurProgram = null;
    }
    if (compositeProgram != null) {
      compositeProgram.close();
      compositeProgram = null;
    }
    ready = false;
  }
}
