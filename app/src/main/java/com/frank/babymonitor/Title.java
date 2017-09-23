package com.frank.babymonitor;

import android.widget.ImageView;

/**
 * List Title(includes image position and name)
 */
public class Title {

    private int functionImagePosition;
    private String functionName;

    public Title(int functionImagePosition, String functionName ){
        this.functionImagePosition = functionImagePosition;
        this.functionName = functionName;
    }

    public int getFunctionImage(){return functionImagePosition;}
    public String getFunctionName(){
        return functionName;
    }
}
