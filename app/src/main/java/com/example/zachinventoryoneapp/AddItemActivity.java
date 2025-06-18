package com.example.zachinventoryoneapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {
    // Declare UI components for item input field and the add button
    private EditText itemNameEditText, quantityEditText, statusEditText;
    private Button onAddItem;
    // Declare database helper
    private InventoryDBHelper dbHelper;

    // This method is called when activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize UI components
        itemNameEditText = findViewById(R.id.itemNameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        statusEditText = findViewById(R.id.statusEditText);
        onAddItem = findViewById(R.id.onAddItem);

        // Create instance of InventoryDBHelper to manage database
        dbHelper = new InventoryDBHelper(this);

        // Set up the add button
        onAddItem.setOnClickListener(v -> {
            // Get values from input fields
            String name = itemNameEditText.getText().toString();
            String quantityStr = quantityEditText.getText().toString();
            String status = statusEditText.getText().toString();

            // Validate input
            if (name.isEmpty() || quantityStr.isEmpty() || status.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the quantity from the string input to an integer
            int quantity = Integer.parseInt(quantityStr);

            // Insert item into the database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            // Put item details into ContentValue object
            values.put(InventoryDBHelper.COLUMN_NAME, name);
            values.put(InventoryDBHelper.COLUMN_QUANTITY, quantity);
            values.put(InventoryDBHelper.COLUMN_STATUS, status);

            // Insert the item into the inventory table
            long newRowId = db.insert(InventoryDBHelper.TABLE_INVENTORY, null, values);

            // Check if the insertion was successful
            if (newRowId != -1) {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                finish();  // Close the AddItemActivity and return to previous activity
            } else {
                Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show();
            }
            db.close();
        });
    }
}
