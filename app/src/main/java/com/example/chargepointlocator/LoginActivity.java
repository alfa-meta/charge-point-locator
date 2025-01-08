package com.example.chargepointlocator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The LoginActivity class handles user authentication by allowing users to log in
 * with their email and password or to register an account to a local sqlite database.
 */
public class LoginActivity extends AppCompatActivity {

    // Instance of DatabaseHelper for database operations
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialise the database helper to interact with user data
        dbHelper = new DatabaseHelper(this);

        // Link UI elements from the layout file
        EditText emailEditText = findViewById(R.id.emailEditText);  // Input for user email
        EditText passwordEditText = findViewById(R.id.passwordEditText);  // Input for user password
        Button loginButton = findViewById(R.id.loginButton);  // Button to trigger login process
        Button registerButton = findViewById(R.id.registerButton);  // Button to navigate to registration screen

        // Set a click listener for the register button to navigate to the RegisterActivity
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Set a click listener for the login button to validate user credentials
        loginButton.setOnClickListener(v -> {
            // Retrieve user input
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Check for empty fields and display an error message if necessary
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            }
            // Validate credentials against the database
            else if (dbHelper.checkUser(email, password)) {
                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                // Navigate to the MainActivity and pass the email
                Intent intent = new Intent(LoginActivity.this, MainActivity.class)
                        .putExtra("email", email);
                startActivity(intent);
                finish(); // Close LoginActivity after successful login
            }
            // Display an error message for invalid credentials
            else {
                Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection when the activity is destroyed
        dbHelper.close();
    }
}
