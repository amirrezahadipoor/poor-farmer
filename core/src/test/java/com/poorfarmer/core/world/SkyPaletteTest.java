package com.poorfarmer.core.world;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SkyPaletteTest {

  @Test
  public void daySkyHasBlueZenith() {
    SkyPalette day = new SkyPalette(45f, 1f, 0);
    assertTrue("zenith should be blue-ish", day.colorOf(SkyPalette.ZENITH, 2) > day.colorOf(SkyPalette.ZENITH, 0));
    assertTrue(day.colorOf(SkyPalette.ZENITH, 2) > 0.5f);
    assertTrue(day.starAlpha < 0.05f);
    assertTrue(day.moonAlpha < 0.05f);
  }

  @Test
  public void nightSkyIsDarkWithStarsAndMoon() {
    SkyPalette night = new SkyPalette(-20f, 0f, 0);
    assertTrue(night.colorOf(SkyPalette.ZENITH, 0) < 0.05f);
    assertTrue(night.colorOf(SkyPalette.HORIZON, 2) < 0.2f);
    assertTrue(night.starAlpha > 0.9f);
    assertTrue(night.moonAlpha > 0.9f);
  }

  @Test
  public void duskHorizonIsWarm() {
    SkyPalette dusk = new SkyPalette(5f, 0.4f, 0);
    assertTrue("horizon red should beat blue at dusk",
        dusk.colorOf(SkyPalette.HORIZON, 0) > dusk.colorOf(SkyPalette.HORIZON, 2));
  }

  @Test
  public void sunGlowOnlyWhenSunIsUp() {
    SkyPalette day = new SkyPalette(30f, 1f, 0);
    SkyPalette night = new SkyPalette(-10f, 0f, 0);
    float dayGlow = day.colorOf(SkyPalette.SUN_GLOW, 0);
    float nightGlow = night.colorOf(SkyPalette.SUN_GLOW, 0);
    assertTrue("day glow should be bright", dayGlow > 0.5f);
    assertTrue("night glow should be zero", nightGlow < 0.001f);
  }

  @Test
  public void seasonShiftsTint() {
    SkyPalette spring = new SkyPalette(45f, 1f, 0);
    SkyPalette autumn = new SkyPalette(45f, 1f, 2);
    float springR = spring.colorOf(SkyPalette.HORIZON, 0);
    float autumnR = autumn.colorOf(SkyPalette.HORIZON, 0);
    assertTrue("autumn horizon should be warmer", autumnR > springR);
  }

  @Test
  public void cloudsAreBrightInDayAndDimAtNight() {
    SkyPalette day = new SkyPalette(45f, 1f, 1);
    SkyPalette night = new SkyPalette(-20f, 0f, 1);
    assertTrue(day.colorOf(SkyPalette.CLOUD_BASE, 0) > 0.8f);
    assertTrue(night.colorOf(SkyPalette.CLOUD_BASE, 0) < 0.3f);
    assertTrue(day.colorOf(SkyPalette.CLOUD_SHADOW, 0) < day.colorOf(SkyPalette.CLOUD_BASE, 0));
  }
}
