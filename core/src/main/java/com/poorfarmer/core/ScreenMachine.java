package com.poorfarmer.core;

import java.util.EnumMap;
import java.util.Map;

public final class ScreenMachine {

  private final Map<GameScreen, ScreenHandler> handlers = new EnumMap<>(GameScreen.class);
  private GameScreen current = GameScreen.LOADING;

  public void register(GameScreen screen, ScreenHandler handler) {
    handlers.put(screen, handler);
  }

  public GameScreen current() {
    return current;
  }

  public ScreenHandler currentHandler() {
    return handlers.get(current);
  }

  public void switchTo(GameScreen next) {
    if (next == current) {
      return;
    }
    ScreenHandler oldHandler = handlers.get(current);
    if (oldHandler != null) {
      oldHandler.onExit();
    }
    current = next;
    ScreenHandler newHandler = handlers.get(next);
    if (newHandler != null) {
      newHandler.onEnter();
    }
  }
}
