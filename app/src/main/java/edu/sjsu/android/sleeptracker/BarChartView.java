package edu.sjsu.android.sleeptracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class BarChartView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread drawThread;
    private SurfaceHolder surfaceHolder;
    private boolean isDrawing;
    private List<Float> data;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDrawing = true;
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
        try {
            drawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isDrawing) {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    drawChart(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void drawChart(Canvas canvas) {
        if (data == null || data.isEmpty()) return;

        canvas.drawColor(Color.WHITE);
        Paint barPaint = new Paint();
        barPaint.setColor(Color.BLUE);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);

        int chartWidth = getWidth();
        int chartHeight = getHeight();
        int barWidth = chartWidth / data.size();
        int maxHeight = chartHeight - 100;

        for (int i = 0; i < data.size(); i++) {
            float barHeight = (data.get(i) * maxHeight) / 200;
            int left = i * barWidth;
            float top = chartHeight - barHeight;
            int right = left + barWidth - 20;
            int bottom = chartHeight;

            canvas.drawRect(left, top, right, bottom, barPaint);

            canvas.drawText(String.valueOf(data.get(i)), left + (barWidth / 4), chartHeight - 10, textPaint);
        }
    }

    public void setData(List<Float> newData) {
        this.data = newData;
        invalidate();
    }
}
