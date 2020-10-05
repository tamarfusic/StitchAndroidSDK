package com.fusit.stitchutils.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.FrameLayout;

import com.fusit.stitchutils.StitchActivity;
//import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by tamarraviv.
 */
public class RecorderCamera implements CameraPreview.OnSurfaceCreated {
    private static final String LOGGER = "RecorderCamera";
    public static final int FUSE_WIDTH = 480;
    public static final int FUSE_HEIGHT = 360;
    private final Boolean recordVideo;
    private final int rotation;
    private final RecorderCameraOptions options;
    private MediaRecorder mediaRecorder;
    private StitchActivity activity;
    private File outputPath;
    private Camera camera;
    private Boolean useFrontCamera;
    private CameraPreview surface;
    private FrameLayout previewHolder;
    private File photoOutputPath;
    private PhotoTaken photoTakenCallback;
    private boolean recorderStarted=false;

    private static int PREVIEW_WIDTH = 640;
    private static int PREVIEW_HEIGHT = 360;
    private boolean recordingActive=false;
    private int cameraId;
    private int finalRotation;


    public RecorderCamera(final StitchActivity activity, FrameLayout previewHolder, Boolean useFrontCamera, File targetDirectory, RecorderCameraOptions options){
        this.activity = activity;
        this.useFrontCamera = useFrontCamera;
        this.recordVideo = options.recordVideo;
        this.rotation = Orientation.getRotationAngle(activity);
        this.previewHolder=previewHolder;
        this.options = options;
        try {
//            audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            getCamera();
            setOutputPath(targetDirectory);
            setPhotoOutputPath(targetDirectory);
//            setPreviewSize();
            mediaRecorder = new MediaRecorder();
            CameraPreview surface;
            if(recordVideo){
                surface = new CameraSurfaceViewPreview(activity,camera,this, options);
            }else{
                surface = new CameraTextureViewPreview(activity,camera,this, options);
            }
            previewHolder.addView(surface.getView());
        } catch (Exception e) {
            clearAndExit(e);
        }
    }

    private void clearAndExit(Exception e){
        Log.e(LOGGER, "Unexpected error, clearing and exiting");
        Log.e(LOGGER, e.toString());
        e.printStackTrace();
//        Crashlytics.logException(e);
        release();
        ((StitchActivity) activity).fusingFailed(); //TODO: should do better managment here
    }


    @Override
    public void onSurfaceCreated(Size previewSize) {
        Log.e("tamar", "+++++++++++++++++++ finalRotation="+finalRotation);
        if(recorderStarted){
            camera.stopPreview();

            return;
        }
        try {
            if(recordVideo){
                recorderStarted = true;
                List<Camera.Size> sizes = camera.getParameters().getSupportedVideoSizes();
                Size bestSize;
                if(sizes==null){
                    //First list can be null if previs===sizes
                    bestSize = previewSize;
                }else{
                    Camera.Size selectedSize = CameraHelpers.pickBestSize(sizes, FUSE_WIDTH, FUSE_HEIGHT,true);
                    bestSize = new Size(selectedSize.width,selectedSize.height);
                }
                Log.i(LOGGER, String.format("Selected %dx%d for camera stream", bestSize.width, bestSize.height));

                mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mediaRecorder, int i, int i2) {
                        Log.e(LOGGER, "Got some error, cleaning up and killing it");
                        mediaRecorder.release();
                        release();
                        ((Activity) activity).finish();
                    }
                });
                camera.unlock();
                mediaRecorder.setCamera(camera);
//                if(audioManager.isWiredHeadsetOn()) {
//                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//                }else{
                //!!!!!WARNING: should not use voice_communication sync it delays the audio
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//should be best for microphone
//                }
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setOutputFile(getOutputPath().toString());
    //            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mediaRecorder.setVideoEncodingBitRate(900 * 1024);
                mediaRecorder.setAudioEncodingBitRate(64 * 1024);
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setVideoSize(bestSize.width, bestSize.height);
                mediaRecorder.setOrientationHint(finalRotation);
                mediaRecorder.prepare();

                camera.stopPreview();
            }else{
                setPictureSize();
            }

        }catch (Exception e){
            clearAndExit(e);
        }
    }

    @Override
    public void onErrorStartingPreview() {
        ((StitchActivity)activity).fusingFailed();
    }

    public void setPictureSize() {
        Log.i(LOGGER,"Setting picture size");
        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
        Camera.Size size = CameraHelpers.pickBestSize(sizes, PREVIEW_WIDTH, PREVIEW_HEIGHT,true);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureSize(size.width, size.height);
//        parameters.setRotation(options.finalRotation);
        camera.setParameters(parameters);
    }


    private void setCamera(){
        Log.e("tamar","in setCamera!!!");
        if(useFrontCamera){
            int numberOfCameras = Camera.getNumberOfCameras();
            if(numberOfCameras>=2){
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        cameraId = i;
                        camera = Camera.open(i);
                        Log.i(LOGGER,"Opening front camera");
                        return;
                    }
                }

            }
        }
        Log.i(LOGGER,"Opening default camera");
        cameraId = 0;
        camera = Camera.open();
    }

    private Camera getCamera(){
        if(camera==null){
            setCamera();
            camera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int i, Camera camera) {
                    clearAndExit(new Exception("Error during camera initialisation"));
                }
            });

            Camera.CameraInfo info =
                    new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);

            finalRotation=0;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                finalRotation = (info.orientation + rotation) % 360;
                finalRotation = (360 - finalRotation) % 360;  // compensate the mirror
            } else {  // back-facing
                finalRotation = (info.orientation - rotation + 360) % 360;
            }
            Log.i(LOGGER, String.format("Set display orientation %d from %d", finalRotation, rotation));
            options.finalRotation = finalRotation;
            Log.e("tamar", " finalRotation="+finalRotation);
//            camera.setDisplayOrientation(finalRotation);
        }
        return camera;
    }

    public void startRecording(){
        mediaRecorder.start();
        recordingActive = true;
    }

    public void stopRecording(){
        mediaRecorder.stop();
        recordingActive = false;
        release();
    }

    public void release(){
        Log.i(LOGGER, "Releasing everything");
        if(camera!=null){
            try{
                camera.lock();
                camera.stopPreview();
            } catch (Exception e){
                Log.i(LOGGER,"Failed to unlock, probably already unlocked");
            }
            Log.d(LOGGER,"Calling camera release");
            camera.release();
        }
        if(mediaRecorder!=null){
            mediaRecorder.release();
        }else{
            Log.e(LOGGER,"skipping release of mediarecorder since not locked");
        }
        previewHolder.removeAllViews();
    }



    public File getOutputPath() {
        return outputPath;
    }

    public File getPhotoOutputPath(){
        return photoOutputPath;
    }

    private void setOutputPath(File targetDirectory) {
        try{
            outputPath = new File(targetDirectory,FileHelpers.randomTimeName("mp4"));
        }catch (Exception e){
            Log.e(LOGGER,"Could not get outputpath");
            Log.e(LOGGER,"random time"+FileHelpers.randomTimeName("mp4"));
            Log.e(LOGGER,"target directory "+targetDirectory.toString());
        }
        Log.i(LOGGER, "Output path" + outputPath);
    }

    public void setPhotoTakenCallback(PhotoTaken callback){
        photoTakenCallback = callback;
    }

    public void takePhoto(){
//        try {
//
//            camera.takePicture(null, null, new Camera.PictureCallback() {
//                @Override
//                public void onPictureTaken(byte[] data, Camera camera) {
//                    Camera.Size size = camera.getParameters().getPictureSize();
//                    float downscale_factor;
//                    if (size.width > PREVIEW_WIDTH) {
//                        downscale_factor = (float) PREVIEW_WIDTH / (float) size.width;
//                    } else {
//                        downscale_factor = 1.0f;
//                    }
//                    File originalFile;
//                    try {
//                        originalFile = new File(FileHelpers.bestDirBySize(activity, true, 50),"__temporary_photo.jpg");
////                        Files.write(data,originalFile);
//                    } catch (IOException e) {
//                        Log.e(LOGGER,"Could not write original file");
//                        e.printStackTrace();
////                        Crashlytics.logException(e);
//                        photoTakenCallback.onPhotoFailed();
//                        return;
//                    } catch (FileHelpers.NoValidDir noValidDir) {
//                        Log.e(LOGGER, "Could not find place for original file");
////                        noValidDir.printStackTrace();
////                        Crashlytics.logException(noValidDir);
//                        photoTakenCallback.onPhotoFailed();
//                        return;
//                    }
//
//                    Log.e(LOGGER, "Downscale factor " + downscale_factor);
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = (int) downscale_factor;
//                    options.inPurgeable = true;
//                    Bitmap picture;
//                    try {
//                        Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                        picture = ImageHelpers.scaleCenterCrop(original, PREVIEW_HEIGHT, PREVIEW_WIDTH);
//                        original.recycle();
//                    } catch (OutOfMemoryError e) {
//                        Log.e(LOGGER, "Out of memory, skipping posterframe");
//                        photoTakenCallback.onPhotoFailed();
//                        originalFile.delete();
//                        return;
//                    }
//                    File pictureFile = getPhotoOutputPath();
//                    try {
//                        FileOutputStream fos = new FileOutputStream(pictureFile);
//                        picture.compress(Bitmap.CompressFormat.JPEG, 90, fos);
//                        fos.close();
//                        Log.i(LOGGER, "Photo saved to " + pictureFile.toString());
//                        picture.recycle();
//                        ExifCopier copier = new ExifCopier();
//                        copier.createInFile(originalFile.toString());
//                        copier.createOutFile(pictureFile.toString());
//                        copier.readExifData();
//                        copier.writeExifData();
//                        photoTakenCallback.onPhotoTaken(pictureFile);
//                    } catch (FileNotFoundException e) {
//                        Log.d(LOGGER, "File not found: " + e.getMessage());
//                    } catch (IOException e) {
//                        Log.d(LOGGER, "Error accessing file: " + e.getMessage());
//                    }
//                    originalFile.delete();
//                }
//            });
//        } catch (Exception e){
//            Log.e(LOGGER, "Photo take failed");
//            Log.e(LOGGER, e.toString());
////            Crashlytics.logException(e);
//            photoTakenCallback.onPhotoFailed();
//        }
    }

    public void setPhotoOutputPath(File targetDirectory) {
        photoOutputPath = new File(targetDirectory,FileHelpers.randomTimeName("jpg"));
    }

    public void stopPreview() {
        camera.stopPreview();
    }

    public void startPreview() {
        camera.startPreview();
    }

    public boolean isRecording() {
        return recordingActive;
    }

    public static interface PhotoTaken{
        void onPhotoTaken(File path);
        void onPhotoFailed();
    }
}
