package com.frank.babymonitor.babyipcamera;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.frank.babymonitor.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Baby Device Activity
 * Monitor the baby behavior and send the image to parent device
 */

public class BabyDeviceActivity extends AppCompatActivity {

    private final String TAG = "BabyDeviceActivity";
    private CameraPreview cameraPreview;
    private CameraManager cameraManager;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;
    private Thread serviceThread;
    private NsdServiceInfo serviceInfo;
    private final String SERVICE_TYPE="_monitor._tcp.";
    private final String SERVICE_NAME = "BabyMon";
    private final String JASON_DATA_KEY = "parent", JASON_DATA_VALUE = "ok", JASON_FORMAT_KEY= "type", JASON_FORMAT_VALUE="data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.baby_ip_camera_view_activity);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    ServerSocket serverSocket = null;
                    Socket socket = null;

                    try {
                        serverSocket = new ServerSocket(0);

                        int port = serverSocket.getLocalPort();

                        registerService(port);

                        //waiting for a parent connecting
                        Log.d(TAG, "waiting for parents");
                        socket = serverSocket.accept();
                        Log.d(TAG, "found parents");

                        serverSocket.close();
                        serverSocket = null;
                        unregisterService();

                        serviceConnection(socket);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(socket != null)
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        if(serverSocket != null){
                            try {
                                serverSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        socket = null;
                        serverSocket = null;
                    }
                }
            }
        });

        cameraManager = new CameraManager(this);
        cameraPreview = new CameraPreview(this, cameraManager.getCamera());
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);

        serviceThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager.onResume();
        cameraPreview.setCamera(cameraManager.getCamera());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause is called");
        if(serviceThread != null){
            serviceThread.interrupt();
            serviceThread = null;
        }
        cameraPreview.onPause();
        cameraManager.onPause();
        unregisterService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void registerService(final int port){
        serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onRegistrationFailed");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onUnregistrationFailed");

            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceRegistered");

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceUnregistered");

            }
        };

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    private void serviceConnection(Socket socket){
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());

            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty(JASON_FORMAT_KEY, JASON_FORMAT_VALUE);
            jsonObj.addProperty("format", cameraPreview.getPreviewFormat());
            jsonObj.addProperty("length", cameraPreview.getPreviewLength());
            jsonObj.addProperty("width", cameraPreview.getPreviewWidth());
            jsonObj.addProperty("height", cameraPreview.getPreviewHeight());

            byte[] buff = new byte[256];
            int len = 0;
            String msg = null;
            outputStream.write(jsonObj.toString().getBytes());
            outputStream.flush();

            while ((len = inputStream.read(buff)) != -1) {
                msg = new String(buff, 0, len);

                // JSON analysis
                JsonParser parser = new JsonParser();
                boolean isJSON = true;
                JsonElement element = null;
                try {
                    element =  parser.parse(msg);
                }
                catch (JsonParseException e) {
                    Log.e(TAG, "exception: " + e);
                    isJSON = false;
                }
                if (isJSON && element != null) {
                    JsonObject obj = element.getAsJsonObject();
                    element = obj.get(JASON_DATA_KEY);
                    if (element != null && element.getAsString().equals(JASON_DATA_VALUE)) {
                        // send data
                        while (true) {
                            byte[] imagebuffer = cameraPreview.getImageBuffer();
                            //outputStream.write(cameraPreview.getImageBuffer());
                            outputStream.write(imagebuffer);
                            outputStream.flush();

                            if (Thread.currentThread().isInterrupted())
                                break;
                        }

                        break;
                    }
                }
                else {
                    break;
                }
            }

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
//			e.printStackTrace();
            Log.e(TAG, "test " , e);
        }
        finally {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "test 2 ", e);
            }
        }
    }

    private void unregisterService(){
        if(registrationListener != null)
        {
            nsdManager.unregisterService(registrationListener);
            registrationListener = null;
        }
    }
}
