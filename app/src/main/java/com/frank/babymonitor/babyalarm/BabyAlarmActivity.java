package com.frank.babymonitor.babyalarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

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
    private TextView sensitivityLevelTextView;
    private final String PHONE_NUMBER = "phoneNumber",CALL = "call",TEXT = "text",SENSITIVITY_LEVEL="sensitivity_level",SENSITIVITY_PRO="sensitivity_pro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baby_alarm_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        phoneNumEditText = (EditText) findViewById(R.id.phone_number);
        callButton = (RadioButton) findViewById(R.id.call);
        textButton = (RadioButton) findViewById(R.id.textMessage);
        sensitivityLevelTextView = (TextView) findViewById(R.id.sensitivity_level);
        sensitivityBar = (SeekBar) findViewById(R.id.sensitivity_seekbar);
        testButton = (Button) findViewById(R.id.sensitivity_test);
        monitorButton = (Button) findViewById(R.id.start_monitor);

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

        if (savedInstanceState != null){
            //get saved instance
            phoneNumEditText.setText(savedInstanceState.getString(PHONE_NUMBER));
            callButton.setChecked(savedInstanceState.getBoolean(CALL));
            textButton.setChecked(savedInstanceState.getBoolean(TEXT));
            sensitivityLevelTextView.setText(savedInstanceState.getString(SENSITIVITY_LEVEL));
            sensitivityBar.setProgress(savedInstanceState.getInt(SENSITIVITY_PRO));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        super.onSaveInstanceState(outState);
    }
}
