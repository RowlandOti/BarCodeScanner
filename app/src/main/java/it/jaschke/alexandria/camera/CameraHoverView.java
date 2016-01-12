package it.jaschke.alexandria.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraHoverView extends View {

    // Class logging Identifier
    private final String LOG_TAG = CameraHoverView.class.getSimpleName();

    private Paint mPaint;
    private int mLeft, mTop, mRight, mBottom;

    public CameraHoverView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Update view Rectangle a new rectangle with the specified coordinates. Note: no range
     * checking is performed, so the caller must ensure that left <= right and
     * top <= bottom.
     *
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    public void update(int left, int top, int right, int bottom) {
        mLeft = left;
        mRight = right;
        mTop = top;
        mBottom = bottom;
        invalidate();
    }

    public int getHoverRight() {
        return mRight;
    }

    public int getHoverLeft() {
        return mLeft;
    }

    public int getHoverTop() {
        return mTop;
    }

    public int getHoverBottom() {
        return mBottom;
    }

    public int getHoverWidth() {
        return mRight - mLeft;
    }

    public int getHoverHeight() {
        return mBottom - mTop;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawRect(mLeft, mTop, mRight, mBottom, mPaint);
    }
}
