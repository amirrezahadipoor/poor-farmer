package com.poorfarmer.render;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.poorfarmer.core.jobs.LoadingProgress;
import com.poorfarmer.core.ui.PersianText;

public final class LoadingOverlayView extends View {

  private static final int BACKGROUND = Color.rgb(12, 22, 30);
  private static final int BAR_TRACK = Color.rgb(38, 58, 74);
  private static final int BAR_FILL = Color.rgb(122, 182, 92);
  private static final int TEXT = Color.rgb(232, 238, 244);

  private final LoadingProgress progress;
  private final Paint backgroundPaint = new Paint();
  private final Paint trackPaint = new Paint();
  private final Paint fillPaint = new Paint();
  private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint percentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private boolean finished;

  public LoadingOverlayView(Context context, LoadingProgress progress) {
    super(context);
    this.progress = progress;
    backgroundPaint.setColor(BACKGROUND);
    trackPaint.setColor(BAR_TRACK);
    fillPaint.setColor(BAR_FILL);
    textPaint.setColor(TEXT);
    textPaint.setTextAlign(Paint.Align.RIGHT);
    percentPaint.setColor(TEXT);
    percentPaint.setTextAlign(Paint.Align.LEFT);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int w = getWidth();
    int h = getHeight();
    canvas.drawRect(0f, 0f, w, h, backgroundPaint);
    float margin = w * 0.08f;
    float barWidth = w - 2f * margin;
    float barHeight = Math.max(8f, h * 0.018f);
    float barY = h * 0.62f;
    float fraction = progress.fraction();
    canvas.drawRect(margin, barY, margin + barWidth, barY + barHeight, trackPaint);
    if (fraction > 0f) {
      float fillWidth = barWidth * fraction;
      float right = margin + barWidth;
      canvas.drawRect(right - fillWidth, barY, right, barY + barHeight, fillPaint);
    }
    textPaint.setTextSize(h * 0.06f);
    canvas.drawText(progress.label(), w - margin, h * 0.52f, textPaint);
    percentPaint.setTextSize(h * 0.045f);
    canvas.drawText(PersianText.percent(fraction), margin, h * 0.52f, percentPaint);
    if (progress.isComplete() && !finished) {
      finished = true;
      post(() -> setVisibility(GONE));
    } else if (!progress.isComplete()) {
      postInvalidateOnAnimation();
    }
  }
}
