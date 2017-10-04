package com.frank.babymonitor.babyipcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

/**
 * Manager the image buffer
 */

public class BufferManager extends Thread {
    private final String TAG = "BufferManager";
    private static final int MAX_BUFFER_SIZE = 2;
    private final int frameLength, format;
    private ImageProcessBuffer[] imageProcessBuffer;
    private int fillCount = 0;
    private int remained = 0;
    private int width, height;
    private LinkedList<byte[]> yuvQueue = new LinkedList<byte[]>();
    private ImageView imageView;
    private Activity activity;

    public BufferManager(int format, int frameLength, int width, int height, ImageView imageView, Activity activity) {
        this.format = format;
        this.frameLength = frameLength;
        this.width = width;
        this.height = height;
        this.imageView = imageView;
        this.activity = activity;
        imageProcessBuffer = new ImageProcessBuffer[MAX_BUFFER_SIZE];
        for (int i = 0; i < MAX_BUFFER_SIZE; ++i) {
            imageProcessBuffer[i] = new ImageProcessBuffer(this.frameLength);
        }
    }

    public void fillBuffer(byte[] data, int len) {
        fillCount = fillCount % MAX_BUFFER_SIZE;
        if (remained != 0) {
            if (remained < len) {
                imageProcessBuffer[fillCount].fillBuffer(data, 0, remained, yuvQueue);
                ++fillCount;
                if (fillCount == MAX_BUFFER_SIZE)
                    fillCount = 0;
                imageProcessBuffer[fillCount].fillBuffer(data, remained, len - remained, yuvQueue);
                remained = frameLength - len + remained;
            } else if (remained == len) {
                imageProcessBuffer[fillCount].fillBuffer(data, 0, remained, yuvQueue);
                remained = 0;
                ++fillCount;
                if (fillCount == MAX_BUFFER_SIZE)
                    fillCount = 0;
            } else {
                imageProcessBuffer[fillCount].fillBuffer(data, 0, len, yuvQueue);
                remained = remained - len;
            }
        } else {
            imageProcessBuffer[fillCount].fillBuffer(data, 0, len, yuvQueue);

            if (len < frameLength) {
                remained = frameLength - len;
            } else {
                ++fillCount;
                if (fillCount == MAX_BUFFER_SIZE)
                    fillCount = 0;
            }
        }
    }

    public void close() {
        interrupt();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        while (!Thread.currentThread().isInterrupted()) {
            byte[] data = null;
            synchronized (yuvQueue) {
                data = yuvQueue.poll();

                if (data != null) {
                    Log.d(TAG, "buffer manager data size " + data.length);
                    YuvImage yuvImage = new YuvImage(data,format,width,height,null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0,0,width,height),50,out);

                    byte[] bytes = out.toByteArray();
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        }
    }

    private class ImageProcessBuffer {
        private final String TAG = "ImageProcessBuffer";
        private int totalLength = 0;
        private final int frameLength;
        private ByteArrayOutputStream byteArrayOutputStream;

        public ImageProcessBuffer(int frameLength) {
            byteArrayOutputStream = new ByteArrayOutputStream();
            this.frameLength = frameLength;
        }

        public int fillBuffer(byte[] data, int off, int len, LinkedList<byte[]> yuvQueue) {
            totalLength += len;
            byteArrayOutputStream.write(data, off, len);

            if (totalLength == frameLength) {

                synchronized (yuvQueue) {
                    yuvQueue.add(byteArrayOutputStream.toByteArray());
                    byteArrayOutputStream.reset();
                }
                totalLength = 0;
            }

            return 0;
        }
    }
}
