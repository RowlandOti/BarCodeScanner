package it.jaschke.alexandria.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraHoverView extends View {

    // Class logging Identifier
    private final String LOG_TAG = CameraHoverView.class.getSimpleName();

    private Paint mPaint;
    private Rect mRect;

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
     * @param rect   The Rectangular coordinate of the HoverView
     */
    public void update(Rect rect) {
        mRect = rect;
        invalidate();
    }

    public Rect getHoverRect() {
        return mRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        //canvas.drawRect(mRect, mPaint);
    }
}
