package com.poorfarmer.core;

public final class Game {

  private final EventBus bus = new EventBus();
  private final GameTime time = new GameTime();
  private final ScreenMachine screens = new ScreenMachine();
  private boolean running;

  public Game() {
    time.bind(bus);
  }

  public EventBus bus() {
    return bus;
  }

  public GameTime time() {
    return time;
  }

  public ScreenMachine screens() {
    return screens;
  }

  public boolean running() {
    return running;
  }

  public void start() {
    if (running) {
      return;
    }
    running = true;
    screens.switchTo(GameScreen.MAIN_MENU);
  }

  public void stop() {
    running = false;
  }

  public void tick(float realSeconds) {
    if (!running) {
      return;
    }
    time.advance(realSeconds);
    ScreenHandler handler = screens.currentHandler();
    if (handler != null) {
      handler.update(realSeconds);
    }
  }
}
