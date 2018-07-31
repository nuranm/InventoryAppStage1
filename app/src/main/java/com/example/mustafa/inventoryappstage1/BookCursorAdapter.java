package com.example.mustafa.inventoryappstage1;

import com.example.mustafa.inventoryappstage1.data.BookContract.BookEntry;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mustafa.inventoryappstage1.utility.DbBitmapUtility;

public class BookCursorAdapter extends CursorAdapter {


    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView name = view.findViewById(R.id.name);
        TextView price = view.findViewById(R.id.price);
        ImageView image = view.findViewById(R.id.image);
        final TextView quantityTextView = view.findViewById(R.id.quantity);
        RelativeLayout parentView = view.findViewById(R.id.parent_layout);

        // Find the columns of Book attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_IMAGE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        // Read the book attributes from the Cursor for the current Book
        final int rowId = cursor.getInt(idColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        double bookPrice = cursor.getDouble(priceColumnIndex);
        byte[] arrayImage = cursor.getBlob(imageColumnIndex);
        Bitmap bookImage = DbBitmapUtility.getImage(arrayImage);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);

        if (bookQuantity <= 1) {
            quantityTextView.setText(bookQuantity + " " + context.getResources().getString(R.string.unit));
        } else {
            quantityTextView.setText(bookQuantity + " " + context.getResources().getString(R.string.units));
        }

        // Update the TextViews with the attributes for the current book
        name.setText(bookName);
        price.setText(String.valueOf(bookPrice));
        image.setImageBitmap(bookImage);

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Detail activity
                Intent intent = new Intent(context, EditorActivity.class);

                // Form the content URI that represents clicked book.
                Uri currentInventoryUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, rowId);

                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);

                context.startActivity(intent);
            }
        });


        Button sellButton = view.findViewById(R.id.sell_Button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                // Get the Text
                String text = quantityTextView.getText().toString();
                // Split it
                String[] splittedText = text.split(" ");
                // Take the first part
                int quantity = Integer.parseInt(splittedText[0]);

                if (quantity == 0) {
                    Toast.makeText(context, R.string.no_more_stock, Toast.LENGTH_SHORT).show();
                } else if (quantity > 0) {
                    quantity = quantity - 1;

                    String quantityString = Integer.toString(quantity);

                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityString);

                    Uri currentInventoryUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, rowId);

                    int rowsAffected = context.getContentResolver().update(currentInventoryUri, values, null, null);

                    if (rowsAffected != 0) {
                        /* update text view if database update is successful */
                        if (bookQuantity <= 1) {
                            quantityTextView.setText(quantity + " " + context.getResources().getString(R.string.unit));
                        } else {
                            quantityTextView.setText(quantity + " " + context.getResources().getString(R.string.units));
                        }
                    } else {
                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


}
