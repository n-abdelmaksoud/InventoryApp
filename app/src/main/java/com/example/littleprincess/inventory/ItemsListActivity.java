package com.example.littleprincess.inventory;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littleprincess.inventory.utils.PermissionUtils;
import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;

import static com.example.littleprincess.inventory.CategoryListActivity.INTENT_ACTIVITY_STATE_TAG;
import static com.example.littleprincess.inventory.CategoryListActivity.INTENT_SELECTED_ITEM_INFO;

/**
 * Created by Little Princess on 1/2/2018.
 */

public class ItemsListActivity extends AppCompatActivity implements RecyclerViewCustomAdapter.RecyclerViewItemListener, LoaderManager.LoaderCallbacks<Cursor> {

    static final String INTENT_PARENT_CATEGORY_ID = "parent category id";
    static final String SAVE_PARENT_CATEGORY_ID = "save parent activity id";
    static final int CURSOR_LOADER = 17;
    static final int ACTIVITY_STATE_ADD = 1;
    static final int ACTIVITY_STATE_EDIT = 2;
    private static final String SAVE_ACTIVITY_LABEL = "save activity label";
    private static final String TAG = ItemsListActivity.class.getSimpleName();
    private static final int mCurrentActivityState = ProductsEntry.STATE_ITEM;
    RecyclerView recyclerView;
    View emptyView;
    FloatingActionButton floatingActionButton;
    RecyclerViewCustomAdapter recyclerViewAdapter;
    ImageView imageButton;
    TextView textView;
    ProgressBar progressBar;
    private String activityLabel;
    private long mParentCategoryID;
    private View.OnClickListener addButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startAddItemActivity();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] tag = getIntent().getStringArrayExtra(INTENT_SELECTED_ITEM_INFO);
        if (tag != null && tag.length != 0) {
            activityLabel = tag[0];
            mParentCategoryID = Long.parseLong(tag[1]);
            setTitle(activityLabel);
        }

        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.rc_empty_view);
        floatingActionButton = findViewById(R.id.floating_button);
        imageButton = findViewById(R.id.image);
        textView = findViewById(R.id.text_view_empty);
        progressBar = findViewById(R.id.progress_bar);


        textView.setText(R.string.add_items_activity);
        imageButton.setOnClickListener(addButtonListener);
        imageButton.setImageResource(R.drawable.document_add);
        floatingActionButton.setOnClickListener(addButtonListener);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new RecyclerViewCustomAdapter(this, mCurrentActivityState);
        recyclerView.setAdapter(recyclerViewAdapter);

        setEmptyViewVisibility();

        if (PermissionUtils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            startCursorLoader();
        }

    }


    void setEmptyViewVisibility() {
        progressBar.setVisibility(View.GONE);
        if (recyclerViewAdapter.getItemCount() == 0) {
            floatingActionButton.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);
        }
    }


    private void startAddItemActivity() {
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra(INTENT_PARENT_CATEGORY_ID, mParentCategoryID);
        intent.putExtra(INTENT_ACTIVITY_STATE_TAG, ACTIVITY_STATE_ADD);
        startActivity(intent);
    }

    private void startEditItemActivity(String[] tag) {
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra(INTENT_SELECTED_ITEM_INFO, tag);
        intent.putExtra(INTENT_ACTIVITY_STATE_TAG, ACTIVITY_STATE_EDIT);
        startActivity(intent);
    }


    void startCursorLoader() {
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        imageButton.setVisibility(View.GONE);
        getLoaderManager().restartLoader(CURSOR_LOADER, null, this);
    }

    @Override
    public void onItemClick(String[] tag) {
        startEditItemActivity(tag);
    }

    @Override
    public void onItemLongClick(String[] tag) {
        //may be used later for something else
        startEditItemActivity(tag);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_ACTIVITY_LABEL, activityLabel);
        outState.putLong(SAVE_PARENT_CATEGORY_ID, mParentCategoryID);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        String[] projection = new String[]{ProductsEntry._ID, ProductsEntry.COLUMN_IMAGE
                , ProductsEntry.COLUMN_NAME
                , ProductsEntry.COLUMN_SELL_PRICE
                , ProductsEntry.COLUMN_SALE_PERCENTAGE
                , ProductsEntry.COLUMN_IN_STOCK};

        String selection = ProductsEntry.COLUMN_PARENT_CATEGORY_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(mParentCategoryID)};
        Uri uri = ProductsEntry.PRODUCTS_CONTENT_URI;
        String order = ProductsEntry._ID + " ASC";
        return new CursorLoader(this, uri, projection, selection, selectionArgs, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        recyclerViewAdapter.activityCursorLoaderFinished(data, mCurrentActivityState);
        progressBar.setVisibility(View.GONE);
        setEmptyViewVisibility();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerViewAdapter.activityCursorLoaderReset(mCurrentActivityState);
        setEmptyViewVisibility();
    }

}
