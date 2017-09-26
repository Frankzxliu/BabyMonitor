package com.frank.babymonitor.babyalarm;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.babymonitor.R;

import org.w3c.dom.Text;

/**
 * Baby Alarm Activity
 */
public class BabyAlarmActivity extends AppCompatActivity {

    private final String TAG = "BabyAlarmActivity";
    private EditText phoneNumEditText;
    private RadioButton callButton, textButton;
    private SeekBar sensitivityBar;
    private Button testButton, monitorButton;
    private TextView sensitivityLevelTextView, displayMonitorTextView;
    private final String PHONE_NUMBER = "phoneNumber",CALL = "call",TEXT = "text",SENSITIVITY_LEVEL="sensitivity_level",SENSITIVITY_PRO="sensitivity_pro",DISPALY_MONITOR="display_monitor";
    public static Handler babyAlarmHandler = null;
    private static final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baby_alarm_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        displayMonitorTextView = (TextView) findViewById(R.id.monitoring_display);
        phoneNumEditText = (EditText) findViewById(R.id.phone_number);
        callButton = (RadioButton) findViewById(R.id.call);
        textButton = (RadioButton) findViewById(R.id.textMessage);
        sensitivityLevelTextView = (TextView) findViewById(R.id.sensitivity_level);
        sensitivityBar = (SeekBar) findViewById(R.id.sensitivity_seekbar);
        testButton = (Button) findViewById(R.id.sensitivity_test);
        monitorButton = (Button) findViewById(R.id.start_monitor);

        babyAlarmHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        enableDisableUI(true);
                        displayMonitorTextView.setText(R.string.not_monitor);
                        if(callButton.isChecked()) {
                            if (isPermissionGranted())
                                makeCall();
                        }
                        else if(textButton.isChecked()){
                            sendSMS(phoneNumEditText.getText().toString(),getString(R.string.baby_up));
                        }
                        Toast.makeText(BabyAlarmActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;

                    default:
                        break;
                }
            }
        };

        sensitivityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivityLevelTextView.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "test is clicked");
                displayMonitorTextView.setText(R.string.monitoring);
                Intent intent = new Intent(getBaseContext(), MonitorService.class);
                intent.putExtra("sensitivity",Double.parseDouble(sensitivityLevelTextView.getText().toString()));
                intent.putExtra("number",phoneNumEditText.getText().toString());
                startService(intent);
            }
        });

        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "monitor is clicked");
                displayMonitorTextView.setText(R.string.monitoring);
                Intent intent = new Intent(getBaseContext(), MonitorService.class);
                intent.putExtra("sensitivity",Double.parseDouble(sensitivityLevelTextView.getText().toString()));
                intent.putExtra("number",phoneNumEditText.getText().toString());
                startService(intent);
            }
        });

        if (savedInstanceState != null){
            //get saved instance
            phoneNumEditText.setText(savedInstanceState.getString(PHONE_NUMBER));
            callButton.setChecked(savedInstanceState.getBoolean(CALL));
            textButton.setChecked(savedInstanceState.getBoolean(TEXT));
            sensitivityLevelTextView.setText(savedInstanceState.getString(SENSITIVITY_LEVEL));
            sensitivityBar.setProgress(savedInstanceState.getInt(SENSITIVITY_PRO));
            displayMonitorTextView.setText(savedInstanceState.getString(DISPALY_MONITOR));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MonitorService.running) {
            displayMonitorTextView.setText(R.string.monitoring);
            Double sensitivityValue = MonitorService.sensitivityTextView;
            sensitivityLevelTextView.setText(sensitivityValue.intValue()+"");
            sensitivityBar.setProgress(sensitivityValue.intValue());
            phoneNumEditText.setText(MonitorService.number);
            enableDisableUI(false);
        }
        else
            displayMonitorTextView.setText(R.string.not_monitor);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(PHONE_NUMBER,phoneNumEditText.getText().toString());
        outState.putBoolean(CALL,callButton.isChecked());
        outState.putBoolean(TEXT,textButton.isChecked());
        outState.putString(SENSITIVITY_LEVEL, sensitivityLevelTextView.getText().toString());
        outState.putInt(SENSITIVITY_PRO,sensitivityBar.getProgress());
        outState.putString(DISPALY_MONITOR,displayMonitorTextView.getText().toString());

        super.onSaveInstanceState(outState);
    }

    private void makeCall(){
        String phoneNumber = phoneNumEditText.getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void sendSMS(String phoneNumber, String message)
    {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    makeCall();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void enableDisableUI(boolean bool){
        phoneNumEditText.setEnabled(bool);
        callButton.setEnabled(bool);
        textButton.setEnabled(bool);
        sensitivityBar.setEnabled(bool);
        testButton.setEnabled(bool);
        monitorButton.setEnabled(bool);
    }
}
