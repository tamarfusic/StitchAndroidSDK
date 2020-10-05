package com.fusit.stitchutils.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.view.WindowManager;


import com.fusit.stitchutils.StitchActivity;

import java.io.IOException;

public class CameraPreviewBase extends SurfaceView implements SurfaceHolder.Callback {
    private static SurfaceHolder mHolder;
//    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "CameraPreviewBase";
    private Activity activity;


    public CameraPreviewBase(Activity context, Camera camera) {
        super(context);
        this.activity = context;
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.e("tamar", "setDisplayOrientation1=90");
//            mCamera.setDisplayOrientation(90);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e("tamar", "in surfaceChanged 1 width="+w);
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {

            Camera.Parameters parameters = mCamera.getParameters();
            Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            if (display.getRotation() == Surface.ROTATION_0) {
                Log.e("tamar", "ROTATION_0");
                parameters.setPreviewSize(h, w);
                mCamera.setDisplayOrientation(90);
            }

            if (display.getRotation() == Surface.ROTATION_90) {
                Log.e("tamar", "ROTATION_90");
                parameters.setPreviewSize(w, h);
                mCamera.setDisplayOrientation(0);
            }

            if (display.getRotation() == Surface.ROTATION_180) {
                Log.e("tamar", "ROTATION_180");
                parameters.setPreviewSize(h, w);
                mCamera.setDisplayOrientation(270);
            }

            if (display.getRotation() == Surface.ROTATION_270) {
                Log.e("tamar", "ROTATION_270");
                parameters.setPreviewSize(w, h);
                mCamera.setDisplayOrientation(180);
            }

//            previewCamera();


            setCameraDisplayOrientation(activity,0,mCamera);
            mCamera.setPreviewDisplay(mHolder);
            Log.e("tamar", "setDisplayOrientation333");
//            if (Build.VERSION.SDK_INT >= 8) {
//                mCamera.setDisplayOrientation(90);
//            }
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);
        camera.stopPreview();
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 90; break;
            case Surface.ROTATION_90: degrees = 0; break;
            case Surface.ROTATION_180: degrees = 270; break;
            case Surface.ROTATION_270: degrees = 180; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
//        degrees=0;
        Log.e("tamar", "***degrees="+degrees);
        StitchActivity.orientation=degrees;
//        camera.setDisplayOrientation(degrees);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }
}

