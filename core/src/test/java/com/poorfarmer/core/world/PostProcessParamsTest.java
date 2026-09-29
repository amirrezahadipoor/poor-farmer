package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PostProcessParamsTest {

  private static PostProcessParams noon(int season) {
    return PostProcessParams.resolve(season, 55f, 1f);
  }

  @Test
  public void seasonBaseExposure() {
    assertEquals(1.00f, noon(0).exposure, 0.001f);
    assertEquals(1.06f, noon(1).exposure, 0.001f);
    assertEquals(0.97f, noon(2).exposure, 0.001f);
    assertEquals(0.92f, noon(3).exposure, 0.001f);
  }

  @Test
  public void winterIsCoolerAndDarkerThanSummer() {
    PostProcessParams winter = noon(3);
    PostProcessParams summer = noon(1);
    assertTrue(winter.tintB > summer.tintB);
    assertTrue(winter.tintR < summer.tintR);
    assertTrue(winter.vignetteStrength > summer.vignetteStrength);
  }

  @Test
  public void autumnIsWarmerThanSpring() {
    assertTrue(noon(2).tintR > noon(0).tintR);
    assertTrue(noon(2).tintB < noon(0).tintB);
  }

  @Test
  public void duskLowersBloomThresholdAndAddsWarmth() {
    PostProcessParams dusk = PostProcessParams.resolve(1, 8f, 0.4f);
    PostProcessParams noon = noon(1);
    assertTrue(dusk.bloomThreshold < noon.bloomThreshold);
    assertTrue(dusk.warmth > 0f);
    assertEquals(0f, noon.warmth, 0f);
    assertTrue(dusk.tintR > noon.tintR);
    assertTrue(dusk.tintB < noon.tintB);
  }

  @Test
  public void duskStrengthGrowsAsSunLowers() {
    PostProcessParams low = PostProcessParams.resolve(1, 3f, 0.3f);
    PostProcessParams high = PostProcessParams.resolve(1, 15f, 0.3f);
    assertTrue(low.warmth > high.warmth);
    assertTrue(low.bloomThreshold < high.bloomThreshold);
  }

  @Test
  public void middayHasNoDusk() {
    PostProcessParams noon = noon(0);
    assertEquals(0f, noon.warmth, 0f);
    assertEquals(PostProcessParams.NOON_BLOOM_THRESHOLD, noon.bloomThreshold, 0.001f);
    assertEquals(PostProcessParams.NOON_BLOOM_INTENSITY, noon.bloomIntensity, 0.001f);
  }

  @Test
  public void nightKillsBloomAndDeepensVignette() {
    PostProcessParams night = PostProcessParams.resolve(1, -10f, 0f);
    PostProcessParams dusk = PostProcessParams.resolve(1, 8f, 0.4f);
    assertEquals(PostProcessParams.NIGHT_BLOOM_INTENSITY, night.bloomIntensity, 0.001f);
    assertEquals(0f, night.warmth, 0f);
    assertTrue(night.vignetteStrength > noon(1).vignetteStrength);
  }

  @Test
  public void elevationAboveTwentyIsNotDusk() {
    PostProcessParams high = PostProcessParams.resolve(2, 25f, 0.6f);
    assertEquals(0f, high.warmth, 0f);
    assertEquals(PostProcessParams.NOON_BLOOM_THRESHOLD, high.bloomThreshold, 0.001f);
  }
}
