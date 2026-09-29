package com.poorfarmer.core.farm;

public final class FarmGrid {

  public static final int CELLS = 48;
  public static final float CELL_SIZE = 1f;
  public static final byte STATE_WILD = 0;
  public static final byte STATE_PLOWED = 1;
  public static final byte STATE_DITCH = 2;

  private static final float SOAK_RATE = 0.12f;
  private static final float BASE_EVAP = 0.008f;
  private static final float[] SEASON_EVAP = {0.8f, 1.6f, 1.0f, 0.4f};

  private final byte[] state = new byte[CELLS * CELLS];
  private final float[] moisture = new float[CELLS * CELLS];
  private int stateVersion;

  public boolean inBounds(int x, int y) {
    return x >= 0 && x < CELLS && y >= 0 && y < CELLS;
  }

  public int index(int x, int y) {
    return y * CELLS + x;
  }

  public static float cellCenterX(int x) {
    return (x + 0.5f - CELLS / 2f) * CELL_SIZE;
  }

  public static float cellCenterZ(int y) {
    return (y + 0.5f - CELLS / 2f) * CELL_SIZE;
  }

  public byte stateAt(int x, int y) {
    return state[index(x, y)];
  }

  public float moistureAt(int x, int y) {
    return moisture[index(x, y)];
  }

  public int stateVersion() {
    return stateVersion;
  }

  public boolean plow(int x, int y) {
    if (!inBounds(x, y)) {
      return false;
    }
    int i = index(x, y);
    if (state[i] != STATE_WILD) {
      return false;
    }
    state[i] = STATE_PLOWED;
    stateVersion++;
    return true;
  }

  public boolean digDitch(int x, int y) {
    if (!inBounds(x, y)) {
      return false;
    }
    int i = index(x, y);
    if (state[i] != STATE_WILD) {
      return false;
    }
    state[i] = STATE_DITCH;
    stateVersion++;
    return true;
  }

  public boolean water(int x, int y) {
    if (!inBounds(x, y)) {
      return false;
    }
    int i = index(x, y);
    if (state[i] == STATE_DITCH) {
      return false;
    }
    moisture[i] = 1f;
    return true;
  }

  public void update(float dt, int season, float sunIntensity) {
    float evap = BASE_EVAP * SEASON_EVAP[season] * (0.15f + 0.85f * sunIntensity) * dt;
    for (int y = 0; y < CELLS; y++) {
      for (int x = 0; x < CELLS; x++) {
        int i = index(x, y);
        if (state[i] == STATE_DITCH) {
          moisture[i] = 1f;
          continue;
        }
        float m = moisture[i];
        if (x > 0 && state[i - 1] == STATE_DITCH) {
          m += SOAK_RATE * dt;
        }
        if (x < CELLS - 1 && state[i + 1] == STATE_DITCH) {
          m += SOAK_RATE * dt;
        }
        if (y > 0 && state[i - CELLS] == STATE_DITCH) {
          m += SOAK_RATE * dt;
        }
        if (y < CELLS - 1 && state[i + CELLS] == STATE_DITCH) {
          m += SOAK_RATE * dt;
        }
        m -= evap;
        moisture[i] = m < 0f ? 0f : (m > 1f ? 1f : m);
      }
    }
  }
}
