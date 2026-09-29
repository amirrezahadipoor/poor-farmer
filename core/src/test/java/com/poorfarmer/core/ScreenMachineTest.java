package com.poorfarmer.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class ScreenMachineTest {

  private static final class RecordingHandler implements ScreenHandler {
    private final String name;
    private final List<String> log;

    private RecordingHandler(String name, List<String> log) {
      this.name = name;
      this.log = log;
    }

    @Override
    public void onEnter() {
      log.add(name + ":enter");
    }

    @Override
    public void onExit() {
      log.add(name + ":exit");
    }

    @Override
    public void update(float realSeconds) {
      log.add(name + ":update");
    }
  }

  @Test
  public void startsOnLoading() {
    ScreenMachine machine = new ScreenMachine();
    assertEquals(GameScreen.LOADING, machine.current());
  }

  @Test
  public void switchFiresExitBeforeEnter() {
    ScreenMachine machine = new ScreenMachine();
    List<String> log = new ArrayList<>();
    RecordingHandler menu = new RecordingHandler("menu", log);
    RecordingHandler farm = new RecordingHandler("farm", log);
    machine.register(GameScreen.MAIN_MENU, menu);
    machine.register(GameScreen.FARM, farm);
    machine.switchTo(GameScreen.MAIN_MENU);
    machine.switchTo(GameScreen.FARM);
    assertEquals(List.of("menu:enter", "menu:exit", "farm:enter"), log);
  }

  @Test
  public void switchingToSameScreenIsNoOp() {
    ScreenMachine machine = new ScreenMachine();
    List<String> log = new ArrayList<>();
    RecordingHandler menu = new RecordingHandler("menu", log);
    machine.register(GameScreen.MAIN_MENU, menu);
    machine.switchTo(GameScreen.MAIN_MENU);
    machine.switchTo(GameScreen.MAIN_MENU);
    assertEquals(List.of("menu:enter"), log);
  }

  @Test
  public void unregisteredScreenStillTracksCurrent() {
    ScreenMachine machine = new ScreenMachine();
    machine.switchTo(GameScreen.SETTINGS);
    assertEquals(GameScreen.SETTINGS, machine.current());
    assertNull(machine.currentHandler());
  }

  @Test
  public void gameTickDrivesCurrentHandler() {
    Game game = new Game();
    List<String> log = new ArrayList<>();
    RecordingHandler menu = new RecordingHandler("menu", log);
    game.screens().register(GameScreen.MAIN_MENU, menu);
    game.start();
    game.tick(0.5f);
    game.tick(0.5f);
    assertEquals(List.of("menu:enter", "menu:update", "menu:update"), log);
  }

  @Test
  public void stoppedGameDoesNotTick() {
    Game game = new Game();
    List<String> log = new ArrayList<>();
    RecordingHandler menu = new RecordingHandler("menu", log);
    game.screens().register(GameScreen.MAIN_MENU, menu);
    game.start();
    game.stop();
    game.tick(0.5f);
    assertEquals(List.of("menu:enter"), log);
  }
}
