package com.poorfarmer.core.procedural;

public final class Texture {

  public final int width;
  public final int height;
  public final byte[] pixels;

  private Texture(int width, int height) {
    this.width = width;
    this.height = height;
    pixels = new byte[width * height * 4];
  }

  public static Texture ofSize(int width, int height) {
    return new Texture(width, height);
  }

  public void setPixel(int x, int y, float r, float g, float b, float a) {
    int i = (y * width + x) * 4;
    pixels[i] = (byte) toByte(r);
    pixels[i + 1] = (byte) toByte(g);
    pixels[i + 2] = (byte) toByte(b);
    pixels[i + 3] = (byte) toByte(a);
  }

  public int getR(int x, int y) {
    return to255(pixels[(y * width + x) * 4]);
  }

  public int getG(int x, int y) {
    return to255(pixels[(y * width + x) * 4 + 1]);
  }

  public int getB(int x, int y) {
    return to255(pixels[(y * width + x) * 4 + 2]);
  }

  public int getA(int x, int y) {
    return to255(pixels[(y * width + x) * 4 + 3]);
  }

  public void tint(float r, float g, float b) {
    for (int i = 0; i < pixels.length; i += 4) {
      pixels[i] = (byte) clamp255(to255(pixels[i]) * r);
      pixels[i + 1] = (byte) clamp255(to255(pixels[i + 1]) * g);
      pixels[i + 2] = (byte) clamp255(to255(pixels[i + 2]) * b);
    }
  }

  public void blend(Texture other, float amount) {
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) clamp255(to255(pixels[i]) * (1f - amount) + to255(other.pixels[i]) * amount);
    }
  }

  private static int clamp255(float v) {
    int r = (int) (v + 0.5f);
    return r < 0 ? 0 : (r > 255 ? 255 : r);
  }

  private static int toByte(float v) {
    int clamped = (int) (v * 255f + 0.5f);
    return clamped < 0 ? 0 : (clamped > 255 ? 255 : clamped);
  }

  private static int to255(byte value) {
    return value & 0xff;
  }
}
