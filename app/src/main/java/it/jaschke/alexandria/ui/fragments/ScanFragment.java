package it.jaschke.alexandria.ui.fragments;


import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.camera.CameraHandlerThread;
import it.jaschke.alexandria.camera.CameraHoverView;
import it.jaschke.alexandria.camera.CameraPreviewSurfaceView;

/**
 * A scan fragment with camera preview
 */
public class ScanFragment extends Fragment {

    // Class logging Identifier
    private final String LOG_TAG = ScanFragment.class.getSimpleName();

    // ButterKnife injected views
    // The surface view containing layout
    @Bind(R.id.preview_container)
    RelativeLayout mPreviewContainer;
    // The surface view
    @Bind(R.id.camera_preview_surfaceview)
    CameraPreviewSurfaceView mPreviewSurface;
    // The hover view container
    @Bind(R.id.camera_hover_view_container)
    CameraHoverView mHoverViewContainer;
    // The ImageView of scan area
    @Bind(R.id.scan_frame_result_image)
    ImageView mResultImage;

    // Use this for preview  of scan frame
    private Bitmap mResultBitmap;
    // A Camera object
    private Camera mCamera;
    // Total hardware cameras
    private int mNumberOfCameras;
    // The Camera in use
    private int mCameraCurrentlyLocked;
    // The first back facing camera
    private int defaultBackFacingCameraId;
    // The first front facing camera
    private int defaultFrontFacingCameraId;
    // Useful handler
    private CameraHandlerThread mCameraHandlerThread = null;

    // Default constructor required
    public ScanFragment() {

    }

    // Actual method to use to create new fragment instance externally
    public static ScanFragment newInstance(@Nullable Bundle args) {
        // Create the new fragment instance
        ScanFragment fragmentInstance = new ScanFragment();
        // Set arguments if it is not null
        if (args != null) {
            fragmentInstance.setArguments(args);
        }
        // Return the new fragment
        return fragmentInstance;
    }

    // Called to do initial creation of fragment. Initialize and set up the fragment's non-view hierarchy
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Our fragments has its own menu
        setHasOptionsMenu(true);
        // Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        // As we iterate, just pick the first result of each type
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            // Acquire the back facing camera
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultBackFacingCameraId = i;
            }
            // Acquire the front facing camera
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                defaultFrontFacingCameraId = i;
            }
        }

    }

    // Called to instantiate the fragment's view hierarchy
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        // Inflate all views
        ButterKnife.bind(this, rootView);
        // Return the view for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ToDo: In future include feature for switching cameras
        mCameraCurrentlyLocked = defaultBackFacingCameraId;
        // Open the default back facing camera.
        openCameraNew();
        // Set the camera to use
        mPreviewSurface.setCamera(mCamera);
        /*if (mPreviewSurface.getParent() == null) {
            mPreviewContainer.addView(mPreviewSurface);
        }*/
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        // The Camera object is a shared resource, release it
        if (mCamera != null) {
            mPreviewSurface.setCamera(null);
            mCamera.autoFocus(null);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            //mPreviewContainer.removeView(mPreviewSurface);
        }

        if (mCameraHandlerThread != null) {
            mCameraHandlerThread.quitSafely();
            mCameraHandlerThread = null;
        }

        if (mResultBitmap != null && !mResultBitmap.isRecycled()) {
            mResultBitmap.recycle();
            mResultBitmap = null;
        }
    }

    // Called to destroy this fragment
    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    public void openCameraOld() {
        mCamera = Camera.open();
    }

    private void openCameraNew() {
        if (mCameraHandlerThread == null) {
            mCameraHandlerThread = new CameraHandlerThread(this);
        }

        synchronized (mCameraHandlerThread) {
            mCameraHandlerThread.openCamera();
        }
    }

    public float getOrientation() {
        return mPreviewSurface.getOrientation();
    }

    public int getCameraCurrentlyLocked() {
        return mCameraCurrentlyLocked;
    }

    public Camera.Size getCameraPreviewSize() {
        return mPreviewSurface.getPreviewSize();
    }

    public CameraPreviewSurfaceView getPreviewSurface() {
        return mPreviewSurface;
    }

    public RectF getBoundingFrameRect() {

        int previewWidth = getCameraPreviewSize().width;
        int previewHeight = getCameraPreviewSize().height;

        int[] location = new int[2];
        mHoverViewContainer.getLocationInWindow(location);

        int cropTopLeftX = location[0];
        int cropTopLeftY = location[1];

        int cropWidth = mHoverViewContainer.getWidth();
        int cropHeight = mHoverViewContainer.getHeight();

        int containerWidth = mPreviewContainer.getWidth();
        int containerHeight = mPreviewContainer.getHeight();

        // Ratio for default camera state - Landscape mode
        int ratioHeight = previewHeight / containerHeight;
        int ratioWidth = previewWidth / containerWidth;

        // Take care of Portrait mode
        if (previewWidth > previewHeight) {
            // Ratio changes for Portrait mode
            ratioHeight = previewHeight / containerWidth;
            ratioWidth = previewWidth / containerHeight;
        }

        int scaledHeight = cropHeight * ratioHeight;
        int scaledWidth = cropWidth * ratioWidth;

        int scaledTopLeftX = cropTopLeftX * ratioWidth;
        int scaledTopLeftY = cropTopLeftY * ratioHeight;

        int scaledBottomRightX = scaledWidth + scaledTopLeftX;
        int scaledBottomRightY = scaledHeight + scaledTopLeftY;

        Log.d(LOG_TAG, "CROPTOPLEFTX: " + cropTopLeftX + " CROTOPLEFTY: " + cropTopLeftY);
        Log.d(LOG_TAG, "WIDTH: " + scaledWidth + " HEIGHT: " + scaledHeight);
        Log.d(LOG_TAG, "PREVIEWWIDTH: " + previewWidth + " PREVIEWHEIGHT: " + previewHeight);
        Log.d(LOG_TAG, "CROPWIDTH: " + cropWidth + " CROPHEIGHT: " + cropHeight);
        Log.d(LOG_TAG, "CONTAINERWIDTH: " + containerWidth + " CONTAINERHEIGHT: " + containerHeight);
        Log.d(LOG_TAG, "XWIDTH: " + scaledTopLeftX + " YHEIGHT: " + scaledTopLeftY);

        RectF rect = new RectF(scaledTopLeftX, scaledTopLeftY, scaledBottomRightX, scaledBottomRightY);

        return rect;
    }

    public void setResultImageBitmap(final Bitmap resultBitmap) {
        // Avoid thread interference error
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mResultBitmap = resultBitmap;
                mResultImage.setImageBitmap(resultBitmap);
            }
        });
    }

    // A callback interface that all containing activities implement
    public interface InterfaceScanCompleteCallBack {
        // Call this when movie is selected.
        void onScanComplete(String scanResult);
    }
}
