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
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database and import CSV
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Load MapFragment by default
        replaceFragment(new MapFragment());

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);


        // DrawerToggle to open and close the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Retrieve the email passed via Intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        View headerView = navigationView.getHeaderView(0);
        TextView navFullName = headerView.findViewById(R.id.nav_fullname);

        String fullName = updateNavigationHeaderWithFullName(email, navigationView, dbHelper);
        navFullName.setText("Welcome, " + fullName);


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chargepoints) {
                replaceFragment(new MapFragment());
            } else if (id == R.id.nav_chargepoint_database) {
                replaceFragment(new ChargePointDatabaseFragment());
            } else if (id == R.id.nav_users) {
                replaceFragment(new UserFragment());
            } else if (id == R.id.nav_settings) {
                replaceFragment(new SettingsFragment());
            } else if (id == R.id.nav_logout) {
                showLogoutConfirmationDialog();
            }
            return true;
        });
    }

    private String updateNavigationHeaderWithFullName(String email, NavigationView navigationView, DatabaseHelper dbHelper) {
        // Retrieve header view from NavigationView
        View headerView = navigationView.getHeaderView(0);

        // Find the TextView for fullname (replace R.id.nav_fullname with the actual ID)
        TextView navFullName = headerView.findViewById(R.id.nav_fullname);

        // Get the currently logged-in user's full name
        String currentUserFullName = dbHelper.getCurrentUserFullName(email); // Replace with actual method to fetch logged-in username

        if (currentUserFullName != null && !currentUserFullName.isEmpty()) {
            navFullName.setText(currentUserFullName);
        } else {
            navFullName.setText("Guest"); // Fallback text
        }
        return currentUserFullName;
    }

    // Helper method to replace the current fragment and remove the previous one
    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Remove all existing fragments
        for (Fragment fragment : fragmentManager.getFragments()) {
            transaction.remove(fragment);
        }

        // Add the new fragment
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null); // Optional: if you want back navigation
        transaction.commit();
    }

    // Show a confirmation dialog before logging out
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes", (dialog, which) -> handleLogout())
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .create()
            .show();
    }

    // Handle Logout
    private void handleLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close MainActivity
        Toast.makeText(this, "You successfully Logged out", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                // Pass the file URI to the active fragment, if needed
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
