package com.github.rahul_gill.attendance.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "attendance.db";
    private static final String TABLE_USER = "Users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating Users table...");
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // Method to insert or update password
    public void insertOrUpdatePassword(String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Use REPLACE to insert or update the first row (id=1)
        db.execSQL("INSERT OR REPLACE INTO " + TABLE_USER + "(id, password) VALUES (1, ?)", new Object[]{password});
        db.close();
    }

    // Method to get the stored password
    public String getPassword() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Query the first row (id=1) for the password
        Cursor cursor = db.rawQuery("SELECT password FROM " + TABLE_USER + " WHERE id = 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            String password = cursor.getString(0);
            cursor.close();
            return password;
        }
        return null;
    }

    public boolean checkPassword(String inputPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM Users WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            cursor.close();
            return inputPassword.equals(storedPassword);
        }
        cursor.close();
        return false;
    }

    public void setPassword(String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE Users SET password = ? WHERE id = 1", new Object[]{newPassword});
        db.close();
    }
}
