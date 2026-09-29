package com.poorfarmer.core;

public enum Quality {
  LOW(0),
  MEDIUM(1),
  HIGH(2);

  private final int level;

  Quality(int level) {
    this.level = level;
  }

  public int level() {
    return level;
  }

  public boolean atLeast(Quality other) {
    return level >= other.level;
  }
}
