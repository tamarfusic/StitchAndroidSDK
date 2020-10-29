package com.fusit.stitchutils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.fusit.stitchutils.utils.ShareUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ShareStitch extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String ACTIVITY_ORIENTATION = "ACTIVITY_ORIENTATION";
    public static final String RECORDER_FILE_PATH = "RECORDER_FILE_PATH";
    public static final String RECORDER_FILE_LENGTH = "RECORDER_FILE_LENGTH";
    public static final String SHOULD_EXIT_LIBRARY = "SHOULD_EXIT_LIBRARY";
    private File stitchedFile;
    private File ShareableStitchedFile;
    public static File vidsDirectory;
    private SurfaceView vidSurface;
    private SurfaceHolder vidHolder;
    private double clipLength;
    private boolean showInstagramShare;
    private boolean showFacebookShare;
    private boolean showTiktokShare;
    private boolean showMoreShare;
//    private File fileToPlay;
    private MediaPlayer mediaPlayer;
    private ImageView shareTiktok, shareFacebook, shareInstagram, shareMore, playBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("tamar", "-----in share oncreate=");
        initVideoDirectory();
        setContentView(R.layout.activity_share_stitch);
        handleIntent();
        vidSurface = (SurfaceView)findViewById(R.id.stitched_video_preview);
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);
        playBtn = (ImageView)findViewById(R.id.play_btn);


    }

    private void playVideo(String fileToPlay) {
        Log.e("tamar", "-----in share playVideo=");
//        vidSurface = (SurfaceView)findViewById(R.id.stitched_video_preview);
//        vidHolder = vidSurface.getHolder();
//        vidHolder.addCallback(this);

//        this.fileToPlay = fileToPlay;
        try {
            mediaPlayer.setDataSource(fileToPlay);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        videoView = (VideoView)findViewById(R.id.video_preview);
//        toggleVideoView(true);
//        MediaController mediaController=new MediaController(this);
//        mediaController.setAnchorView(videoView);
////        Uri videoToPlay = Uri.parse("https:" + url);
//        videoView.setMediaController(mediaController);
////        videoView.setVideoURI(videoToPlay);
//
//        videoView.setVideoURI(Uri.parse(fileToPlay));
//
//
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
////                toggleVideoView(false);
//            }
//        });
//        videoView.start();
    }

    private void initVideoDirectory() {
        vidsDirectory = new File(getFilesDir()+ File.separator+ "vids");
        vidsDirectory.mkdirs();
//        String test_grannyVidsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "test_fuse.it";
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(RECORDER_FILE_PATH)) {
            setActivityOrientation(intent.getStringExtra(ACTIVITY_ORIENTATION));
            String recordedFilePath = intent.getStringExtra(RECORDER_FILE_PATH);
            stitchedFile = new File(recordedFilePath);
            clipLength = intent.getDoubleExtra(RECORDER_FILE_LENGTH, 0);
            Log.e("tamar", "-----clipLength="+clipLength+"-------");
//            playVideo(recordedFilePath);
            String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
            ShareableStitchedFile = new File(vidsDirectory.getAbsolutePath() + File.separator + "video_"+ timestamp +".mp4");
            copyFile(stitchedFile, ShareableStitchedFile);
            shareTiktok = (ImageView)findViewById(R.id.share_tiktok);
            shareFacebook = (ImageView)findViewById(R.id.share_fb);
            shareInstagram = (ImageView)findViewById(R.id.share_instagram);
            shareMore = (ImageView)findViewById(R.id.share_more);
            showInstagramShare = intent.getBooleanExtra(StitchActivity.SHARE_TO_INSTAGRAM, false);
            if(showInstagramShare) {
                shareInstagram.setVisibility(View.VISIBLE);
            } else {
                shareInstagram.setVisibility(View.GONE);
            }
            showFacebookShare = intent.getBooleanExtra(StitchActivity.SHARE_TO_FACEBOOK, false);
            if(showFacebookShare){
                shareFacebook.setVisibility(View.VISIBLE);
            } else {
                shareFacebook.setVisibility(View.GONE);
            }
            showMoreShare = intent.getBooleanExtra(StitchActivity.SHARE_TO_MORE, false);
            if(showMoreShare){
                shareMore.setVisibility(View.VISIBLE);
            } else {
                shareMore.setVisibility(View.GONE);
            }
            showTiktokShare = intent.getBooleanExtra(StitchActivity.SHARE_TO_TIKTOK, false);
            if(showTiktokShare){
                shareTiktok.setVisibility(View.VISIBLE);
            } else {
                shareTiktok.setVisibility(View.GONE);
            }
        }//stitchedFile = "/storage/emulated/0/Download/fuse.it/withwm_story.mp4";
    }

    private void setActivityOrientation(String orientation) {
        Log.e("tamar", "in setActivityOrientation!!!s orientation="+orientation);
        if(orientation.equalsIgnoreCase("horizontal")) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }
    }

    private void copyFile(File fromLocation, File toLocation) {
        try{
            InputStream in = new FileInputStream(fromLocation);
            OutputStream out = new FileOutputStream(toLocation);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            Log.e("fusit", "Error coping file:" + e.getMessage());
        } catch (IOException e) {
            Log.e("fusit", "Error coping file:" + e.getMessage());
        } catch (Exception e) {
            Log.e("fusit", "Error coping file:" + e.getMessage());
        }
    }

    public void onShareClicked(View view) {
        String shareTo = "";
        int id = view.getId();
        if (id == R.id.share_instagram) {
            shareTo = "instagram";
            if (ShareableStitchedFile != null && ShareableStitchedFile.exists()) {
                ShareUtils.shareToInstagram(this, ShareableStitchedFile);
            }
        } else if (id == R.id.share_fb) {
            shareTo = "facebook";
            if (ShareableStitchedFile != null && ShareableStitchedFile.exists()) {
                ShareUtils.shareToFacebook(this, ShareableStitchedFile);
            }
        } else if (id == R.id.share_more) {
            shareTo = "other";
            if (ShareableStitchedFile != null && ShareableStitchedFile.exists()) {
                ShareUtils.shareToOther(this, ShareableStitchedFile);
            }
        } else if (id == R.id.share_tiktok) {
            shareTo = "tiktok";
            if (ShareableStitchedFile != null && ShareableStitchedFile.exists()) {
                ShareUtils.shareToTikTok(this, ShareableStitchedFile, 5.0f);
            }
        }
//        LogAnalytics.gifuseShared(DataManager.getInstance().getGifDataItem(), shareTo);
    }

    public void onBackClicked(View view) {
        Intent data = new Intent();
        data.putExtra(SHOULD_EXIT_LIBRARY,false);
        setResult(Activity.RESULT_OK, data);
        finish();
//        finishAndRemoveTask();
    }

    public void onCloseClicked(View view) {
        Intent data = new Intent();
        data.putExtra(SHOULD_EXIT_LIBRARY,true);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        onCloseClicked(null);
//        super.onBackPressed();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(vidHolder);
            mediaPlayer.setDataSource(stitchedFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        mp.setLooping(true);
        mp.start();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer=null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playBtn.setVisibility(View.VISIBLE);
    }

    public void onPlayClicked(View view) {
        playBtn.setVisibility(View.INVISIBLE);
        mediaPlayer.start();
    }
}
