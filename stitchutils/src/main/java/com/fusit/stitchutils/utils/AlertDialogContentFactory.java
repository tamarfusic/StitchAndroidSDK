package com.fusit.stitchutils.utils;

import android.content.Context;

import com.fusit.stitchutils.R;

/**
 * Created by tamarraviv on 11/10/15.
 */
public class AlertDialogContentFactory {

    public static AlertDialogContent createDialogContent(Context context, AlertDialogType dialogType){
        AlertDialogContent dialogContent;

        switch (dialogType){
//            case ERROR_MAINTENANCE:
//                dialogContent = initAlertDialogContent(context.getString(R.string.maintenance_error_title),
//                        context.getString(R.string.maintenance_error_message),
//                        context.getString(R.string.maintenance_error_exit_label),
//                        null,
//                        null);
//                break;
            case ERROR_CONNECTIVITY:
                dialogContent = initAlertDialogContent(context.getString(R.string.connectivity_error_title),
                        context.getString(R.string.connectivity_error_message),
                        context.getString(R.string.connectivity_error_exit_label),
                        null,
                        null);
                break;
//            case ERROR_SETTINGS:
//                dialogContent = initAlertDialogContent(context.getString(R.string.settings_error_title),
//                        context.getString(R.string.settings_error_message), //todo: change this message
//                        context.getString(R.string.settings_error_retry_label),
//                        null,
//                        context.getString(R.string.dialog_cancel_label));
//                break;
//            case ERROR_LOGIN:
//                dialogContent = initAlertDialogContent(context.getString(R.string.login_error_title),
//                        context.getString(R.string.login_error_message),
//                        context.getString(R.string.login_error_ok_label),
//                        null,
//                        null);
//                break;
            case RECORDING_INTERRUPT:
                dialogContent = initAlertDialogContent(context.getString(R.string.recording_interrupt_dialog_title),
                        context.getString(R.string.recording_interrupt_dialog_message),
                        context.getString(R.string.recording_interrupt_dialog_cancel_label),
                        null,
                        context.getString(R.string.recording_interrupt_dialog_ok_label));
                break;
//            case ERROR_FUSE_NOT_EXIST:
//                dialogContent = initAlertDialogContent(context.getString(R.string.fuse_not_found_error_title),
//                        context.getString(R.string.fuse_not_found_error_message),
//                        context.getString(R.string.fuse_not_found_error_ok_label),
//                        null,
//                        null);
//                break;
//            case ERROR_LOGIN_PERMISSION:
//                dialogContent = initAlertDialogContent(context.getString(R.string.login_permission_title),
//                        context.getString(R.string.login_permission_message),
//                        context.getString(R.string.login_permission_ok_label),
//                        null,
//                        null);
//                break;
//            case ERROR_PHONE_PERMISSION:
//                dialogContent = initAlertDialogContent(context.getString(R.string.phone_permission_title),
//                        context.getString(R.string.phone_permission_message),
//                        context.getString(R.string.phone_permission_ok_label),
//                        null,
//                        null);
//                break;
            default:
                return null;
        }

        return dialogContent;
    }

    private static AlertDialogContent initAlertDialogContent(String title, String message, String positiveLabel, String neutralLabel, String negativeLabel){
        AlertDialogContent dialogContent = new AlertDialogContent();
        dialogContent.setTitle(title);
        dialogContent.setMessage(message);
        dialogContent.setPositiveLabel(positiveLabel);
        dialogContent.setNeutralLabel(neutralLabel);
        dialogContent.setNegativeLabel(negativeLabel);
        return dialogContent;
    }


}
