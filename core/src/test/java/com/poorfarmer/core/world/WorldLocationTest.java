package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WorldLocationTest {

  @Test
  public void farmFocusIsOrigin() {
    assertEquals(0f, WorldLocation.FARM.focusX(), 0f);
    assertEquals(0f, WorldLocation.FARM.focusZ(), 0f);
  }

  @Test
  public void focusPointsAreDistinct() {
    WorldLocation[] all = WorldLocation.values();
    for (int a = 0; a < all.length; a++) {
      for (int b = a + 1; b < all.length; b++) {
        if (all[a] == WorldLocation.TEHRAN || all[b] == WorldLocation.TEHRAN) {
          continue;
        }
        boolean same = all[a].focusX() == all[b].focusX() && all[a].focusZ() == all[b].focusZ();
        assertFalse("focus clash between " + all[a] + " and " + all[b], same);
      }
    }
  }

  @Test
  public void villageFocusMatchesVillageCluster() {
    float x = 0f;
    float z = 0f;
    int n = 0;
    for (Village.VillageKind kind : Village.VillageKind.values()) {
      x += Village.positionX(kind);
      z += Village.positionZ(kind);
      n++;
    }
    float cx = x / n;
    float cz = z / n;
    assertTrue("focus should be near the cluster centroid (" + cx + "," + cz + ")",
        Math.abs(WorldLocation.VILLAGE.focusX() - cx) < 12f
            && Math.abs(WorldLocation.VILLAGE.focusZ() - cz) < 12f);
  }

  @Test
  public void townFocusMatchesTownCluster() {
    float x = 0f;
    float z = 0f;
    int n = 0;
    for (Town.TownKind kind : Town.TownKind.values()) {
      x += Town.positionX(kind);
      z += Town.positionZ(kind);
      n++;
    }
    float cx = x / n;
    float cz = z / n;
    assertTrue("focus should be near the cluster centroid (" + cx + "," + cz + ")",
        Math.abs(WorldLocation.TOWN.focusX() - cx) < 12f
            && Math.abs(WorldLocation.TOWN.focusZ() - cz) < 12f);
  }

  @Test
  public void everyLocationHasAPersianLabel() {
    for (WorldLocation location : WorldLocation.values()) {
      assertFalse(location.label().isEmpty());
      assertTrue("label should contain Persian script: " + location.label(),
          location.label().codePoints().anyMatch(cp -> cp >= 0x0600 && cp <= 0x06FF));
    }
  }
}
