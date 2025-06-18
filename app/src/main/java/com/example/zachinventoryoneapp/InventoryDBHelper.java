package com.example.zachinventoryoneapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDBHelper extends SQLiteOpenHelper {

    // Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Inventory.db";

    // Table name and columns
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_STATUS = "status";

    // SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_INVENTORY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " + COLUMN_QUANTITY + " INTEGER, " +
                    COLUMN_STATUS + " TEXT);";

    // Constructor for InventoryDBHelper, initialize the SQLiteOpenHelper
    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // Create table
    }

    // Method is called when database is created
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY); // Drop table if exists
        onCreate(db); // Recover the table
    }

}
