package com.fusit.stitchutils.utils;

import com.fusit.stitchutils.R;
import com.fusit.stitchutils.StitchApp;

/**
 * Created by tamarraviv on 11/10/15.
 */
public enum AlertDialogType {
    ERROR_MAINTENANCE("Maintenance", true),
    ERROR_CONNECTIVITY(StitchApp.getAppContext().getString(R.string.connectivity_alert_title), true),
    ERROR_LOGIN(StitchApp.getAppContext().getString(R.string.login_alert_title), true),
    ERROR_SETTINGS(StitchApp.getAppContext().getString(R.string.settings_alert_title), true),
    ERROR_FUSE_NOT_EXIST(StitchApp.getAppContext().getString(R.string.fuse_not_found), true),
    RECORDING_INTERRUPT(StitchApp.getAppContext().getString(R.string.recording_interrupted), false),
    MESSAGE_RATE_THIS_APP("Rate This App", false);
//    ERROR_PHONE_PERMISSION(StitchApp.getAppContext().getString(R.string.phone_permission_required), false),
//    ERROR_STORAGE_PERMISSION(StitchApp.getAppContext().getString(R.string.permission_required), false),
//    ERROR_LOGIN_PERMISSION(StitchApp.getAppContext().getString(R.string.permission_required), false);


    private final String displayName;
    private final boolean isError;


    AlertDialogType(String displayName, boolean isError) {
        this.displayName = displayName;
        this.isError = isError;
    }


    public String getDisplayName() {
        return displayName;
    }

    public boolean isError() {
        return isError;
    }

}
