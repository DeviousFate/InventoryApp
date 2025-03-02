package com.example.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// MainActivity handles user login and registration.
public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnRegister;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnRegister= findViewById(R.id.btnRegister);

        dbHelper = new DBHelper(this);

        // Attempt login on button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        // Register new user on button click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    // Login function: checks user credentials and moves to Dashboard if successful
    private void login() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (dbHelper.checkUser(username, password)) {
            // User exists; launch Dashboard
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Login failed. Incorrect username or password.", Toast.LENGTH_SHORT).show();
        }
    }

    // Register function: creates a new user record
    private void register() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = dbHelper.addUser(username, password);
        if (success) {
            Toast.makeText(this, "Registration successful. You can now log in.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Registration failed. Username may already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
