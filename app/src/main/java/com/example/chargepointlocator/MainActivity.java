package com.example.chargepointlocator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database and import CSV
        DatabaseHelper dbHelper = new DatabaseHelper(this);
//        String csvFileName = "Sample_national_chargepoints.csv"; // Name of the file in assets
//
//        try {
//            AssetManager assetManager = getAssets();
//            InputStream inputStream = assetManager.open(csvFileName);
//            dbHelper.importChargepointsFromCSV(inputStream); // Pass InputStream to your database helper
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error loading CSV file", Toast.LENGTH_SHORT).show();
//        }

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
}
