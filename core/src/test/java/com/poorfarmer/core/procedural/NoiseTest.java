package com.poorfarmer.core.procedural;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoiseTest {

  @Test
  public void noise2StaysInRange() {
    Noise noise = new Noise(42);
    for (int i = 0; i < 2000; i++) {
      float x = (i % 100) * 0.37f;
      float y = (i / 100) * 0.37f;
      float v = noise.noise2(x, y);
      assertTrue(v >= 0f && v <= 1f);
      assertTrue(!Float.isNaN(v));
    }
  }

  @Test
  public void noise2IsDeterministicPerSeed() {
    Noise a = new Noise(7);
    Noise b = new Noise(7);
    Noise c = new Noise(8);
    for (float x = 0.1f; x < 5f; x += 0.1f) {
      assertEquals(a.noise2(x, x * 2f), b.noise2(x, x * 2f), 0f);
    }
    boolean differs = false;
    for (float x = 0.1f; x < 5f && !differs; x += 0.1f) {
      differs = a.noise2(x, x * 2f) != c.noise2(x, x * 2f);
    }
    assertTrue(differs);
  }

  @Test
  public void noise2IsContinuous() {
    Noise noise = new Noise(1);
    float maxJump = 0f;
    for (int i = 0; i < 500; i++) {
      float x = i * 0.02f;
      float y = 3.3f;
      maxJump = Math.max(maxJump, Math.abs(noise.noise2(x + 0.02f, y) - noise.noise2(x, y)));
    }
    assertTrue(maxJump < 0.2f);
  }

  @Test
  public void fbmStaysInRangeAndIsDeterministic() {
    Noise a = new Noise(99);
    Noise b = new Noise(99);
    for (int i = 0; i < 1000; i++) {
      float x = (i % 64) * 0.21f;
      float y = (i / 64) * 0.21f;
      float v = a.fbm2(x, y, 4, 0.5f, 2f);
      assertTrue(v >= 0f && v <= 1f);
      assertEquals(a.fbm2(x, y, 4, 0.5f, 2f), b.fbm2(x, y, 4, 0.5f, 2f), 0f);
    }
  }

  @Test
  public void ridgedPeaksNearZero() {
    Noise noise = new Noise(5);
    float max = 0f;
    for (int i = 0; i < 2000; i++) {
      max = Math.max(max, noise.ridged2((i % 80) * 0.3f, (i / 80) * 0.3f, 3));
    }
    assertTrue(max >= 0f);
    assertTrue(max <= 1f);
  }

  @Test
  public void worleyStaysInRangeAndDeterministic() {
    Noise a = new Noise(12);
    Noise b = new Noise(12);
    for (int i = 0; i < 1000; i++) {
      float x = (i % 40) * 0.31f;
      float y = (i / 40) * 0.31f;
      float v = a.worley2(x, y);
      assertTrue(v >= 0f && v <= 1.5f);
      assertEquals(a.worley2(x, y), b.worley2(x, y), 0f);
    }
  }
}
