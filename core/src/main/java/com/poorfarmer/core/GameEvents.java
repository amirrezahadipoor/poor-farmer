package com.poorfarmer.core;

public final class GameEvents {

  public static final class DayChanged {
    public final int year;
    public final int season;
    public final int day;

    public DayChanged(int year, int season, int day) {
      this.year = year;
      this.season = season;
      this.day = day;
    }
  }

  public static final class SeasonChanged {
    public final int year;
    public final int season;

    public SeasonChanged(int year, int season) {
      this.year = year;
      this.season = season;
    }
  }

  public static final class YearChanged {
    public final int year;

    public YearChanged(int year) {
      this.year = year;
    }
  }

  public static final class SpeedChanged {
    public final int speed;

    public SpeedChanged(int speed) {
      this.speed = speed;
    }
  }

  public static final class Tapped {
    public final float x;
    public final float z;

    public Tapped(float x, float z) {
      this.x = x;
      this.z = z;
    }
  }

  public static final class LongPressed {
    public final float x;
    public final float z;

    public LongPressed(float x, float z) {
      this.x = x;
      this.z = z;
    }
  }

  public static final class TravelStarted {
    public final com.poorfarmer.core.world.WorldLocation from;
    public final com.poorfarmer.core.world.WorldLocation to;

    public TravelStarted(com.poorfarmer.core.world.WorldLocation from,
                         com.poorfarmer.core.world.WorldLocation to) {
      this.from = from;
      this.to = to;
    }
  }

  public static final class TravelArrived {
    public final com.poorfarmer.core.world.WorldLocation location;

    public TravelArrived(com.poorfarmer.core.world.WorldLocation location) {
      this.location = location;
    }
  }

  private GameEvents() {
  }
}
