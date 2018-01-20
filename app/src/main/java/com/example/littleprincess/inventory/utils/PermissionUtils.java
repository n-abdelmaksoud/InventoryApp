package com.example.littleprincess.inventory.utils;


import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.littleprincess.inventory.R;


/**
 * Created by Little Princess on 12/27/2017.
 */


public class PermissionUtils {


    public static final int REQUEST_CAMERA_PERMISSIONS = 18;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 19;

    private static final String TAG = PermissionUtils.class.getSimpleName();

    private PermissionUtils() {

    }

    public static boolean hasPermission(Activity activity, String permission, int code) {
        if (PermissionUtils.hasSelfPermission(activity, permission)) {
            return true;
        } else {
            PermissionUtils.askForPermission(activity, permission, code);
            return false;
        }
    }

    private static boolean hasSelfPermission(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED;
        }
    }

    private static void askForPermission(Activity activity, String permission, Integer requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            displayPermissionInfoDialog(activity,permission, requestCode);

        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    private static void displayPermissionInfoDialog(final Activity activity,final String permission,final int requestCode) {

        String message="";
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                message=activity.getString(R.string.dialog_permission_camera);
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                message=activity.getString(R.string.dialog_permission_write_external_storage);
                break;
        }


        AlertDialog mAlertDialog = new AlertDialog.Builder(activity).create();

        mAlertDialog.setMessage(message);
        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                activity.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                        if(dialog!=null)
                            dialog.dismiss();
                    }
                });
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                activity.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog!=null)
                            dialog.dismiss();
                    }
                });
        mAlertDialog.show();
    }

}