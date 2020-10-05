package com.fusit.stitchutils.network;

import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.SerializedName;

/**
 * Created by tamarraviv.
 */

public class Exclude implements ExclusionStrategy {

    @Override
    public boolean shouldSkipClass(Class<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes field) {
        SerializedName ns = field.getAnnotation(SerializedName.class);
        if(field.getName().equalsIgnoreCase("serialVersionUID")) {
            Log.e("tamar", "skipping field "+field.getName());
            return true;
        } else {
            return false;
        }
    }
}
