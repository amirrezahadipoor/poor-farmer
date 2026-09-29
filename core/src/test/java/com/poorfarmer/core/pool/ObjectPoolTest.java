package com.poorfarmer.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.poorfarmer.core.AllocationProbe;
import org.junit.Test;

public class ObjectPoolTest {

  private static final class Box {
    int value;
  }

  @Test
  public void factoryCreatesOnEmptyPool() {
    ObjectPool<Box> pool = new ObjectPool<>(4, Box::new, null);
    Box box = pool.obtain();
    assertNotNull(box);
    assertEquals(0, pool.pooledCount());
  }

  @Test
  public void recycledInstanceIsReused() {
    ObjectPool<Box> pool = new ObjectPool<>(4, Box::new, null);
    Box box = pool.obtain();
    pool.recycle(box);
    assertEquals(1, pool.pooledCount());
    assertSame(box, pool.obtain());
  }

  @Test
  public void resetterRunsOnReuseOnly() {
    ObjectPool<Box> pool = new ObjectPool<>(4, Box::new, box -> box.value = 0);
    Box box = pool.obtain();
    box.value = 5;
    pool.recycle(box);
    Box reused = pool.obtain();
    assertSame(box, reused);
    assertEquals(0, reused.value);
  }

  @Test
  public void recycleBeyondCapacityIsDropped() {
    ObjectPool<Box> pool = new ObjectPool<>(2, Box::new, null);
    Box a = pool.obtain();
    Box b = pool.obtain();
    Box c = pool.obtain();
    pool.recycle(a);
    pool.recycle(b);
    pool.recycle(c);
    assertEquals(2, pool.pooledCount());
    assertEquals(2, pool.capacity());
  }

  @Test
  public void nullRecycleIgnored() {
    ObjectPool<Box> pool = new ObjectPool<>(2, Box::new, null);
    pool.recycle(null);
    assertEquals(0, pool.pooledCount());
  }

  @Test
  public void invalidCapacityRejected() {
    try {
      new ObjectPool<>(0, Box::new, null);
      fail("expected capacity rejection");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void obtainRecycleLoopDoesNotAllocate() {
    ObjectPool<Box> pool = new ObjectPool<>(8, Box::new, box -> box.value = 0);
    AllocationProbe.assertStable(() -> {
      Box a = pool.obtain();
      Box b = pool.obtain();
      Box c = pool.obtain();
      Box d = pool.obtain();
      a.value += 1;
      b.value += 2;
      c.value += 3;
      d.value += 4;
      pool.recycle(a);
      pool.recycle(b);
      pool.recycle(c);
      pool.recycle(d);
    }, 500, 20_000, 256 * 1024L);
  }
}
