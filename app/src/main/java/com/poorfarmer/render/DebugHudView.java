package com.poorfarmer.render;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.poorfarmer.core.profiler.FpsMeter;
import com.poorfarmer.core.profiler.MemoryProbe;

public final class DebugHudView extends View {

  private static final int PANEL = 0x99000000;
  private static final int GOOD = Color.rgb(122, 222, 122);
  private static final int BAD = Color.rgb(232, 92, 92);
  private static final int NEUTRAL = Color.rgb(232, 238, 244);

  private final FpsMeter fpsMeter;
  private final MemoryProbe memoryProbe;
  private final Paint panelPaint = new Paint();
  private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Runnable selfRefresh = new Runnable() {
    @Override
    public void run() {
      if (getVisibility() == VISIBLE) {
        invalidate();
        postDelayed(this, 250);
      }
    }
  };

  public DebugHudView(Context context, FpsMeter fpsMeter, MemoryProbe memoryProbe) {
    super(context);
    this.fpsMeter = fpsMeter;
    this.memoryProbe = memoryProbe;
    panelPaint.setColor(PANEL);
    textPaint.setTextSize(34f);
  }

  @Override
  protected void onVisibilityChanged(View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    removeCallbacks(selfRefresh);
    if (visibility == VISIBLE) {
      postDelayed(selfRefresh, 250);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    float left = 8f;
    float top = 8f;
    float lineHeight = 40f;
    int fps = fpsMeter.fps();
    long usedKb = memoryProbe.usedBytes() / 1024;
    long maxKb = memoryProbe.maxBytes() / 1024;
    String fpsLine = "FPS " + fps;
    String avgLine = "avg " + String.format(java.util.Locale.ROOT, "%.1f", fpsMeter.averageFrameMs()) + " ms";
    String worstLine = "worst " + String.format(java.util.Locale.ROOT, "%.1f", fpsMeter.worstFrameMs()) + " ms";
    String memLine = "heap " + (usedKb / 1024) + "/" + (maxKb / 1024) + " MB";
    float panelWidth = Math.max(
        Math.max(textPaint.measureText(fpsLine), textPaint.measureText(avgLine)),
        Math.max(textPaint.measureText(worstLine), textPaint.measureText(memLine))) + 24f;
    canvas.drawRect(left, top, left + panelWidth, top + lineHeight * 4 + 16f, panelPaint);
    float x = left + 12f;
    float y = top + lineHeight;
    textPaint.setColor(fpsMeter.meetsTarget(60) ? GOOD : BAD);
    canvas.drawText(fpsLine, x, y, textPaint);
    textPaint.setColor(NEUTRAL);
    canvas.drawText(avgLine, x, y + lineHeight, textPaint);
    canvas.drawText(worstLine, x, y + 2 * lineHeight, textPaint);
    canvas.drawText(memLine, x, y + 3 * lineHeight, textPaint);
  }
}
