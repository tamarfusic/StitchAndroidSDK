package com.fusit.stitchutils.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tamarraviv.
 */
public class OKHttpFileDownloader extends AsyncTask<Void, Long, Boolean> {

    private static final String TAG = OKHttpFileDownloader.class.getSimpleName();
    private final String url;
    private final File downloadTo;
    private final OkHttpClient client;
    private final RateLimiter rateLimiter;
    private final ProgressListener progressListener;
    private FileOutputStream outStream = null;
    private String tag = null;

    public interface ProgressListener{
        public void onProgress(int percents);
        public void onCompleted();
        public void onFailed();

    }

    public OKHttpFileDownloader(OkHttpClient client, String url, File downloadTo, ProgressListener listener){
        this.client = client;
        this.url = url;
        this.downloadTo = downloadTo;
        this.progressListener = listener;
        this.rateLimiter = RateLimiter.create(25);
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        OkHttpClient httpClient = client;
        try {
            outStream = new FileOutputStream(downloadTo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "FileNotFoundException e=" + e.getMessage());
            return false;
        }

        Request.Builder builder = new Request.Builder().url(url).get();
        if(tag!=null){
            Log.i(TAG, "Starting download with tag " + tag);
            builder.tag(tag);
        }


        Call call = httpClient.newCall(builder.build());
        try {
            Response response = call.execute();
            if (response.code() == 200) {
                InputStream inputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    byte[] buff = new byte[1024 * 4];
                    long downloaded = 0;
                    long target = response.body().contentLength();

                    publishProgress(0L, target);
                    while (true) {
                        int readed = inputStream.read(buff);
                        if(readed == -1){
                            break;
                        }
                        //write buff
                        outStream.write(buff,0,readed);
                        downloaded += readed;
                        if(rateLimiter.tryAcquire()){
                            publishProgress(downloaded, target);
                        }
                        if (isCancelled()) {
                            Log.e(TAG, "isCancelled");
                            return false;
                        }
                    }
                    return downloaded == target;
                } catch (IOException ignore) {
                    Log.e(TAG, "IOException ignore=" + ignore.getMessage());
                    return false;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } else {
                Log.e(TAG, "response code=" + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to download file=" + e.getMessage());
//            FLog.e(TAG, "failed to download file=" + e.getCause().toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        int percents = (int) ((((float) values[0].intValue()) / ((float) values[1].intValue())) * 100.0);
        progressListener.onProgress(percents);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(outStream!=null){
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(result){
            progressListener.onCompleted();
        }else{
            progressListener.onFailed();
        }
    }
}