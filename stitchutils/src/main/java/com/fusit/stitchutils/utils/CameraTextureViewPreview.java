package com.fusit.stitchutils.utils;

/**
 * Created by tamarraviv.
 */

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
public class CameraTextureViewPreview extends TextureView implements TextureView.SurfaceTextureListener,CameraPreview {
    private final RecorderCameraOptions options;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private final String LOGGER = "aa.CameraPreview";
    OnSurfaceCreated delegate;

    public CameraTextureViewPreview(Context context, Camera camera, OnSurfaceCreated delegate, RecorderCameraOptions options) {
        super(context);
        this.delegate = delegate;
        this.options = options;
        setSurfaceTextureListener(this);
        mCamera = camera;
    }

    private void updateTextureViewSize(float viewWidth, float viewHeight, float cameraWidth,float cameraHeight) {
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        Log.d(LOGGER, "cameraWidth " + cameraWidth);
        Log.d(LOGGER,"cameraHeight "+cameraHeight);
        Log.d(LOGGER,"viewWidth "+viewWidth);
        Log.d(LOGGER,"viewHeight "+viewHeight);

        float scaledHeight = viewWidth / cameraWidth * cameraHeight;
        Log.i(LOGGER,"scaled height"+scaledHeight);
        scaleY = scaledHeight / viewHeight;

        // Calculate pivot points, in our case crop from center
        float pivotPointX = viewWidth / 2;
        float pivotPointY = viewHeight / 2;
        Log.d(LOGGER,"pivotX"+pivotPointX);
        Log.d(LOGGER,"pivotY"+pivotPointY);
        Log.d(LOGGER,"scaleX"+scaleX);
        Log.d(LOGGER,"scaleY"+scaleY);
//        scaleY = 1.0f;

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        setTransform(matrix);
        setLayoutParams(new FrameLayout.LayoutParams((int) viewWidth, (int) viewHeight));
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> recordingSizes = mCamera.getParameters().getSupportedVideoSizes();
        Size bestSize;
        Size requestSize;

        Log.i(LOGGER, "Texture available"+width+" "+height);
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
        }


        if(recordingSizes==null){
            Log.i(LOGGER, "Using same-stream flow");
            requestSize = new Size(RecorderCamera.FUSE_WIDTH, RecorderCamera.FUSE_HEIGHT);
//            TODO: Remove for release
//            Crashlytics.logException(new Exception("Same stream-flow found"));
        }else{
            Log.i(LOGGER, "Using different-streams flow");
            requestSize= new Size(width,height);
        }

        Log.i(LOGGER,"Requesting size of preview: "+requestSize.width+"x"+requestSize.height);
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size selectedSize = CameraHelpers.pickBestSize(sizes, requestSize.width, requestSize.height,true);
        bestSize = new Size(selectedSize.width,selectedSize.height);

        parameters.setPreviewSize(bestSize.width,bestSize.height);
        parameters.setRotation(options.finalRotation);
        try {
            mCamera.setParameters(parameters);
        }catch (RuntimeException e){
            Log.e(LOGGER, "Could not set parameters for camera, probably not supported preview size");
//            Crashlytics.logException(e);
        }
        mCamera.startPreview();
        updateTextureViewSize(width, height,bestSize.width, bestSize.height);
        delegate.onSurfaceCreated(bestSize);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        Log.e(LOGGER, "Texture size changed" + i + " " + i2);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public View getView() {
        return this;
    }
}