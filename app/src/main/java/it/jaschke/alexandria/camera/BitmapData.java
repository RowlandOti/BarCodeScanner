package it.jaschke.alexandria.camera;

import android.hardware.Camera;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class BitmapData {

    private byte[] mBytes;
    private float mOrientation;
    private Camera.Size mSize;

    // Default constructor
    public BitmapData() {
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
        return mSize;
    }

    public void setSize(Camera.Size size) {
        this.mSize = size;
    }
}
