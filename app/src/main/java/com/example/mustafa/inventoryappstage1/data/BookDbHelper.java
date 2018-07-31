package com.example.mustafa.inventoryappstage1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.example.mustafa.inventoryappstage1.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "book.db";
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_PHONE + " TEXT, "
                + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL,"
                + BookEntry.COLUMN_IMAGE + " BLOB, "
                + BookEntry.COLUMN_BOOK_SUPPLIER + " TEXT);";

        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the Database
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        // Create a new one
        onCreate(db);
    }
}