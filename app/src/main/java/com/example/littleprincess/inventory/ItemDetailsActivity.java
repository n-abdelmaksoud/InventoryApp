package com.example.littleprincess.inventory;

import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;

public class ItemDetailsActivity extends CategoryDetailsActivity {


    private static final String TAG = ItemDetailsActivity.class.getSimpleName();
    EditText editTextBuyPrice, editTextSellPrice, editTextSalePercentage;
    EditText editTextDescription, editTextStock, editTextOrderedNumber;
    private long mParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean isSaleValueValid() {
        String saleString = editTextSalePercentage.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(saleString)) {
            int sale = Integer.valueOf(saleString);
            if (sale > 100 || sale < 0) {
                Toast.makeText(ItemDetailsActivity.this, R.string.item_details_sale_not_valid, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    @Override
    void initializeViews() {
        super.initializeViews();
        editTextBuyPrice = findViewById(R.id.item_edit_text_buy_price);
        editTextSellPrice = findViewById(R.id.item_edit_text_sell_price);
        editTextSalePercentage = findViewById(R.id.item_edit_text_sale);
        editTextDescription = findViewById(R.id.item_edit_text_description);
        editTextStock = findViewById(R.id.item_edit_text_stock);
        editTextOrderedNumber = findViewById(R.id.item_edit_text_ordered_no);
    }

    @Override
    void setViewsVisibility() {
        moreDetailsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    void setActivityLabel() {
        switch (mCurrentActivityState) {
            case CategoryListActivity.ACTIVITY_STATE_ADD:
                setTitle(R.string.add_item_string);
                break;
            case CategoryListActivity.ACTIVITY_STATE_EDIT:
                setTitle(getString(R.string.edit_category_activity_string) + " " + activityTitle);
                break;
        }

    }

    @Override
    void setCurrentActivityState() {
        mCurrentActivityState = getIntent().getIntExtra(CategoryListActivity.INTENT_ACTIVITY_STATE_TAG, 0);
        if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_ADD) {
            mParentId = getIntent().getLongExtra(ItemsListActivity.INTENT_PARENT_CATEGORY_ID, -1);
        }

    }

    @Override
    void checkIsCategoryDataCompleted() {
        title = editTextName.getText().toString().trim();
        if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_ADD && imageUri != null && !TextUtils.isEmpty(title)) {
            isCategoryDataInputCompleted = true;
            invalidateOptionsMenu();
        } else if (mCurrentActivityState == CategoryListActivity.ACTIVITY_STATE_EDIT && !TextUtils.isEmpty(title)) {
            isCategoryDataInputCompleted = true;
            invalidateOptionsMenu();
        } else {
            isCategoryDataInputCompleted = false;
            invalidateOptionsMenu();
        }
    }

    @Override
    void deleteCurrentItemFromDB(long id) {
        Uri uri = Uri.withAppendedPath(ProductsEntry.PRODUCTS_CONTENT_URI, String.valueOf(id));
        int rowsDeleted = getContentResolver().delete(uri, null, null);
        showToastDeleteResult(rowsDeleted);
    }

    @Override
    void showToastDeleteResult(int i) {
        if (i > 0)
            Toast.makeText(this, R.string.toast_item_deleted_successfully, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, R.string.toast_item_deleted_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    void startCursorLoader() {
        String[] projection = new String[]{ProductsEntry._ID, ProductsEntry.COLUMN_IMAGE
                , ProductsEntry.COLUMN_NAME, ProductsEntry.COLUMN_DESCRIPTION
                , ProductsEntry.COLUMN_SELL_PRICE, ProductsEntry.COLUMN_BUY_PRICE
                , ProductsEntry.COLUMN_IN_STOCK, ProductsEntry.COLUMN_NO_ORDER_ITEMS
                , ProductsEntry.COLUMN_SALE_PERCENTAGE};

        Bundle args = new Bundle();
        args.putStringArray(CategoryListActivity.PROJECTION, projection);
        args.putLong(OLD_CATEGORY_ID_TAG, itemId);
        getLoaderManager().restartLoader(CATEGORY_LOADER, args, this);

    }


    @Override
    void saveCategoryInfoOnDatabase() {
        Uri uri = ProductsEntry.PRODUCTS_CONTENT_URI;
        ContentValues values = getContentValues();

        if (values == null) {
            return;
        }

        switch (mCurrentActivityState) {
            case CategoryListActivity.ACTIVITY_STATE_ADD:
                values.put(ProductsEntry.COLUMN_TYPE, ProductsEntry.STATE_ITEM);
                values.put(ProductsEntry.COLUMN_PARENT_CATEGORY_ID, mParentId);
                Uri rowUri = getContentResolver().insert(uri, values);
                if (rowUri != null) {
                    long rowId = Long.parseLong(rowUri.getLastPathSegment());
                    if (rowId != -1) {
                        Toast.makeText(this, R.string.toast_new_item_add_successfully, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.toast_new_item_add_failed, Toast.LENGTH_LONG).show();
                    }

                }
                break;
            case CategoryListActivity.ACTIVITY_STATE_EDIT:
                Log.i(TAG, "old category id = " + itemId);
                Uri updatedItemUri = Uri.withAppendedPath(uri, String.valueOf(itemId));
                int updatedRows = getContentResolver().update(updatedItemUri, values, null, null);
                if (updatedRows > 0) {
                    Toast.makeText(this, R.string.toast_item_updated_successfully, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.toast_item_updated_failed, Toast.LENGTH_LONG).show();
                }
        }


    }

    private ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ProductsEntry.COLUMN_NAME, title);

        String saleString = editTextSalePercentage.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(saleString)) {
            int sale = Integer.parseInt(saleString);
            values.put(ProductsEntry.COLUMN_SALE_PERCENTAGE, sale);
        }

        if (imageUri != null) {
            values.put(ProductsEntry.COLUMN_IMAGE, imageUri.toString());
        }
        String stockString = editTextStock.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(stockString)) {
            int stock = Integer.parseInt(stockString);
            values.put(ProductsEntry.COLUMN_IN_STOCK, stock);
        }

        String orderedNumberString = editTextOrderedNumber.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(orderedNumberString)) {
            int ordered = Integer.parseInt(orderedNumberString);
            values.put(ProductsEntry.COLUMN_NO_ORDER_ITEMS, ordered);
        }


        String buyPriceString = editTextBuyPrice.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(buyPriceString)) {
            Double buyPrice = Double.parseDouble(buyPriceString);
            values.put(ProductsEntry.COLUMN_BUY_PRICE, buyPrice);
        }
        String sellPriceString = editTextSellPrice.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(sellPriceString)) {
            Double sellPrice = Double.parseDouble(sellPriceString);
            values.put(ProductsEntry.COLUMN_SELL_PRICE, sellPrice);
        }
        String description = editTextDescription.getEditableText().toString().trim();
        if (!TextUtils.isEmpty(description)) {
            values.put(ProductsEntry.COLUMN_DESCRIPTION, description);
        }
        return values;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            activityTitle = data.getString(data.getColumnIndex(ProductsEntry.COLUMN_NAME));
            editTextName.setText(activityTitle);
            Uri oldImageUri = Uri.parse(data.getString(data.getColumnIndex(ProductsEntry.COLUMN_IMAGE)));
            updateImageViewPhoto(oldImageUri);

            Double sellPrice = data.getDouble(data.getColumnIndex(ProductsEntry.COLUMN_SELL_PRICE));
            Double buyPrice = data.getDouble(data.getColumnIndex(ProductsEntry.COLUMN_BUY_PRICE));
            Integer stock = data.getInt(data.getColumnIndex(ProductsEntry.COLUMN_IN_STOCK));
            Integer orderedNumber = data.getInt(data.getColumnIndex(ProductsEntry.COLUMN_NO_ORDER_ITEMS));
            Integer sale = data.getInt(data.getColumnIndex(ProductsEntry.COLUMN_SALE_PERCENTAGE));
            String description = data.getString(data.getColumnIndex(ProductsEntry.COLUMN_DESCRIPTION));

            if (sellPrice != null)
                editTextSellPrice.setText(String.valueOf(sellPrice));
            if (buyPrice != null)
                editTextBuyPrice.setText(String.valueOf(buyPrice));
            if (stock != null)
                editTextStock.setText(String.valueOf(stock));

            if (orderedNumber != null)
                editTextOrderedNumber.setText(String.valueOf(orderedNumber));

            if (sale != null)
                editTextSalePercentage.setText(String.valueOf(sale));

            if (description != null)
                editTextDescription.setText(String.valueOf(description));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_add:
                if (isSaleValueValid()) {
                    saveCategoryInfoOnDatabase();
                    NavUtils.navigateUpFromSameTask(this);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
