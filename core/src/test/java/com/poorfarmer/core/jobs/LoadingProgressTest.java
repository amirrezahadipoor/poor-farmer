package com.poorfarmer.core.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoadingProgressTest {

  @Test
  public void startsEmptyAndCompletesIncrementally() {
    LoadingProgress progress = new LoadingProgress("load", 4);
    assertEquals(0f, progress.fraction(), 0f);
    assertFalse(progress.isComplete());
    progress.completeUnits(1);
    assertEquals(0.25f, progress.fraction(), 0f);
    progress.completeUnits(2);
    assertEquals(0.75f, progress.fraction(), 0f);
    progress.completeUnits(1);
    assertEquals(1f, progress.fraction(), 0f);
    assertTrue(progress.isComplete());
  }

  @Test
  public void overCompletionClampsToOne() {
    LoadingProgress progress = new LoadingProgress("load", 2);
    progress.completeUnits(9);
    assertEquals(1f, progress.fraction(), 0f);
    assertTrue(progress.isComplete());
  }

  @Test
  public void resetRestarts() {
    LoadingProgress progress = new LoadingProgress("load", 2);
    progress.completeUnits(2);
    assertTrue(progress.isComplete());
    progress.reset(5);
    assertFalse(progress.isComplete());
    assertEquals(0, progress.completedUnits());
    assertEquals(5, progress.totalUnits());
    assertEquals(0f, progress.fraction(), 0f);
  }

  @Test
  public void zeroUnitsMeansComplete() {
    LoadingProgress progress = new LoadingProgress("load", 0);
    assertTrue(progress.isComplete());
    assertEquals(1f, progress.fraction(), 0f);
  }

  @Test
  public void labelIsStable() {
    LoadingProgress progress = new LoadingProgress("در حال بارگذاری", 3);
    progress.completeUnits(1);
    assertEquals("در حال بارگذاری", progress.label());
  }
}
