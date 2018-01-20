package com.example.littleprincess.inventory.data_entry;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Little Princess on 12/4/2017.
 */

public class DataContract {


    static final String CONTENT_AUTHORITY = "com.example.littleprincess.inventory";


    private DataContract() {
    }

    public static class ProductsEntry implements BaseColumns {

        public static final String COLUMN_NAME = "COLUMN_NAME";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_IN_STOCK = "in_stock";
        public static final String COLUMN_NO_ORDER_ITEMS = "ordered_items";
        public static final String COLUMN_SELL_PRICE = "COLUMN_SELL_PRICE";
        public static final String COLUMN_SALE_PERCENTAGE = "sale";
        public static final String COLUMN_BUY_PRICE = "sale_price";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TYPE = "category_type";
        public static final String COLUMN_PARENT_CATEGORY_ID = "COLUMN_PARENT_CATEGORY_ID";

        public static final int STATE_CATEGORY = 10;
        public static final int STATE_ITEM = 11;

        static final String TABLE_NAME = "products";
        static final String PRODUCTS_PATH = "products";
        static final String BASE_CONTENT_PRODUCTS = "content://com.example.littleprincess.inventory";
        public static final Uri BASE_CONTENT_URI = Uri.parse(BASE_CONTENT_PRODUCTS);
        public static final Uri PRODUCTS_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PRODUCTS_PATH);


        static final String PRODUCT_ITEM_PATH = "products/#";
        static final String PRODUCTS_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PRODUCTS_PATH;
        static final String PRODUCT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PRODUCT_ITEM_PATH;


    }
}
