package com.poorfarmer.core.world;

public enum WorldLocation {

  FARM(0f, 0f, "مزرعه"),
  VILLAGE(60f, 15f, "روستا"),
  TOWN(-70f, 14f, "شهر"),
  TEHRAN(71f, -78f, "تهران");

  private final float focusX;
  private final float focusZ;
  private final String label;

  WorldLocation(float focusX, float focusZ, String label) {
    this.focusX = focusX;
    this.focusZ = focusZ;
    this.label = label;
  }

  public float focusX() {
    return focusX;
  }

  public float focusZ() {
    return focusZ;
  }

  public String label() {
    return label;
  }
}
