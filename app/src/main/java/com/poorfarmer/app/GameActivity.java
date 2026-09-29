package com.poorfarmer.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.poorfarmer.core.Game;
import com.poorfarmer.core.jobs.JobQueue;
import com.poorfarmer.core.jobs.LoadingProgress;
import com.poorfarmer.core.model.MeshGeometry;
import com.poorfarmer.core.procedural.Noise;
import com.poorfarmer.core.procedural.ProceduralTextures;
import com.poorfarmer.core.procedural.Texture;
import com.poorfarmer.core.world.BootAssets;
import com.poorfarmer.core.world.Terrain;
import com.poorfarmer.render.GameView;
import com.poorfarmer.render.LoadingOverlayView;

public final class GameActivity extends Activity {

  public static final long BOOT_SEED = 20260930L;
  public static final int BOOT_TEXTURE_SIZE = 256;
  public static final float TERRAIN_MESH_STEP = 2f;

  private GameView gameView;
  private Game game;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    game = new Game();
    FrameLayout root = new FrameLayout(this);
    gameView = new GameView(this, game);
    root.addView(gameView, new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    LoadingOverlayView overlay = new LoadingOverlayView(this, game.loading());
    root.addView(overlay, new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    setContentView(root);
    startBoot();
  }

  @Override
  protected void onResume() {
    super.onResume();
    game.start();
    gameView.onResume();
  }

  @Override
  protected void onPause() {
    game.stop();
    gameView.onPause();
    super.onPause();
  }

  private void startBoot() {
    LoadingProgress loading = game.loading();
    loading.reset(4);
    JobQueue jobs = game.jobs();
    Terrain[] terrainHolder = new Terrain[1];
    MeshGeometry[] meshHolder = new MeshGeometry[1];
    Texture[] textures = new Texture[3];
    jobs.submit(() -> {
      terrainHolder[0] = new Terrain(BOOT_SEED);
      loading.completeUnits(1);
      return terrainHolder[0];
    });
    jobs.submit(() -> {
      meshHolder[0] = terrainHolder[0].toMesh(TERRAIN_MESH_STEP);
      loading.completeUnits(1);
      return meshHolder[0];
    });
    jobs.submit(() -> {
      Noise noise = new Noise(BOOT_SEED);
      textures[0] = ProceduralTextures.grass(noise, BOOT_TEXTURE_SIZE);
      textures[1] = ProceduralTextures.dirt(noise, BOOT_TEXTURE_SIZE);
      textures[2] = ProceduralTextures.stone(noise, BOOT_TEXTURE_SIZE);
      loading.completeUnits(1);
      return textures;
    });
    jobs.submit(() -> {
      game.setBootAssets(new BootAssets(
          terrainHolder[0], meshHolder[0], textures[0], textures[1], textures[2]));
      loading.completeUnits(1);
      return null;
    });
  }
}
