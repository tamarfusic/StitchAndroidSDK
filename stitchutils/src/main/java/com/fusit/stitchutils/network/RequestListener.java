package com.fusit.stitchutils.network;

import com.android.volley.VolleyError;

import java.util.EventListener;

/**
 * Created by tamarraviv.
 */
public interface RequestListener extends EventListener {

    public <T> void onResponseSuccess(RequestType requestType, T object, Object extra);

    public void onResponseFail(RequestType requestType, VolleyError error, Object extra);
}
