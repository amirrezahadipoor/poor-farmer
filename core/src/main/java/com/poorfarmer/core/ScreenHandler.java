package com.poorfarmer.core;

public interface ScreenHandler {

  void onEnter();

  void onExit();

  void update(float realSeconds);
}
