package com.poorfarmer.core.world;

import com.poorfarmer.core.EventBus;
import com.poorfarmer.core.GameEvents;

public final class TravelState {

  private static final float FARM_TO_VILLAGE_SECONDS = 12f;
  private static final float FARM_TO_TOWN_SECONDS = 20f;
  private static final float VILLAGE_TO_TOWN_SECONDS = 30f;
  private static final float TEHRAN_ROUTE_SECONDS = 45f;

  private final EventBus bus;
  private WorldLocation current = WorldLocation.FARM;
  private boolean inTransit;
  private WorldLocation target;
  private float totalSeconds;
  private float remainingSeconds;

  public TravelState(EventBus bus) {
    this.bus = bus;
  }

  public WorldLocation current() {
    return current;
  }

  public WorldLocation target() {
    return inTransit ? target : null;
  }

  public boolean inTransit() {
    return inTransit;
  }

  public float fraction() {
    if (!inTransit || totalSeconds <= 0f) {
      return 1f;
    }
    float done = 1f - remainingSeconds / totalSeconds;
    if (done < 0f) {
      return 0f;
    }
    if (done > 1f) {
      return 1f;
    }
    return done;
  }

  public void startTravel(WorldLocation to) {
    if (inTransit || to == null || to == current) {
      return;
    }
    inTransit = true;
    target = to;
    totalSeconds = baseTravelSeconds(current, to);
    remainingSeconds = totalSeconds;
    bus.publish(new GameEvents.TravelStarted(current, to));
  }

  public void update(float realSeconds) {
    if (!inTransit) {
      return;
    }
    remainingSeconds -= realSeconds;
    if (remainingSeconds <= 0f) {
      inTransit = false;
      current = target;
      bus.publish(new GameEvents.TravelArrived(current));
    }
  }

  public static float baseTravelSeconds(WorldLocation a, WorldLocation b) {
    if (a == WorldLocation.TEHRAN || b == WorldLocation.TEHRAN) {
      return TEHRAN_ROUTE_SECONDS;
    }
    boolean farmA = a == WorldLocation.FARM;
    boolean farmB = b == WorldLocation.FARM;
    if (farmA || farmB) {
      WorldLocation other = farmA ? b : a;
      return other == WorldLocation.VILLAGE ? FARM_TO_VILLAGE_SECONDS : FARM_TO_TOWN_SECONDS;
    }
    return VILLAGE_TO_TOWN_SECONDS;
  }
}
