package com.example.mustafa.inventoryappstage1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.mustafa.inventoryappstage1.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    private static final int BOOKS = 100;
    private static final int BOOKS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOKS_ID);
    }

    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOKS_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }

        // Check that the price is not null and it's greater than or equal to 0
        Double price = values.getAsDouble(BookEntry.COLUMN_BOOK_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Book requires valid price");
        }
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Book requires valid quantity");
        }

        String supplier = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Book requires a supplier");
        }
        String phone = values.getAsString(BookEntry.COLUMN_BOOK_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Book requires a supplier");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ID:
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOKS_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            Double price = values.getAsDouble(BookEntry.COLUMN_BOOK_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Book requires a price");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_IMAGE)) {
            byte[] image = values.getAsByteArray(BookEntry.COLUMN_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Book requires an image");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER)) {
            String supplier = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Book requires a supplier");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_PHONE)) {
            String phone = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER);
            if (phone == null) {
                throw new IllegalArgumentException("Phone Number is required");
            }
        }
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
