package com.poorfarmer.core.procedural;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TextureTest {

  @Test
  public void setAndGetPixelRoundTrip() {
    Texture t = Texture.ofSize(4, 4);
    t.setPixel(2, 3, 0.25f, 0.5f, 0.75f, 1f);
    assertEquals(64, t.getR(2, 3));
    assertEquals(128, t.getG(2, 3));
    assertEquals(191, t.getB(2, 3));
    assertEquals(255, t.getA(2, 3));
  }

  @Test
  public void pixelValuesClampTo255() {
    Texture t = Texture.ofSize(2, 2);
    t.setPixel(0, 0, 1.5f, -0.5f, 0f, 1f);
    assertEquals(255, t.getR(0, 0));
    assertEquals(0, t.getG(0, 0));
    assertEquals(0, t.getB(0, 0));
  }

  @Test
  public void tintMultipliesChannels() {
    Texture t = Texture.ofSize(2, 2);
    t.setPixel(0, 0, 1f, 1f, 1f, 1f);
    t.setPixel(1, 0, 0.5f, 0.5f, 0.5f, 1f);
    t.tint(0.5f, 1f, 0.25f);
    assertEquals(128, t.getR(0, 0));
    assertEquals(255, t.getG(0, 0));
    assertEquals(64, t.getB(0, 0));
    assertEquals(64, t.getR(1, 0));
    assertEquals(128, t.getG(1, 0));
    assertEquals(32, t.getB(1, 0));
  }

  @Test
  public void blendInterpolates() {
    Texture a = Texture.ofSize(2, 2);
    Texture b = Texture.ofSize(2, 2);
    a.setPixel(0, 0, 0f, 1f, 0f, 1f);
    b.setPixel(0, 0, 1f, 0f, 0f, 1f);
    a.blend(b, 0.5f);
    assertEquals(128, a.getR(0, 0));
    assertEquals(128, a.getG(0, 0));
    assertEquals(0, a.getB(0, 0));
  }
}
