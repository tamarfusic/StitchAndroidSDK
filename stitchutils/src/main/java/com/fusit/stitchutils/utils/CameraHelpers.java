package com.fusit.stitchutils.utils;

import android.hardware.Camera;
import android.util.Log;


import java.util.List;

/**
 * Created by tamarraviv.
 */
public class CameraHelpers {
    private static final String LOGGER = "CameraHelpers";

    public static Camera.Size pickBestSize(List<Camera.Size> sizes, int width, int height) {
        return pickBestSize(sizes,width,height,false);
    }

    public static Camera.Size pickBestSize(List<Camera.Size> sizes, int width, int height, Boolean forceAspectRate) {
        Camera.Size bestMatch = sizes.get(0);
        boolean foundAspectRated = false;

        float requiredRatio = (float) width / (float) height;
        for(Camera.Size size:sizes){
            Log.d(LOGGER, String.format("Available size: %dx%d", size.width, size.height));
            if(forceAspectRate){
                float ratio = (float) size.width / (float) size.height;
                float ratiosRatio = requiredRatio / ratio;
                if(Math.abs(ratiosRatio - 1) > 0.05){
                    Log.d(LOGGER, String.format("Declined based on aspect rate. Required: %s Ration: %s Ratiotoration:%s", requiredRatio, ratio, ratiosRatio));
                    continue;
                }
                Log.d(LOGGER, String.format("Accepted based on aspect rate. Required: %s Ration: %s Ratiotoration:%s", requiredRatio, ratio, ratiosRatio));
                if(!foundAspectRated){
                    bestMatch = size;
                    foundAspectRated = true;
                    continue;
                }
            }



            if(size.width < bestMatch.width && size.width >= width){
                if(size.height >= height){
                    bestMatch = size;
                }
            }else{//In case list received in reverse order will try to go up
                if(bestMatch.width < width || bestMatch.height < height){
                    if(size.width > bestMatch.width && size.height > bestMatch.height){
                        bestMatch = size;
                    }
                }
            }
        }


        if(forceAspectRate){
            if((bestMatch.width < Math.min(width / 2, 640)) || !foundAspectRated){
                /**
                 * If selected width is too small - fallback to non wide
                 */
                Log.i(LOGGER,"Falling back to not aspect-rate dependant size");
                return pickBestSize(sizes,width,height,false);
            }else{
                Log.i(LOGGER,"Have good aspect rate-dependant size");
                Log.i(LOGGER, String.format("Selected %dx%d as best match. Requests %dx%d", bestMatch.width, bestMatch.height, width, height));
                return bestMatch;
            }
        }

        Log.i(LOGGER, String.format("Selected %dx%d as best match. Requests %dx%d", bestMatch.width, bestMatch.height, width, height));
        return bestMatch;
    }
}
