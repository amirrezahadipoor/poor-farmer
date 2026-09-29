package com.poorfarmer.render;

import android.opengl.GLES30;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class SolidColorRenderer {

  public static final float SKY_TOP_RED = 0.45f;
  public static final float SKY_TOP_GREEN = 0.57f;
  public static final float SKY_TOP_BLUE = 0.71f;

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES30.glClearColor(SKY_TOP_RED, SKY_TOP_GREEN, SKY_TOP_BLUE, 1.0f);
  }

  public void onSurfaceChanged(GL10 gl, int width, int height) {
    GLES30.glViewport(0, 0, width, height);
  }

  public void onDrawFrame(GL10 gl, float interpolationAlpha) {
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
  }
}
