package com.poorfarmer.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public class HitTesterTest {

  private static final class Box implements HitTarget {
    private final String name;
    private final float x;
    private final float z;
    private final float radius;

    Box(String name, float x, float z, float radius) {
      this.name = name;
      this.x = x;
      this.z = z;
      this.radius = radius;
    }

    @Override
    public Vec3 position() {
      return new Vec3(x, 0f, z);
    }

    @Override
    public float radius() {
      return radius;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @Test
  public void nearestWithinRadiusWins() {
    HitTester tester = new HitTester();
    Box far = new Box("far", 10f, 0f, 1f);
    Box near = new Box("near", 2f, 0f, 1f);
    tester.add(far);
    tester.add(near);
    assertSame(near, tester.nearest(1.5f, 0f));
  }

  @Test
  public void nothingWithinRadiusReturnsNull() {
    HitTester tester = new HitTester();
    tester.add(new Box("far", 10f, 0f, 1f));
    assertNull(tester.nearest(0f, 0f));
  }

  @Test
  public void removeStopsHitting() {
    HitTester tester = new HitTester();
    Box box = new Box("a", 1f, 1f, 2f);
    tester.add(box);
    assertSame(box, tester.nearest(1f, 1f));
    tester.remove(box);
    assertNull(tester.nearest(1f, 1f));
    assertEquals(0, tester.size());
  }

  @Test
  public void gridCellIndex() {
    assertEquals(0, HitTester.cellIndex(0f, 0f, 0f, 0f, 1f, 48));
    assertEquals(48 * 5 + 7, HitTester.cellIndex(7.5f, 5.2f, 0f, 0f, 1f, 48));
    assertEquals(-1, HitTester.cellIndex(48f, 0f, 0f, 0f, 1f, 48));
    assertEquals(-1, HitTester.cellIndex(-0.1f, 0f, 0f, 0f, 1f, 48));
  }

  @Test
  public void gridCellIndexWithOrigin() {
    assertEquals(0, HitTester.cellIndex(20f, 20f, 20f, 20f, 1f, 48));
    assertEquals(-1, HitTester.cellIndex(19.9f, 20f, 20f, 20f, 1f, 48));
  }
}
