package com.rowland.scanner.camera;

import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraPreviewData {

    private byte[] mBytes;
    private float mOrientation;
    private Camera.Size mPreviewSize;
    private RectF mBoundingRectF;

    // Default constructor
    public CameraPreviewData() {
    }

    public byte[] getCroppedBytes(RectF boundingRectF) {
        // Convert to a compatible type
        Rect rect = getBoundingRect(boundingRectF);
        // Get a local copy
        byte[] nBytes = mBytes;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        YuvImage yuvImage = new YuvImage(nBytes, ImageFormat.NV21, mPreviewSize.width, mPreviewSize.height, null);
        yuvImage.compressToJpeg(rect, 100, outputStream);

        // Finally, create the cropped data
        byte[] croppedBytes = outputStream.toByteArray();

        return croppedBytes;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public void setBytes(byte[] data) {
        this.mBytes = data;
    }

    public float getOrientation() {
        return mOrientation;
    }

    public void setOrientation(float mOrientation) {
        this.mOrientation = mOrientation;
    }

    public Camera.Size getSize() {
        return mPreviewSize;
    }

    public void setSize(Camera.Size size) {
        this.mPreviewSize = size;
    }

    public RectF getBoundingRectF() {
        return mBoundingRectF;
    }

    public void setBoundingRectF(RectF cropRectF) {
        this.mBoundingRectF = cropRectF;
    }

    public Rect getBoundingRect(RectF boundingRectF) {
        Rect cropRect = new Rect();
        boundingRectF.roundOut(cropRect);
        return cropRect;
    }

    public Rect getRotatedBoundingRect(RectF boundingRectF) {
        // Matrix object for the transformation
        Matrix m = new Matrix();
        // Remember we rotated  the bitmap, so should the bounding box
        m.setRotate(90, boundingRectF.centerX(), boundingRectF.centerY());
        m.mapRect(boundingRectF);
        // Convert to Rect
        Rect rect = getBoundingRect(boundingRectF);

        return rect;
    }
}
