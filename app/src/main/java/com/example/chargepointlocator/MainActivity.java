package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

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
        String csvFileName = "Sample_national_chargepoints.csv"; // Name of the file in assets
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(csvFileName);
            dbHelper.importChargepointsFromCSV(inputStream); // Pass InputStream to your database helper
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading CSV file", Toast.LENGTH_SHORT).show();
        }


        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_users) {
                Toast.makeText(this, "Users clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }
}
