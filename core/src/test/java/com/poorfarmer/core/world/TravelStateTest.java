package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.EventBus;
import com.poorfarmer.core.GameEvents;
import org.junit.Test;

public class TravelStateTest {

  private static final class Recorder {
    GameEvents.TravelStarted started;
    GameEvents.TravelArrived arrived;

    Recorder(EventBus bus) {
      bus.subscribe(GameEvents.TravelStarted.class, event -> started = event);
      bus.subscribe(GameEvents.TravelArrived.class, event -> arrived = event);
    }
  }

  @Test
  public void travelCompletesAfterBaseDuration() {
    EventBus bus = new EventBus();
    Recorder recorder = new Recorder(bus);
    TravelState travel = new TravelState(bus);
    travel.startTravel(WorldLocation.VILLAGE);
    assertTrue(travel.inTransit());
    assertEquals(WorldLocation.FARM, travel.current());
    assertEquals(WorldLocation.VILLAGE, travel.target());
    travel.update(TravelState.baseTravelSeconds(WorldLocation.FARM, WorldLocation.VILLAGE) - 0.1f);
    assertTrue("not yet arrived", travel.inTransit());
    travel.update(0.2f);
    assertFalse(travel.inTransit());
    assertEquals(WorldLocation.VILLAGE, travel.current());
    assertEquals(WorldLocation.FARM, recorder.started.from);
    assertEquals(WorldLocation.VILLAGE, recorder.started.to);
    assertEquals(WorldLocation.VILLAGE, recorder.arrived.location);
  }

  @Test
  public void cannotTravelWhileInTransit() {
    EventBus bus = new EventBus();
    Recorder recorder = new Recorder(bus);
    TravelState travel = new TravelState(bus);
    travel.startTravel(WorldLocation.TOWN);
    travel.startTravel(WorldLocation.VILLAGE);
    assertEquals(WorldLocation.TOWN, travel.target());
    travel.update(1000f);
    assertEquals(WorldLocation.TOWN, travel.current());
    assertEquals(WorldLocation.TOWN, recorder.arrived.location);
  }

  @Test
  public void sameLocationIsNoOp() {
    EventBus bus = new EventBus();
    Recorder recorder = new Recorder(bus);
    TravelState travel = new TravelState(bus);
    travel.startTravel(WorldLocation.FARM);
    assertFalse(travel.inTransit());
  }

  @Test
  public void fractionGrowsToOne() {
    EventBus bus = new EventBus();
    Recorder recorder = new Recorder(bus);
    TravelState travel = new TravelState(bus);
    assertEquals(1f, travel.fraction(), 0f);
    travel.startTravel(WorldLocation.VILLAGE);
    assertEquals(0f, travel.fraction(), 0f);
    travel.update(TravelState.baseTravelSeconds(WorldLocation.FARM, WorldLocation.VILLAGE) / 2f);
    assertEquals(0.5f, travel.fraction(), 0.01f);
    travel.update(TravelState.baseTravelSeconds(WorldLocation.FARM, WorldLocation.VILLAGE));
    assertEquals(1f, travel.fraction(), 0f);
    assertNull(travel.target());
  }

  @Test
  public void baseTimesAreDistinctPerRoute() {
    float farmVillage = TravelState.baseTravelSeconds(WorldLocation.FARM, WorldLocation.VILLAGE);
    float farmTown = TravelState.baseTravelSeconds(WorldLocation.FARM, WorldLocation.TOWN);
    float villageTown = TravelState.baseTravelSeconds(WorldLocation.VILLAGE, WorldLocation.TOWN);
    float tehran = TravelState.baseTravelSeconds(WorldLocation.FARM, WorldLocation.TEHRAN);
    assertTrue(farmVillage > 0f && farmVillage < farmTown);
    assertTrue(farmTown < villageTown);
    assertTrue(villageTown < tehran);
    assertEquals(farmTown, TravelState.baseTravelSeconds(WorldLocation.TOWN, WorldLocation.FARM), 0f);
  }
}
