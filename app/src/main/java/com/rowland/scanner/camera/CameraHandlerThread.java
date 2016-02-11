package com.rowland.scanner.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.rowland.scanner.ui.fragments.ScanFragment;

import java.lang.ref.WeakReference;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraHandlerThread extends HandlerThread implements Camera.AutoFocusCallback, Camera.PreviewCallback {

    // The threading identifier
    private static String THREAD_TAG = "CameraHandlerThread";
    // Class logging Identifier
    private final String LOG_TAG = CameraHandlerThread.class.getSimpleName();
    // The thread handler
    private Handler mHandler = null;
    // Soft reference
    private WeakReference<ScanFragment> mWeakReferenceCameraPreviewFragment = null;
    // The Barcode scanner thread
    private CameraBarcodeScanThread mCameraBarCodeScannerThread = null;

    // Default constructor
    public CameraHandlerThread(ScanFragment scanFragment) {
        super(THREAD_TAG);
        // This is a call to begin the thread
        start();
        mHandler = new Handler(getLooper());
        mWeakReferenceCameraPreviewFragment = new WeakReference<>(scanFragment);
        mWeakReferenceCameraPreviewFragment.get().getPreviewSurface().setPreviewCallback(this);
        mWeakReferenceCameraPreviewFragment.get().getPreviewSurface().setAutoFocusCallback(this);
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //
        if (mWeakReferenceCameraPreviewFragment.get() != null) {
            // We might need a scanner thread
            if (mCameraBarCodeScannerThread == null) {
                mCameraBarCodeScannerThread = new CameraBarcodeScanThread(mWeakReferenceCameraPreviewFragment.get());
                // Begin the scanning
                mCameraBarCodeScannerThread.initializeScan();
            }
            mCameraBarCodeScannerThread.queueCreateScanResult(data, camera);
        }

        Log.d(LOG_TAG, "onPreviewFrame Called");
    }

    synchronized void notifyCameraOpened() {
        notify();
    }

    public void openCamera() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mWeakReferenceCameraPreviewFragment.get() != null) {
                    mWeakReferenceCameraPreviewFragment.get().openCameraOld();
                    notifyCameraOpened();
                }
            }
        });
        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(LOG_TAG, "wait was interrupted");
        }
    }

    @Override
    public boolean quitSafely() {
        if (mCameraBarCodeScannerThread != null) {
            mCameraBarCodeScannerThread.quitSafely();
            mCameraBarCodeScannerThread = null;
        }
        return super.quitSafely();
    }
}
