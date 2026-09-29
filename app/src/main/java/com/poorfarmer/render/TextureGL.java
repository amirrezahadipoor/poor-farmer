package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.procedural.Texture;

import java.nio.ByteBuffer;

public final class TextureGL implements AutoCloseable {

  public final int id;

  private TextureGL(int id) {
    this.id = id;
  }

  public static TextureGL upload(Texture texture, boolean repeat) {
    int[] out = new int[1];
    GLES30.glGenTextures(1, out, 0);
    int id = out[0];
    if (id == 0) {
      throw new RenderException("glGenTextures returned 0");
    }
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id);
    ByteBuffer data = ByteBuffer.wrap(texture.pixels);
    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, texture.width, texture.height, 0,
        GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data);
    int wrap = repeat ? GLES30.GL_REPEAT : GLES30.GL_CLAMP_TO_EDGE;
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, wrap);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, wrap);
    GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    return new TextureGL(id);
  }

  public void bind(int unit) {
    GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id);
  }

  @Override
  public void close() {
    GLES30.glDeleteTextures(1, new int[]{id}, 0);
  }
}
