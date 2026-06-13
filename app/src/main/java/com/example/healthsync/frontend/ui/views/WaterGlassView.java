package com.example.healthsync.frontend.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.healthsync.R;

public class WaterGlassView extends View {
    private float fillPercent = 0.7f;
    private final Paint waterPaint;
    private final Paint glassPaint;

    public WaterGlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaint.setColor(ContextCompat.getColor(context, R.color.cyan_glow));
        
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setStyle(Paint.Style.STROKE);
        glassPaint.setColor(ContextCompat.getColor(context, R.color.divider_light));
        glassPaint.setStrokeWidth(4f);
    }

    public void setFillPercent(float percent) {
        ValueAnimator animator = ValueAnimator.ofFloat(fillPercent, percent);
        animator.setDuration(1500);
        animator.addUpdateListener(a -> {
            fillPercent = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        float fillTop = h * (1f - fillPercent);
        
        // Draw water fill
        canvas.drawRect(10, fillTop, w - 10, h - 10, waterPaint);
        
        // Draw glass outline (rounded rect)
        RectF rect = new RectF(4, 4, w - 4, h - 4);
        canvas.drawRoundRect(rect, 20, 20, glassPaint);
    }
}
