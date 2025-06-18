package com.example.zachinventoryoneapp;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RealtimeAnalytics {
    // Callback interface to handle analytics results
    public interface AnalyticsCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    // Fetch analytics: calculates total restocks per item
    public static void fetchAnalytics(Context context, AnalyticsCallback callback) {
        FirebaseDatabase.getInstance().getReference("logs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onSuccess("No analytics data found.");
                            return;
                        }

                        Map<String, Integer> restockCounts = new HashMap<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String itemId = String.valueOf(child.child("itemId").getValue());
                            int quantity = 1;
                            try {
                                quantity = Integer.parseInt(String.valueOf(child.child("quantity").getValue()));
                            } catch (Exception ignored) {}

                            int currentTotal = restockCounts.getOrDefault(itemId, 0);
                            restockCounts.put(itemId, currentTotal + quantity);
                        }

                        StringBuilder resultBuilder = new StringBuilder("Most Restocked Items:\n");
                        for (Map.Entry<String, Integer> entry : restockCounts.entrySet()) {
                            resultBuilder.append("Item ID ").append(entry.getKey())
                                    .append(": ").append(entry.getValue()).append(" restocked\n");
                        }
                        callback.onSuccess(resultBuilder.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onFailure(error.getMessage());
                        Toast.makeText(context, "Firebase Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
