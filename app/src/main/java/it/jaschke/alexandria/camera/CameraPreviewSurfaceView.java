package it.jaschke.alexandria.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import it.jaschke.alexandria.utilities.CameraUtility;
import it.jaschke.alexandria.utilities.DeviceUtility;

/**
 * Created by Oti Rowland on 1/8/2016.
 */
public class CameraPreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    // Class logging Identifier
    private final String LOG_TAG = CameraPreviewSurfaceView.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private Camera.PreviewCallback previewCallback;
    private Camera.AutoFocusCallback autoFocusCallback;
    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private Camera.Size mPictureSize;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<Camera.Size> mSupportedPictureSizes;
    private int mOrientation = 0;

    public CameraPreviewSurfaceView(Context context) {
        super(context);
        // Acquire the drawing surface
        mSurfaceView = new SurfaceView(context);
        mSurfaceView = this;
        // Acquire holder of our display surface
        mHolder = mSurfaceView.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder.addCallback(this);
        // Deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraPreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Acquire the drawing surface
        mSurfaceView = new SurfaceView(context);
        mSurfaceView = this;
        // Acquire holder of our display surface
        mHolder = mSurfaceView.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder.addCallback(this);
        // Deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            mPictureSize = getOptimalPreviewSize(mSupportedPictureSizes, width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Try to set the display surface holder on the camera
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.d(LOG_TAG, "Error setting camera preview: " + exception.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or reformatting it.
         */
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // Now that the size is known, set up the camera parameters and begin the preview.
        try {
            // Acquire camera parameters
            Camera.Parameters parameters = mCamera.getParameters();
            // Take care of events such as rotation
            fixOrientation(parameters);
            // Set some camera parameters
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPictureSize.width, mPictureSize.height);

            requestLayout();
            // Set the parameters on the camera and start the preview
            mCamera.setParameters(parameters);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
        } catch (Exception exception) {
            Log.d(LOG_TAG, "Error starting camera preview: " + exception.getMessage());
        }

        Log.d(LOG_TAG, "We were called");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    // A method to take care of the events of mOrientation changes on deveice rotation
    private void fixOrientation(Camera.Parameters parameters) {
        // Acquire a Display object
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        // We are not gona run these in the emulator
        if (!DeviceUtility.isRunningOnEmulator()) {
            // The preview is rotated on devices, so we have to straighten it.
            // We do not need to do this in an emulator
            if (display.getRotation() == Surface.ROTATION_0) {
                //parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
                mOrientation = 90;
                mCamera.setDisplayOrientation(mOrientation);
            }

            if (display.getRotation() == Surface.ROTATION_90) {
                //parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mOrientation = 0;
                mCamera.setDisplayOrientation(mOrientation);
            }

            if (display.getRotation() == Surface.ROTATION_180) {
                //parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
                mOrientation = 0;
                mCamera.setDisplayOrientation(mOrientation);
            }

            if (display.getRotation() == Surface.ROTATION_270) {
                //parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mOrientation = 180;
                mCamera.setDisplayOrientation(mOrientation);
            }
        }
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
    }

    // A method that will be used to set the first camera to be used
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
            setFlash(camera, true);
            requestLayout();
        }
    }

    public void setFlash(Camera camera, boolean flag) {
        // Check for flash in Camera
        if(CameraUtility.isFlashSupported(camera)) {
            // Acquire Camera parameters
            Camera.Parameters parameters = camera.getParameters();
            // We want flash
            if(flag) {
                // Already in torch mode
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    return;
                }
                // Set flash to torch mode
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                // Already out of flash mode
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    return;
                }
                // Set flash off
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(parameters);
        }
    }

    // A handy method that returns the most reasonable preview size
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public float getOrientation() {
        return mOrientation;
    }

    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    public Camera.Size getPictureSize() {
        return mPictureSize;
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    public void setAutoFocusCallback(Camera.AutoFocusCallback autoFocusCallback) {
        this.autoFocusCallback = autoFocusCallback;
    }


}
