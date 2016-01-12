package it.jaschke.alexandria.ui.fragments;


import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.camera.BeepManager;
import it.jaschke.alexandria.camera.CameraHandlerThread;
import it.jaschke.alexandria.camera.CameraHoverView;
import it.jaschke.alexandria.camera.CameraPreviewSurfaceView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraPreviewFragment extends Fragment {

    // Class logging Identifier
    private final String LOG_TAG = CameraPreviewFragment.class.getSimpleName();

    // ButterKnife injected views
    // The surface view containing layout
    @Bind(R.id.preview_container)
    RelativeLayout mPreviewContainer;
    // The surface view
    @Bind(R.id.camera_preview_surfaceview)
    CameraPreviewSurfaceView mPreviewSurface;
    // The hover view container
    @Bind(R.id.camera_hover_view_container)
    FrameLayout mHoverViewContainer;
    // The View of rectangular hover
    @Bind(R.id.camera_hover_view)
    CameraHoverView mHoverView;
    @Bind(R.id.scan_frame_result_image)
    ImageView mResultImage;

    // Use this for preview  of scan frame
    private Bitmap mResultBitmap;
    // Media use
    private BeepManager mBeepManager;

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
    private CameraHandlerThread mThread = null;

    // Default constructor required
    public CameraPreviewFragment() {

    }

    // Actual method to use to create new fragment instance externally
    public static CameraPreviewFragment newInstance() {
        return new CameraPreviewFragment();
    }

    // Called to do initial creation of fragment. Initialize and set up the fragment's non-view hierarchy
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Our fragments has its own menu
        setHasOptionsMenu(true);
        // Initialize the beepmanager
        mBeepManager = new BeepManager(getActivity());
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
        View rootView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        // Inflate all views
        ButterKnife.bind(this, rootView);
        // Return the view for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Open the default back facing camera.
        openCameraNew();
        mCameraCurrentlyLocked = defaultBackFacingCameraId;
        // Set the camera to use
        mPreviewSurface.setCamera(mCamera);
        if (mPreviewSurface.getParent() == null) {
            mPreviewContainer.addView(mPreviewSurface);
        }
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        // The Camera object is a shared resource, release it
        if (mCamera != null) {
            mPreviewSurface.setCamera(null);
            mCamera.release();
            mCamera = null;
            mPreviewContainer.removeView(mPreviewSurface);
        }

        if (mThread != null) {
            mThread.quit();
            mThread = null;
        }
    }


    public void openCameraOld() {
        mCamera = Camera.open();
    }

    private void openCameraNew() {
        if (mThread == null) {
            mThread = new CameraHandlerThread(this);
        }

        synchronized (mThread) {
            mThread.openCamera();
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

    public Camera.Size getCameraPictureSize() {
        return mPreviewSurface.getPictureSize();
    }

    public CameraPreviewSurfaceView getPreviewSurface() {
        return mPreviewSurface;
    }

    public Rect getFramingRectInPreview() {

        int previewWidth = getCameraPreviewSize().width;
        int previewHeight = getCameraPreviewSize().height;

        int[] location = new int[2];
        mHoverViewContainer.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1];

        int cropWidth = mHoverViewContainer.getWidth();
        int cropHeight = mHoverViewContainer.getHeight();

        int containerWidth = mPreviewContainer.getWidth();
        int containerHeight = mPreviewContainer.getHeight();

        int x = cropLeft * previewWidth / containerWidth;
        int y = cropTop * previewHeight / containerHeight;

        int width = cropWidth * previewWidth / containerWidth;
        int height = cropHeight * previewHeight / containerHeight;

        Log.d(LOG_TAG, "CROPLEFT: " + cropLeft + " CROPRIGHT: " + cropTop);
        Log.d(LOG_TAG, "WIDTH: " + width + " HEIGHT: " + height);
        Log.d(LOG_TAG, "PREVIEWWIDTH: " + previewWidth + " PREVIEWHEIGHT: " + previewHeight);
        Log.d(LOG_TAG, "CROPWIDTH: " + cropWidth + " CROPHEIGHT: " + cropHeight);
        Log.d(LOG_TAG, "CONTAINERWIDTH: " + containerWidth + " CONTAINERHEIGHT: " + containerHeight);
        Log.d(LOG_TAG, "XWIDTH: " + x + " YHEIGHT: " + y);

       return new Rect(x, y, width + x, height + y);
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
}