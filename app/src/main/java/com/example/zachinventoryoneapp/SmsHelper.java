package com.example.zachinventoryoneapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsHelper {
    public static final int SMS_PERMISSION_CODE = 100;

    // Check if the app is running on an emulator
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.HARDWARE.contains("goldfish");
    }

    // Send SMS or request permission if not granted
    public static void sendSms(Activity activity, String phoneNumber, String message) {
        if (isEmulator()) {
            Toast.makeText(activity, "SMS would be sent to " + phoneNumber + ": \n" + message, Toast.LENGTH_LONG).show();
            return;
        }

        // Check for SMS permission and request if not granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            // Send SMS if permission is already granted
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(activity, "Low stock notification sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
    }
}
