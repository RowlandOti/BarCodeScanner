package it.jaschke.alexandria.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.lang.ref.WeakReference;

import it.jaschke.alexandria.ui.fragments.CameraPreviewFragment;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraHandlerThread extends HandlerThread implements Camera.AutoFocusCallback, Camera.PreviewCallback {

    // Class logging Identifier
    private final String LOG_TAG = CameraHandlerThread.class.getSimpleName();
    // The threading identifier
    private static String THREAD_TAG = "CameraHandlerThread";
    // The thread handler
    private Handler mHandler = null;
    // Soft reference
    private WeakReference<CameraPreviewFragment> mWeakReferenceCameraPreviewFragment = null;
    // The Barcode scanner thread
    private CameraBarcodeScanThread mCameraBarCodeScannerThread;

    // Default constructor
    public CameraHandlerThread(CameraPreviewFragment cameraPreviewFragment) {
        super(THREAD_TAG);
        // This is a call to begin the thread
        start();
        mHandler = new Handler(getLooper());
        mWeakReferenceCameraPreviewFragment = new WeakReference<>(cameraPreviewFragment);
        cameraPreviewFragment.getPreviewSurface().setPreviewCallback(this);
        cameraPreviewFragment.getPreviewSurface().setAutoFocusCallback(this);
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // Acquire the containing fragment instance
        CameraPreviewFragment cameraPreviewFragment = mWeakReferenceCameraPreviewFragment.get();
        //
        if (cameraPreviewFragment != null) {
            // We might need a scanner thread
            if (mCameraBarCodeScannerThread == null) {
                mCameraBarCodeScannerThread = new CameraBarcodeScanThread(cameraPreviewFragment);
                // Begin the scanning
                mCameraBarCodeScannerThread.initializeScan();
            }
            mCameraBarCodeScannerThread.queueCreateScanResultFromPreview(data, camera);
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
}
