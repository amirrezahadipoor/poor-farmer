package com.poorfarmer.core;

import com.poorfarmer.core.farm.FarmGrid;
import com.poorfarmer.core.jobs.JobQueue;
import com.poorfarmer.core.jobs.LoadingProgress;
import com.poorfarmer.core.world.BootAssets;
import com.poorfarmer.core.world.SunState;

public final class Game {

  private final EventBus bus = new EventBus();
  private final GameTime time = new GameTime();
  private final ScreenMachine screens = new ScreenMachine();
  private final FarmGrid farm = new FarmGrid();
  private final LoadingProgress loading = new LoadingProgress("در حال بارگذاری", 5);
  private JobQueue jobs;
  private BootAssets bootAssets;
  private SunState sun;
  private int sunMinute = -1;
  private int sunSeason = -1;
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

  public FarmGrid farm() {
    return farm;
  }

  public SunState sunState() {
    return sun;
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
    int minute = time.clockMinute();
    int season = time.season();
    if (sun == null || minute != sunMinute || season != sunSeason) {
      sun = new SunState(minute / 60f, season);
      sunMinute = minute;
      sunSeason = season;
    }
    farm.update(realSeconds, season, sun.sunIntensity());
    ScreenHandler handler = screens.currentHandler();
    if (handler != null) {
      handler.update(realSeconds);
    }
  }
}
