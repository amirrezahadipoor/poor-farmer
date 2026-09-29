package com.poorfarmer.render;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.poorfarmer.core.FixedTimestepLoop;
import com.poorfarmer.core.Game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class GameView extends GLSurfaceView {

  private final Game game;
  private final FixedTimestepLoop loop = new FixedTimestepLoop();
  private final SolidColorRenderer renderer = new SolidColorRenderer();
  private final LoopRenderer glRenderer = new LoopRenderer();
  private long lastFrameNanos;

  public GameView(Context context, Game game) {
    super(context);
    this.game = game;
    setEGLContextClientVersion(3);
    setRenderer(glRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
  }

  private final class LoopRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      lastFrameNanos = 0L;
      renderer.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
      renderer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
      long nowNanos = System.nanoTime();
      float frameSeconds = lastFrameNanos == 0L ? 0f : (nowNanos - lastFrameNanos) / 1_000_000_000f;
      lastFrameNanos = nowNanos;
      loop.accumulateAndStep(frameSeconds, game::tick);
      renderer.onDrawFrame(gl, loop.interpolationAlpha());
    }
  }
}
