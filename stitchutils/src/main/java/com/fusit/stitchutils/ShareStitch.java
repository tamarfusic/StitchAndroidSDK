package com.fusit.stitchutils;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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


public class ShareStitch extends AppCompatActivity {

    public static final String RECORDER_FILE_PATH = "RECORDER_FILE_PATH";
    public static final String RECORDER_FILE_LENGTH = "RECORDER_FILE_LENGTH";
    private File stitchedFile;
    private File ShareableStitchedFile;
    public static File vidsDirectory;
    private VideoView videoView;
    private double clipLength;
    private boolean showInstagramShare;
    private boolean showFacebookShare;
    private boolean showTiktokShare;
    private boolean showMoreShare;
    private ImageView shareTiktok, shareFacebook, shareInstagram, shareMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_stitch);
        initVideoDirectory();
        shareTiktok = (ImageView)findViewById(R.id.share_tiktok);
        shareFacebook = (ImageView)findViewById(R.id.share_fb);
        shareInstagram = (ImageView)findViewById(R.id.share_instagram);
        shareMore = (ImageView)findViewById(R.id.share_more);
        handleIntent();
    }

    private void playVideo(String fileToPlay) {
        videoView = (VideoView)findViewById(R.id.stitched_video_preview);
//        videoView = (VideoView)findViewById(R.id.video_preview);
//        toggleVideoView(true);
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
//        Uri videoToPlay = Uri.parse("https:" + url);
        videoView.setMediaController(mediaController);
//        videoView.setVideoURI(videoToPlay);

        videoView.setVideoURI(Uri.parse(fileToPlay));


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
//                toggleVideoView(false);
            }
        });
        videoView.start();
    }

    private void initVideoDirectory() {
        vidsDirectory = new File(getFilesDir()+ File.separator+ "vids");
        vidsDirectory.mkdirs();
//        String test_grannyVidsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "test_fuse.it";
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(RECORDER_FILE_PATH)) {
            String recordedFilePath = intent.getStringExtra(RECORDER_FILE_PATH);
            stitchedFile = new File(recordedFilePath);
            clipLength = intent.getDoubleExtra(RECORDER_FILE_LENGTH, 0);
            Log.e("tamar", "-----clipLength="+clipLength+"-------");
            playVideo(recordedFilePath);
            String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
            ShareableStitchedFile = new File(vidsDirectory.getAbsolutePath() + File.separator + "video_"+ timestamp +".mp4");
            copyFile(stitchedFile, ShareableStitchedFile);

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
        onBackPressed();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("tamar", "in onBackPressed set result OK-----");
        if (getParent() == null) {
            Log.e("tamar", "in onBackPressed set result OK----- 1");
            setResult(Activity.RESULT_OK);
        }
        else {
            Log.e("tamar", "in onBackPressed set result OK----- 2");
            getParent().setResult(Activity.RESULT_OK);
        }
//        setResult(RESULT_OK);
        finish();
    }
}
