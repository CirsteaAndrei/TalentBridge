package com.cst.talentbridge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

public class CustomPieChart extends View {

    private List<Float> values; // List of percentages
    private List<Integer> colors; // List of colors
    private String centerText = ""; // Optional center text

    private Paint paint;
    private Paint textPaint;

    public CustomPieChart(Context context) {
        super(context);
        init();
    }

    public CustomPieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Float> values, List<Integer> colors, String centerText) {
        this.values = values;
        this.colors = colors;
        this.centerText = centerText;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || colors == null || values.isEmpty() || colors.isEmpty()) {
            return; // No data to draw
        }

        float startAngle = 0f;
        RectF rect = new RectF(100, 100, getWidth() - 100, getHeight() - 100); // Adjust as needed

        for (int i = 0; i < values.size(); i++) {
            paint.setColor(colors.get(i));
            float sweepAngle = values.get(i) * 360; // Convert percentage to angle
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        // Draw center text
        if (!centerText.isEmpty()) {
            canvas.drawText(centerText, getWidth() / 2f, getHeight() / 2f, textPaint);
        }
    }
}
