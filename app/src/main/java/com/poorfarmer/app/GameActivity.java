package com.poorfarmer.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.poorfarmer.core.Game;
import com.poorfarmer.render.GameView;

public final class GameActivity extends Activity {

  private GameView gameView;
  private Game game;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    game = new Game();
    gameView = new GameView(this);
    setContentView(gameView);
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
}
