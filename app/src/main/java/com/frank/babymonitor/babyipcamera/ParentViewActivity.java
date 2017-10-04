package com.frank.babymonitor.babyipcamera;

import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.frank.babymonitor.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Received image from parent device and display it
 */

public class ParentViewActivity extends AppCompatActivity{

    private final String TAG = "ParentViewActivity";
    private String IP,name;
    private int port;
    private ImageView imageView;
    private Thread serviceThread;
    private final String JASON_DATA_KEY = "parent", JASON_DATA_VALUE = "ok", JASON_FORMAT_KEY= "type", JASON_FORMAT_VALUE="data";
    private BufferManager bufferManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_view_activity);
        Log.d(TAG, "oncreate");

        final Bundle bundle = getIntent().getExtras();
        IP = bundle.getString("ip");
        port = bundle.getInt("port");
        name = bundle.getString("name");

        Log.d(TAG, "ip is " + IP + " port is " + port + " name is " + name);

        imageView = (ImageView) findViewById(R.id.parent_image_view);

        serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "client is running");
                    try {
                        Socket socket = new Socket(IP,port);
                        streamImage(socket);
                    } catch (IOException e) {
                        if(serviceThread != null)
                            serviceThread.interrupt();
                        e.printStackTrace();
                    }
                }

                //disconnect and stop sending image
            }
        });

        serviceThread.start();
    }

    private void streamImage(Socket socket){

        try {

            byte[] buff = new byte[256];
            byte[] imageBuff = null;
            int len = 0;
            String msg = null;

            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());

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
                    Log.d(TAG, "test: ", e);
                    isJSON = false;
                }
                if (isJSON && element != null) {
                    JsonObject obj = element.getAsJsonObject();
                    element = obj.get(JASON_FORMAT_KEY);
                    if (element != null && element.getAsString().equals(JASON_FORMAT_VALUE)) {
                        element = obj.get("format");
                        int format = element.getAsInt();
                        element = obj.get("length");
                        int length = element.getAsInt();
                        element = obj.get("width");
                        int width = element.getAsInt();
                        element = obj.get("height");
                        int height = element.getAsInt();

                        imageBuff = new byte[length];
                        bufferManager = new BufferManager(format,length,width,height,imageView,ParentViewActivity.this);
                        bufferManager.start();
                        //Log.d(TAG, "width is " + width + " height is " + height + " imagebuff " + imageBuff.length);
                        break;
                    }
                }
            }

            if(imageBuff != null) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty(JASON_DATA_KEY, JASON_DATA_VALUE);
                outputStream.write(jsonObj.toString().getBytes());
                outputStream.flush();

                // read image data
                while ((len = inputStream.read(imageBuff)) != -1) {
                    if(bufferManager != null)
                        bufferManager.fillBuffer(imageBuff,len);
                    Log.d(TAG, "Process image data" + imageBuff.length);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(serviceThread != null){
            serviceThread.interrupt();
            if(bufferManager != null)
                bufferManager.close();
            serviceThread = null;
            bufferManager = null;
        }
    }
}
