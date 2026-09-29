package com.poorfarmer.core.profiler;

public final class JvmMemoryProbe implements MemoryProbe {

  private final Runtime runtime = Runtime.getRuntime();

  @Override
  public long usedBytes() {
    return runtime.totalMemory() - runtime.freeMemory();
  }

  @Override
  public long maxBytes() {
    return runtime.maxMemory();
  }
}
