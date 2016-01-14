package it.jaschke.alexandria.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.zxing.PlanarYUVLuminanceSource;

import java.lang.ref.WeakReference;

import it.jaschke.alexandria.decoders.InterfaceDecoder;
import it.jaschke.alexandria.decoders.ZBarDecoder;
import it.jaschke.alexandria.decoders.ZXingDecoder;
import it.jaschke.alexandria.ui.fragments.CameraPreviewFragment;
import it.jaschke.alexandria.utilities.BitmapUtility;
import it.jaschke.alexandria.utilities.ScreenUtility;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraBarcodeScanThread extends HandlerThread {

    // Class logging Identifier
    private final String LOG_TAG = CameraBarcodeScanThread.class.getSimpleName();

    private static final int WHAT_CREATE_SCAN_RESULT_FROM_PREVIEW = 0;
    private static final int WHAT_CREATE_SCAN_FRAME_PREVIEW = 1;
    // The threading identifier
    private static String THREAD_TAG = "CameraBarcodeScanThread";
    // Soft reference
    private WeakReference<CameraPreviewFragment> mWeakReferenceCameraPreviewFragment = null;
    // Our decoders
    private InterfaceDecoder mZXingDecoder;
    private InterfaceDecoder mZBarDecoder;
    // The thread handler
    private Handler mHandler = null;

    // Default Constructor
    public CameraBarcodeScanThread(CameraPreviewFragment cameraPreviewFragment) {
        super(THREAD_TAG);
        // This is a call to begin the thread
        start();
        mWeakReferenceCameraPreviewFragment = new WeakReference<>(cameraPreviewFragment);
        mHandler = new Handler(getLooper());
    }

    public void initializeScan() {
        // Where all the magic of Barcode decoding happens
        mHandler = new Handler(getLooper(), new Handler.Callback() {
            //
            @Override
            public boolean handleMessage(Message msg) {

                if (msg.what == WHAT_CREATE_SCAN_RESULT_FROM_PREVIEW) {
                    // Add to ThreadPool
                    ScanThreadPool.post(new Runnable() {
                        // POJO for preview data
                        private CameraPreviewData mBitmapData;
                        // The outcome of the decoding
                        private String zxingResult;
                        private String zbarResult;

                        @Override
                        public void run() {
                            mZBarDecoder = new ZBarDecoder(mBitmapData);
                            mZXingDecoder = new ZXingDecoder(mBitmapData, new ZXingDecoder.InterfaceCommand() {
                                @Override
                                public void execute(PlanarYUVLuminanceSource source) {
                                    queueCreateScanFramePreview(source);
                                }
                            });
                            // Decode Scan
                            zbarResult = mZBarDecoder.decode();
                            zxingResult = mZXingDecoder.decode();
                            if (zxingResult != null) {
                                Log.d(LOG_TAG, "CREATED_SCAN_RESULT_FROM_ZXING" + zxingResult);
                            }

                            if (zbarResult != null) {
                                Log.d(LOG_TAG, "CREATED_SCAN_RESULT_FROM_ZBAR" + zbarResult);
                            }
                        }

                        public Runnable init(CameraPreviewData bitmapData) {
                            this.mBitmapData = bitmapData;
                            return this;
                        }
                    }.init((CameraPreviewData) msg.obj));
                } else if (msg.what == WHAT_CREATE_SCAN_FRAME_PREVIEW) {
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

    public void queueCreateScanResultFromPreview(byte[] data, Camera camera) {
        // The POJO for bitmap data
        CameraPreviewData bitmapData = new CameraPreviewData();
        // Set the bitmap bytes array
        bitmapData.setBytes(data);
        // Acquire a CameraPreviewFragment reference
        CameraPreviewFragment cameraPreviewFragment = mWeakReferenceCameraPreviewFragment.get();
        // Simple null check
        if (cameraPreviewFragment != null) {
            // set default mOrientation, assuming back facing camera
            bitmapData.setOrientation(cameraPreviewFragment.getOrientation());
            //Set the preview size used
            bitmapData.setSize(cameraPreviewFragment.getCameraPreviewSize());
            // Set the rect scan area for cropping
            bitmapData.setCropRectF(cameraPreviewFragment.getBoundingFrameRect());
            // Use Camera info to determine active camera
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            // Use Camera info to determine active camera
            camera.getCameraInfo(cameraPreviewFragment.getCameraCurrentlyLocked(), camInfo);
            // Determine Camera in use
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                bitmapData.setOrientation(cameraPreviewFragment.getOrientation() * -1);
            }
        }
        mHandler.obtainMessage(WHAT_CREATE_SCAN_RESULT_FROM_PREVIEW, bitmapData).sendToTarget();

        Log.d(LOG_TAG, "Added create scan results to queue");
    }

    public void queueCreateScanFramePreview(PlanarYUVLuminanceSource source) {

        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        Bitmap rotated;

        // In portrait we have to rotate by 90 degrees for Bitmap to get match preview orientation
        if (ScreenUtility.isInPortraitOrientation(mWeakReferenceCameraPreviewFragment.get().getActivity())) {
            // Rotate it
            rotated = BitmapUtility.rotateBitmap(bitmap, 90);
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

        mHandler.obtainMessage(WHAT_CREATE_SCAN_FRAME_PREVIEW, rotated).sendToTarget();

        Log.d(LOG_TAG, "Added create scan frame preview to queue");
    }

    @Override
    public boolean quit() {
        BitmapThreadPool.finish();
        ScanThreadPool.finish();
        return super.quit();
    }

    @Override
    public boolean quitSafely() {
        BitmapThreadPool.finish();
        ScanThreadPool.finish();
        return super.quitSafely();
    }
}
