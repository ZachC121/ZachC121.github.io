package com.example.zachinventoryoneapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 2; // Increment version for upgrades
    private static final String TABLE_NAME = "users";
    private static final String COL_ID = "ID";
    private static final String COL_EMAIL = "EMAIL";
    private static final String COL_PASSWORD = "PASSWORD";
    private static final String COL_SALT = "SALT"; // Store salt separately

    // Constructor for DatabaseHelper, initializing database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_SALT + " TEXT)"; // New column for salt
        db.execSQL(createTable);
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing table if ti exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Method to insert a new user with salted and hashed password
    public boolean insertUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Generate salt for password
        byte[] salt = generateSalt();
        // Hash the password with the generated salt
        String hashedPassword = hashPassword(password, salt);

        ContentValues contentValues = new ContentValues();

        // Put users data into contentValues
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PASSWORD, hashedPassword);
        contentValues.put(COL_SALT, Base64.encodeToString(salt, Base64.DEFAULT)); // Encode salt to Base64

        // Insert the new user record into the database
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1; // Return true if insertion was successful
    }

    // Hash password with SHA-256 and salt
    private String hashPassword(String password, byte[] salt) {
        try {
            // Create MessageDigest instance for SHA-246 hashing
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes()); // Hash password
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Return null in case of error
        }
    }

    // Generate random salt
    private byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom(); // Get a random number generator
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt); // Fill it with random bytes
        return salt; // Return generated salt
    }

    // Method to check if user exists in the database
    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + " = ?"; // SQL query to find user by email
        // Query the database to get the stored password and salt
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_PASSWORD, COL_SALT}, COL_EMAIL +
                "=?", new String[]{email}, null, null, null);

        // If cursor moves to the first row, it means user exists
        if (cursor.moveToFirst()) {
            // Retrieve stored password and salt
            @SuppressLint("Range") String storedPassword = cursor.getString(cursor.getColumnIndex(COL_PASSWORD));
            @SuppressLint("Range") String storedSalt = cursor.getString(cursor.getColumnIndex(COL_SALT));
            byte[] salt = Base64.decode(storedSalt, Base64.DEFAULT);
            // Hash the provided password with the retrieved salt
            String hashedInputPassword = hashPassword(password, salt);

            cursor.close();
            // Compare stored password with the hashed input password
            return storedPassword.equals(hashedInputPassword);
        }
        cursor.close();
        return false; // Return false if user not found
    }

}




