package it.jaschke.alexandria.managers;

import android.hardware.Camera;

import it.jaschke.alexandria.utilities.CameraUtility;

/**
 * Created by Oti Rowland on 1/12/2016.
 */
public class CameraManager {

    // Class logging Identifier
    private final String LOG_TAG = CameraManager.class.getSimpleName();

    private Camera mCamera;

    public CameraManager(Camera mCamera) {
        this.mCamera = mCamera;
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

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }
}
