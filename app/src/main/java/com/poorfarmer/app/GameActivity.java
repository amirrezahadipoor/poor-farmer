package com.poorfarmer.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.poorfarmer.render.GameView;

public final class GameActivity extends Activity {

  private GameView gameView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    gameView = new GameView(this);
    setContentView(gameView);
  }

  @Override
  protected void onResume() {
    super.onResume();
    gameView.onResume();
  }

  @Override
  protected void onPause() {
    gameView.onPause();
    super.onPause();
  }
}
