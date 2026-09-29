package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.AllocationProbe;
import org.junit.Test;

public class ParticleSystemTest {

  @Test
  public void emitAndAgeParticles() {
    ParticleSystem ps = new ParticleSystem(256, 1L);
    ps.emit(ParticleType.DUST, 0f, 0f, 0f, 10, 0);
    assertEquals(10, ps.activeCount());
    for (int i = 0; i < 600; i++) {
      ps.update(1f / 60f);
    }
    assertTrue(ps.activeCount() < 10);
  }

  @Test
  public void poolReusesFreedSlots() {
    ParticleSystem ps = new ParticleSystem(16, 2L);
    ps.emit(ParticleType.WATER, 0f, 0f, 0f, 16, 0);
    assertEquals(16, ps.activeCount());
    for (int i = 0; i < 240; i++) {
      ps.update(1f / 60f);
    }
    assertEquals(0, ps.activeCount());
    ps.emit(ParticleType.WATER, 0f, 0f, 0f, 16, 0);
    assertEquals(16, ps.activeCount());
  }

  @Test
  public void emissionRejectsWhenFull() {
    ParticleSystem ps = new ParticleSystem(8, 3L);
    assertTrue(ps.emit(ParticleType.SMOKE, 0f, 0f, 0f, 8, 0));
    assertFalse(ps.emit(ParticleType.SMOKE, 0f, 0f, 0f, 1, 0));
    assertEquals(8, ps.activeCount());
  }

  @Test
  public void rainFallsFastAndDiesAtGround() {
    ParticleSystem ps = new ParticleSystem(64, 4L);
    ps.emit(ParticleType.RAIN, 0f, 0f, 0f, 4, 0);
    float startY = 0f;
    for (int i = 0; i < 8; i++) {
      startY += ps.particleY(i);
    }
    startY /= 8;
    assertTrue(startY > 5f);
    for (int i = 0; i < 300 && ps.activeCount() > 0; i++) {
      ps.update(1f / 60f);
    }
    assertEquals(0, ps.activeCount());
  }

  @Test
  public void smokeRisesAndGrows() {
    ParticleSystem ps = new ParticleSystem(16, 5L);
    ps.emit(ParticleType.SMOKE, 0f, 0f, 0f, 1, 0);
    float startSize = ps.particleSize(0);
    for (int i = 0; i < 60; i++) {
      ps.update(1f / 60f);
    }
    assertTrue(ps.particleY(0) > 0.2f);
    assertTrue(ps.particleSize(0) > startSize);
  }

  @Test
  public void coinFollowsArc() {
    ParticleSystem ps = new ParticleSystem(16, 6L);
    ps.emit(ParticleType.COIN, 0f, 0f, 0f, 1, 0);
    float maxY = 0f;
    boolean rising = true;
    for (int i = 0; i < 120; i++) {
      ps.update(1f / 60f);
      float y = ps.particleY(0);
      if (rising && y < maxY - 0.05f) {
        rising = false;
      }
      maxY = Math.max(maxY, y);
    }
    assertFalse(rising);
    assertTrue(maxY > 1f);
  }

  @Test
  public void smokeAlphaFadesBeforeDeath() {
    ParticleSystem ps = new ParticleSystem(16, 7L);
    ps.emit(ParticleType.SMOKE, 0f, 0f, 0f, 1, 0);
    for (int i = 0; i < 60; i++) {
      ps.update(1f / 60f);
    }
    float peakAlpha = ps.particleAlpha(0);
    assertEquals(0.5f, peakAlpha, 0.01f);
    float lastAlpha = 0f;
    while (ps.activeCount() > 0) {
      lastAlpha = ps.particleAlpha(0);
      ps.update(1f / 60f);
    }
    assertTrue(lastAlpha < peakAlpha - 0.05f);
  }

  @Test
  public void snowAndLeafSwayHorizontally() {
    ParticleSystem snow = new ParticleSystem(16, 8L);
    snow.emit(ParticleType.SNOW, 0f, 0f, 0f, 1, 3);
    for (int i = 0; i < 180; i++) {
      snow.update(1f / 60f);
    }
    assertTrue(Math.abs(snow.particleX(0)) > 0.1f);
    ParticleSystem leaf = new ParticleSystem(16, 9L);
    leaf.emit(ParticleType.LEAF, 0f, 0f, 0f, 1, 2);
    for (int i = 0; i < 180; i++) {
      leaf.update(1f / 60f);
    }
    assertTrue(leaf.particleY(0) < 2f);
  }

  @Test
  public void sameSeedGivesSameTrajectory() {
    ParticleSystem a = new ParticleSystem(32, 42L);
    ParticleSystem b = new ParticleSystem(32, 42L);
    a.emit(ParticleType.DUST, 0f, 0f, 0f, 5, 0);
    b.emit(ParticleType.DUST, 0f, 0f, 0f, 5, 0);
    for (int i = 0; i < 120; i++) {
      a.update(1f / 60f);
      b.update(1f / 60f);
    }
    for (int p = 0; p < 5; p++) {
      assertEquals(a.particleX(p), b.particleX(p), 0f);
      assertEquals(a.particleY(p), b.particleY(p), 0f);
    }
  }

  @Test
  public void killAllFreesEverything() {
    ParticleSystem ps = new ParticleSystem(32, 10L);
    ps.emit(ParticleType.SMOKE, 0f, 0f, 0f, 20, 0);
    ps.killAll();
    assertEquals(0, ps.activeCount());
    ps.emit(ParticleType.SMOKE, 0f, 0f, 0f, 20, 0);
    assertEquals(20, ps.activeCount());
  }

  @Test
  public void updateWithFullPoolDoesNotAllocate() {
    ParticleSystem ps = new ParticleSystem(4096, 11L);
    while (ps.emit(ParticleType.DUST, 0f, 0f, 0f, 64, 0)) {
    }
    assertTrue(ps.activeCount() >= 4000);
    AllocationProbe.assertStable(() -> ps.update(1f / 60f), 300, 3000, 256 * 1024L);
  }
}
