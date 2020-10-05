package com.fusit.stitchutils.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bytedance.sdk.open.aweme.TikTokOpenApiFactory;
import com.bytedance.sdk.open.aweme.api.TiktokOpenApi;
import com.bytedance.sdk.open.aweme.base.TikTokMediaContent;
import com.bytedance.sdk.open.aweme.base.TikTokVideoObject;
import com.bytedance.sdk.open.aweme.share.Share;
import com.fusit.stitchutils.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tamarraviv.
 */
public class ShareUtils {

    private static final int INSTAGRAM_REQUEST_CODE = 117;
    private static final int FB_MESSENGER_REQUEST_CODE = 118;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE = 21;
    private static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    private static final String TIKTOK_PACKAGE = "com.zhiliaoapp.musically";
    private static final String FB_APP_PACKAGE = "com.facebook.katana";
    private static final String TAG = ShareUtils.class.getSimpleName();


    private static Uri getFileUri(Activity parentActivity, File videoToSharePath){
        if (videoToSharePath != null && videoToSharePath.exists()) {
            try {
                final String provider = parentActivity.getResources().getString(R.string.file_provider);
                Uri uri = FileProvider.getUriForFile(
                        parentActivity,
                        provider,
                        videoToSharePath);
                return uri;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }


    public static void shareToInstagram(Activity parentActivity, File videoToSharePath) {
        if(isAppInstalled(parentActivity, INSTAGRAM_PACKAGE)) {
            String type = "video/*";
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(type);
            Uri uri = getFileUri(parentActivity, videoToSharePath);
            if(uri != null) {
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.setPackage(INSTAGRAM_PACKAGE);
                parentActivity.startActivityForResult(share, INSTAGRAM_REQUEST_CODE);
//            }
//            String type = "video/mp4";
//        Uri storyUri = getFileUri(parentActivity, videoToSharePath);
//        if(storyUri != null) {
//            Intent storiesIntent = new Intent("com.instagram.share.ADD_TO_STORY");
//            storiesIntent.setDataAndType(storyUri, type);
//            storiesIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            parentActivity.grantUriPermission("com.instagram.android", storyUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
////            parentActivity.instaChooser.setVisibility(View.GONE);
//            parentActivity.startActivityForResult(storiesIntent, INSTAGRAM_REQUEST_CODE);
        } else {
                Log.e("tamar", "insta URI NULL");
            }

        } else {
            Log.e("tamar", "Instagram Not installed");
            Toast.makeText(parentActivity, "Instagram Not installed", Toast.LENGTH_SHORT).show();
        }
    }

//    public void onInstaStoryClicked(View view) {
//        String type = "video/mp4";
//        Uri storyUri = getFileUri(recordedFile);
//        if(storyUri != null) {
//            Intent storiesIntent = new Intent("com.instagram.share.ADD_TO_STORY");
//            storiesIntent.setDataAndType(storyUri, type);
//            storiesIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            grantUriPermission("com.instagram.android", storyUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            instaChooser.setVisibility(View.GONE);
//            startActivityForResult(storiesIntent, SHARE_REQ_CODE);
//        }
//    }
//
//    public void onInstaFeedClicked(View view) {
//        String type = "video/mp4";
////            String type = "video/*";
//        Uri feedUri;
//        if(unityRecordedFileInstaFeed!=null && unityRecordedFileInstaFeed.exists()) { //specific feed video exists
//            String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
//            recordedFileForInstaFeed = new File(MyPlayerActivity.vidsDirectory.getAbsolutePath() + File.separator + "video_feed_" + timestamp + ".mp4");
//            copyFile(unityRecordedFileInstaFeed, recordedFileForInstaFeed);
//            instaChooser.setVisibility(View.GONE);
//            feedUri = getFileUri(recordedFileForInstaFeed);
//
//        } else {
//            feedUri = getFileUri(recordedFile);
//        }
//
//
//        if(feedUri != null) {
//            Intent feedIntent = new Intent("com.instagram.share.ADD_TO_FEED");
//            feedIntent.setType(type);
//            feedIntent.putExtra(Intent.EXTRA_STREAM, feedUri);
//            feedIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            grantUriPermission("com.instagram.android", feedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            feedIntent.setPackage(INSTAGRAM_PACKAGE);
//            startActivityForResult(feedIntent, SHARE_REQ_CODE);
//
//        }
//    }


    public static void shareToTikTok(Activity parentActivity, File videoToSharePath, float clipLength) {
        //check clip length is greater then 3 sec
        if(clipLength < 3.0)
        {
            Toast.makeText(parentActivity, "Video must be longer then 3 seconds", Toast.LENGTH_SHORT).show();
        } else {

            if (isAppInstalled(parentActivity, TIKTOK_PACKAGE)) {
                File grannyDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + ".fuse.it");
                grannyDirectory.mkdirs();

                String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                File tiktokDownloadFile = new File(grannyDirectory.getAbsoluteFile() + File.separator + "video_" + timestamp + ".mp4");
                String downloadedFilePath = tiktokDownloadFile.getAbsolutePath();
                try {
                    InputStream in = new FileInputStream(videoToSharePath);
                    OutputStream out = new FileOutputStream(tiktokDownloadFile);
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                TiktokOpenApi tiktokOpenApi = TikTokOpenApiFactory.create(parentActivity);
                Share.Request request = new Share.Request();
                ArrayList<String> mUri = new ArrayList<>();
//                request.callerLocalEntry = "com.fusit.fuseitar.ShareVideoActivity";
                request.callerLocalEntry = "com.fusit.stitchutils.ShareStitch";
                mUri.add(downloadedFilePath);
                TikTokVideoObject videoObject = new TikTokVideoObject();
                videoObject.mVideoPaths = mUri;
                TikTokMediaContent content = new TikTokMediaContent();
                content.mMediaObject = videoObject;
                request.mMediaContent = content;
                request.mState = "ss";
                tiktokOpenApi.share(request);
            } else {
                Toast.makeText(parentActivity, "TikTok application is Not installed", Toast.LENGTH_SHORT).show();
            }
        }

    }


    public static void shareToOther(Activity parentActivity, File videoToSharePath) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        Uri uri = getFileUri(parentActivity, videoToSharePath);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        parentActivity.startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    public static void shareToFacebook(Activity parentActivity, File videoToSharePath) {
        if(isAppInstalled(parentActivity, FB_APP_PACKAGE)) {
            String type = "video/*";
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(type);
            Uri uri = getFileUri(parentActivity, videoToSharePath);

            if(uri != null) {
                share.putExtra(Intent.EXTRA_STREAM, uri);
                // Grant temporary read permission to the content URI
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.setPackage(FB_APP_PACKAGE);
                parentActivity.startActivity(share);
            } else {
                Log.e("tamar", "other URI NULL");
            }
        } else {
            Toast.makeText(parentActivity, "Facebook application Not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public static String downloadGifToDevice(Activity parentActivity, File videoToDownload) {
        File gifuseDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ File.separator+ "GiFuse");
        gifuseDirectory.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        File downloadFile = new File(gifuseDirectory.getAbsoluteFile()+ File.separator +  "gifuse_"+ timestamp +".gif");

        try {
            InputStream in = new FileInputStream(videoToDownload);
            OutputStream out = new FileOutputStream(downloadFile);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            Toast.makeText(parentActivity, "Download completed", Toast.LENGTH_SHORT).show();
            return downloadFile.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isAppInstalled(Activity parentActivity, String packageName) {
        PackageManager pm = parentActivity.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "isAppInstalled: "+e.getMessage());
        }

        return false;
    }

}
