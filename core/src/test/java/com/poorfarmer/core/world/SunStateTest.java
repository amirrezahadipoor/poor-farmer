package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public class SunStateTest {

  @Test
  public void noonSummerSunIsHighAndWhite() {
    SunState s = new SunState(12.5f, 1);
    assertTrue(s.elevationDegrees() > 60f);
    assertTrue(s.sunIntensity() > 0.99f);
    Vec3 d = s.direction();
    assertTrue(Math.abs(d.length() - 1f) < 0.01f);
    assertTrue(d.z < -0.3f);
    assertTrue(d.x > -0.05f && d.x < 0.05f);
    assertTrue(s.sunColor()[2] > 0.9f);
    assertFalse(s.isNight());
  }

  @Test
  public void winterNoonSunIsLowerThanSummer() {
    SunState winter = new SunState(12f, 3);
    SunState summer = new SunState(12f, 1);
    assertTrue(winter.elevationDegrees() < summer.elevationDegrees());
  }

  @Test
  public void sunriseSunIsLowAndWarmTowardEast() {
    SunState s = new SunState(6.6f, 0);
    assertTrue(s.elevationDegrees() < 10f);
    assertTrue(s.direction().x > 0.5f);
    assertTrue(s.sunColor()[0] > s.sunColor()[2]);
  }

  @Test
  public void sunsetSunIsTowardWest() {
    SunState s = new SunState(18.2f, 0);
    assertTrue(s.direction().x < -0.5f);
    assertTrue(s.elevationDegrees() < 15f);
  }

  @Test
  public void nightHasNoSunAndLowAmbient() {
    SunState s = new SunState(2f + 24f, 1);
    assertTrue(s.isNight());
    assertEquals(0f, s.sunIntensity(), 0f);
    assertEquals(0f, s.dayFactor(), 0f);
    assertTrue(s.ambientIntensity() < 0.5f);
    assertTrue(s.ambientColor()[2] > s.ambientColor()[0]);
  }

  @Test
  public void intensityPeaksAroundNoon() {
    SunState morning = new SunState(9f, 1);
    SunState noon = new SunState(12.75f, 1);
    SunState evening = new SunState(16f, 1);
    assertTrue(noon.sunIntensity() > morning.sunIntensity());
    assertTrue(noon.sunIntensity() > evening.sunIntensity());
  }

  @Test
  public void fogIsDenserAtDuskThanNoon() {
    SunState noon = new SunState(12f, 0);
    SunState dusk = new SunState(18f, 0);
    assertTrue(dusk.fogEnd() < noon.fogEnd());
    assertTrue(dusk.fogStart() < noon.fogStart());
  }

  @Test
  public void fogColorIsBlueishAtNightAndLightByDay() {
    SunState day = new SunState(12f, 0);
    SunState night = new SunState(25f, 0);
    assertTrue(day.fogColor()[1] > 0.6f);
    assertTrue(night.fogColor()[1] < 0.2f);
  }

  @Test
  public void sunDirectionIsAlwaysUnitLength() {
    for (float hour = 6f; hour <= 26f; hour += 0.5f) {
      SunState s = new SunState(hour, 2);
      if (!s.isNight()) {
        assertEquals(1f, s.direction().length(), 0.01f);
      }
    }
  }
}
