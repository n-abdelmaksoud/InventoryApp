package com.example.littleprincess.inventory;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littleprincess.inventory.utils.PermissionUtils;
import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;

public class CategoryListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewCustomAdapter.RecyclerViewItemListener {

    static final String INTENT_SELECTED_ITEM_INFO = "item  tag ";
    static final String PROJECTION = "projection";
    static final String SELECTION = "selection";
    static final String SELECTION_ARG = "selection args";
    static final int CURSOR_LOADER = 7;
    static final String INTENT_ACTIVITY_STATE_TAG = "category activity type tag";
    static final int ACTIVITY_STATE_ADD = 1;
    static final int ACTIVITY_STATE_EDIT = 2;
    private static final String TAG = CategoryListActivity.class.getSimpleName();
    private static final int mCurrentActivityState = ProductsEntry.STATE_CATEGORY;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView startTextView;

    FloatingActionButton floatingActionButton;
    RecyclerViewCustomAdapter recyclerViewAdapter;
    ImageView imageButton;
    private View.OnClickListener addButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startAddCategoryActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        floatingActionButton = findViewById(R.id.floating_button);
        imageButton = findViewById(R.id.image);
        progressBar = findViewById(R.id.progress_bar);
        startTextView = findViewById(R.id.text_view_empty);
        startTextView.setText(R.string.add_categories_activity);


        imageButton.setOnClickListener(addButtonListener);
        imageButton.setImageResource(R.drawable.matt_icons_folder_add);
        floatingActionButton.setOnClickListener(addButtonListener);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.hasFixedSize();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewAdapter = new RecyclerViewCustomAdapter(this, mCurrentActivityState);
        recyclerView.setAdapter(recyclerViewAdapter);

        setEmptyViewVisibility();

        if (PermissionUtils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            startCursorLoader();
        }
    }


    void startCursorLoader() {

        progressBar.setVisibility(View.VISIBLE);
        imageButton.setVisibility(View.GONE);
        startTextView.setVisibility(View.GONE);
        Log.i(TAG, "start cursor loader ");
        String[] projection = new String[]{ProductsEntry._ID, ProductsEntry.COLUMN_IMAGE, ProductsEntry.COLUMN_NAME};
        String selection = ProductsEntry.COLUMN_TYPE + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ProductsEntry.STATE_CATEGORY)};
        Bundle loaderBundle = new Bundle();
        loaderBundle.putStringArray(PROJECTION, projection);
        loaderBundle.putString(SELECTION, selection);
        loaderBundle.putStringArray(SELECTION_ARG, selectionArgs);
        getLoaderManager().restartLoader(CURSOR_LOADER, loaderBundle, this);
    }

    void setEmptyViewVisibility() {
        progressBar.setVisibility(View.GONE);
        if (recyclerViewAdapter.getItemCount() == 0) {
            floatingActionButton.setVisibility(View.INVISIBLE);
            imageButton.setVisibility(View.VISIBLE);
            startTextView.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.GONE);
            startTextView.setVisibility(View.GONE);
        }
    }

    private void startAddCategoryActivity() {
        Intent intent = new Intent(this, CategoryDetailsActivity.class);
        intent.putExtra(INTENT_ACTIVITY_STATE_TAG, ACTIVITY_STATE_ADD);
        startActivity(intent);
    }

    private void startEditCategoryActivity(String[] tag) {
        Intent intent = new Intent(this, CategoryDetailsActivity.class);
        intent.putExtra(INTENT_SELECTED_ITEM_INFO, tag);
        intent.putExtra(INTENT_ACTIVITY_STATE_TAG, ACTIVITY_STATE_EDIT);
        startActivity(intent);
    }

    private void startItemsOfCategoryActivity(String[] tag) {
        Intent intent = new Intent(this, ItemsListActivity.class);
        intent.putExtra(INTENT_SELECTED_ITEM_INFO, tag);
        startActivity(intent);
    }

    @Override
    public void onItemClick(String[] tag) {
        startItemsOfCategoryActivity(tag);
    }

    @Override
    public void onItemLongClick(String[] tag) {
        startEditCategoryActivity(tag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG, "onRequestPermission called permission: " + permissions[0] + grantResults[0]);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                    startCursorLoader();
                    break;
            }
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            switch (requestCode) {
                case PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                    Toast.makeText(this, "Unable to upload data from device, Access Denied", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ProductsEntry.PRODUCTS_CONTENT_URI;
        String order = ProductsEntry._ID + " ASC";
        return new CursorLoader(this, uri, args.getStringArray(PROJECTION), args.getString(SELECTION), args.getStringArray(SELECTION_ARG), order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished");
        progressBar.setVisibility(View.GONE);
        recyclerViewAdapter.activityCursorLoaderFinished(data, mCurrentActivityState);
        setEmptyViewVisibility();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerViewAdapter.activityCursorLoaderReset(mCurrentActivityState);
        setEmptyViewVisibility();
    }

}