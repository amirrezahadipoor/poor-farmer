package com.poorfarmer.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.poorfarmer.app.TouchController;
import com.poorfarmer.core.FixedTimestepLoop;
import com.poorfarmer.core.Game;
import com.poorfarmer.core.Quality;
import com.poorfarmer.core.profiler.FpsMeter;
import com.poorfarmer.core.world.GameCamera;
import com.poorfarmer.core.world.PostProcessParams;
import com.poorfarmer.core.world.SkyPalette;
import com.poorfarmer.core.world.SunState;
import com.poorfarmer.core.world.Terrain;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class GameView extends GLSurfaceView {

  private static final Quality QUALITY = Quality.HIGH;

  private final Game game;
  private final FixedTimestepLoop loop = new FixedTimestepLoop();
  private final SceneRenderer sceneRenderer = new SceneRenderer();
  private final PostProcessor postProcessor = new PostProcessor();
  private final GameCamera camera = new GameCamera(Terrain.WORLD_HALF);
  private final TouchController touchController;
  private final FpsMeter fpsMeter = new FpsMeter(120);
  private final LoopRenderer glRenderer = new LoopRenderer();
  private long lastFrameNanos;

  public GameView(Context context, Game game) {
    super(context);
    this.game = game;
    this.touchController = new TouchController(camera, game.bus());
    setEGLContextClientVersion(3);
    setRenderer(glRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    sceneRenderer.setCloudsEnabled(QUALITY != Quality.LOW);
  }

  public FpsMeter fpsMeter() {
    return fpsMeter;
  }

  public SceneRenderer sceneRenderer() {
    return sceneRenderer;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return touchController.onTouchEvent(event);
  }

  private final class LoopRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      lastFrameNanos = 0L;
      sceneRenderer.onSurfaceCreated(gl, config);
      postProcessor.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
      sceneRenderer.onSurfaceChanged(gl, width, height);
      postProcessor.onSurfaceChanged(gl, width, height);
      touchController.setScreenSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
      long nowNanos = System.nanoTime();
      float frameSeconds = lastFrameNanos == 0L ? 0f : (nowNanos - lastFrameNanos) / 1_000_000_000f;
      lastFrameNanos = nowNanos;
      loop.accumulateAndStep(frameSeconds, game::tick);
      fpsMeter.addFrame(frameSeconds);
      game.jobs().pollCallbacks();
      camera.update(frameSeconds);
      SunState sun = game.sunState();
      SkyPalette palette = game.skyPalette();
      if (sun == null) {
        sun = new SunState(9f, 0);
        palette = new SkyPalette(sun.elevationDegrees(), sun.sunIntensity(), 0);
      }
      float timeSeconds = (float) (System.nanoTime() / 1e9);
      postProcessor.beginScene();
      sceneRenderer.draw(camera, sun, palette, game.farm(), timeSeconds);
      postProcessor.endScene();
      PostProcessParams params = PostProcessParams.resolve(
          game.time().season(), sun.elevationDegrees(), sun.sunIntensity());
      postProcessor.composite(QUALITY, params);
    }
  }

  @Override
  public void surfaceDestroyed(android.view.SurfaceHolder holder) {
    super.surfaceDestroyed(holder);
    queueEvent(() -> {
      sceneRenderer.close();
      postProcessor.close();
    });
  }
}
