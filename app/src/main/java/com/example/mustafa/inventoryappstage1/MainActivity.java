package com.example.mustafa.inventoryappstage1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.mustafa.inventoryappstage1.data.BookContract.BookEntry;
import com.example.mustafa.inventoryappstage1.utility.DbBitmapUtility;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BOOK_LOADER = 0;
    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView bookListView = findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);
        getSupportLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    private void insertDummyBook() {

        // Dummy Image
        byte[] bitMapData = DbBitmapUtility.getImage(this, R.drawable.book);

        // Create a ContentValues object where column names are the keys,
        // and Toto's book attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, "spider man");
        values.put(BookEntry.COLUMN_BOOK_PRICE, 20);
        values.put(BookEntry.COLUMN_IMAGE, bitMapData);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, 5);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER, "Mustafa");
        values.put(BookEntry.COLUMN_BOOK_PHONE, "01114208082");
        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    private void deleteAllbooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from books database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.insert:
                // insert new book
                insertDummyBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete:
                deleteAllbooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_IMAGE,
                BookEntry.COLUMN_BOOK_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                BookEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null); // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link bookCursorAdapter} with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
