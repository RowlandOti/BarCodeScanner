package it.jaschke.alexandria.camera;

import android.graphics.RectF;
import android.hardware.Camera;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraPreviewData {

    private byte[] mBytes;
    private float mOrientation;
    private Camera.Size mPreviewSize;
    private RectF mCropRectF;

    // Default constructor
    public CameraPreviewData() {
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

    public RectF getCropRectF() {
        return mCropRectF;
    }

    public void setCropRectF(RectF cropRectF) {
        this.mCropRectF = cropRectF;
    }
}
