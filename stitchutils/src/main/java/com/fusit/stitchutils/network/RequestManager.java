package com.fusit.stitchutils.network;

import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.fusit.stitchutils.StitchApp;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Created by tamarraviv.
 */
public class RequestManager<T> {

    private static final String TAG = RequestManager.class.getSimpleName();
    private final RequestQueue requestQueue;
    private static RequestManager instance;
    private ImageLoader imageLoader;
//    private LruBitmapCache bitmapImageCache;

    private RequestManager() {
//        CookieHandler.setDefault(new CookieManager(new PersistentHttpCookieStore(StitchApp.getAppContext()), CookiePolicy.ACCEPT_ALL));
        this.requestQueue = Volley.newRequestQueue(StitchApp.getAppContext());
//        createImageLoader();
    }

    public static void initRequestManager() {
        if (instance == null){
            instance = new RequestManager();
        }
    }

    public static RequestManager getInstance() {
        if (instance == null){
            initRequestManager();
        }
        return instance;
    }

    public void performRequest(VRequest<T> request){
        Log.e("tamar", "adding req  to Q");
        requestQueue.add(request);
    }

    public void performRequest(VolleyMultipartRequest request){
        Log.e("tamar", "adding VolleyMultipartRequest req  to Q");
        requestQueue.add(request);
    }

    public void cancelAllRequests(String tag)
    {
        if (requestQueue != null)
        {
            requestQueue.cancelAll(tag);
        }
    }


//    public ImageLoader getImageLoader () {
//        if(imageLoader == null){
//            createImageLoader();
//        }
//        return imageLoader;
//    }

//    private void createImageLoader() {
//        imageLoader = new ImageLoader(requestQueue, getVolleyImageCache());
////        ImageLoader.ImageCache() {
////            private final LruCache<String, Bitmap> imageCache = new LruCache<String, Bitmap>(10);
////            @Override
////            public Bitmap getBitmap(String url) {
////                return imageCache.get(url);
////            }
////
////            @Override
////            public void putBitmap(String url, Bitmap bitmap) {
////                imageCache.put(url, bitmap);
////            }
////        });
//    }


    //todo: add a cache for image loader
//    private LruBitmapCache getVolleyImageCache()
//    {
//        if (bitmapImageCache == null)
//        {
////            int cacheSize = 24 * 1024 * 1024; // 24MiB
//            bitmapImageCache = new LruBitmapCache();
//        }
//        return bitmapImageCache;
//    }

    public void removeFromCache(String key) {
        Cache cache = requestQueue.getCache();
        cache.remove(key);
    }
}
