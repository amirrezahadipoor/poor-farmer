package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.Quality;
import com.poorfarmer.core.world.PostProcessParams;

final class FrameBuffer {

  final int fbo;
  final int texture;
  int width;
  int height;

  FrameBuffer(int width, int height) {
    int[] fboId = new int[1];
    int[] texId = new int[1];
    GLES30.glGenFramebuffers(1, fboId, 0);
    GLES30.glGenTextures(1, texId, 0);
    fbo = fboId[0];
    texture = texId[0];
    if (fbo == 0 || texture == 0) {
      throw new RenderException("framebuffer or texture allocation failed");
    }
    allocate(width, height);
  }

  private void allocate(int width, int height) {
    this.width = width;
    this.height = height;
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, width, height, 0,
        GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
    GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
        GLES30.GL_TEXTURE_2D, texture, 0);
    if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
      throw new RenderException("post-process framebuffer incomplete");
    }
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
  }

  void resize(int width, int height) {
    allocate(width, height);
  }

  void bind() {
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
  }

  void close() {
    GLES30.glDeleteFramebuffers(1, new int[]{fbo}, 0);
    GLES30.glDeleteTextures(1, new int[]{texture}, 0);
  }
}
