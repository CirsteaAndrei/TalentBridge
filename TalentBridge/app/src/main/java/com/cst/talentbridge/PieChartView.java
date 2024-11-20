package com.cst.talentbridge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {

    private float[] values;
    private int[] colors;
    private String[] labels;
    private Paint paint;
    private Paint textPaint;
    private RectF rectF;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.LEFT);
        rectF = new RectF();
    }

    public void setData(float[] values, int[] colors, String[] labels) {
        this.values = values;
        this.colors = colors;
        this.labels = labels;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || colors == null || labels == null || values.length != colors.length) {
            return; // Skip drawing if data is invalid
        }

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 200; // Leave space for the legend
        rectF.set(50, 100, size + 50, size + 100); // Shift pie chart down for the legend

        float total = 0;
        for (float value : values) {
            total += value;
        }

        float startAngle = 0;
        for (int i = 0; i < values.length; i++) {
            float sweepAngle = (values[i] / total) * 360;
            paint.setColor(colors[i]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // Draw the number on the slice
            float angle = startAngle + sweepAngle / 2;
            float x = (float) (rectF.centerX() + (rectF.width() / 3) * Math.cos(Math.toRadians(angle)));
            float y = (float) (rectF.centerY() + (rectF.height() / 3) * Math.sin(Math.toRadians(angle)));
            textPaint.setColor(Color.WHITE);
            canvas.drawText(String.valueOf((int) values[i]), x, y, textPaint);

            startAngle += sweepAngle;
        }

        // Draw the legend on top of the chart
        drawLegend(canvas, width);
    }

    private void drawLegend(Canvas canvas, int width) {
        int legendLeft = 50; // Start of the legend
        int legendTop = 20; // Position above the chart
        int boxSize = 40; // Size of the color box
        int spacing = 20; // Space between legend items

        for (int i = 0; i < labels.length; i++) {
            // Draw the color box
            paint.setColor(colors[i]);
            canvas.drawRect(legendLeft, legendTop, legendLeft + boxSize, legendTop + boxSize, paint);

            // Draw the label next to the box
            textPaint.setColor(Color.BLACK);
            canvas.drawText(labels[i], legendLeft + boxSize + spacing, legendTop + boxSize - 10, textPaint);

            legendLeft += 200; // Move to the next label position
        }
    }
}
