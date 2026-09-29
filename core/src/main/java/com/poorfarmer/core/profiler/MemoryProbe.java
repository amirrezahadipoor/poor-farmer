package com.poorfarmer.core.profiler;

public interface MemoryProbe {

  long usedBytes();

  long maxBytes();
}
