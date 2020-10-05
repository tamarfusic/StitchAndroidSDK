package com.fusit.stitchutils.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by tamarraviv.
 */
public class Orientation {
    public static int getRotation(Context context){
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRotation();
    }

    public static int getRotationAngle(Context context){
        int rotation = getRotation(context);
        if(rotation ==  Surface.ROTATION_0){
            return 0;
        }else if(rotation == Surface.ROTATION_90){
            return 90;
        }else if(rotation == Surface.ROTATION_180){
            return 180;
        }else if(rotation == Surface.ROTATION_270){
            return 270;
        }else{
            return 0;
        }
    }

    public static void lockOrientation(Activity activity) {
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int tempOrientation = activity.getResources().getConfiguration().orientation;
        int orientation;
        switch(tempOrientation)
        {
            case Configuration.ORIENTATION_LANDSCAPE:
                if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                else
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
            default:
                if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                else
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
        activity.setRequestedOrientation(orientation);
    }

    public static void lockOrientationToLandscape(Activity activity){
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int setOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        if(rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270){
            setOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
        activity.setRequestedOrientation(setOrientation);
    }
}
