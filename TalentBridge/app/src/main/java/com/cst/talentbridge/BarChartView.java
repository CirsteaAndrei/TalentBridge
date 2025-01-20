package com.cst.talentbridge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class BarChartView extends View {

    private List<Integer> dataValues;
    private List<String> labels;
    private int barColor = Color.BLUE;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(List<Integer> dataValues, List<String> labels) {
        this.dataValues = dataValues;
        this.labels = labels;
        invalidate(); // Redraw the view with new data
    }

    public void setBarColor(int color) {
        this.barColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataValues == null || labels == null || dataValues.isEmpty() || labels.isEmpty()) {
            return;
        }

        Paint barPaint = new Paint();
        barPaint.setColor(barColor);
        barPaint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);

        int width = getWidth();
        int height = getHeight();
        int barWidth = width / (dataValues.size() * 2);
        int maxDataValue = 0;

        for (int value : dataValues) {
            maxDataValue = Math.max(maxDataValue, value);
        }

        for (int i = 0; i < dataValues.size(); i++) {
            int barHeight = (int) ((dataValues.get(i) / (float) maxDataValue) * (height * 0.6));
            int xStart = i * 2 * barWidth + barWidth / 2;
            int yStart = height - barHeight;
            int xEnd = xStart + barWidth;
            int yEnd = height;

            // Draw bar
            canvas.drawRect(xStart, yStart, xEnd, yEnd, barPaint);

            // Draw label
            canvas.drawText(labels.get(i), xStart, height - 10, textPaint);

            // Draw value
            canvas.drawText(String.valueOf(dataValues.get(i)), xStart + barWidth / 4, yStart - 10, textPaint);
        }
    }
}
