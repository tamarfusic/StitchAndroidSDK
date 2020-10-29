package com.fusit.stitchutils.utils;

/**
 * Created by tamarraviv.
 */

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
public class CameraSurfaceViewPreview extends SurfaceView implements SurfaceHolder.Callback,CameraPreview {
    private final RecorderCameraOptions options;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private final String LOGGER = "aa.CameraSurfacePreview";
    OnSurfaceCreated delegate;


    public CameraSurfaceViewPreview(Context context, Camera camera, OnSurfaceCreated delegate, RecorderCameraOptions options) {
        super(context);
        this.delegate = delegate;
        this.options=options;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        }

    /**
     * {@inheritDoc}
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }


//    private void updateTextureViewSize(float viewWidth, float viewHeight, float cameraWidth,float cameraHeight) {
//        float scaleX = 1.0f;
//        float scaleY = 1.0f;
//
//        MLog.i(LOGGER,"cameraWidth "+cameraWidth);
//        MLog.i(LOGGER,"cameraHeight "+cameraHeight);
//        MLog.i(LOGGER,"viewWidth "+viewWidth);
//        MLog.i(LOGGER,"viewHeight "+viewHeight);
//
//        float scaledHeight = viewWidth / cameraWidth * cameraHeight;
//        MLog.i(LOGGER,"scaled height"+scaledHeight);
//        scaleY = scaledHeight / viewHeight;
//
//        // Calculate pivot points, in our case crop from center
//        float pivotPointX = viewWidth / 2;
//        float pivotPointY = viewHeight / 2;
//        MLog.i(LOGGER,"pivotX"+pivotPointX);
//        MLog.i(LOGGER,"pivotY"+pivotPointY);
//        MLog.i(LOGGER,"scaleX"+scaleX);
//        MLog.i(LOGGER,"scaleY"+scaleY);
////        scaleY = 1.0f;
//
//        Matrix matrix = new Matrix();
//        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);
//
//        setTransform(matrix);
//        setLayoutParams(new FrameLayout.LayoutParams((int) viewWidth, (int) viewHeight));
//    }
////
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//
//    }

//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
//        Log.e(LOGGER, "Texture size changed"+i+" "+i2);
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//        return false;
//    }

//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(LOGGER, "Texture created");
        requestLayout();
        Log.i(LOGGER, "Requested layout");
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("tamar", "in surfaceChanged 2");
        Camera.Parameters parameters;
        try{
             parameters = mCamera.getParameters();
        }catch (Exception e){
            Log.e(LOGGER,e.toString());
            return;
        }

        List<Camera.Size> recordingSizes = mCamera.getParameters().getSupportedVideoSizes();
        Size bestSize;
        Size requestSize;

        Log.i(LOGGER, "Texture available"+width+" "+height);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
//            Crashlytics.logException(e);

        }
        if(recordingSizes==null){
            Log.i(LOGGER, "Using same-stream flow");
            requestSize = new Size(RecorderCamera.FUSE_WIDTH, RecorderCamera.FUSE_HEIGHT);
        }else{
            Log.i(LOGGER, "Using different-streams flow");
            requestSize = new Size(width,height);
            if(options.previewContainerWidth > 0 && options.previewContainerHeight > 0){
                requestSize = new Size(options.previewContainerWidth,options.previewContainerHeight);
            }
        }

        Log.i(LOGGER,"Requesting size of preview: "+requestSize.width+"x"+requestSize.height);
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size selectedSize = CameraHelpers.pickBestSize(sizes, requestSize.width, requestSize.height,true);
        bestSize = new Size(selectedSize.width,selectedSize.height);
        Log.e("tamar", "==========parameters.setPreviewSize width="+bestSize.width+"     height"+bestSize.height);
        parameters.setPreviewSize(bestSize.width, bestSize.height);
        parameters.setRotation(options.finalRotation);
        try {
            mCamera.setParameters(parameters);
        }catch (RuntimeException e){
            Log.e(LOGGER, "Could not set parameters for camera, probably not supported preview size");
//            Crashlytics.logException(e);
        }
        /*
        Setting aspect rate
         */
        int new_width=0, new_height=0;
        CameraSurfaceViewPreview preview = this;

        if(options.previewContainerWidth > 0 && options.previewContainerHeight>-0){
            new_width = options.previewContainerWidth;
            new_height = options.previewContainerHeight;
        }else{
            float ratio = (float)bestSize.width/bestSize.height;

            if(getWidth()/getHeight()<ratio){
                new_width = Math.round(preview.getHeight() * ratio);
                new_height = getHeight();
            }else{
                new_width = preview.getWidth();
                new_height = Math.round(preview.getWidth() / ratio);
            }
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(new_width, new_height);
        params.gravity = Gravity.CENTER;
        preview.setLayoutParams(params);

        try{
        mCamera.startPreview();
        }catch (Exception e){
            Log.e(LOGGER,"Error starting camera");
//            Crashlytics.logException(new Exception("Error starting camera"));
            delegate.onErrorStartingPreview();
        }
        delegate.onSurfaceCreated(bestSize);
    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public View getView() {
        return this;
    }
}