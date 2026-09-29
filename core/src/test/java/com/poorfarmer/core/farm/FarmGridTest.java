package com.poorfarmer.core.farm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FarmGridTest {

  @Test
  public void gridIs48By48() {
    FarmGrid farm = new FarmGrid();
    assertTrue(farm.inBounds(0, 0));
    assertTrue(farm.inBounds(47, 47));
    assertFalse(farm.inBounds(-1, 0));
    assertFalse(farm.inBounds(0, -1));
    assertFalse(farm.inBounds(48, 0));
    assertFalse(farm.inBounds(0, 48));
    assertEquals(2303, farm.index(47, 47));
  }

  @Test
  public void cellsAreCenteredOnOrigin() {
    assertEquals(FarmGrid.cellCenterX(0), -23.5f, 0.001f);
    assertEquals(FarmGrid.cellCenterX(47), 23.5f, 0.001f);
    assertEquals(FarmGrid.cellCenterZ(23), -0.5f, 0.001f);
    assertEquals(FarmGrid.cellCenterZ(24), 0.5f, 0.001f);
  }

  @Test
  public void plowOnlyWildCells() {
    FarmGrid farm = new FarmGrid();
    assertTrue(farm.plow(10, 10));
    assertEquals(FarmGrid.STATE_PLOWED, farm.stateAt(10, 10));
    assertFalse(farm.plow(10, 10));
    assertFalse(farm.digDitch(10, 10));
    assertTrue(farm.digDitch(11, 11));
    assertEquals(FarmGrid.STATE_DITCH, farm.stateAt(11, 11));
    assertFalse(farm.plow(11, 11));
    assertFalse(farm.plow(-1, 5));
  }

  @Test
  public void stateVersionOnlyOnRealChange() {
    FarmGrid farm = new FarmGrid();
    int v0 = farm.stateVersion();
    farm.plow(3, 3);
    int v1 = farm.stateVersion();
    farm.plow(3, 3);
    farm.digDitch(3, 3);
    assertEquals(v1, farm.stateVersion());
    farm.digDitch(4, 4);
    assertEquals(v1 + 1, farm.stateVersion());
    assertTrue(v1 > v0);
  }

  @Test
  public void manualWaterFillsCellThenEvaporates() {
    FarmGrid farm = new FarmGrid();
    assertTrue(farm.water(20, 20));
    assertEquals(1f, farm.moistureAt(20, 20), 0.001f);
    for (int i = 0; i < 820; i++) {
      farm.update(0.05f, 1, 1f);
    }
    float m = farm.moistureAt(20, 20);
    assertTrue("should have dried, m=" + m, m < 0.5f);
    assertTrue("should not go negative, m=" + m, m >= 0f);
    farm.digDitch(21, 21);
    assertFalse(farm.water(21, 21));
  }

  @Test
  public void ditchesStayFullAndSoakNeighbors() {
    FarmGrid farm = new FarmGrid();
    farm.digDitch(30, 30);
    for (int i = 0; i < 200; i++) {
      farm.update(0.05f, 1, 1f);
    }
    assertEquals(1f, farm.moistureAt(30, 30), 0.001f);
    assertTrue(farm.moistureAt(29, 30) > 0.9f);
    assertTrue(farm.moistureAt(31, 30) > 0.9f);
    assertTrue(farm.moistureAt(30, 29) > 0.9f);
    assertTrue(farm.moistureAt(30, 31) > 0.9f);
    assertEquals(0f, farm.moistureAt(28, 30), 0.001f);
  }

  @Test
  public void winterEvaporatesSlowerThanSummer() {
    FarmGrid summer = new FarmGrid();
    FarmGrid winter = new FarmGrid();
    summer.water(5, 5);
    winter.water(5, 5);
    for (int i = 0; i < 400; i++) {
      summer.update(0.05f, 1, 1f);
      winter.update(0.05f, 3, 1f);
    }
    assertTrue(winter.moistureAt(5, 5) > summer.moistureAt(5, 5));
  }

  @Test
  public void nightEvaporatesLessThanMidday() {
    FarmGrid day = new FarmGrid();
    FarmGrid night = new FarmGrid();
    day.water(5, 5);
    night.water(5, 5);
    for (int i = 0; i < 400; i++) {
      day.update(0.05f, 1, 1f);
      night.update(0.05f, 1, 0f);
    }
    assertTrue(night.moistureAt(5, 5) > day.moistureAt(5, 5));
  }

  @Test
  public void simulationIsDeterministic() {
    FarmGrid a = new FarmGrid();
    FarmGrid b = new FarmGrid();
    a.digDitch(10, 10);
    a.digDitch(10, 20);
    a.plow(11, 10);
    a.water(40, 40);
    b.digDitch(10, 10);
    b.digDitch(10, 20);
    b.plow(11, 10);
    b.water(40, 40);
    for (int i = 0; i < 1000; i++) {
      a.update(0.05f, 1, 0.7f);
      b.update(0.05f, 1, 0.7f);
    }
    for (int x = 0; x < FarmGrid.CELLS; x++) {
      for (int y = 0; y < FarmGrid.CELLS; y++) {
        assertEquals(a.moistureAt(x, y), b.moistureAt(x, y), 0f);
      }
    }
  }

  @Test
  public void updateAllocatesNothing() {
    FarmGrid farm = new FarmGrid();
    farm.digDitch(10, 10);
    farm.plow(11, 10);
    com.poorfarmer.core.AllocationProbe.assertStable(
        () -> farm.update(0.05f, 1, 0.7f), 50, 2000, 256 * 1024L);
  }
}
