package com.poorfarmer.render;

import android.content.Context;
import android.opengl.GLSurfaceView;

public final class GameView extends GLSurfaceView {

  private final SolidColorRenderer renderer;

  public GameView(Context context) {
    super(context);
    setEGLContextClientVersion(3);
    renderer = new SolidColorRenderer();
    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
  }
}
