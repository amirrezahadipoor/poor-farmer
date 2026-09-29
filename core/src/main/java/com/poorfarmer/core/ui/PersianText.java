package com.poorfarmer.core.ui;

public final class PersianText {

  private static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

  private PersianText() {
  }

  public static String toPersianDigits(String input) {
    if (input == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c >= '0' && c <= '9') {
        builder.append(PERSIAN_DIGITS[c - '0']);
      } else {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  public static String percent(float fraction) {
    int value = (int) Math.round(clamp(fraction, 0f, 1f) * 100f);
    return toPersianDigits(String.valueOf(value)) + "٪";
  }

  private static float clamp(float v, float min, float max) {
    return v < min ? min : (v > max ? max : v);
  }
}
