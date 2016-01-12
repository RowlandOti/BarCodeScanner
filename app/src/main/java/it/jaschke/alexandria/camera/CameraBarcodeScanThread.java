package it.jaschke.alexandria.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import it.jaschke.alexandria.ui.fragments.CameraPreviewFragment;
import it.jaschke.alexandria.utilities.BitmapUtility;
import it.jaschke.alexandria.utilities.ScreenUtility;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraBarcodeScanThread extends HandlerThread {

    private static final int WHAT_CREATE_SCAN_RESULT_FROM_PREVIEW = 0;
    private static final int WHAT_CREATE_SCAN_FRAME_PREVIEW = 1;
    // The threading identifier
    private static String THREAD_TAG = "CameraBarcodeScanThread";
    // Class logging Identifier
    private final String LOG_TAG = CameraBarcodeScanThread.class.getSimpleName();
    // The thread handler
    private Handler mHandler = null;
    // Soft reference
    private WeakReference<CameraPreviewFragment> mWeakReferenceCameraPreviewFragment = null;

    // Default Constructor
    public CameraBarcodeScanThread(CameraPreviewFragment cameraPreviewFragment) {
        super(THREAD_TAG);
        // This is a call to begin the thread
        start();
        mHandler = new Handler(getLooper());
        mWeakReferenceCameraPreviewFragment = new WeakReference<>(cameraPreviewFragment);
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
                        private BitmapData mBitmapData;
                        // The outcome of the decoding
                        private Result result;

                        @Override
                        public void run() {
                            // Decode Scan
                            result = decodeWithZxing(mBitmapData.getBytes(), mBitmapData.getSize().width, mBitmapData.getSize().height);
                            if (result != null) {
                                Log.d(LOG_TAG, "CREATED_SCAN_RESULT" + result.getText());
                            }
                        }

                        public Runnable init(BitmapData bitmapData) {
                            this.mBitmapData = bitmapData;
                            return this;
                        }
                    }.init((BitmapData) msg.obj));
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

    public Result decodeWithZxing(byte[] data, int width, int height) {

        Rect crop = mWeakReferenceCameraPreviewFragment.get().getFramingRectInPreview();
        // The MultiFormatReader used to decode
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(createDecodeHints());

        Result result = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height,
                crop.left, crop.top, crop.width(), crop.height(), false);

        if (source != null) {
            // Queue the scan preview
            queueCreateScanFramePreview(source);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = multiFormatReader.decodeWithState(binaryBitmap);
                Log.d(LOG_TAG, "READER TRIED");
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }
        return result != null ? result : null;
    }

    public Result decodeWithZxing(Bitmap bitmap) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(createDecodeHints());

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        Result result = null;
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        if (source != null) {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = multiFormatReader.decodeWithState(binaryBitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        return result != null ? result : null;
    }

    public void queueCreateScanResultFromPreview(byte[] data, Camera camera) {
        // The POJO for bitmap data
        BitmapData bitmapData = new BitmapData();
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
        Bitmap rotated = null;

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

    private Map<DecodeHintType, Object> createDecodeHints() {
        // Map object for hints
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>();
        // Use both BarCodes and QRCodes
        decodeFormats.addAll(ScanFormatManager.getBarCodeFormats());
        decodeFormats.addAll(ScanFormatManager.getQrCodeFormats());
        // Map above to all possible formarts allowed
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        return hints;
    }
}
