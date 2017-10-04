package com.frank.babymonitor.babyipcamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Camera Manager to manage cameras
 */

public class CameraManager {

    private Camera camera;
    private Context context;


    public CameraManager(Context context){
        this.context = context;
        camera = getCameraInstance();
    }

    public Camera getCamera(){
        return camera;
    }

    public void onPause(){
        releaseCamera();
    }

    public void onResume(){
        if(camera == null)
            camera = getCameraInstance();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * Make sure only get an instance of the Camera object
     * @return an camera object
     */
    private static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
            if(camera == null){
                camera = openFrontFacingCamera();
                Log.d(TAG, "front camera opened");
            }
        }
        catch (Exception e){
        }
        return camera;
    }

    private static Camera openFrontFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

}
