package com.frank.babymonitor.babyipcamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.frank.babymonitor.R;

/**
 * Baby IP Camera
 * Select the device as parent or baby device
 */

public class BabyIpCameraActivity extends AppCompatActivity {

    private final String TAG = "BabyIpCameraActivity";
    private Button babyButton, parentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baby_ip_camera_activity);

        babyButton = (Button) findViewById(R.id.use_as_baby_device);
        parentButton = (Button) findViewById(R.id.use_as_parent_device);

        babyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),BabyDeviceActivity.class);
                startActivity(intent);
            }
        });

        parentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ParentDeviceActivity.class);
                startActivity(intent);
            }
        });
    }
}