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
  private SunState sunCache;
  private int sunMinute = -1;
  private int sunSeason = -1;

  public GameView(Context context, Game game) {
    super(context);
    this.game = game;
    this.touchController = new TouchController(camera, game.bus());
    setEGLContextClientVersion(3);
    setRenderer(glRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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
      sunCache = null;
      sunMinute = -1;
      sunSeason = -1;
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
      SunState sun = sunForCurrentClock();
      postProcessor.beginScene();
      sceneRenderer.draw(camera, sun);
      postProcessor.endScene();
      PostProcessParams params = PostProcessParams.resolve(
          game.time().season(), sun.elevationDegrees(), sun.sunIntensity());
      postProcessor.composite(QUALITY, params);
    }
  }

  private SunState sunForCurrentClock() {
    int minute = game.time().clockMinute();
    int season = game.time().season();
    if (sunCache == null || minute != sunMinute || season != sunSeason) {
      sunCache = new SunState(minute / 60f, season);
      sunMinute = minute;
      sunSeason = season;
    }
    return sunCache;
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
