package com.example.zachinventoryoneapp;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {
    private InventoryDBHelper dbHelper;
    private LinearLayout chartContainer;
    private LinearLayout statisticsContainer;
    private String analyticsSummary = "";
    private static final int LOW_STOCK_THRESHOLD = 1;

    static class InventoryItem {
        String name;
        int quantity;
        InventoryItem(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        dbHelper = new InventoryDBHelper(this);
        chartContainer = findViewById(R.id.chartContainer);
        statisticsContainer = findViewById(R.id.statisticsContainer);

        displayStatistics();
        drawBarChart();

        Button backButton = findViewById(R.id.backToDashboardButton);
        backButton.setOnClickListener(v -> finish());

        Button shareButton = findViewById(R.id.shareAnalyticsButton);
        shareButton.setOnClickListener(v -> shareAnalytics());
    }
    @SuppressLint("SetTextI18n")
    private void displayStatistics() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                InventoryDBHelper.TABLE_INVENTORY,
                null, null, null, null, null, null
        );

        int totalItems = 0;
        int totalQuantity = 0;
        int lowStockCount = 0;

        while (cursor.moveToNext()) {
            @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(InventoryDBHelper.COLUMN_QUANTITY));
            totalItems++;
            totalQuantity += quantity;
            if (quantity <= LOW_STOCK_THRESHOLD) lowStockCount++;
        }
        cursor.close();

        int avgQuantity = totalItems > 0 ? totalQuantity / totalItems : 0;

        analyticsSummary = "Total Items: " + totalItems + "\n"
                + "Average Quantity: " + avgQuantity + "\n"
                + "Low Stock Items: " + lowStockCount;

        TextView totalItemsView = new TextView(this);
        totalItemsView.setText("Total Items: " + totalItems);

        TextView avgQuantityView = new TextView(this);
        avgQuantityView.setText("Average Quantity: " + avgQuantity);

        TextView lowStockView = new TextView(this);
        lowStockView.setText("Low Stock Items: " + lowStockCount);

        statisticsContainer.addView(totalItemsView);
        statisticsContainer.addView(avgQuantityView);
        statisticsContainer.addView(lowStockView);

        RealtimeAnalytics.fetchAnalytics(this, new RealtimeAnalytics.AnalyticsCallback() {
            @Override
            public void onSuccess(String result) {
                TextView firebaseStats = new TextView(AnalyticsActivity.this);
                firebaseStats.setText(result);
                statisticsContainer.addView(firebaseStats);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AnalyticsActivity.this, "Error: " + error, Toast.LENGTH_LONG);
            }
        });

    }

    private void drawBarChart() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                InventoryDBHelper.TABLE_INVENTORY,
                null, null, null, null, null, null
        );

        List<InventoryItem> items = new ArrayList<>();
        int maxQuantity = 1;

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(InventoryDBHelper.COLUMN_NAME));
            @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(InventoryDBHelper.COLUMN_QUANTITY));
            items.add(new InventoryItem(name, quantity));
            if (quantity > maxQuantity) maxQuantity = quantity;
        }
        cursor.close();

        Collections.sort(items, Comparator.comparingInt(i -> -i.quantity)); // descending

        for (InventoryItem item : items) {
            View bar = new View(this);
            int barHeight = (int) ((item.quantity / (float) maxQuantity) * 300);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(100, barHeight);
            barParams.setMargins(16, 0, 16, 0);
            bar.setLayoutParams(barParams);

            if (item.quantity <= LOW_STOCK_THRESHOLD) {
                bar.setBackgroundColor(Color.RED);
            } else {
                bar.setBackgroundColor(Color.BLUE);
            }

            TextView label = new TextView(this);
            label.setText(item.name + "\n(" + item.quantity + ")");
            label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            itemLayout.addView(bar);
            itemLayout.addView(label);

            chartContainer.addView(itemLayout);
        }
    }
    private void shareAnalytics() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Analytics", analyticsSummary);
        clipboard.setPrimaryClip(clip);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, analyticsSummary);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Inventory Analytics");
        startActivity(shareIntent);
    }
}
