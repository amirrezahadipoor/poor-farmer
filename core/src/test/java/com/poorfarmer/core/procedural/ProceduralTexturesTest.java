package com.poorfarmer.core.procedural;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProceduralTexturesTest {

  private static final int S = 64;

  @Test
  public void dirtIsBrownishWithVariation() {
    Texture t = ProceduralTextures.dirt(new Noise(1), S);
    assertEquals(S, t.width);
    assertEquals(S, t.height);
    int min = 256;
    int max = -1;
    long rSum = 0;
    long bSum = 0;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        min = Math.min(min, t.getR(x, y));
        max = Math.max(max, t.getR(x, y));
        rSum += t.getR(x, y);
        bSum += t.getB(x, y);
      }
    }
    assertTrue(max - min > 20);
    assertTrue(rSum > bSum * 3 / 2);
  }

  @Test
  public void grassIsGreenerThanRed() {
    Texture t = ProceduralTextures.grass(new Noise(2), S);
    long rSum = 0;
    long gSum = 0;
    long bSum = 0;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        rSum += t.getR(x, y);
        gSum += t.getG(x, y);
        bSum += t.getB(x, y);
      }
    }
    assertTrue(gSum > rSum * 3 / 2);
    assertTrue(gSum > bSum);
  }

  @Test
  public void stoneIsNeutralGray() {
    Texture t = ProceduralTextures.stone(new Noise(3), S);
    long rSum = 0;
    long gSum = 0;
    long bSum = 0;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        rSum += t.getR(x, y);
        gSum += t.getG(x, y);
        bSum += t.getB(x, y);
      }
    }
    long n = (long) S * S;
    assertTrue(Math.abs(rSum - bSum) < n * 12);
    assertTrue(Math.abs(rSum - gSum) < n * 12);
  }

  @Test
  public void woodHasRingContrast() {
    Texture t = ProceduralTextures.wood(new Noise(4), S);
    int min = 256;
    int max = -1;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        min = Math.min(min, t.getR(x, y));
        max = Math.max(max, t.getR(x, y));
      }
    }
    assertTrue(max - min > 40);
  }

  @Test
  public void clothFollowsBaseColor() {
    Texture t = ProceduralTextures.cloth(0.8f, 0.2f, 0.2f, new Noise(5), S);
    long rSum = 0;
    long gSum = 0;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        rSum += t.getR(x, y);
        gSum += t.getG(x, y);
      }
    }
    assertTrue(rSum > gSum * 3);
  }

  @Test
  public void waterIsBluish() {
    Texture t = ProceduralTextures.water(new Noise(6), S);
    long rSum = 0;
    long bSum = 0;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        rSum += t.getR(x, y);
        bSum += t.getB(x, y);
      }
    }
    assertTrue(bSum > rSum * 3 / 2);
  }

  @Test
  public void skyGradientGoesFromTopToHorizon() {
    Texture t = ProceduralTextures.sky(0.1f, 0.2f, 0.6f, 0.9f, 0.9f, 0.8f, new Noise(7), S);
    assertEquals(S * 2, t.height);
    int topR = t.getR(0, 0);
    int bottomR = t.getR(0, t.height - 1);
    assertTrue(bottomR > topR * 2);
    int topB = t.getB(0, 0);
    int bottomB = t.getB(0, t.height - 1);
    assertTrue(bottomB >= topB);
  }

  @Test
  public void sandIsLighterThanDirt() {
    Texture sand = ProceduralTextures.sand(new Noise(8), S);
    Texture dirt = ProceduralTextures.dirt(new Noise(8), S);
    long sandSum = 0;
    long dirtSum = 0;
    for (int y = 0; y < S; y++) {
      for (int x = 0; x < S; x++) {
        sandSum += tMean(sand, x, y);
        dirtSum += tMean(dirt, x, y);
      }
    }
    assertTrue(sandSum > dirtSum);
  }

  private static int tMean(Texture t, int x, int y) {
    return (t.getR(x, y) + t.getG(x, y) + t.getB(x, y)) / 3;
  }

  @Test
  public void generatorsAreDeterministic() {
    Noise a = new Noise(21);
    Noise b = new Noise(21);
    Noise c = new Noise(22);
    Texture ta = ProceduralTextures.grass(a, 16);
    Texture tb = ProceduralTextures.grass(b, 16);
    Texture tc = ProceduralTextures.grass(c, 16);
    assertEquals(ta.pixels.length, tb.pixels.length);
    boolean identical = java.util.Arrays.equals(ta.pixels, tb.pixels);
    boolean differs = !java.util.Arrays.equals(ta.pixels, tc.pixels);
    assertTrue(identical);
    assertTrue(differs);
  }
}
