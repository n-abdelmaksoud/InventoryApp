package com.example.littleprincess.inventory.data_entry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.littleprincess.inventory.data_entry.DataContract.ProductsEntry;

/**
 * Created by Little Princess on 12/7/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //you can create multiple tables here by calling db.execSQL() for each table

        String createTableStatement = "CREATE TABLE " + ProductsEntry.TABLE_NAME + " ( "
                + ProductsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductsEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ProductsEntry.COLUMN_IMAGE + " TEXT NOT NULL, "
                + ProductsEntry.COLUMN_DESCRIPTION + " TEXT,"
                + ProductsEntry.COLUMN_IN_STOCK + " INTEGER, "
                + ProductsEntry.COLUMN_NO_ORDER_ITEMS + " INTEGER, "
                + ProductsEntry.COLUMN_BUY_PRICE + " REAL, "
                + ProductsEntry.COLUMN_SELL_PRICE + " REAL, "
                + ProductsEntry.COLUMN_SALE_PERCENTAGE + " INTEGER DEFAULT 0, "
                + ProductsEntry.COLUMN_TYPE + " INTEGER NOT NULL, "
                + ProductsEntry.COLUMN_PARENT_CATEGORY_ID + " INTEGER DEFAULT -1 " + ");";

        db.execSQL(createTableStatement);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
