package com.poorfarmer.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class GameTimeTest {

  @Test
  public void startsAtMorningOfFirstSpringDay() {
    GameTime time = new GameTime();
    assertEquals(1, time.year());
    assertEquals(0, time.season());
    assertEquals(1, time.day());
    assertEquals(GameTime.DAY_START_MINUTE, time.clockMinute());
    assertEquals("بهار", time.seasonNameFa());
  }

  @Test
  public void oneDayRollsOverAfter480RealSeconds() {
    GameTime time = new GameTime();
    time.advance(GameTime.REAL_SECONDS_PER_DAY);
    assertEquals(2, time.day());
    assertEquals(0, time.clockMinute() - GameTime.DAY_START_MINUTE);
  }

  @Test
  public void seasonRolloverAfterFourteenDays() {
    GameTime time = new GameTime();
    time.advance(GameTime.REAL_SECONDS_PER_DAY * GameTime.DAYS_PER_SEASON);
    assertEquals(1, time.season());
    assertEquals(1, time.day());
    assertEquals("تابستان", time.seasonNameFa());
  }

  @Test
  public void yearRolloverAfterFiftySixDays() {
    GameTime time = new GameTime();
    time.advance(GameTime.REAL_SECONDS_PER_DAY * GameTime.DAYS_PER_SEASON * GameTime.SEASONS_PER_YEAR);
    assertEquals(2, time.year());
    assertEquals(0, time.season());
    assertEquals(1, time.day());
  }

  @Test
  public void doubleSpeedAdvancesTwiceAsFast() {
    GameTime single = new GameTime();
    GameTime doubled = new GameTime();
    doubled.setSpeed(2);
    single.advance(100f);
    doubled.advance(100f);
    int singleElapsed = single.clockMinute() - GameTime.DAY_START_MINUTE;
    int doubledElapsed = doubled.clockMinute() - GameTime.DAY_START_MINUTE;
    assertEquals(2 * singleElapsed, doubledElapsed);
  }

  @Test
  public void invalidSpeedIsIgnored() {
    GameTime time = new GameTime();
    time.setSpeed(0);
    time.setSpeed(3);
    assertEquals(1, time.speed());
  }

  @Test
  public void speedChangedEventOnlyOnActualChange() {
    EventBus bus = new EventBus();
    GameTime time = new GameTime();
    time.bind(bus);
    List<Integer> changes = new ArrayList<>();
    bus.subscribe(GameEvents.SpeedChanged.class, event -> changes.add(event.speed));
    time.setSpeed(2);
    assertEquals(List.of(2), changes);
    time.setSpeed(2);
    assertEquals(List.of(2), changes);
    time.setSpeed(1);
    assertEquals(List.of(2, 1), changes);
    assertEquals(1, time.speed());
  }

  @Test
  public void dayProgressStaysWithinRange() {
    GameTime time = new GameTime();
    for (int i = 0; i < 5000; i++) {
      time.advance(1f);
      float progress = time.dayProgress();
      assertTrue(progress >= 0f && progress < 1f);
      assertTrue(time.clockMinute() >= GameTime.DAY_START_MINUTE
          && time.clockMinute() < GameTime.DAY_END_MINUTE);
    }
  }

  @Test
  public void publishesDaySeasonAndYearEvents() {
    EventBus bus = new EventBus();
    GameTime time = new GameTime();
    time.bind(bus);
    int[] dayEvents = {0};
    int[] seasonEvents = {0};
    int[] yearEvents = {0};
    bus.subscribe(GameEvents.DayChanged.class, event -> dayEvents[0] += 1);
    bus.subscribe(GameEvents.SeasonChanged.class, event -> seasonEvents[0] += 1);
    bus.subscribe(GameEvents.YearChanged.class, event -> yearEvents[0] += 1);
    time.advance(GameTime.REAL_SECONDS_PER_DAY * GameTime.DAYS_PER_SEASON * GameTime.SEASONS_PER_YEAR);
    assertEquals(GameTime.DAYS_PER_SEASON * GameTime.SEASONS_PER_YEAR, dayEvents[0]);
    assertEquals(GameTime.SEASONS_PER_YEAR, seasonEvents[0]);
    assertEquals(1, yearEvents[0]);
  }

  @Test
  public void snapshotRoundTrips() {
    GameTime time = new GameTime();
    time.advance(123.4f);
    GameTime other = new GameTime();
    other.set(time.snapshot());
    assertEquals(time.year(), other.year());
    assertEquals(time.season(), other.season());
    assertEquals(time.day(), other.day());
    assertEquals(time.clockMinute(), other.clockMinute());
  }
}
