package com.createchance.avflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.createchance.avflow.utils.DensityUtil;

public class SquareProgressView extends View {

    private double progress;
    private Paint progressBarPaint;

    private float widthInDp = 10;
    private float strokewidth = 0;

    private boolean showProgress = false;

    private boolean roundedCorners = false;
    private float roundedCornersRadius = 10;

    public SquareProgressView(Context context) {
        super(context);
        initializePaints(context);
    }

    public SquareProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializePaints(context);
    }

    public SquareProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializePaints(context);
    }

    private void initializePaints(Context context) {
        progressBarPaint = new Paint();
        progressBarPaint.setColor(context.getResources().getColor(
                android.R.color.holo_red_light));
        progressBarPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), widthInDp));
        progressBarPaint.setAntiAlias(true);
        progressBarPaint.setStyle(Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        strokewidth = DensityUtil.dip2px(getContext(), widthInDp);
        float scope = canvas.getWidth() + canvas.getHeight()
                + canvas.getHeight() + canvas.getWidth() - strokewidth;

        if ((progress <= 0.0)) {
            return;
        }

        Path path = new Path();
        DrawStop drawEnd = getDrawEnd((scope / 100) * Float.valueOf(String.valueOf(progress)), canvas);

        if (drawEnd.place == Place.TOP) {
            path.moveTo(0, strokewidth / 2);
            path.lineTo(drawEnd.location, strokewidth / 2);
            canvas.drawPath(path, progressBarPaint);
        }

        if (drawEnd.place == Place.RIGHT) {
            path.moveTo(0, strokewidth / 2);
            path.lineTo(canvas.getWidth() - (strokewidth / 2), strokewidth / 2);
            path.lineTo(canvas.getWidth() - (strokewidth / 2), strokewidth / 2
                    + drawEnd.location);
            canvas.drawPath(path, progressBarPaint);
        }

        if (drawEnd.place == Place.BOTTOM) {
            path.moveTo(0, strokewidth / 2);
            path.lineTo(canvas.getWidth() - (strokewidth / 2), strokewidth / 2);
            path.lineTo(canvas.getWidth() - (strokewidth / 2), canvas.getHeight());
            path.moveTo(canvas.getWidth(), canvas.getHeight() - strokewidth / 2);
            path.lineTo(drawEnd.location, canvas.getHeight()
                    - (strokewidth / 2));
            canvas.drawPath(path, progressBarPaint);
        }

        if (drawEnd.place == Place.LEFT) {
            path.moveTo(0, strokewidth / 2);
            path.lineTo(canvas.getWidth() - (strokewidth / 2), strokewidth / 2);
            path.lineTo(canvas.getWidth() - (strokewidth / 2), canvas.getHeight() - strokewidth / 2);
            path.lineTo(0, canvas.getHeight() - strokewidth / 2);
            path.moveTo(strokewidth / 2, canvas.getHeight() - strokewidth / 2);
            path.lineTo((strokewidth / 2), drawEnd.location);
            canvas.drawPath(path, progressBarPaint);
        }
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
        this.invalidate();
    }

    public void setColor(int color) {
        progressBarPaint.setColor(color);
        this.invalidate();
    }

    public void setWidthInDp(int width) {
        this.widthInDp = width;
        progressBarPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), widthInDp));
        this.invalidate();
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
        this.invalidate();
    }

    public DrawStop getDrawEnd(float percent, Canvas canvas) {
        DrawStop drawStop = new DrawStop();
        strokewidth = DensityUtil.dip2px(getContext(), widthInDp);
        float imageWidth = canvas.getWidth();

        if (percent > imageWidth) {
            float second = percent - imageWidth;

            if (second > canvas.getHeight()) {
                float third = second - canvas.getHeight();

                if (third > canvas.getWidth()) {
                    float forth = third - canvas.getWidth();

                    if (forth == imageWidth) {
                        drawStop.place = Place.LEFT;
                        drawStop.location = imageWidth;
                    } else {
                        drawStop.place = Place.LEFT;
                        drawStop.location = canvas.getHeight() - forth;
                    }

                } else {
                    drawStop.place = Place.BOTTOM;
                    drawStop.location = canvas.getWidth() - third;
                }
            } else {
                drawStop.place = Place.RIGHT;
                drawStop.location = strokewidth + second;
            }

        } else {
            drawStop.place = Place.TOP;
            drawStop.location = 0 + percent;
        }

        return drawStop;
    }

    public void setRoundedCorners(boolean roundedCorners, float radius) {
        this.roundedCorners = roundedCorners;
        this.roundedCornersRadius = radius;
        if (roundedCorners) {
            progressBarPaint.setPathEffect(new CornerPathEffect(roundedCornersRadius));
        } else {
            progressBarPaint.setPathEffect(null);
        }
        this.invalidate();
    }

    public boolean isRoundedCorners() {
        return roundedCorners;
    }

    private class DrawStop {

        private Place place;
        private float location;

        public DrawStop() {

        }
    }

    public enum Place {
        TOP, RIGHT, BOTTOM, LEFT
    }

}
