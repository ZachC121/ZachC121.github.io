package com.example.zachinventoryoneapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch smsNotificationToggle;
    private TextView permissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_notification);

        smsNotificationToggle = findViewById(R.id.smsNotificationToggle);
        permissionStatus = findViewById(R.id.permissionStatus);

        // Handle toggle switch changes
        smsNotificationToggle.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                showConfirmationDialog(); // Ask for SMS permission
            } else {
                proceedToDashboard(false); // Disable SMS
            }
        });
    }

    // Show confirmation dialog to request SMS permission
    @SuppressLint("SetTextI18n")
    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable SMS Notifications")
                .setMessage("Do you want to enable SMS notifications?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (PermissionHelper.hasSMSPermission(this)) {
                        permissionStatus.setText("SMS Permission already granted!");
                        proceedToDashboard(true);
                    } else {
                        PermissionHelper.requestSMSPermission(this); // Request permission
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    smsNotificationToggle.setChecked(false);
                    Toast.makeText(this, "SMS Notifications Disabled", Toast.LENGTH_SHORT).show();
                    proceedToDashboard(false);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Handle the result of the permission request
    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionStatus.setText("SMS Permission granted!");
                proceedToDashboard(true);
            } else {
                permissionStatus.setText("SMS Permission denied.");
                smsNotificationToggle.setChecked(false);
                proceedToDashboard(false);
            }
        }
    }

    // Launch the dashboard activity with SMS notification state
    private void proceedToDashboard(boolean areSmsNotificationsEnabled) {
        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.putExtra("sms_notifications_enabled", areSmsNotificationsEnabled);
        startActivity(intent);
        finish();
    }
}
