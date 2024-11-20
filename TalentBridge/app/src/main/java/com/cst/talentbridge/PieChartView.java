package com.cst.talentbridge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    private List<Slice> slices = new ArrayList<>();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();

    public PieChartView(Context context) {
        super(context);
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Sets the data for the pie chart.
     * Clears existing slices and adds new slices based on the provided values and colors.
     *
     * @param values Array of slice values.
     * @param colors Array of slice colors.
     * @param labels Array of slice labels.
     */
    public void setData(float[] values, int[] colors, String[] labels) {
        if (values.length != colors.length || values.length != labels.length) {
            throw new IllegalArgumentException("Values, colors, and labels arrays must have the same length");
        }

        slices.clear(); // Clear existing slices

        for (int i = 0; i < values.length; i++) {
            slices.add(new Slice((int) values[i], colors[i], labels[i]));
        }

        invalidate(); // Redraw the view
    }

    // Add a slice to the pie chart
    public void addSlice(int value, int color, String label) {
        slices.add(new Slice(value, color, label));
        invalidate(); // Redraw the view
    }

    // Clear all slices
    public void clearSlices() {
        slices.clear();
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (slices.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int padding = 20;

        // Define the pie chart area
        rectF.set(padding, padding, size - padding, size - padding);

        // Calculate total value of slices
        int total = 0;
        for (Slice slice : slices) {
            total += slice.value;
        }

        // Draw each slice
        float startAngle = 0;
        for (Slice slice : slices) {
            float sweepAngle = (slice.value / (float) total) * 360;

            // Draw slice
            paint.setColor(slice.color);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            startAngle += sweepAngle;
        }

        // Optional: Draw labels
        drawLabels(canvas, width, height);
    }

    private void drawLabels(Canvas canvas, int width, int height) {
        int legendX = 50; // X coordinate for labels
        int legendY = 20; // Starting Y coordinate for labels
        int lineHeight = 40; // Space between labels

        for (Slice slice : slices) {
            // Draw legend color box
            paint.setColor(slice.color);
            canvas.drawRect(legendX, legendY, legendX + 30, legendY + 30, paint);

            // Draw legend text
            paint.setColor(Color.BLACK);
            paint.setTextSize(30);
            canvas.drawText(slice.label + " (" + slice.value + ")", legendX + 40, legendY + 25, paint);

            legendY += lineHeight; // Move to the next label position
        }
    }

    // Helper class to represent a slice
    private static class Slice {
        int value;
        int color;
        String label;

        public Slice(int value, int color, String label) {
            this.value = value;
            this.color = color;
            this.label = label;
        }
    }
}
