package com.example.littleprincess.inventory.data_entry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;

/**
 * Created by Little Princess on 12/7/2017.
 */

public class ProductsProvider extends ContentProvider {

    private static final int PRODUCTS_CODE = 1;
    private static final int PRODUCT_ITEM_CODE = 2;
    private static final String TAG = ProductsProvider.class.getSimpleName();
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, ProductsEntry.PRODUCTS_PATH, PRODUCTS_CODE);
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, ProductsEntry.PRODUCT_ITEM_PATH, PRODUCT_ITEM_CODE);
    }

    DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        Cursor cursor;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        switch (match) {
            case PRODUCTS_CODE:
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ITEM_CODE:
                long id = ContentUris.parseId(uri);
                Log.i(TAG, "long id =" + id);
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                break;

            default:
                throw new IllegalArgumentException("uri is not valid: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.i(TAG, "the content values" + values.toString());
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS_CODE:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("uri is not valid: " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        if (values.size() == 0) {
            return null;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String name = values.getAsString(ProductsEntry.COLUMN_NAME);
        if (name == null || TextUtils.isEmpty(name))
            throw new IllegalArgumentException("you have to enter a COLUMN_NAME");

        String image = values.getAsString(ProductsEntry.COLUMN_IMAGE);
        if (image == null || TextUtils.isEmpty(image))
            throw new IllegalArgumentException("you have to enter an COLUMN_IMAGE url");

        if (values.containsKey(ProductsEntry.COLUMN_PARENT_CATEGORY_ID)) {
            int item_type = values.getAsInteger(ProductsEntry.COLUMN_PARENT_CATEGORY_ID);
            if (item_type < 0)
                throw new IllegalArgumentException("the COLUMN_PARENT_CATEGORY_ID cannot not be negative");
        }


        int category_type = values.getAsInteger(ProductsEntry.COLUMN_TYPE);
        if (!(category_type == ProductsEntry.STATE_CATEGORY || category_type == ProductsEntry.STATE_ITEM))
            throw new IllegalArgumentException("The main type is Invalid, choose STATE_CATEGORY or STATE_ITEM");

        if (values.containsKey(ProductsEntry.COLUMN_IN_STOCK)) {
            int stock = values.getAsInteger(ProductsEntry.COLUMN_IN_STOCK);
            if (stock < 0)
                throw new IllegalArgumentException("the stock no. of items must not be negative");
        }

        if (values.containsKey(ProductsEntry.COLUMN_NO_ORDER_ITEMS)) {
            int order = values.getAsInteger(ProductsEntry.COLUMN_NO_ORDER_ITEMS);
            if (order < 0)
                throw new IllegalArgumentException("the ordered items no. must not be negative");
        }

        if (values.containsKey(ProductsEntry.COLUMN_SELL_PRICE)) {
            int sellPrice = values.getAsInteger(ProductsEntry.COLUMN_SELL_PRICE);
            if (sellPrice < 0)
                throw new IllegalArgumentException("the price must not be negative");
        }
        if (values.containsKey(ProductsEntry.COLUMN_SALE_PERCENTAGE)) {
            int sale = values.getAsInteger(ProductsEntry.COLUMN_SALE_PERCENTAGE);
            if (sale < 0 || sale > 100)
                throw new IllegalArgumentException("the sale percentage must be between 0-100");
        }

        if (values.containsKey(ProductsEntry.COLUMN_BUY_PRICE)) {
            int buyPrice = values.getAsInteger(ProductsEntry.COLUMN_BUY_PRICE);
            if (buyPrice < 0)
                throw new IllegalArgumentException("the price must not be negative");
        }

        long id = database.insert(ProductsEntry.TABLE_NAME, null, values);

        if (id > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (match) {
            case PRODUCTS_CODE:
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ITEM_CODE:
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("uri is invalid; " + uri);
        }
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case PRODUCTS_CODE:
                rowsUpdated = updateProduct(uri, values, selection, selectionArgs);
                break;
            case PRODUCT_ITEM_CODE:
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = updateProduct(uri, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri is invalid; " + uri);
        }

        return rowsUpdated;
    }

    private int updateProduct(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        if (values.containsKey(ProductsEntry.COLUMN_NAME)) {
            String name = values.getAsString(ProductsEntry.COLUMN_NAME);
            if (name == null || TextUtils.isEmpty(name))
                throw new IllegalArgumentException("you have to enter a COLUMN_NAME");
        }


        if (values.containsKey(ProductsEntry.COLUMN_IMAGE)) {
            String image = values.getAsString(ProductsEntry.COLUMN_IMAGE);
            if (image == null || TextUtils.isEmpty(image))
                throw new IllegalArgumentException("you have to enter an COLUMN_IMAGE url");
        }

        if (values.containsKey(ProductsEntry.COLUMN_PARENT_CATEGORY_ID)) {
            throw new IllegalArgumentException("item COLUMN_PARENT_CATEGORY_ID connot be changed");
        }

        if (values.containsKey(ProductsEntry.COLUMN_IN_STOCK)) {
            Integer stock = values.getAsInteger(ProductsEntry.COLUMN_IN_STOCK);
            if (stock < 0)
                throw new IllegalArgumentException("the stock no. of items must not be negative");
        }

        if (values.containsKey(ProductsEntry.COLUMN_NO_ORDER_ITEMS)) {
            int order = values.getAsInteger(ProductsEntry.COLUMN_NO_ORDER_ITEMS);
            if (order < 0)
                throw new IllegalArgumentException("the ordered items no. must not be negative");
        }

        if (values.containsKey(ProductsEntry.COLUMN_SELL_PRICE)) {
            int price = values.getAsInteger(ProductsEntry.COLUMN_SELL_PRICE);
            if (price < 0)
                throw new IllegalArgumentException(" the COLUMN_SELL_PRICE must not be negative");
        }

        if (values.containsKey(ProductsEntry.COLUMN_BUY_PRICE)) {
            int price = values.getAsInteger(ProductsEntry.COLUMN_BUY_PRICE);
            if (price < 0)
                throw new IllegalArgumentException(" the COLUMN_BUY_PRICE must not be negative");
        }
        if (values.containsKey(ProductsEntry.COLUMN_SALE_PERCENTAGE)) {
            int sale = values.getAsInteger(ProductsEntry.COLUMN_SALE_PERCENTAGE);
            if (sale < 0 || sale > 100)
                throw new IllegalArgumentException("the sale percentage must be between 0-100");
        }
        if (values.containsKey(ProductsEntry.COLUMN_TYPE)) {
            Integer category_type = values.getAsInteger(ProductsEntry.COLUMN_TYPE);
            if (category_type == null || category_type != ProductsEntry.STATE_CATEGORY || category_type != ProductsEntry.STATE_ITEM)
                throw new IllegalArgumentException("The main type is Invalid, choose STATE_CATEGORY or STATE_ITEM");
        }

        int rowsUpdated = database.update(ProductsEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS_CODE:
                return ProductsEntry.PRODUCTS_TYPE;
            case PRODUCT_ITEM_CODE:
                return ProductsEntry.PRODUCT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("uri is invalid; " + uri);
        }
    }
}
