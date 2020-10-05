package com.fusit.stitchutils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.bytedance.sdk.open.aweme.TikTokOpenApiFactory;
import com.bytedance.sdk.open.aweme.TikTokOpenConfig;
import com.fusit.stitchutils.network.RequestManager;

public class StitchApp extends Application {
    private static StitchApp instance;
    private static final int DISABLE_TOUCH_DURATION = 400;
    private static boolean touchDisabled = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RequestManager.initRequestManager();
    }

    public static StitchApp getAppInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public static void disableTouchEventForOnClick() {
        touchDisabled = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                touchDisabled = false;
            }
        }, DISABLE_TOUCH_DURATION);
    }

    public static boolean isTouchDisabled() {
        return touchDisabled;
    }

}
