package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_CSV = 1; // Code for identifying CSV file pick action
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100; // Code for identifying permission requests

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for the activity

        // Initialize the database helper class
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Load the default fragment (MapFragment) when the app starts
        replaceFragment(new MapFragment());

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize DrawerLayout and NavigationView for the navigation menu
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Add a toggle button to open and close the navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // Synchronize the toggle state

        // Retrieve the email passed via Intent (from LoginActivity or elsewhere)
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        // Get the navigation drawer's header view and update the user's name
        View headerView = navigationView.getHeaderView(0);
        TextView navFullName = headerView.findViewById(R.id.nav_fullname);
        String fullName = updateNavigationHeaderWithFullName(email, navigationView, dbHelper);

        navFullName.setText("Welcome, " + fullName); // This shows Welcome, fullName on the Navigation menu

        // Set the navigation item selection listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Switch fragments based on the selected item
            if (id == R.id.nav_chargepoints) {
                replaceFragment(new MapFragment());
            } else if (id == R.id.nav_chargepoint_database) {
                replaceFragment(new ChargePointDatabaseFragment());
            } else if (id == R.id.nav_users) {
                replaceFragment(new UserFragment());
            } else if (id == R.id.nav_settings) {
                replaceFragment(new SettingsFragment());
            } else if (id == R.id.nav_logout) {
                showLogoutConfirmationDialog(); // Show logout confirmation
            }
            return true;
        });
    }

    // Update the navigation header with the user's full name
    private String updateNavigationHeaderWithFullName(String email, NavigationView navigationView, DatabaseHelper dbHelper) {
        View headerView = navigationView.getHeaderView(0); // Get the header view of the NavigationView
        TextView navFullName = headerView.findViewById(R.id.nav_fullname); // Find the TextView for displaying the name

        // Fetch the user's full name from the database using their email
        String currentUserFullName = dbHelper.getCurrentUserFullName(email);

        // Update the TextView with the user's name or set it to "Guest" if the name is unavailable
        if (currentUserFullName != null && !currentUserFullName.isEmpty()) {
            navFullName.setText(currentUserFullName);
        } else {
            navFullName.setText("Guest");
        }
        return currentUserFullName; // Return the full name for any additional use
    }

    // Replace the current fragment and remove the previous one
    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Remove all existing fragments
        for (Fragment fragment : fragmentManager.getFragments()) {
            transaction.remove(fragment);
        }

        // Replace with the new fragment
        transaction.replace(R.id.fragment_container, newFragment);

        // Optional: Add to back stack for navigation
        transaction.addToBackStack(null);

        // Add a listener to close the drawer after the fragment transaction is complete
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        // Commit the transaction
        transaction.commit();
    }

    // Show a confirmation dialog before logging out
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout") // Title of the dialog
                .setMessage("Are you sure you want to log out?") // Message of the dialog
                .setPositiveButton("Yes", (dialog, which) -> handleLogout()) // Confirm logout
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Cancel logout
                .create()
                .show();
    }

    // Handle the logout process
    private void handleLogout() {
        Intent intent = new Intent(this, LoginActivity.class); // Navigate to LoginActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Close MainActivity
        Toast.makeText(this, "You successfully Logged out", Toast.LENGTH_SHORT).show(); // Show a toast message
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed when the activity is destroyed
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close drawer if open
        } else {
            super.onBackPressed(); // Default back press behavior
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                // Pass the file URI to the active fragment, if it's of type SettingsFragment
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof SettingsFragment) {
                    ((SettingsFragment) currentFragment).processCSVFile(fileUri);
                } else {
                    Toast.makeText(this, "File selected, but no suitable fragment to handle it.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
