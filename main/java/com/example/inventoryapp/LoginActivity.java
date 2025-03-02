package com.example.inventoryapp;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Make the password field obscure
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login logic here
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                // ... (Check credentials, navigate to next screen, etc.)
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle create account logic here
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                // ... (Add user to database, etc.)
            }
        });
    }
}