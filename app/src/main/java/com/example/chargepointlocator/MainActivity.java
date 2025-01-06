package com.example.chargepointlocator;
import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
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

        // Load MapFragment by default
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mapFragment, new MapFragment())
                .commit();

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
            if (id == R.id.nav_users) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mapFragment, new UserFragment()) // Correct ID
                        .addToBackStack(null)
                        .commit();
                Toast.makeText(this, "Users clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_chargepoints) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mapFragment, new MapFragment())
                        .addToBackStack(null)
                        .commit();
                Toast.makeText(this, "Chargepoints Map", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
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
