package com.poorfarmer.core.save;

import com.poorfarmer.core.GameTime;
import com.poorfarmer.core.json.Json;
import com.poorfarmer.core.json.Json.Value;

public final class SessionState {

  public static final int CURRENT_VERSION = 1;

  public int version = CURRENT_VERSION;
  public int year = 1;
  public int season = 0;
  public int day = 1;
  public int clockMinute = 6 * 60;
  public int speed = 1;

  public String toJson() {
    Value root = Value.objectValue();
    root.set("version", Value.numberValue(version));
    root.set("year", Value.numberValue(year));
    root.set("season", Value.numberValue(season));
    root.set("day", Value.numberValue(day));
    root.set("clockMinute", Value.numberValue(clockMinute));
    root.set("speed", Value.numberValue(speed));
    return Json.stringify(root);
  }

  public static SessionState fromJson(String text) {
    SessionState state = new SessionState();
    Value root = Json.parse(text);
    state.version = intOf(root, "version", CURRENT_VERSION);
    if (state.version < CURRENT_VERSION) {
      state.migrateFrom(state.version);
    }
    state.year = intOf(root, "year", 1);
    state.season = intOf(root, "season", 0);
    state.day = intOf(root, "day", 1);
    state.clockMinute = intOf(root, "clockMinute", 6 * 60);
    state.speed = intOf(root, "speed", 1);
    return state;
  }

  private static int intOf(Value root, String key, int fallback) {
    Value value = root.get(key);
    if (value == null || value.isNull()) {
      return fallback;
    }
    return value.asInt();
  }

  public static SessionState fromGameTime(GameTime time) {
    SessionState state = new SessionState();
    state.year = time.year();
    state.season = time.season();
    state.day = time.day();
    state.clockMinute = time.clockMinute();
    state.speed = time.speed();
    return state;
  }

  public void applyTo(GameTime time) {
    time.set(new GameTime.DateTime(year, season, day, clockMinute));
    time.setSpeed(speed);
  }

  private void migrateFrom(int fromVersion) {
    version = CURRENT_VERSION;
  }
}
