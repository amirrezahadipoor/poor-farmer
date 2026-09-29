package com.poorfarmer.core.save;

import static org.junit.Assert.assertEquals;

import com.poorfarmer.core.GameTime;
import org.junit.Test;

public class SessionStateTest {

  @Test
  public void roundTrip() {
    SessionState state = new SessionState();
    state.year = 3;
    state.season = 2;
    state.day = 11;
    state.clockMinute = 15 * 60 + 20;
    state.speed = 2;
    String json = state.toJson();
    SessionState restored = SessionState.fromJson(json);
    assertEquals(3, restored.year);
    assertEquals(2, restored.season);
    assertEquals(11, restored.day);
    assertEquals(15 * 60 + 20, restored.clockMinute);
    assertEquals(2, restored.speed);
    assertEquals(SessionState.CURRENT_VERSION, restored.version);
  }

  @Test
  public void missingFieldsFallBackToDefaults() {
    SessionState restored = SessionState.fromJson("{\"version\":1}");
    assertEquals(1, restored.year);
    assertEquals(0, restored.season);
    assertEquals(1, restored.day);
    assertEquals(6 * 60, restored.clockMinute);
    assertEquals(1, restored.speed);
  }

  @Test
  public void versionIsPersisted() {
    SessionState state = new SessionState();
    state.day = 9;
    SessionState restored = SessionState.fromJson(state.toJson());
    assertEquals(SessionState.CURRENT_VERSION, restored.version);
  }

  @Test
  public void gameTimeRoundTrip() {
    GameTime time = new GameTime();
    time.setSpeed(2);
    time.advance(1370f);
    SessionState state = SessionState.fromGameTime(time);
    GameTime restored = new GameTime();
    state.applyTo(restored);
    assertEquals(time.year(), restored.year());
    assertEquals(time.season(), restored.season());
    assertEquals(time.day(), restored.day());
    assertEquals(time.clockMinute(), restored.clockMinute());
    assertEquals(2, restored.speed());
  }
}
