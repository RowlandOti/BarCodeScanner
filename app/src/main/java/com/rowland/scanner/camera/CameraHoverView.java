package com.rowland.scanner.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraHoverView extends FrameLayout {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80l;
    private static final int POINT_SIZE = 10;
    // Class logging Identifier
    private final String LOG_TAG = CameraHoverView.class.getSimpleName();
    protected int mBorderLineLength = 2;
    private Rect mBoundingScanRect;
    //Drawing materials
    private Paint mBoundingRectBoderPaint;
    private Paint mLaserPaint;
    private int scannerAlpha;


    public CameraHoverView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBoundingRectBoderPaint = new Paint();
        mBoundingRectBoderPaint.setColor(Color.RED);
        mBoundingRectBoderPaint.setStyle(Paint.Style.STROKE);

        mLaserPaint = new Paint();
        mLaserPaint.setColor(Color.RED);
        mLaserPaint.setStyle(Paint.Style.FILL);
    }

    public void update(Rect rect) {
        this.mBoundingScanRect = rect;
        postInvalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        //drawHoverViewBorder(canvas);
        //drawHoverViewLaser(canvas);
    }

    private void drawHoverViewLaser(Canvas canvas) {
        // Draw a red "laser scanner" line through the middle to show decoding is active
        mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = mBoundingScanRect.height() / 2 + mBoundingScanRect.top;
        canvas.drawRect(mBoundingScanRect.left + 2, middle - 1, mBoundingScanRect.right - 1, middle + 2, mLaserPaint);

        postInvalidateDelayed(ANIMATION_DELAY,
                mBoundingScanRect.left - POINT_SIZE,
                mBoundingScanRect.top - POINT_SIZE,
                mBoundingScanRect.right + POINT_SIZE,
                mBoundingScanRect.bottom + POINT_SIZE);
    }

    private void drawHoverViewBorder(Canvas canvas) {

        canvas.drawLine(mBoundingScanRect.left - 1, mBoundingScanRect.top - 1, mBoundingScanRect.left - 1, mBoundingScanRect.top - 1 + mBorderLineLength, mBoundingRectBoderPaint);
        canvas.drawLine(mBoundingScanRect.left - 1, mBoundingScanRect.top - 1, mBoundingScanRect.left - 1 + mBorderLineLength, mBoundingScanRect.top - 1, mBoundingRectBoderPaint);

        canvas.drawLine(mBoundingScanRect.left - 1, mBoundingScanRect.bottom + 1, mBoundingScanRect.left - 1, mBoundingScanRect.bottom + 1 - mBorderLineLength, mBoundingRectBoderPaint);
        canvas.drawLine(mBoundingScanRect.left - 1, mBoundingScanRect.bottom + 1, mBoundingScanRect.left - 1 + mBorderLineLength, mBoundingScanRect.bottom + 1, mBoundingRectBoderPaint);

        canvas.drawLine(mBoundingScanRect.right + 1, mBoundingScanRect.top - 1, mBoundingScanRect.right + 1, mBoundingScanRect.top - 1 + mBorderLineLength, mBoundingRectBoderPaint);
        canvas.drawLine(mBoundingScanRect.right + 1, mBoundingScanRect.top - 1, mBoundingScanRect.right + 1 - mBorderLineLength, mBoundingScanRect.top - 1, mBoundingRectBoderPaint);

        canvas.drawLine(mBoundingScanRect.right + 1, mBoundingScanRect.bottom + 1, mBoundingScanRect.right + 1, mBoundingScanRect.bottom + 1 - mBorderLineLength, mBoundingRectBoderPaint);
        canvas.drawLine(mBoundingScanRect.right + 1, mBoundingScanRect.bottom + 1, mBoundingScanRect.right + 1 - mBorderLineLength, mBoundingScanRect.bottom + 1, mBoundingRectBoderPaint);
    }
}
