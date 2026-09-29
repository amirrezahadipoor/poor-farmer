package com.poorfarmer.core.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PersianTextTest {

  @Test
  public void convertsDigits() {
    assertEquals("۱۲۳۴۵۶۷۸۹۰", PersianText.toPersianDigits("1234567890"));
  }

  @Test
  public void leavesOtherCharacters() {
    assertEquals("a۱b", PersianText.toPersianDigits("a1b"));
    assertEquals("بهار۱", PersianText.toPersianDigits("بهار1"));
  }

  @Test
  public void nullStaysNull() {
    assertEquals(null, PersianText.toPersianDigits(null));
  }

  @Test
  public void percentRoundsAndClamps() {
    assertEquals("۶۵٪", PersianText.percent(0.65f));
    assertEquals("۰٪", PersianText.percent(0f));
    assertEquals("۱۰۰٪", PersianText.percent(1f));
    assertEquals("۰٪", PersianText.percent(-0.5f));
    assertEquals("۱۰۰٪", PersianText.percent(2f));
  }
}
