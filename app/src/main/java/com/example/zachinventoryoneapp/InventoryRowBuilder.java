package com.example.zachinventoryoneapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class InventoryRowBuilder {
    // Create the header row with column titles
    public static TableRow createHeaderRow(Context context) {
        TableRow row = new TableRow(context);
        row.addView(createBoldTextView(context, "Item ID"));    // Added ID column
        row.addView(createBoldTextView(context, "Item Name"));
        row.addView(createBoldTextView(context, "Quantity"));
        row.addView(createBoldTextView(context, "Status"));
        row.addView(createBoldTextView(context, "Actions"));
        return row;
    }
    // Helper for bold, centered text views
    private static TextView createBoldTextView(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }
    // Helper for centered text views
    private static TextView createCenteredTextView(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }
    // Applies consistent size and layout to action buttons
    private static void setButtonSize(Button button) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        layoutParams.setMargins(8, 0, 8, 0);
        button.setLayoutParams(layoutParams);
        button.setMinWidth(10);
        button.setMinHeight(10);
        button.setTextSize(20);
    }
    // Builds a row for each inventory item
    public static TableRow createInventoryRow(
            Context context,
            int itemId,
            String name,
            int quantity,
            String status,
            InventoryRowActionListener listener
    ) {
        TableRow row = new TableRow(context);

        // Show item ID
        row.addView(createCenteredTextView(context, String.valueOf(itemId)));

        // Show name, quantity, and status
        row.addView(createCenteredTextView(context, name));
        row.addView(createCenteredTextView(context, String.valueOf(quantity)));
        row.addView(createCenteredTextView(context, status));

        // Create buttons layout
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);

        // Subtract button
        Button subtractButton = new Button(context);
        subtractButton.setText("-");
        setButtonSize(subtractButton);
        subtractButton.setOnClickListener(v -> {
            int newQuantity = Math.max(0, quantity - 1);
            if (newQuantity == 0) {
                Toast.makeText(context, "Warning: Low stock for " + name, Toast.LENGTH_SHORT).show();
            }
            listener.onRestockClicked(itemId, newQuantity);
        });
        buttonLayout.addView(subtractButton);

        // Add button
        Button addButton = new Button(context);
        addButton.setText("+");
        setButtonSize(addButton);
        addButton.setOnClickListener(v -> listener.onRestockClicked(itemId, quantity + 1));
        buttonLayout.addView(addButton);

        // Delete button
        Button deleteButton = new Button(context);
        deleteButton.setText("");
        deleteButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_delete_forever_24, 0, 0, 0
        );
        setButtonSize(deleteButton);
        deleteButton.setOnClickListener(v -> listener.onDeleteClicked(itemId));
        buttonLayout.addView(deleteButton);

        // Add buttons to row
        row.addView(buttonLayout);
        return row;
    }
    // Interface for handling button actions on inventory rows
    public interface InventoryRowActionListener {
        void onRestockClicked(int itemId, int newQuantity);
        void onDeleteClicked(int itemId);
    }
}
