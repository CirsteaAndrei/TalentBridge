package com.cst.talentbridge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomBarChartView extends View {
    private int studentsCount = 0;
    private int jobsCount = 0;

    private Paint studentPaint;
    private Paint jobPaint;
    private Paint textPaint;

    public CustomBarChartView(Context context) {
        super(context);
        init();
    }

    public CustomBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        studentPaint = new Paint();
        studentPaint.setColor(Color.parseColor("#4CAF50")); // Green
        studentPaint.setStyle(Paint.Style.FILL);

        jobPaint = new Paint();
        jobPaint.setColor(Color.parseColor("#2196F3")); // Blue
        jobPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int students, int jobs) {
        this.studentsCount = students;
        this.jobsCount = jobs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int maxCount = Math.max(studentsCount, jobsCount);
        int barHeightUnit = (int) (0.5 * height / (maxCount == 0 ? 1 : maxCount));

        int barWidth = width / 4;
        int barSpacing = width / 6;

        // Draw Students Bar
        int studentBarHeight = studentsCount * barHeightUnit;
        canvas.drawRect(
                barSpacing,
                height - studentBarHeight - 100,
                barSpacing + barWidth,
                height - 100,
                studentPaint
        );

        // Draw Jobs Bar
        int jobBarHeight = jobsCount * barHeightUnit;
        canvas.drawRect(
                2 * barSpacing + barWidth,
                height - jobBarHeight - 100,
                2 * barSpacing + 2 * barWidth,
                height - 100,
                jobPaint
        );

        // Draw Labels
        canvas.drawText("Students", barSpacing + barWidth / 2f, height - 40, textPaint);
        canvas.drawText("Jobs", 2 * barSpacing + 1.5f * barWidth, height - 40, textPaint);

        // Draw Values
        canvas.drawText(String.valueOf(studentsCount), barSpacing + barWidth / 2f, height - studentBarHeight - 120, textPaint);
        canvas.drawText(String.valueOf(jobsCount), 2 * barSpacing + 1.5f * barWidth, height - jobBarHeight - 120, textPaint);
    }
}
