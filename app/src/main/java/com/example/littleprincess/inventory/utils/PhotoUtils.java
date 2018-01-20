package com.example.littleprincess.inventory.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Little Princess on 12/10/2017.
 */

public class PhotoUtils {
    public static final int INTENT_TAKE_PHOTO_CODE = 33;
    public static final int INTENT_SELECT_PHOTO_CODE = 22;
    private static final String AUTHORITY_STRING="com.example.littleprincess.inventory.fileprovider";
    private static final String TAG=PhotoUtils.class.getName();

    private PhotoUtils(){

    }


    //A method to create an empty File (will be used later to save the captured photos by the camera)
    public static File createImageFile(Activity activity) {
        File image = null;
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            image = File.createTempFile(imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            Log.e(TAG,"cannot create image file"+e.toString());
        }
        return image;
    }


        //method to start camera intent and save the captured photo in an empty file
        public static void dispatchTakePictureIntent(Activity activity, File image) {
        Log.i(TAG,"started taking phto method");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Continue only if the File was successfully created
            if (image != null) {
                Uri photoURI = FileProvider.getUriForFile(activity, AUTHORITY_STRING,image);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i(TAG,"camera intent will start");
                activity.startActivityForResult(takePictureIntent, INTENT_TAKE_PHOTO_CODE);
            }
        }
    }


    public static void startSelectPhotoFromDeviceIntent(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        if(Build.VERSION.SDK_INT<19){
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
       activity.startActivityForResult(intent, INTENT_SELECT_PHOTO_CODE);
    }

    public static Bitmap getBitmapFromUri(Uri uri, Activity activity) {
        Bitmap bitmap=null;
        try {
            ParcelFileDescriptor parcelFileDescriptor=activity.getContentResolver().openFileDescriptor(uri,"r");
            FileDescriptor fileDescriptor=parcelFileDescriptor.getFileDescriptor();
            bitmap=BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        } catch (IOException| NullPointerException e){
            Toast.makeText(activity,"Image Not Found",Toast.LENGTH_LONG).show();
        }
        return bitmap;
    }

}
