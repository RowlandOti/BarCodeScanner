package com.rowland.scanner.camera;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.rowland.scanner.decoders.InterfaceDecoder;
import com.rowland.scanner.decoders.ZBarDecoder;
import com.rowland.scanner.ui.activities.ScanActivity;
import com.rowland.scanner.ui.fragments.ScanFragment;
import com.rowland.scanner.utilities.BitmapUtility;
import com.rowland.scanner.utilities.ScreenUtility;

import java.lang.ref.WeakReference;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraBarcodeScanThread extends HandlerThread {

    private static final int WHAT_CREATE_SCAN_RESULT = 0;
    private static final int WHAT_CREATE_SCAN_PREVIEW = 1;
    // The threading identifier
    private static String THREAD_TAG = "CameraBarcodeScanThread";
    // Class logging Identifier
    private final String LOG_TAG = CameraBarcodeScanThread.class.getSimpleName();
    // Soft reference
    private WeakReference<ScanFragment> mWeakReferenceCameraPreviewFragment = null;
    // Our decoders
    private InterfaceDecoder mZBarDecoder = null;
    // The thread handler
    private Handler mHandler = null;

    // Default Constructor
    public CameraBarcodeScanThread(ScanFragment scanFragment) {
        super(THREAD_TAG);
        // This is a call to begin the thread
        start();
        mWeakReferenceCameraPreviewFragment = new WeakReference<>(scanFragment);
        mHandler = new Handler(getLooper());
    }

    public void initializeScan() {
        // Where all the magic of Barcode decoding happens
        mHandler = new Handler(getLooper(), new Handler.Callback() {
            //
            @Override
            public boolean handleMessage(Message msg) {

                if (msg.what == WHAT_CREATE_SCAN_RESULT) {
                    // Add to ThreadPool
                    ScanThreadPool.post(new Runnable() {
                        // POJO for preview data
                        private CameraPreviewData mBitmapData;
                        // The outcomes of the decoding
                        private String zxingResult;
                        private String zbarResult;

                        @Override
                        public void run() {

                            mZBarDecoder = new ZBarDecoder(mBitmapData, new ZBarDecoder.InterfaceCommand() {
                                @Override
                                public void execute() {
                                    queueCreateScanPreview(mBitmapData);
                                }
                            });
                            // Decode using ZBar
                            zbarResult = mZBarDecoder.decode();
                            if (zbarResult != null) {
                                // Check which instance we are dealing with
                                if (mWeakReferenceCameraPreviewFragment.get().getActivity() instanceof ScanActivity) {
                                    // Trigger callback
                                    ((ScanActivity) mWeakReferenceCameraPreviewFragment.get().getActivity()).onScanComplete(zbarResult);
                                }

                                Log.d(LOG_TAG, "CREATED_SCAN_RESULT_FROM_ZBAR: " + zbarResult);
                            }
                        }

                        public Runnable init(CameraPreviewData bitmapData) {
                            this.mBitmapData = bitmapData;
                            return this;
                        }
                    }.init((CameraPreviewData) msg.obj));
                } else if (msg.what == WHAT_CREATE_SCAN_PREVIEW) {
                    // Add Runnable to ThreadPool
                    BitmapThreadPool.post(new Runnable() {
                        // Bitmap object
                        private Bitmap mBitmap;

                        @Override
                        public void run() {
                            mWeakReferenceCameraPreviewFragment.get().setResultImageBitmap(mBitmap);
                        }

                        public Runnable init(Bitmap bitmap) {
                            this.mBitmap = bitmap;
                            return this;
                        }

                    }.init((Bitmap) msg.obj));
                }
                return true;
            }
        });
    }

    public void queueCreateScanResult(byte[] data, Camera camera) {
        // The POJO for bitmap data
        CameraPreviewData bitmapData = new CameraPreviewData();
        // Set the bitmap bytes array
        bitmapData.setBytes(data);
        // Acquire a ScanFragment reference
        ScanFragment scanFragment = mWeakReferenceCameraPreviewFragment.get();
        // Simple null check
        if (scanFragment != null) {
            // set default mOrientation, assuming back facing camera
            bitmapData.setOrientation(scanFragment.getOrientation());
            //Set the preview size used
            bitmapData.setSize(scanFragment.getCameraPreviewSize());
            // Set the rect scan area for cropping
            bitmapData.setBoundingRectF(scanFragment.getBoundingFrameRect());
            // Use Camera info to determine active camera
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            // Use Camera info to determine active camera
            camera.getCameraInfo(scanFragment.getCameraCurrentlyLocked(), camInfo);
            // Determine Camera in use
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                bitmapData.setOrientation(scanFragment.getOrientation() * -1);
            }
        }
        mHandler.obtainMessage(WHAT_CREATE_SCAN_RESULT, bitmapData).sendToTarget();

        Log.d(LOG_TAG, "Added create scan results to queue");
    }

    public void queueCreateScanPreview(CameraPreviewData cameraPreviewData) {

        byte[] nCroppedBytes = cameraPreviewData.getCroppedBytes(cameraPreviewData.getBoundingRectF());
        RectF rectF = cameraPreviewData.getBoundingRectF();

        int reqWidth = cameraPreviewData.getBoundingRect(rectF).width() / 2;
        int reqHeight = cameraPreviewData.getBoundingRect(rectF).height() / 2;

        Bitmap bitmap = BitmapUtility.createBitmap(nCroppedBytes, reqWidth, reqHeight);

        Bitmap rotated;

        // In portrait we have to rotate by 90 degrees for Bitmap to get match preview orientation
        if (ScreenUtility.isInPortraitOrientation(mWeakReferenceCameraPreviewFragment.get().getActivity())) {
            // Rotate it
            rotated = BitmapUtility.rotateBitmap(bitmap, 90);
            // Check identity
            if (bitmap != rotated) {
                //Rotation successful - Recycle if they aren't referencing the same Bitmap object.
                bitmap.recycle();
            }
        }
        // Most Cameras are in orientation, no need of rotation
        else {
            // Same old bitmap
            rotated = bitmap;
        }

        mHandler.obtainMessage(WHAT_CREATE_SCAN_PREVIEW, rotated).sendToTarget();

        Log.d(LOG_TAG, "Added create scan frame preview to queue");
    }

    @Override
    public boolean quitSafely() {
        //BitmapThreadPool.finish();
        //ScanThreadPool.finish();

        if (mZBarDecoder != null) {
            mZBarDecoder = null;
        }
        return super.quitSafely();
    }
}
