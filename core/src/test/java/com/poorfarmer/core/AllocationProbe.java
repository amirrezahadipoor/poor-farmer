package com.poorfarmer.core;

import static org.junit.Assert.assertTrue;

public final class AllocationProbe {

  private AllocationProbe() {
  }

  public static long usedHeapBytes() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() - runtime.freeMemory();
  }

  public static void assertStable(Runnable body, int warmupRuns, int measuredRuns, long budgetBytes) {
    for (int i = 0; i < warmupRuns; i++) {
      body.run();
    }
    System.gc();
    long before = usedHeapBytes();
    for (int i = 0; i < measuredRuns; i++) {
      body.run();
    }
    long after = usedHeapBytes();
    long delta = after - before;
    assertTrue("hot loop allocated " + delta + " bytes over " + measuredRuns + " runs, budget " + budgetBytes,
        delta < budgetBytes);
  }
}
