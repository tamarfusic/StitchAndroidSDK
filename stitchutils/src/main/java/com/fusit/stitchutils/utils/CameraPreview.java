package com.fusit.stitchutils.utils;

import android.view.View;

/**
 * Created by tamarraviv.
 */
public interface CameraPreview {
    View getView();

    interface OnSurfaceCreated{
        public void onSurfaceCreated(Size previewSize);
        void onErrorStartingPreview();
    }
}
