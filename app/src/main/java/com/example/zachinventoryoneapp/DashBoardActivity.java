package com.example.zachinventoryoneapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashBoardActivity extends AppCompatActivity {
    private InventoryDBHelper dbHelper;
    private TableLayout inventoryGrid;
    private boolean smsNotificationsEnabled;
    private static final String USER_PHONE_NUMBER = "+19876543210";
    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_inventory_dashboard);

        dbHelper = new InventoryDBHelper(this);
        inventoryGrid = findViewById(R.id.inventoryGrid);
        smsNotificationsEnabled = getIntent().getBooleanExtra("sms_notifications_enabled", false);

        setupButtons();
        displayInventory();
    }
    private void setupButtons() {
        Button addItemButton = findViewById(R.id.addItemButton);
        Button backToPermissionButton = findViewById(R.id.backToPermissionButton);

        addItemButton.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddItemActivity.class), 1);
        });

        backToPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        Button viewAnalyticsButton = findViewById(R.id.viewAnalyticsButton);
        viewAnalyticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalyticsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) displayInventory();
    }
    @SuppressLint("SetTextI18n")
    private void displayInventory() {
        inventoryGrid.removeAllViews();
        inventoryGrid.addView(InventoryRowBuilder.createHeaderRow(this));

        Cursor cursor = dbHelper.getReadableDatabase().query(
                InventoryDBHelper.TABLE_INVENTORY, null, null, null, null, null, null);

        boolean lowStockDetected = false;
        StringBuilder lowStockMessage = new StringBuilder();

        while (cursor.moveToNext()) {
            @SuppressLint("Range") int itemId = cursor.getInt(cursor.getColumnIndex(InventoryDBHelper.COLUMN_ID));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(InventoryDBHelper.COLUMN_NAME));
            @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(InventoryDBHelper.COLUMN_QUANTITY));
            @SuppressLint("Range") String status = cursor.getString(cursor.getColumnIndex(InventoryDBHelper.COLUMN_STATUS));

            if (quantity <= 0) {
                lowStockDetected = true;
                lowStockMessage.append("Low Stock Alert! Item: ").append(name).append("\n");
            }

            TableRow itemRow = InventoryRowBuilder.createInventoryRow(
                    this,
                    itemId,
                    name,
                    quantity,
                    status,
                    new InventoryRowBuilder.InventoryRowActionListener() {
                        @Override
                        public void onRestockClicked(int id, int newQuantity) {
                            dbHelper.getWritableDatabase().execSQL(
                                    "UPDATE " + InventoryDBHelper.TABLE_INVENTORY +
                                            " SET " + InventoryDBHelper.COLUMN_QUANTITY + " = ?" +
                                            " WHERE " + InventoryDBHelper.COLUMN_ID + " = ?",
                                    new Object[]{newQuantity, id}
                            );
                            logRestockToFirebase(id, 1);
                            displayInventory();
                        }
                        @Override
                        public void onDeleteClicked(int id) {
                            dbHelper.getWritableDatabase().delete(
                                    InventoryDBHelper.TABLE_INVENTORY,
                                    InventoryDBHelper.COLUMN_ID + "=?",
                                    new String[]{String.valueOf(id)}
                            );
                            displayInventory();
                        }
                    }
            );
            inventoryGrid.addView(itemRow);
        }

        cursor.close();

        if (lowStockDetected && smsNotificationsEnabled) {
            if (PermissionHelper.hasSMSPermission(this)) {
                sendSMS(USER_PHONE_NUMBER, lowStockMessage.toString());
            } else {
                Toast.makeText(this, "SMS permission denied. Notifications not sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void sendSMS(String phoneNumber, String message) {
        if (DeviceHelper.isEmulator()) {
            Toast.makeText(this, "Mock SMS to " + phoneNumber + ": \n" + message, Toast.LENGTH_LONG).show();
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Notification sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
    }
    private void logRestockToFirebase(int itemId, int restockedAmount) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("logs");

        // Format timestamp as "Jun 13, 2025 03:13:45 PM"
        String formattedTimestamp = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault())
                .format(new Date());

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("itemId", itemId);
        logEntry.put("quantity", restockedAmount);
        logEntry.put("timestamp", formattedTimestamp);

        logsRef.push().setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Restock logged to Firebase", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseLog", "Restock logged successfully.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firebase log failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseLog", "Failed to log restock", e);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == SMS_PERMISSION_CODE && results.length > 0) {
            String msg = (results[0] == PackageManager.PERMISSION_GRANTED)
                    ? "SMS permission granted"
                    : "SMS permission denied. Notifications cannot be sent.";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
