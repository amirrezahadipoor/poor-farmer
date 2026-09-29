package com.poorfarmer.core;

public final class GameTime {

  public static final int DAY_START_MINUTE = 6 * 60;
  public static final int DAY_END_MINUTE = 26 * 60;
  public static final int DAY_LENGTH_MINUTE = DAY_END_MINUTE - DAY_START_MINUTE;
  public static final float REAL_SECONDS_PER_DAY = 480f;
  public static final float MINUTES_PER_REAL_SECOND = DAY_LENGTH_MINUTE / REAL_SECONDS_PER_DAY;
  public static final int DAYS_PER_SEASON = 14;
  public static final int SEASONS_PER_YEAR = 4;
  public static final int STORY_YEARS = 5;
  public static final String[] SEASON_NAMES_FA = {"بهار", "تابستان", "پاییز", "زمستان"};

  private EventBus events;
  private int year = 1;
  private int season = 0;
  private int day = 1;
  private int clockMinute = DAY_START_MINUTE;
  private int speed = 1;

  public void bind(EventBus events) {
    this.events = events;
  }

  public void setSpeed(int newSpeed) {
    if (newSpeed >= 1 && newSpeed <= 2 && newSpeed != speed) {
      speed = newSpeed;
      if (events != null) {
        events.publish(new GameEvents.SpeedChanged(newSpeed));
      }
    }
  }

  public int speed() {
    return speed;
  }

  public int year() {
    return year;
  }

  public int season() {
    return season;
  }

  public String seasonNameFa() {
    return SEASON_NAMES_FA[season];
  }

  public int day() {
    return day;
  }

  public int clockMinute() {
    return clockMinute;
  }

  public int hour() {
    return (clockMinute / 60) % 24;
  }

  public int minute() {
    return clockMinute % 60;
  }

  public float dayProgress() {
    return (clockMinute - DAY_START_MINUTE) / (float) DAY_LENGTH_MINUTE;
  }

  public float seasonProgress() {
    return (day - 1) / (float) DAYS_PER_SEASON;
  }

  public float storyProgress() {
    int totalDays = DAYS_PER_SEASON * SEASONS_PER_YEAR;
    int elapsed = (year - 1) * totalDays + season * DAYS_PER_SEASON + (day - 1);
    return Math.min(1f, elapsed / (float) (totalDays * STORY_YEARS));
  }

  public void advance(float realSeconds) {
    clockMinute += Math.round(realSeconds * MINUTES_PER_REAL_SECOND * speed);
    while (clockMinute >= DAY_END_MINUTE) {
      clockMinute -= DAY_LENGTH_MINUTE;
      day += 1;
      boolean seasonRollover = day > DAYS_PER_SEASON;
      if (seasonRollover) {
        day = 1;
        season += 1;
        boolean yearRollover = season >= SEASONS_PER_YEAR;
        if (yearRollover) {
          season = 0;
          year += 1;
          if (events != null) {
            events.publish(new GameEvents.YearChanged(year));
          }
        }
        if (events != null) {
          events.publish(new GameEvents.SeasonChanged(year, season));
        }
      }
      if (events != null) {
        events.publish(new GameEvents.DayChanged(year, season, day));
      }
    }
  }

  public void set(DateTime dateTime) {
    year = dateTime.year;
    season = dateTime.season;
    day = dateTime.day;
    clockMinute = dateTime.clockMinute;
  }

  public DateTime snapshot() {
    return new DateTime(year, season, day, clockMinute);
  }

  public static final class DateTime {
    public final int year;
    public final int season;
    public final int day;
    public final int clockMinute;

    public DateTime(int year, int season, int day, int clockMinute) {
      this.year = year;
      this.season = season;
      this.day = day;
      this.clockMinute = clockMinute;
    }
  }
}
