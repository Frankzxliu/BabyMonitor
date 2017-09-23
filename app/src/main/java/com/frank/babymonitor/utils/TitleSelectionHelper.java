package com.frank.babymonitor.utils;

import android.util.Log;

import com.frank.babymonitor.BabyMonitorListFragment;

/**
 * Help to bring up correct activity
 */
public class TitleSelectionHelper implements BabyMonitorListFragment.OnTitleSelected{

    private final String TAG = "TitleSelectionHelper";

    @Override
    public void OnTitleSelected(int position) {

        switch (position){
            case 0:
                Log.d(TAG, "1 selected");
                break;
            case 1:
                Log.d(TAG, "2 selected");
                break;
            case 2:
                Log.d(TAG, "3 selected");
                break;
            case 3:
                Log.d(TAG, "4 selected");
                break;
        }

    }
}
