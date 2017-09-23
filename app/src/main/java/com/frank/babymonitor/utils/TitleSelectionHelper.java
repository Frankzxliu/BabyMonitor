package com.frank.babymonitor.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.frank.babymonitor.BabyMonitorListFragment;
import com.frank.babymonitor.babyalarm.BabyAlarmActivity;

/**
 * Help to bring up correct activity
 */
public class TitleSelectionHelper implements BabyMonitorListFragment.OnTitleSelected{

    private Context context;

    public TitleSelectionHelper(Context context){
        this.context = context;
    }

    private final String TAG = "TitleSelectionHelper";

    @Override
    public void OnTitleSelected(int position) {

        switch (position){
            case 0:
                context.startActivity(new Intent(context, BabyAlarmActivity.class));
                Log.d(TAG, "1 selected");
                break;
            case 1:
                Log.d(TAG, "2 selected");
                Toast.makeText(context,"Coming soon",Toast.LENGTH_LONG).show();
                break;
            case 2:
                Log.d(TAG, "3 selected");
                Toast.makeText(context,"Coming soon",Toast.LENGTH_LONG).show();
                break;
            case 3:
                Log.d(TAG, "4 selected");
                Toast.makeText(context,"Coming soon",Toast.LENGTH_LONG).show();
                break;
        }

    }
}
