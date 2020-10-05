package com.fusit.stitchutils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.bytedance.sdk.open.aweme.TikTokOpenApiFactory;
import com.bytedance.sdk.open.aweme.TikTokOpenConfig;
import com.fusit.stitchutils.customui.CircularRevealTransition;
import com.fusit.stitchutils.managers.SharedPrefsManager;
import com.fusit.stitchutils.network.RequestListener;
import com.fusit.stitchutils.network.RequestManager;
import com.fusit.stitchutils.network.RequestType;
import com.fusit.stitchutils.network.VRequest;
import com.fusit.stitchutils.network.VolleyMultipartRequest;
import com.fusit.stitchutils.utils.CameraPreviewBase;
import com.fusit.stitchutils.utils.Character;
import com.fusit.stitchutils.utils.OKHttpFileDownloader;
import com.fusit.stitchutils.utils.RecorderCamera;
import com.fusit.stitchutils.utils.StitchedVideo;
import com.fusit.stitchutils.utils.StructUtils;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class StitchActivity extends /*AppCompat*/ Activity implements MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, RequestListener, SurfaceHolder.Callback {

    private static final int SHARE_ACTIVITY_REQ_CODE = 148;
    private static String videoName;
    //    private File fileToStich = null;
    private String charID;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
//    private VideoView videoView;
    private MediaPlayer mediaPlayer;
    private boolean isMediaPlayerReleased;
    FrameLayout loadingScreen, camerPreview;
    RelativeLayout rootView;
    RecorderCamera camera;
    private int previewWidth=0;
    private int previewHeight;
    private boolean holderFragmentPrepared=false;
    private int numberOfCameras;
    private Camera mCamera;
    private CameraPreviewBase mPreview;
    private MediaRecorder mediaRecorder;
    private static final String TAG = StitchActivity.class.getSimpleName();
    private boolean isRecording;
    private Character fCharacter;
    public static int orientation;
    private static final int ID_TIME_COUNT = 0x1006;
    public static final String CHARACTER_ID = "CHARACTER_ID";
    public static final String SHOW_SHARE_SCREEN = "SHOW_SHARE_SCREEN";
    public static final String SHARE_TO_INSTAGRAM = "SHARE_TO_INSTAGRAM";
    public static final String SHARE_TO_FACEBOOK = "SHARE_TO_FACEBOOK";
    public static final String SHARE_TO_TIKTOK = "SHARE_TO_TIKTOK";
    public static final String TIKTOK_SHARE_KEY = "TIKTOK_SHARE_KEY";
    public static final String SHARE_TO_MORE = "SHARE_TO_MORE";
    public static final String STITCHED_FILE_PATH = "STITCHED_FILE_PATH";
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 111;

    private String currentOutputFile;
    private ImageView recBtn, stopBtn;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private int videoDuration;
    private int amountToUpdate;
    private boolean showShareScreen;
    private boolean showInstagramShare;
    private boolean showMoreShare;
    private boolean showFacebookShare;
    private boolean showTiktokShare;
    private String tiktokKey;
    private Dialog showPermissionsNeededDialog;
    private static final int MY_MULTIPLE_PERMISSIONS_REQUEST = 15;
    List<String> permissionsRequestNeededList = new ArrayList<>();
    List<String> missingPermissionsList = new ArrayList<>();
    List<String> permissionsNeverGrantedList = new ArrayList<>();
    private static final int SETTINGS_RC = 18;
    private File fileToPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        Log.e("tamar", "width="+size.x+"  hieght="+size.y);
//        int width = size.x;
//        int height = size.y;
        if(permissionsAllowed()) {
            initScreenAndIntent();

        }
    }

    private void initScreen(){
        setContentView(R.layout.activity_stitch);
        loadingScreen = (FrameLayout)findViewById(R.id.loading_screen);
        rootView = (RelativeLayout)findViewById(R.id.root_view);
        rootView.post(new Runnable() {
            @Override public void run() {
                animateLoadingScreen(true);
            }
        });
        callGetCharacterApi(charID);
//        vidSurface = (SurfaceView) findViewById(R.id.video_preview);
//        vidHolder = vidSurface.getHolder();
//        vidHolder.addCallback(this);
        initSurfaceViewPlayer();
        recBtn = (ImageView) findViewById(R.id.start_recording);
        stopBtn = (ImageView) findViewById(R.id.stop_recording);
        progressBar = (ProgressBar) findViewById(R.id.line_progress_bar);
    }

    private void initSurfaceViewPlayer() {
        vidSurface = (SurfaceView) findViewById(R.id.video_preview);
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);
        vidHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        vidSurface.setZOrderMediaOverlay(true);
//        objVideoView.videoView.setZOrderOnTop(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("tamar", "in onActivityResult1 requestCode="+requestCode+" resultCode="+resultCode);
        if(requestCode==SHARE_ACTIVITY_REQ_CODE){ // && resultCode==RESULT_OK) {
            Log.e("tamar", "in onActivityResult2 requestCode="+requestCode+" resultCode="+resultCode);
            initScreen();
        }
        if (requestCode == SETTINGS_RC) {
            if (permissionsAllowed()) {
                if (showPermissionsNeededDialog != null) {
                    showPermissionsNeededDialog.dismiss();
                    showPermissionsNeededDialog = null;
                }
                initScreenAndIntent();
            }
        }
    }

    private void initScreenAndIntent() {
        handleIntent();
        initScreen();
    }


    private void toggleRecBtn(boolean showPlay) {
        if(showPlay) {
            recBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);

        } else {
            stopBtn.setVisibility(View.VISIBLE);
            recBtn.setVisibility(View.INVISIBLE);

        }
    }


    private void setCameraPriviewSize(int width, int height){
        mCamera = getCameraInstance();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(width, height); //temp hard coded

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            parameters.set("orientation", "portrait");
            parameters.set("rotation",90);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            parameters.set("orientation", "landscape");
            parameters.set("rotation", 90);
        }

        mCamera.setParameters(parameters);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreviewBase(this, mCamera);

    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean isDeviceSupportCamera() {
        return getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }


//    private void prepareCameraHolderFragment() {
//        Log.e("tamar", "in prepareCameraHolderFragment!!!!!!");
//        cameraHolder = (FrameLayout)findViewById(R.id.camera_video);
//        previewWidth = cameraHolder.getWidth();
//        //4/3 is not mistake, this way by cropping outside parts making 4/3 stream look as 16/9 one
//        previewHeight = previewWidth / 4 * 3;
//        ViewGroup.LayoutParams cameraHolderParams = cameraHolder.getLayoutParams();
//        Log.e("tamar", "Setting height of " + previewHeight+" width="+previewWidth);
//        cameraHolderParams.height = previewHeight;
//        cameraHolder.setLayoutParams(cameraHolderParams);
//        holderFragmentPrepared=true;
//    }


    private void showVideo(File fileToPlay) {
//        vidSurface = (SurfaceView) findViewById(R.id.video_preview);
//        vidHolder = vidSurface.getHolder();
//        vidHolder.addCallback(this);
        this.fileToPlay = fileToPlay;
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        initSurfaceViewPlayer();
//        vidSurface.setVideoURI(Uri.parse(fileToPlay.getAbsolutePath()));
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
////                toggleVideoView(false);
//            }
//        });
////        videoView.setZOrderOnTop(true);
////        videoView.requestFocus();
//        videoView.start();
//        videoView.requestFocus();
//        //videoView.setZOrderOnTop(true);
////        camerPreview.setZOrderOnTop(false);
//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//
//            public void onPrepared(MediaPlayer mp) {
////                videoDuration = videoView.getDuration();
//                mp.setLooping(true);
//                videoDuration = (int)((double)fCharacter.getLength()*1000);
//                animateLoadingScreen(false);
//                //((long)fCharacter.getLength()*1000))
//                amountToUpdate = videoDuration / 100;
//                Log.e("tamar", "==============videoDuration="+videoDuration+"   amountToUpdate"+amountToUpdate);
////                Timer mTimer = new Timer();
////                mTimer.schedule(new TimerTask() {
////
////                    @Override
////                    public void run() {
////                        runOnUiThread(new Runnable() {
////
////                            @Override
////                            public void run() {
////                                if (!(amountToUpdate * progressBar.getProgress() >= videoDuration)) {
////                                    int p = progressBar.getProgress();
////                                    p += 1;
////                                    progressBar.setProgress(p);
////                                }
////                            }
////                        });
////                    };
////                }, amountToUpdate);
//
////                loadingScreen.setVisibility(View.GONE);
//            }
//        });
    }


    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(CHARACTER_ID)) {
            charID = intent.getStringExtra(CHARACTER_ID);
            showShareScreen = intent.getBooleanExtra(SHOW_SHARE_SCREEN, false);
            if(showShareScreen) {
                showInstagramShare = intent.getBooleanExtra(SHARE_TO_INSTAGRAM, false);
                showFacebookShare = intent.getBooleanExtra(SHARE_TO_FACEBOOK, false);
                showMoreShare = intent.getBooleanExtra(SHARE_TO_MORE, false);
                showTiktokShare = intent.getBooleanExtra(SHARE_TO_TIKTOK, false);
                tiktokKey = intent.getStringExtra(TIKTOK_SHARE_KEY);
                if (!TextUtils.isEmpty(tiktokKey)) {
                    TikTokOpenApiFactory.init(new TikTokOpenConfig(tiktokKey));
                } else {
                    showTiktokShare = false;
                }
            }

        } else {
            Toast.makeText(this, "Make sure you sent a video id", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStop(){
        super.onStop();
        if(mediaPlayer!=null){
            mediaPlayer.release();
            isMediaPlayerReleased = true;
            mediaPlayer=null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer=null;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.e("tamar", "------in onVideoSizeChanged width="+width);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("tamar", "^^^^^^^^^^^^in onPrepared starting player"); //todo: check if this code is used
        videoDuration = (int)((double)fCharacter.getLength()*1000);
        animateLoadingScreen(false);
        //((long)fCharacter.getLength()*1000))
        amountToUpdate = videoDuration / 100;
        Log.e("tamar", "==============videoDuration="+videoDuration+"   amountToUpdate"+amountToUpdate);
        mp.setLooping(true);
        mp.start();
        vidSurface.setVisibility(View.VISIBLE);
    }

    public void fusingFailed() {
//        model.setStage(FuseActivity.Stage.FAILED);
//        if(isJoin){
//            LogAnalytics.fuseFailed(videoId);
//        } else {
//            LogAnalytics.fuseFailed(songId);
//        }
//        navigateBasedOnStage();
    }


    private void rebuildCameraFramgent(){
//        if(camera!=null){
//            camera.release();
//            camera=null;
//        }
//        try {
//            RecorderCameraOptions recordingOptions = new RecorderCameraOptions();
//            recordingOptions.recordVideo = true;
//            recordingOptions.previewContainerHeight = previewHeight;
//            recordingOptions.previewContainerWidth = previewWidth;
//            camera = new RecorderCamera(this, cameraHolder, true, new File("/storage/emulated/0/Download/fuse.it"),recordingOptions);
//        } catch (Exception e) {
////            noValidDir.printStackTrace();
////            Crashlytics.logException(noValidDir);
////            onDiskSpaceError();
//
//        }
    }

    public void onDiskSpaceError() {
//        model.setStage(Stage.DISK_FAILED);
//        if(isJoin){
//            LogAnalytics.fuseDiskFailed(videoId, model.fullDataMap());
//        } else {
//            LogAnalytics.fuseDiskFailed(songId, model.fullDataMap());
//        }
//        navigateBasedOnStage();
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        setCameraDisplayOrientation(this,0,mCamera);
        mediaRecorder = new MediaRecorder();
        Log.e("tamar", "new MediaRecorder()!!!");
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mediaRecorder.setVideoSize
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        currentOutputFile = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(currentOutputFile);
// recorder.setOutputFile(getVideoFolder()+rnd.nextString()+".mp4");
        // Step 5: Set the camerPreview output
        mediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mediaRecorder.setVideoSize(fCharacter.getWidth(), fCharacter.getHeight()); //1920, 1080);
        mediaRecorder.setVideoFrameRate(fCharacter.getFps());
        mediaRecorder.setVideoEncodingBitRate(900000);
//        mediaRecorder.setVideoSize(bestSize.width, bestSize.height);
        mediaRecorder.setOrientationHint(orientation);
        Log.e("tamar", "setOrientationHint=" + orientation);
        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        StitchActivity.orientation=result;
        camera.setDisplayOrientation(result);
    }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            videoName = "VID_"+ timeStamp + ".mp4";
            Log.e("tamar", "videoName="+videoName);
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + videoName);
        } else {
            return null;
        }
        Log.e("tamar", "mediaf file="+mediaFile.getAbsolutePath());
        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        Log.e("tamar", "in release MediaRecorder()!!!");
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void onStopClicked(View view) {
        stopRecording();
    }

    public void onRecordClicked(View view) {
        Log.e("tamar", "!!!!!!!!!!!!!in onRecordClicked!!!!!!");
        progressBar.setVisibility(View.VISIBLE);
        Camera.CameraInfo camera_info = new Camera.CameraInfo();
//        int camera_orientation = camera_info.orientation;
//        Log.e("tamar", "=======cam orientation="+camera_orientation);
        if (isRecording) {

            //need to stop recording
//            stopRecording();

            // stop recording and release camera
//            Log.e("tamar", "stopping recording");
//            mediaRecorder.stop();  // stop the recording
//            releaseMediaRecorder(); // release the MediaRecorder object
//            mCamera.lock();         // take camera access back from MediaRecorder
//
//            // inform the user that recording has stopped
////            setCaptureButtonText("Capture");
//            isRecording = false;
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
//                recBtn.setImageResource(R.drawable.inner_button_stop);
                toggleRecBtn(false);
                Log.e("tamar", "in prepare cal start MediaRecorder()!!!");
                mediaRecorder.start();
                progressBar.setVisibility(View.VISIBLE);

                isRecording = true;

                final ScheduledExecutorService service =
                        Executors.newScheduledThreadPool(0);
                service.scheduleWithFixedDelay(new Runnable() {
                    int progress = 1;
                    @Override public void run() {
//                        Log.e("tamar", "set progress before if:"+progress);
                        if(progress<=100) {
//                            Log.e("tamar", "set progress to:"+progress);
                            progressBar.setProgress(++progress);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("tamar", "isRecording="+isRecording);
                                    if(isRecording) {
                                        stopRecording();
                                    }
                                    Log.e("tamar", "call service.shutdown");
                                    service.shutdown();
                                }
                            });

                        }
                    }
                }, 1, amountToUpdate, TimeUnit.MILLISECONDS);

//                isRecording = true;

//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    public void run() {
//                            // Actions to do after 10 seconds
////                        isRecording=false;
//                        if(isRecording) {
//                            stopRecording();
//                        }
//                    }
//                }, (long)((double)fCharacter.getLength()*1000));
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    
    }

    private void stopRecording(){
        isRecording=false;
        mediaPlayer.stop();    //pause();
        Log.e("tamar", "stopping recording");
        progressBar.setVisibility(View.GONE);

        if(mediaRecorder!=null) {
            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    Log.e("tamar", "Recorder INFO="+what);
                }
            });
            mediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
//            setCaptureButtonText("Capture");
            isRecording = false;
//        callStitchApi(charID);
        }

        try {
            callStitchApi(charID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //    /* SurfaceHolder.Callback */
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//    }
    @Override
    public <T> void onResponseSuccess(RequestType requestType, T object, Object extra) {
        Log.e(TAG, "in onResponseSuccess!!!");
        switch (requestType) {
            case GET_CHAR:
                fCharacter = (Character) object;
                if(object!=null){
                    setAppOrientation(fCharacter.orientation);
                    Log.e(TAG, "in onResponseSuccess!!! object="+object.getClass());
//                    Log.e(TAG, "in onResponseSuccess!!! object vid path="+((Character)object).getVideo_path());
                    ((Character) object).print();
//                    setCameraPriviewSize(fCharacter.getWidth(), fCharacter.getHeight());
                } else {
                    Log.e(TAG, "in onResponseSuccess!!! object is NULL!!!");
                }
                Log.e(TAG, "in onResponseSuccess!!! vid pATH="+fCharacter.getVideo_path());
                downloadCharVid(fCharacter);
                break;
//
//            case STITCH_UPLOAD:
//                //download stitched file
//                Log.e("tamar", "in stitchActivity ******response vid path="+((Character)object).getVideo_path());
//                downloadStitchedVid(((Character)object).getVideo_path());
//                break;
//        }
        }
    }

    private void setAppOrientation(String orientation) {
        Log.e("tamar", "in setAppOrientation!!!s orientation="+orientation);
        if(orientation.equalsIgnoreCase("horizontal")) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }
    }

    private void downloadCharVid(Character charcterToDownlad) {
        Log.e(TAG, "in downloadCharVid!!! video url="+charcterToDownlad.getVideo_path());
        final File downloadFile =  getOutputMediaFile(MEDIA_TYPE_VIDEO);
//        public void downloadPreviewVideo(){
//            try {

//                if(!cachePath(false).exists()) {
//                    try {
                        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        File recordedFile = new File(getFilesDir()+ File.separator+ "vids" + File.separator + "video_"+ timestamp +".mp4");

                        final OKHttpFileDownloader downloader = new OKHttpFileDownloader(new OkHttpClient(), fCharacter.getVideo_path(),
                                downloadFile, new OKHttpFileDownloader.ProgressListener() {

                            @Override
                            public void onProgress(int percents) {
//                                Log.e("tamar", "in onProgress =========== percnts="+percents);
//                                if(percents<100) {
//                                    progressStatus += 1;
//                                    progressBar.setProgress(percents);
//                                } else {
//
//                                }
                            }

                            @Override
                            public void onCompleted() {
                                Log.e("tamar", "download completed: "+downloadFile );

                                showCameraPreview();
                                Log.e("tamar", "^^^^^call show video "+downloadFile );
                                showVideo(downloadFile);
//                                Log.e("tamar", "^^^^^call show video "+downloadFile );
//                                showVideo(downloadFile);
//                                loadingFirst.dismiss();
                            }

                            @Override
                            public void onFailed() {
                                Log.e("tamar", "download failed!!!---- "+downloadFile);
                            }


                        });
                        downloader.execute();

    }


    private void downloadStitchedVid(String vidUrl) {
        Log.e("tamar", "in downloadStitchedVid!!! video url="+vidUrl);
        final File downloadFile =  getOutputMediaFile(MEDIA_TYPE_VIDEO);
//        public void downloadPreviewVideo(){
//            try {

//                if(!cachePath(false).exists()) {
//                    try {
//        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
//        File recordedFile = new File(getFilesDir()+ File.separator+ "vids" + File.separator + "video_"+ timestamp +".mp4");

        final OKHttpFileDownloader downloader = new OKHttpFileDownloader(new OkHttpClient(), vidUrl,
                downloadFile, new OKHttpFileDownloader.ProgressListener() {

            @Override
            public void onProgress(int percents) {

            }

            @Override
            public void onCompleted() {
                Log.e("tamar", "download completed2: "+downloadFile );
                animateLoadingScreen(false);
//                loading.dismiss();
                releaseMediaRecorder();
//                FrameLayout camerPreview = (FrameLayout) findViewById(R.id.camera_video);
//                camerPreview.removeView(mPreview);
                if(showShareScreen) {
                    Intent shareIntent = new Intent(StitchActivity.this, ShareStitch.class);
                    Log.e("tamar", "download completed3: " + downloadFile);
                    if (downloadFile != null && downloadFile.exists()) {
                        Log.e("tamar", "^^^^^^^^download completed4 FILE NOT NULL!!!: " + downloadFile);
                        String recordedFilePath = downloadFile.getAbsolutePath();
                        shareIntent.putExtra(ShareStitch.RECORDER_FILE_PATH, recordedFilePath);
                        shareIntent.putExtra(ShareStitch.RECORDER_FILE_LENGTH, fCharacter.getLength());
                        shareIntent.putExtra(StitchActivity.SHARE_TO_INSTAGRAM, showInstagramShare);
                        shareIntent.putExtra(StitchActivity.SHARE_TO_FACEBOOK, showFacebookShare);
                        shareIntent.putExtra(StitchActivity.SHARE_TO_MORE, showMoreShare);
                        shareIntent.putExtra(StitchActivity.SHARE_TO_TIKTOK, showTiktokShare);
                        startActivityForResult(shareIntent, SHARE_ACTIVITY_REQ_CODE);
                    } else {
                        //todo: handle problem
                    }
                } else {
                    //todo: return the file destination to the calling app
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(STITCHED_FILE_PATH, downloadFile.getAbsolutePath());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }

            @Override
            public void onFailed() {
                Log.e("tamar", "download failed!!!---- "+downloadFile);
            }


        });
        downloader.execute();

    }


    private void showCameraPreview() {
        numberOfCameras = Camera.getNumberOfCameras();
//        cameraHolder = (AspectLockedFrameLayout) findViewById(R.id.camera_video);
//        cameraHolder.setAspectRatio(16.0/9.0);
        // Create an instance of Camera
        setCameraPriviewSize(fCharacter.width,fCharacter.height);
//        mCamera = getCameraInstance();
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(1920, 1080); //temp hard coded
////        for (Camera.Size previewSize: parameters.getSupportedPreviewSizes())
//
//        for (Camera.Size previewSize: parameters.getSupportedVideoSizes())
//        {
//            Log.e("tamar", "=========cam supported video size= "+previewSize.width+ ":"+previewSize.height);
//            // if the size is suitable for you, use it and exit the loop.
////            parameters.setPreviewSize(previewSize.width, previewSize.height);
////            break;
//        }
//
//        mCamera.setParameters(parameters);
////        mCamera.startPreview();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreviewBase(this, mCamera);

        camerPreview = (FrameLayout) findViewById(R.id.camera_video);
        //set the priview size according to video size
        ViewGroup.LayoutParams cameraHolderParams = camerPreview.getLayoutParams();
//        Log.e("tamar", "Setting height of " + previewHeight+" width="+previewWidth);
        Log.e("tamar", "width11111="+cameraHolderParams.width);
        cameraHolderParams.width = fCharacter.getWidth();
        Log.e("tamar", "width22222="+cameraHolderParams.width);
        cameraHolderParams.height = fCharacter.getHeight();
        Log.e("tamar", "height11111="+cameraHolderParams.height);
        camerPreview.setLayoutParams(cameraHolderParams);

        camerPreview.addView(mPreview);

    }


    @Override
    public void onResponseFail(RequestType requestType, VolleyError error, Object extra) {
//        switch (requestType) {
//            case ARTIST:
//                if(followBtn!=null) {
//                    followBtn.setVisibility(View.INVISIBLE);
//                }
                Log.e(TAG, "in onResponseFail:error getting character data=" + error.getMessage());
//                break;
//
//            case SONG_DETAILS:
//                FLog.e(TAG, "in onResponseFail:error getting Song data for artist=" + error.getMessage());
//                break;
//        }
    }


    private void callGetCharacterApi(String charId){
        Log.e(TAG, "in callGetCharacterApi");
        Map<String, String> params = new HashMap<>();
        VRequest request =  new VRequest<>(Request.Method.GET, "https://fuseit-video-terminal-alt.appspot.com/api/character/"+charId, RequestType.GET_CHAR, this, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e(TAG, "Error on calling get character api. message=" + error.getMessage());
                    onResponseFail(RequestType.GET_CHAR, error, null);
                }
            }
            });

        request.setResponseType(VRequest.ResponseType.GSON);
        request.setResponseClass(Character.class);
        request.setContentType("application/json");

//        params.put(ARTIST_PARAM_KEY, Long.toString(artistId));
        if(request != null) {
            RequestManager.getInstance().performRequest(request);
        } else {
            Log.e(TAG, "callCharApi: request is null");
        }



//            get-character: route to retrieve a single character by its
//            cURL:
//            curl --location --request GET 'https://fuseit-video-terminal-alt.appspot.com/api/character/<CHAR_SEQUENTIAL_ID>' \
//            --header 'Authorization: Token a70f6d1f700e316061e90a417eb1ac2bba786c44' \
//            --header 'Content-Type: application/json'
    }

    public void callStitchApi(String charId) throws IOException {

        Log.e("tamar", "**************in addPost currentOutputFile="+currentOutputFile);
            final byte[] videoBytes = FileUtils.readFileToByteArray(new File(currentOutputFile));
            if(videoBytes!=null){
                Log.e("tamar", "videoBytes is not null!!!");
            } else {
                Log.e("tamar", "videoBytes is NULL!!!");
            }

        String POST_URL =  "https://fuseit-video-terminal-alt.appspot.com/api/stitch-character/"+charId;
//        loading = ProgressDialog.show(this, "Posting", "Please wait...", false, false);
        animateLoadingScreen(true);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, POST_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
//                loading.dismiss();
                if(response!=null) {

                    String resultResponse = new String(response.data);
//                    String videPath = ((Character)resultResponse).getVideo_path());
                    Log.e("tamar", "========response.statusCode="+response.statusCode);
                    Log.e("tamar", "========resultResponse="+resultResponse);


                    Object parsedResponse = StructUtils.getGson().fromJson(resultResponse, StitchedVideo.class);
                    if(parsedResponse!=null) {
                        Log.e("tamar", "=======response vid path=" + parsedResponse.getClass());
                        String videoPath = ((StitchedVideo) parsedResponse).getPath();
                        Log.e("tamar", "=======response vid path=" + videoPath);
                        downloadStitchedVid(videoPath);
                    } else {
                        Log.e("tamar", "===parsedResponse is Null");
                    }
                    }

                }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                loading.dismiss();
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");
                        Log.e("tamar", "Error Status="+status);
                        Log.e("tamar", "Error Message="+message);
                        Log.i("tamar", "networkResponse.statusCode:"+networkResponse.statusCode);
//                        if (networkResponse.statusCode == 404) {
////                            Toast.makeText(PostVideo.this, message + "", Toast.LENGTH_SHORT).show();
//                            errorMessage = "Resource not found";
//                        } else if (networkResponse.statusCode == 401) {
////                            Toast.makeText(PostVideo.this, message + "", Toast.LENGTH_SHORT).show();
//                            errorMessage = message + " Please login again";
//                        } else if (networkResponse.statusCode == 400) {
////                            Toast.makeText(PostVideo.this, message + "", Toast.LENGTH_SHORT).show();
//                            errorMessage = message + " Check your inputs";
//                        } else if (networkResponse.statusCode == 500) {
////                            Toast.makeText(PostVideo.this, message + "", Toast.LENGTH_SHORT).show();
//                            errorMessage = message + " Something is getting wrong";
//                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.e("tamar","======================= in getHeaders!!!");
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Token a70f6d1f700e316061e90a417eb1ac2bba786c44");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
//                params.put("user_id", user_id);
//                params.put("cat_id", selected_cat_id);
//                params.put("sub_cat_id", selected_sub_cat_id);
//                params.put("video_type", "3");
//                params.put("video_title", et_video_title.getText().toString());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Log.e("tamar", "**************in getByteData videoName="+videoName);
                Map<String, DataPart> params = new HashMap<>();
                params.put("video", new DataPart(videoName, videoBytes));
//                params.put("video", new DataPart(videoName, videoBytes, "video/mp4"));
                //            params.put("video", new DataPart("file_cover2.mp4", videoBytes, "video/mp4"));

                //    params.put("thumb_img", new DataPart("file_cover3.jpg", thumbBytes, "image/jpeg"));
                return params;
            }
        };
//        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
        if(multipartRequest != null) {
            RequestManager.getInstance().performRequest(multipartRequest);
        } else {
            Log.e("tamar", "callSitchApi: request is null");
        }
    }

    /* permissions related code */
    @TargetApi(23)
    private boolean permissionsAllowed() {
        Log.e("tamar", "in permissionsAllowed");
        missingPermissionsList.clear();
        boolean permissionsAllowed = true;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 23) {
            return true;
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            missingPermissionsList.add(Manifest.permission.CAMERA);
            permissionsAllowed = false;
        } else {
            SharedPrefsManager.getInstance(getApplicationContext()).setPermissionAllowed(Manifest.permission.CAMERA);
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            missingPermissionsList.add(Manifest.permission.RECORD_AUDIO);
            permissionsAllowed = false;
        } else {
            SharedPrefsManager.getInstance(getApplicationContext()).setPermissionAllowed(Manifest.permission.RECORD_AUDIO);
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            missingPermissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionsAllowed = false;
        } else {
            SharedPrefsManager.getInstance(getApplicationContext()).setPermissionAllowed(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(missingPermissionsList.size() > 0) {
            if(SharedPrefsManager.getInstance(getApplicationContext()).isPermissionNeverDetected()) {
                showPermissionsExplainScreen(true);
            } else {
                showPermissionsExplainScreen(false);
            }

        }
        Log.e("tamar", "in permissionsAllowed missingPermissionsList size="+missingPermissionsList.size()+ "   permissionsAllowed="+permissionsAllowed);
        return permissionsAllowed;
    }


    private void setGrantedPermsInUI(Dialog showPermissionsNeededDialog) {
        TextView cameraCheck = (TextView)showPermissionsNeededDialog.findViewById(R.id.camera_permission);
        TextView storageCheck = (TextView)showPermissionsNeededDialog.findViewById(R.id.storage_permission);
        TextView micCheck = (TextView)showPermissionsNeededDialog.findViewById(R.id.mic_permission);
        TextView grantBtn = (TextView)showPermissionsNeededDialog.findViewById(R.id.grant_permissions);

        if (missingPermissionsList.contains(Manifest.permission.CAMERA)) {
            cameraCheck.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
        } else {
            cameraCheck.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        if (missingPermissionsList.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            storageCheck.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
        } else {
            storageCheck.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        if (missingPermissionsList.contains(Manifest.permission.RECORD_AUDIO)) {

            micCheck.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
        } else {
            micCheck.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }


    public void handlePermissionsNextMove(View view) {
        TextView grantTv = (TextView)view;
        if(showPermissionsNeededDialog!=null) {
            showPermissionsNeededDialog.dismiss();
            showPermissionsNeededDialog=null;
        }
      //  showPermissionsNeededDialog.cancel();
        String openSettings = getResources().getString(R.string.open_settings);
        String btnText = grantTv.getText().toString();
        if (btnText.equalsIgnoreCase(openSettings)) {
            openSettingsScreen();
        } else {
            composeRequestForNeededPermissions();
        }
    }


    private void openSettingsScreen() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTINGS_RC);
    }


    @TargetApi(23)
    private void composeRequestForNeededPermissions() {
        if (missingPermissionsList.size() > 0) { //one or more permissions needed
            for(String permission : missingPermissionsList) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    addToNeededPermsList(permission);
                } else { // "dont ask me again" or permission disabled for device
                    SharedPrefsManager sharedPrefMngr = SharedPrefsManager.getInstance(getApplicationContext());
                    if(sharedPrefMngr.isFirstPermissionResponse(permission)) {
                        addToNeededPermsList(permission);
                        sharedPrefMngr.setFirstPermissionResponse(permission);
                    } else {
                        permissionsNeverGrantedList.add(permission); //TODO:SAVE TO shared prefs
                        sharedPrefMngr.setPermissionNeverDetected();
                    }
                }
            }

            if(permissionsRequestNeededList.size() > 0 && permissionsNeverGrantedList.size() == 0) {  //todo: check this - tamar
                requestNeededPermission(permissionsRequestNeededList);
            }
            else {
                finish();
            }
        }
    }


    private void addToNeededPermsList(String permissionToAdd) {
        if (!permissionsRequestNeededList.contains(permissionToAdd)) {
            permissionsRequestNeededList.add(permissionToAdd);
        }
    }


    @TargetApi(23)
    private void requestNeededPermission(List<String> listPermissionsNeeded){
        String[] permissionsArray = listPermissionsNeeded.toArray(new String[0]);
        requestPermissions(permissionsArray, MY_MULTIPLE_PERMISSIONS_REQUEST);
    }


    private void handlePermissionGranted(String permission) {
        permissionsRequestNeededList.remove(permission);
        missingPermissionsList.remove(permission);
        SharedPrefsManager.getInstance(getApplicationContext()).setPermissionAllowed(permission);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_MULTIPLE_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    int length = grantResults.length;
                    for(int i=0;i<length; i++) {
                        String permission = permissions[i];
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            handlePermissionGranted(permission);
                        } else {
                            boolean isNever = checkIfNever(permission);
                            if(isNever) {
                                Log.e("tamar", "in never");
                                SharedPrefsManager.getInstance(getApplicationContext()).setPermissionNeverDetected();
                            }
                        }
                    }

                    if (checkAllNeededPermissionsAllowed()) {
                        if(showPermissionsNeededDialog!=null) {
                            showPermissionsNeededDialog.dismiss();
                            showPermissionsNeededDialog=null;
                        }
                        initScreenAndIntent();
                    } else {
                        String pers = "";
                        for (String per : missingPermissionsList) {
                            pers = pers + per + ",";
                        }
                        //show permission screen again
                        permissionsAllowed();

                    }
                } else { // permission denied
                    if(missingPermissionsList.size() > 0) {
                        Toast.makeText(this, "Sorry Without permissions you can't Continue", Toast.LENGTH_LONG).show();
                        finish();
                    } else if (permissionsNeverGrantedList.size() > 0) {
                        finish();
                    }
                }
                return;
            }
        }
    }

    @TargetApi(23)
    private boolean checkIfNever(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
        } else { // "dont ask me again" or permission disabled for device
            SharedPrefsManager sharedPrefMngr = SharedPrefsManager.getInstance(getApplicationContext());
            if(!sharedPrefMngr.isFirstPermissionResponse(permission)) {
                return true; ///ITS NEVERRRRRRRR!!!!!!!!
            }
        }
        return false;
    }

    private boolean checkAllNeededPermissionsAllowed() {
        Log.e("tamar", "in checkAllNeededPermissionsAllowed");
        SharedPrefsManager sharedPrefMngr = SharedPrefsManager.getInstance(getApplicationContext());
        boolean cameraAllowed = sharedPrefMngr.isPermissionAllowed(Manifest.permission.CAMERA);
        boolean phoneAllowed = sharedPrefMngr.isPermissionAllowed(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean micAllowed = sharedPrefMngr.isPermissionAllowed(Manifest.permission.RECORD_AUDIO);
        if(cameraAllowed && phoneAllowed && micAllowed) {
            Log.e("tamar", "in checkAllNeededPermissionsAllowed return true");
            return true;
        } else {
            return false;
        }
    }


    private void showPermissionsExplainScreen(boolean never) {
        showPermissionsNeededDialog=null;
        showPermissionsNeededDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        showPermissionsNeededDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        showPermissionsNeededDialog.setContentView(R.layout.permissions_screen);

        setGrantedPermsInUI(showPermissionsNeededDialog);
        TextView grantTv = (TextView)showPermissionsNeededDialog.findViewById(R.id.grant_permissions);
//        Button nextBtn= (Button)showPermissionsNeededDialog.findViewById(R.id.perms_button);
//        nextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handlePermissionsNextMove(v);
//            }
//        });
        String permissionsNeeded = "test222 test222 test222";
        if(never) {
            grantTv.setText(getResources().getString(R.string.open_settings));
//  tamar          permissionsNeeded = getResources().getString(R.string.permissions_needed_never_pressed);
//            titleTv.setText(permissionsNeeded);
//            nextBtn.setText(getResources().getString(R.string.open_settings));
        } else {
            grantTv.setText(getResources().getString(R.string.grant_permissions));
            if (SharedPrefsManager.getInstance(getApplicationContext()).isFirstTimeExplainShown()) {
                SharedPrefsManager.getInstance(getApplicationContext()).setFirstTimeExplainShown();
//                permissionsNeeded = getResources().getString(R.string.permissions_needed);
            } else {
//                permissionsNeeded = getResources().getString(R.string.permissions_still_needed);
            }
//            titleTv.setText(permissionsNeeded);    //or permissions_still_needed
//            nextBtn.setText(getResources().getString(R.string.grant_permissions));
        }

        Window window = showPermissionsNeededDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        showPermissionsNeededDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        showPermissionsNeededDialog.show();
    }

    private void animateLoadingScreen(boolean showLoadingScreen) {
        Log.e("tamar", "^^^^^^^^in animateLoadingScreen showLoadingScreen="+showLoadingScreen);
//        Transition transition = new Fade();
//        Transition transition = new Slide(Gravity.BOTTOM);
        Transition transition = new CircularRevealTransition();
        transition.setDuration(600);
        transition.addTarget(R.id.loading_screen);

        TransitionManager.beginDelayedTransition(rootView, transition);
        loadingScreen.setVisibility(showLoadingScreen ? View.VISIBLE : View.GONE);
//        loadingScreen.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("tamar", "^^^^^^^^in surfaceCreated");
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(vidHolder);
//            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
//            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("tamar", "^^^^^^^^in surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("tamar", "^^^^^^^^in surfaceDestroyed");
    }
}
