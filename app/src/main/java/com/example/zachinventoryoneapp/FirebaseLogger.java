package com.example.zachinventoryoneapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseLogger {
    public static void logRestock(Context context, String itemId, int restockedAmount) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("logs");

        // Format current time as Jun 13, 2025 03:13:45 PM
        String formattedTimestamp = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault())
                .format(new Date());

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("itemId", itemId);
        logEntry.put("quantity", restockedAmount);
        logEntry.put("timestamp", formattedTimestamp);

        logsRef.push().setValue(logEntry)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseLog", "Restock logged successfully.");
                    Toast.makeText(context, "Restock logged!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseLog", "Failed to log restock", e);
                    Toast.makeText(context, "Failed to log restock", Toast.LENGTH_SHORT).show();
                });
    }
}
