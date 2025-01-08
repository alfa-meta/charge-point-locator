package com.example.chargepointlocator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The RegisterActivity class allows users to register a new account by providing
 * their name, email, and password. It validates the inputs and interacts with the
 * local database via DatabaseHelper to store user information.
 */
public class RegisterActivity extends AppCompatActivity {

    // Instance of DatabaseHelper for interacting with the database
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialise DatabaseHelper for user registration operations
        dbHelper = new DatabaseHelper(this);

        // Link UI elements from the layout file
        EditText nameEditText = findViewById(R.id.nameEditText); // Input for user's name
        EditText emailEditText = findViewById(R.id.emailEditText); // Input for user's email
        EditText passwordEditText = findViewById(R.id.passwordEditText); // Input for user's password
        Button registerButton = findViewById(R.id.registerButton); // Button to trigger registration
        Button goToLoginButton = findViewById(R.id.goToLoginButton); // Button to navigate back to LoginActivity

        // Set a click listener for the register button
        registerButton.setOnClickListener(v -> {
            // Retrieve user inputs
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate user inputs and provide feedback if inputs are invalid
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                // Display a message if any field is empty
                Toast.makeText(RegisterActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            }
            // Validate email format using Patterns
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Display a message if email format is invalid
                Toast.makeText(RegisterActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            }
            // Attempt to add the user to the database
            else if (dbHelper.addUser(email, password, name)) {
                // Display a success message and navigate to LoginActivity
                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close RegisterActivity after successful registration
            }
            // Display an error message if registration fails (e.g., user already exists)
            else {
                Toast.makeText(RegisterActivity.this, "Registration Failed. User might already exist.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set a click listener for the go-to-login button
        goToLoginButton.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close RegisterActivity
        });
    }
}
