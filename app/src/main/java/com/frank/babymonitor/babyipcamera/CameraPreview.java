package com.frank.babymonitor.babyipcamera;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Size previewSize;

    private LinkedList<byte[]> LinkedListQueue = new LinkedList<byte[]>();
    private static final int MAX_BUFFER = 20;

    private byte[] lastFrame = null;
    private int frameLength, format;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        Parameters params = this.camera.getParameters();
        //could use params.getSupportedPreviewSizes() to find width and height

        params.setPreviewSize(640, 480); // need handle in rotation situation onCreate in BabyIpCamera
        this.camera.setParameters(params);

        previewSize = this.camera.getParameters().getPreviewSize();
        Log.i(TAG, "preview size = " + previewSize.width + ", " + previewSize.height);

        format = this.camera.getParameters().getPreviewFormat();
        frameLength = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(format) / 8;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface Created called");
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        Log.d(TAG, "surface changed calledddd");

        if (surfaceHolder.getSurface() == null){
            return;
        }

        try {
            camera.stopPreview();
            resetBuff();

        } catch (Exception e){

        }

        try {
            camera.setPreviewCallback(mPreviewCallback);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public byte[] getImageBuffer() {
        synchronized (LinkedListQueue) {
            if (LinkedListQueue.size() > 0) {
                lastFrame = LinkedListQueue.poll();
            }
        }

        return lastFrame;
    }

    private void resetBuff() {

        synchronized (LinkedListQueue) {
            LinkedListQueue.clear();
            lastFrame = null;
        }
    }

    public int getPreviewFormat(){
        return format;
    }
    public int getPreviewLength() {
        return frameLength;
    }

    public int getPreviewWidth() {
        return previewSize.width;
    }

    public int getPreviewHeight() {
        return previewSize.height;
    }

    public void onPause() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
        }
        resetBuff();
    }

    private Camera.PreviewCallback mPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            synchronized (LinkedListQueue) {
                if (LinkedListQueue.size() == MAX_BUFFER) {
                    LinkedListQueue.poll();
                }
                LinkedListQueue.add(data);
            }
        }
    };

    private void saveYUV(byte[] byteArray) {

        YuvImage im = new YuvImage(byteArray, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        Rect r = new Rect(0, 0, previewSize.width, previewSize.height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(r, 100, baos);

        try {
            FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/yuv.jpg");
            output.write(baos.toByteArray());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
}
