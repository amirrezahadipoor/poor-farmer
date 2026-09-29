package com.poorfarmer.core;

import com.poorfarmer.core.jobs.JobQueue;
import com.poorfarmer.core.jobs.LoadingProgress;
import com.poorfarmer.core.world.BootAssets;

public final class Game {

  private final EventBus bus = new EventBus();
  private final GameTime time = new GameTime();
  private final ScreenMachine screens = new ScreenMachine();
  private final LoadingProgress loading = new LoadingProgress("در حال بارگذاری", 4);
  private JobQueue jobs;
  private BootAssets bootAssets;
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

  public JobQueue jobs() {
    if (jobs == null) {
      jobs = new JobQueue();
    }
    return jobs;
  }

  public LoadingProgress loading() {
    return loading;
  }

  public BootAssets bootAssets() {
    return bootAssets;
  }

  public void setBootAssets(BootAssets assets) {
    this.bootAssets = assets;
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
