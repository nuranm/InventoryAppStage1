package com.example.mustafa.inventoryappstage1;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mustafa.inventoryappstage1.data.BookContract.BookEntry;
import com.example.mustafa.inventoryappstage1.utility.DbBitmapUtility;


import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_Book_LOADER = 0;
    Button order;
    byte[] imageByteArray;
    Bitmap bitmap;
    String phone;
    /**
     * It stores the quantity of books
     */
    int quantityInt;
    private Uri mCurrentBookUri;
    private EditText nameEditText;
    private EditText priceEditText;
    private Button imageButton;
    private ImageView imageView;
    private EditText quantityEditText;
    private EditText supplierEditText;
    private EditText phoneEditText;
    private boolean bookHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the bookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Initialize all views..
        initializeUIElements();

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a Book content URI, then we know that we are
        // creating a new Book.
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            // I know we are coming from the FAB button being pressed
            setTitle(getString(R.string.detail_activity_title_new_book));

            // Hide the Order Button if the book is going to be added and not stored yet.
            order.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            // I know we are coming from clicking a book in the Main Activity.
            setTitle(getString(R.string.detail_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the detail Activity
            getSupportLoaderManager().initLoader(EXISTING_Book_LOADER, null, this);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });

    }

    /**
     * This method initializes all UI elements used in the Activity
     */
    public void initializeUIElements() {
        nameEditText = findViewById(R.id.edit_name);
        priceEditText = findViewById(R.id.edit_price);
        imageButton = findViewById(R.id.buttonForImage);
        imageView = findViewById(R.id.imageView);
        quantityEditText = findViewById(R.id.edit_quantity);
        supplierEditText = findViewById(R.id.edit_Supplier);
        phoneEditText = findViewById(R.id.edit_Phone);
        order = findViewById(R.id.orderButton);
        Button increaseButton = findViewById(R.id.increaseButton);
        Button decreaseButton = findViewById(R.id.decreaseButton);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        imageButton.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        supplierEditText.setOnTouchListener(mTouchListener);
        phoneEditText.setOnTouchListener(mTouchListener);
        increaseButton.setOnTouchListener(mTouchListener);
        decreaseButton.setOnTouchListener(mTouchListener);
    }

    // Get Image from Gallery
    private void getImageFromGallery() {
        int READ_EXTERNAL_STORAGE = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (EditorActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                EditorActivity.this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                return;
            }
        }

        try {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        } catch (Exception exp) {
            Log.i("Error", exp.toString());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            imageView.setImageURI(selectedImage);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageByteArray = DbBitmapUtility.getBytes(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get user input from editor and save new book into database.
     */
    private void saveBook() {
        // Read from input fields
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierString = supplierEditText.getText().toString().trim();
        String phoneString = phoneEditText.getText().toString().trim();

        // Default Image if the user has no image for the book.
        byte[] defaultImage = DbBitmapUtility.getImage(this, R.drawable.book);

        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        if (imageByteArray == null) {
            values.put(BookEntry.COLUMN_IMAGE, defaultImage);
        } else {
            values.put(BookEntry.COLUMN_IMAGE, imageByteArray);
        }
        values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, priceString);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER, supplierString);
        values.put(BookEntry.COLUMN_BOOK_PHONE, phoneString);

        // Check if there are no empty values
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierString)
                || TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, "No null values are accepted, kindly enter the correct data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add Data
        if (mCurrentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
                // Exit activity
                finish();
            }
        } else {
            // Update Data
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
                // Exit activity
                finish();
            }
        }


    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentbookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the  detail activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edite, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.insert:
                // insert new book
                saveBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                //NavUtils.navigateUpFromSameTask(this);
                //return true;

                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_IMAGE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER,
                BookEntry.COLUMN_BOOK_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_IMAGE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PHONE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            imageByteArray = cursor.getBlob(imageColumnIndex);
            Bitmap image = DbBitmapUtility.getImage(imageByteArray);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            phone = cursor.getString(phoneColumnIndex);

            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            priceEditText.setText(String.format("%s", Double.toString(price)));
            imageView.setImageBitmap(image);
            quantityEditText.setText(String.format("%s", Integer.toString(quantity)));
            supplierEditText.setText(supplier);
            phoneEditText.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.setText("");
        priceEditText.setText("");
        imageView.setImageResource(0);
        quantityEditText.setText("");
        supplierEditText.setText("");
        phoneEditText.setText("");
    }

    /**
     * Orders the product from suppliers using their phone numbers.
     */
    public void orderProductFromSupplier(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Decreases the quantity of books
     */
    public void decrement(View view) {
        quantityInt = Integer.valueOf(quantityEditText.getText().toString());
        if (quantityInt == 0) {
            // Show an error message as a toast
            Toast.makeText(this, "Negative values are not accepted", Toast.LENGTH_SHORT).show();
            // Exit this method early because there's nothing left to do
            return;
        }
        quantityInt = quantityInt - 1;
        quantityEditText.setText(String.valueOf(quantityInt));

    }

    /**
     * Increases the quantity of books
     */
    public void increment(View view) {
        quantityInt = Integer.valueOf(quantityEditText.getText().toString());
        quantityInt = quantityInt + 1;
        quantityEditText.setText(String.valueOf(quantityInt));
    }
}
