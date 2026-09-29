package com.poorfarmer.core.jobs;

public final class LoadingProgress {

  private final String label;
  private int totalUnits;
  private int completedUnits;

  public LoadingProgress(String label, int totalUnits) {
    this.label = label;
    this.totalUnits = totalUnits;
  }

  public String label() {
    return label;
  }

  public synchronized void reset(int totalUnits) {
    this.totalUnits = totalUnits;
    this.completedUnits = 0;
  }

  public synchronized void completeUnits(int units) {
    completedUnits += units;
    if (completedUnits > totalUnits) {
      completedUnits = totalUnits;
    }
  }

  public synchronized float fraction() {
    if (totalUnits <= 0) {
      return 1f;
    }
    return completedUnits / (float) totalUnits;
  }

  public synchronized int completedUnits() {
    return completedUnits;
  }

  public synchronized int totalUnits() {
    return totalUnits;
  }

  public synchronized boolean isComplete() {
    return completedUnits >= totalUnits;
  }
}
