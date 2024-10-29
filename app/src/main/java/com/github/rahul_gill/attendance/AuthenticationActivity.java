package com.github.rahul_gill.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.rahul_gill.attendance.db.DatabaseHelper;

public class AuthenticationActivity extends AppCompatActivity {

    private EditText passwordInput;
    private Button loginButton;
    private DatabaseHelper databaseHelper;
    private static final String PREF_NAME = "AuthenticationPrefs";
    private static final String IS_AUTHENTICATED_KEY = "isAuthenticated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        databaseHelper = new DatabaseHelper(this);

        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        TextView titleText = findViewById(R.id.titleText);

        databaseHelper = new DatabaseHelper(this);

        if (isPasswordSet()) {
            titleText.setText("Enter Password");
            loginButton.setText("Login");
        } else {
            titleText.setText("Set Password");
            loginButton.setText("Set Password");
        }

        loginButton.setOnClickListener(view -> {
            String inputPassword = passwordInput.getText().toString();
            if (isPasswordSet()) {
                if (checkPassword(inputPassword)) {
                    setAuthenticationState(true); // Set authenticated state
                    navigateToMainActivity();
                } else {
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (inputPassword.isEmpty()) {
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    setPassword(inputPassword);
                    Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show();
                    setAuthenticationState(true); // Set authenticated state
                    navigateToMainActivity();
                }
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean checkPassword(String inputPassword) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM Users WHERE id = 1", null);
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            cursor.close();
            db.close();
            return inputPassword.equals(storedPassword);
        }
        cursor.close();
        db.close();
        return false;
    }

    private void setPassword(String newPassword) {
        databaseHelper.insertOrUpdatePassword(newPassword);
    }

    private boolean isPasswordSet() {
        return databaseHelper.getPassword() != null;
    }

    // Method to set authentication state in SharedPreferences
    private void setAuthenticationState(boolean isAuthenticated) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_AUTHENTICATED_KEY, isAuthenticated);
        editor.apply();
    }
}
