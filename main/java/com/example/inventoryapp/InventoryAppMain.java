package com.example.inventoryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class InventoryAppMain extends AppCompatActivity {
    private EditText usernameField, passwordField;
    private static final int SMS_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);

        if (usernameField == null || passwordField == null || loginButton == null || registerButton == null) {
            Log.e("Debug", "One or more UI elements are missing in activity_login.xml.");
        } else {
            loginButton.setOnClickListener(v -> loginUser());
            registerButton.setOnClickListener(v -> startActivity(new Intent(InventoryAppMain.this, RegisterActivity.class)));
        }

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> startActivity(new Intent(InventoryAppMain.this, RegisterActivity.class)));

        checkSmsPermission();
    }

    private void loginUser() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(InventoryAppMain.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(InventoryAppMain.this, "Login successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(InventoryAppMain.this, DashboardActivity.class));
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(InventoryAppMain.this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InventoryAppMain.this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
