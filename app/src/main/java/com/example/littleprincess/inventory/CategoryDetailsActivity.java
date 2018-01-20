package com.example.littleprincess.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.littleprincess.inventory.utils.PermissionUtils;
import com.example.littleprincess.inventory.utils.PhotoUtils;
import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;
import com.squareup.picasso.Picasso;

import java.io.File;

public class CategoryDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    static final int CATEGORY_LOADER = 38;
    static final String DATA_COMPLETED_TAG = "data completed tag";
    static final String FILE_PATH_TAG = "new image file path ";
    static final String IMAGE_URI_TAG = "image uri";
    static final String BACK_ACTION_DIALOG_DISPLAYING_TAG = "the dialog is displayed for back-action";
    static final String DELETE_ACTION_DIALOG_DISPLAYING_TAG = "the dialog is displayed for delete action";
    static final String CATEGORY_ACTIVITY_STATE_TAG = "category activity type tag";
    static final String OLD_CATEGORY_ID_TAG = "old categoty id tag";
    private static final String TAG = CategoryDetailsActivity.class.getName();
    boolean isCategoryDataInputCompleted = false;
    ImageView imageView;
    EditText editTextName;
    View moreDetailsLayout;
    Uri imageUri;
    File imageFile;
    String title;
    boolean isBackActionDialogDisplaying = false;
    boolean isDeleteActionDialogDisplaying = false;
    String activityTitle = "";
    long itemId;
    int mCurrentActivityState = CategoryListActivity.ACTIVITY_STATE_ADD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        initializeViews();

        setViewsVisibility();

        if (savedInstanceState != null) {
            isCategoryDataInputCompleted = savedInstanceState.getBoolean(DATA_COMPLETED_TAG);

            if (savedInstanceState.containsKey(BACK_ACTION_DIALOG_DISPLAYING_TAG)) {
                isBackActionDialogDisplaying = savedInstanceState.getBoolean(BACK_ACTION_DIALOG_DISPLAYING_TAG);
            }

            if (savedInstanceState.containsKey(DELETE_ACTION_DIALOG_DISPLAYING_TAG)) {
                isDeleteActionDialogDisplaying = savedInstanceState.getBoolean(DELETE_ACTION_DIALOG_DISPLAYING_TAG);
            }
            if (savedInstanceState.containsKey(IMAGE_URI_TAG)) {
                imageUri = Uri.parse(savedInstanceState.getString(IMAGE_URI_TAG));
            }
            if (savedInstanceState.containsKey(FILE_PATH_TAG)) {
                String path = savedInstanceState.getString(FILE_PATH_TAG);
                if (path != null)
                    imageFile = new File(path);
            }
        }

        updateImageViewPhoto(imageUri);

        if (isBackActionDialogDisplaying) {
            showConfirmDialogBackAction();
        }

        if (isDeleteActionDialogDisplaying) {
            showConfirmDialogDeleteAction();
        }
        registerForContextMenu(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.showContextMenu();
            }
        });

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkIsCategoryDataCompleted();
            }
        });

        setCurrentActivityState();

        if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_EDIT) {
            setCategoryOldInfo();
        }
        setActivityLabel();

        checkIsCategoryDataCompleted();
    }

    void initializeViews() {
        imageView = findViewById(R.id.add_category_image);
        editTextName = findViewById(R.id.add_category_title_edit_text);
        moreDetailsLayout = findViewById(R.id.items_details_layout);
    }

    void setViewsVisibility() {
        moreDetailsLayout.setVisibility(View.GONE);
    }

    void setCurrentActivityState() {
        mCurrentActivityState = getIntent().getIntExtra(CATEGORY_ACTIVITY_STATE_TAG, 0);
    }

    void setActivityLabel() {
        switch(mCurrentActivityState){
            case CategoryListActivity.ACTIVITY_STATE_ADD:
                setTitle(R.string.add_category_activity_string);
                break;
            case CategoryListActivity.ACTIVITY_STATE_EDIT:
                setTitle(getString(R.string.edit_category_activity_string) + " " + activityTitle);
                break;
        }
    }

    void setCategoryOldInfo() {
        if (imageUri == null) {
            String[] tag = getIntent().getStringArrayExtra(CategoryListActivity.INTENT_SELECTED_ITEM_INFO);
            itemId = Long.parseLong(tag[1]);
            activityTitle = tag[0];
            if (itemId > 0) {
                startCursorLoader();
            }
        }

    }

    void startCursorLoader() {
        String[] projection = new String[]{ProductsEntry._ID, ProductsEntry.COLUMN_IMAGE, ProductsEntry.COLUMN_NAME};
        Bundle args = new Bundle();
        args.putStringArray(CategoryListActivity.PROJECTION, projection);
        args.putLong(OLD_CATEGORY_ID_TAG, itemId);
        getLoaderManager().restartLoader(CATEGORY_LOADER, args, this);

    }


    void deleteCurrentItemFromDB(long id) {
        Uri uri = ProductsEntry.PRODUCTS_CONTENT_URI;
        String selection = ProductsEntry._ID + " =? OR " + ProductsEntry.COLUMN_PARENT_CATEGORY_ID + " =?";
        String[] selectionArg = new String[]{String.valueOf(id), String.valueOf(id)};
        int rowsDeleted = getContentResolver().delete(uri, selection, selectionArg);
        Log.i(TAG, "number of deleted rows=" + rowsDeleted);
        showToastDeleteResult(rowsDeleted);
    }

    void showToastDeleteResult(int i) {
        if (i > 0)
            Toast.makeText(this, R.string.toast_category_deleted_successfully, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, R.string.toast_category_deleted_failed, Toast.LENGTH_LONG).show();
    }


    void checkIsCategoryDataCompleted() {
        title = editTextName.getText().toString().trim();
        if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_ADD && imageUri != null && !TextUtils.isEmpty(title)) {
            isCategoryDataInputCompleted = true;
            invalidateOptionsMenu();
        } else if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_EDIT && !TextUtils.isEmpty(title)) {
            if (imageUri != null || !title.equals(activityTitle)) {
                isCategoryDataInputCompleted = true;
                invalidateOptionsMenu();
            }
        }else{
            isCategoryDataInputCompleted =false;
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.photo_menu,menu);
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload_photo_pop_up_menu:
                uploadPhotoFromDevice();
                return true;
            case R.id.take_photo_pop_up_menu:
                takePhotoByCamera();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    private void uploadPhotoFromDevice() {
        if (PermissionUtils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            PhotoUtils.startSelectPhotoFromDeviceIntent(this);
        }
    }


    private void takePhotoByCamera() {
        imageFile = PhotoUtils.createImageFile(this);
        if (imageFile != null) {
            if (PermissionUtils.hasPermission(this, Manifest.permission.CAMERA, PermissionUtils.REQUEST_CAMERA_PERMISSIONS)) {
                PhotoUtils.dispatchTakePictureIntent(this, imageFile);
            }
        }
    }

    void saveCategoryInfoOnDatabase() {

        Uri uri = ProductsEntry.PRODUCTS_CONTENT_URI;
        ContentValues values = new ContentValues();

        switch (mCurrentActivityState){
            case CategoryListActivity.ACTIVITY_STATE_ADD:
                values.put(ProductsEntry.COLUMN_NAME,title);
                values.put(ProductsEntry.COLUMN_IMAGE,imageUri.toString());
                values.put(ProductsEntry.COLUMN_TYPE, ProductsEntry.STATE_CATEGORY);
                Uri rowUri=getContentResolver().insert(uri,values);
                if(rowUri!=null) {
                    long rowId = Long.parseLong(rowUri.getLastPathSegment());
                    if (rowId != -1) {
                        Toast.makeText(this, R.string.toast_new_category_added_successfully, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.toast_new_category_add_failed, Toast.LENGTH_LONG).show();

                    }
                }
                break;

            case CategoryListActivity.ACTIVITY_STATE_EDIT:
                Uri updatedItemUri = Uri.withAppendedPath(uri, String.valueOf(itemId));
                values.put(ProductsEntry.COLUMN_NAME, title);
                if (imageUri != null) {
                    values.put(ProductsEntry.COLUMN_IMAGE, imageUri.toString());
                }
                int updatedRows = getContentResolver().update(updatedItemUri, values, null, null);
                if (updatedRows != 0) {
                    Toast.makeText(this, R.string.toast_category_updated_successfully, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.toast_category_updated_failed, Toast.LENGTH_LONG).show();
                }
        }

    }

    private void showConfirmDialogBackAction() {
         AlertDialog.Builder builder=new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setMessage(getString(R.string.dialog_msg_back_action))
                .setPositiveButton(getString(R.string.dialog_pos_button_back_action), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           if(dialog!=null)
                               dialog.dismiss();
                        }
                }).setNegativeButton(getString(R.string.dialog_neg_button_back_action), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(dialog!=null)
                                dialog.dismiss();
                            NavUtils.navigateUpFromSameTask(CategoryDetailsActivity.this);
                        }
                    }).setCancelable(true)
                    .create();
            dialog.show();
        isBackActionDialogDisplaying = dialog.isShowing();
    }

    private void showConfirmDialogDeleteAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setMessage(getString(R.string.dialog_msg_delete_action))
                .setPositiveButton(getString(R.string.dialog_pos_button_delete_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                        deleteCurrentItemFromDB(itemId);
                        NavUtils.navigateUpFromSameTask(CategoryDetailsActivity.this);
                    }
                }).setNegativeButton(getString(R.string.dialog_neg_button_delete_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                }).setCancelable(true)
                .create();
        dialog.show();
        isDeleteActionDialogDisplaying = dialog.isShowing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PhotoUtils.INTENT_SELECT_PHOTO_CODE) {
            imageUri = data.getData();
        } else if (resultCode == RESULT_OK && requestCode == PhotoUtils.INTENT_TAKE_PHOTO_CODE) {
            Log.i(TAG, "camera intent result is ok");
            // This code line solved a big issue on my App
            //take care which uri you use and how to get it.
            imageUri = Uri.fromFile(imageFile);
        }
        updateImageViewPhoto(imageUri);
        checkIsCategoryDataCompleted();
    }


    void updateImageViewPhoto(Uri uri) {
        if (uri != null) {
            Picasso.with(this).load(uri).placeholder(R.drawable.loading_placeholder).error(R.drawable.unavailable).resizeDimen(R.dimen.add_category_image_size, R.dimen.add_category_image_size).onlyScaleDown().centerCrop().into(imageView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DATA_COMPLETED_TAG, isCategoryDataInputCompleted);
        if (isBackActionDialogDisplaying) {
            outState.putBoolean(BACK_ACTION_DIALOG_DISPLAYING_TAG, isBackActionDialogDisplaying);
        }
        if (isDeleteActionDialogDisplaying) {
            outState.putBoolean(DELETE_ACTION_DIALOG_DISPLAYING_TAG, isDeleteActionDialogDisplaying);
        }
        if(imageUri!=null)
            outState.putString(IMAGE_URI_TAG, imageUri.toString());
        if(imageFile!=null){
            outState.putString(FILE_PATH_TAG,imageFile.getPath());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem doneMenuItem = menu.findItem(R.id.menu_action_add);
        doneMenuItem.setEnabled(isCategoryDataInputCompleted);
        if(doneMenuItem.isEnabled())
            doneMenuItem.setIcon(R.drawable.ic_check_white_24dp);
        else
            doneMenuItem.setIcon(R.drawable.icons_checkmark_gray24);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case android.R.id.home:
                if(isCategoryDataInputCompleted)
                    showConfirmDialogBackAction();
                else
                    NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_action_add:
                saveCategoryInfoOnDatabase();
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_action_delete:
                showConfirmDialogDeleteAction();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG,"onRequestPermission called permission: "+permissions[0]+ grantResults[0]);

        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                case PermissionUtils.REQUEST_CAMERA_PERMISSIONS:
                    PhotoUtils.dispatchTakePictureIntent(this, imageFile);
                    break;
                case PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                    if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_ADD)
                        PhotoUtils.startSelectPhotoFromDeviceIntent(this);
                    break;
            }
        } else if(grantResults[0]==PackageManager.PERMISSION_DENIED){
            switch (requestCode) {
                case PermissionUtils.REQUEST_CAMERA_PERMISSIONS:
                    Toast.makeText(this,"Unable to upload take photo, Camera Access Denied",Toast.LENGTH_LONG).show();
                    break;
                case PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                    Toast.makeText(this,"Unable to upload photo from device, Access Denied",Toast.LENGTH_LONG).show();
                    break;
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onBackPressed() {
        if(isCategoryDataInputCompleted)
            showConfirmDialogBackAction();
        else
            NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Long categoryID = args.getLong(OLD_CATEGORY_ID_TAG);
        Uri uri = Uri.withAppendedPath(ProductsEntry.PRODUCTS_CONTENT_URI, String.valueOf(categoryID));
        return new CursorLoader(this, uri, args.getStringArray(CategoryListActivity.PROJECTION), null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            activityTitle = data.getString(data.getColumnIndex(ProductsEntry.COLUMN_NAME));
            editTextName.setText(activityTitle);
            Uri oldImageUri = Uri.parse(data.getString(data.getColumnIndex(ProductsEntry.COLUMN_IMAGE)));
            updateImageViewPhoto(oldImageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
