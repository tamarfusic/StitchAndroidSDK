package com.fusit.stitchutils.managers;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tamarraviv on 9/29/15.
 */
public class SharedPrefsManager {

    private static final String PERSISTENT_FILE_NAME = "green_hornet_persistent_data";
    private final static String ACCOUNT_PERMISSION = "accounts_permission";
    private final static String STORAGE_PERMISSION = "storage_permission";
    private final static String NEVER_PERMISSION = "never_permission";
    private final static String MULTIPLE_ACCOUNT_PERMISSIONS = "multiple_account_permissions";
    private final static String MULTIPLE_PERMISSIONS_ALLOWED = "multiple_permissions_allowed";
    private final static String PERMISSION_EXPLAIN_SHOWN = "permissions_explain_shown";
    private final static String NEVER_RATE_US = "never_rate_us";
    private static final String APP_RATED = "app_rated";
    private static final String FUSE_SHARED = "fuse_shared";
    private static final String RATEUS_SHOWN = "rateus_shown";
    private static final String SHARED_TIKTOK = "shared_tiktok";
    private static final String SHARED_OTHER = "shared_other";
    private static final String AR_WARN = "ar_warn";
    private Dialog loadingDialog;


    private static SharedPrefsManager sharedPrefManager;
    private SharedPreferences sharedPref;

    private SharedPrefsManager(Context context) {
        sharedPref = context.getSharedPreferences(PERSISTENT_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPrefsManager getInstance(Context context) {
        if(sharedPrefManager == null) {
            sharedPrefManager = new SharedPrefsManager(context);

        }
        return sharedPrefManager;
    }


    public boolean isFirstAccountPermissionResponse() {
        return !sharedPref.contains(ACCOUNT_PERMISSION);
    }

    public void setFirstAccountPermissionResponse() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(ACCOUNT_PERMISSION, true);
        editor.apply();
    }

    public boolean shouldShowRateUs() {
        if(sharedPref.contains(NEVER_RATE_US)) {
            return false;
        }
        if(isAppRated()) {
            return false;
        }
        return true;
    }

    public void setNeverShowRateUs() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(NEVER_RATE_US, true);
        editor.apply();
    }

    public boolean isFirstStoragePermissionResponse() {
        return !sharedPref.contains(STORAGE_PERMISSION);
    }

    public void setFirstStoragePermissionResponse() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(STORAGE_PERMISSION, true);
        editor.apply();
    }

    public void setFirstPermissionResponse(String permission) {
        Set<String> permissionsSet = sharedPref.getStringSet(MULTIPLE_ACCOUNT_PERMISSIONS, null);
        if(permissionsSet==null) {
            permissionsSet = new HashSet<>();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        permissionsSet.add(permission);
        editor.putStringSet(MULTIPLE_ACCOUNT_PERMISSIONS, permissionsSet);
        editor.apply();
    }

    public boolean isFirstPermissionResponse(String permission) {
        Set<String> permissionsAllowedList = sharedPref.getStringSet(MULTIPLE_ACCOUNT_PERMISSIONS, null);
        if (permissionsAllowedList!=null) {
            return !permissionsAllowedList.contains(permission);
        } else {
            return true;
        }
    }

    public void setPermissionAllowed(String permission) {
        Set<String> permissionsSet = sharedPref.getStringSet(MULTIPLE_PERMISSIONS_ALLOWED, null);
        if(permissionsSet==null) {
            permissionsSet = new HashSet<>();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        permissionsSet.add(permission);
        editor.putStringSet(MULTIPLE_PERMISSIONS_ALLOWED, permissionsSet);
        editor.apply();
    }

    public boolean isPermissionAllowed(String permission) {
        Set<String> permissionsAllowedList = sharedPref.getStringSet(MULTIPLE_PERMISSIONS_ALLOWED, null);
        if(permissionsAllowedList != null) {
            return permissionsAllowedList.contains(permission);
        }
        return false;
    }

    public void setPermissionNeverDetected() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(NEVER_PERMISSION, true);
        editor.apply();
    }

    public boolean isPermissionNeverDetected() {
        return sharedPref.contains(NEVER_PERMISSION);
    }

    public boolean isFirstTimeExplainShown() {
        return !sharedPref.contains(PERMISSION_EXPLAIN_SHOWN);
    }

    public void setFirstTimeExplainShown() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PERMISSION_EXPLAIN_SHOWN, true);
        editor.apply();
    }

    public void setLoadingDialog(Dialog loadingDialog) {
        this.loadingDialog = loadingDialog;
    }

    public Dialog getLoadingDialog() {
        return loadingDialog;
    }

    public void setAppRated() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(APP_RATED, true);
        editor.apply();
    }

    public boolean isAppRated() {
        return sharedPref.getBoolean(APP_RATED, false);
    }

    public void setFuseShared() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FUSE_SHARED, true);
        editor.apply();
    }

    public boolean hasFuseShared() {
        return sharedPref.getBoolean(FUSE_SHARED, false);
    }


    public void setRateusShownInSession(String recordedFilePath) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(RATEUS_SHOWN, recordedFilePath);
        editor.apply();
    }

    public boolean hasShownRateusInSession(String recordedFilePath) {
        String result = sharedPref.getString(RATEUS_SHOWN, "");
        return result.equals(recordedFilePath);
    }

    public void setTiktokShareClicked(String recordedFilePath) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_TIKTOK, recordedFilePath);
        editor.apply();
    }

    public boolean wasTiktokShareClicked(String recordedFilePath) {
        String result = sharedPref.getString(SHARED_TIKTOK, "");
        return result.equals(recordedFilePath);
    }

    public void removeTiktokShareClicked() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(SHARED_TIKTOK);
        editor.apply();
    }

    public void setOtherShareClicked(String recordedFilePath) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_OTHER, recordedFilePath);
        editor.apply();
    }

    public boolean wasOtherShareClicked(String recordedFilePath) {
        String result = sharedPref.getString(SHARED_OTHER, "");
        return result.equals(recordedFilePath);
    }

    public void removeOtherShareClicked() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(SHARED_OTHER);
        editor.apply();
    }

    public void setARwarningShown() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AR_WARN, true);
        editor.apply();
    }

    public boolean wasARwarningShown() {
        return sharedPref.getBoolean(AR_WARN, false);
    }
}