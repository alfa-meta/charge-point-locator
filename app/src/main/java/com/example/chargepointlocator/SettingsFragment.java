package com.example.chargepointlocator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.io.InputStream;

public class SettingsFragment extends Fragment {

    // Constants for request codes
    private static final int REQUEST_CODE_PICK_CSV = 1; // Code to identify file picker result
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100; // Code to identify permission result

    private TextView tvUploadStatus; // TextView to display the upload status

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize UI components
        Button btnUploadCSV = view.findViewById(R.id.btnUploadCSV); // Button to trigger CSV upload
        tvUploadStatus = view.findViewById(R.id.tvUploadStatus); // Status TextView

        // Check the current status of the database
        checkDatabaseStatus();

        // Set a click listener for the upload button
        btnUploadCSV.setOnClickListener(v -> checkStoragePermission());

        return view;
    }

    /**
     * Check and request storage permission if necessary.
     */
    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // For Android 11+ (API level 30+), check if the app can manage all files
            if (!Environment.isExternalStorageManager()) {
                showManageExternalStoragePermissionDialog();
            } else {
                openFilePicker();
            }
        } else {
            // For Android 10 and below, check READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if not granted
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
                );
            } else {
                // If permission is granted, open the file picker
                openFilePicker();
            }
        }
    }

    /**
     * Display a dialog to request MANAGE_EXTERNAL_STORAGE permission.
     * Only applicable for Android 11+.
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void showManageExternalStoragePermissionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("This app requires access to manage all files to upload CSV files. Please grant the permission.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    // Redirect to the system settings to grant the permission
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Launch a file picker to select a CSV file.
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Restrict to openable files
        intent.setType("*/*"); // Allow all file types for selection

        startActivityForResult(intent, REQUEST_CODE_PICK_CSV);
    }

    /**
     * Handle the result from the file picker or permission dialog.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected file URI
            Uri fileUri = data.getData();
            if (fileUri != null) {
                processCSVFile(fileUri); // Process the selected file
            }
        }
    }

    /**
     * Process the selected CSV file.
     *
     * @param fileUri The URI of the selected file.
     */
    public void processCSVFile(Uri fileUri) {
        String fileName = getFileName(fileUri); // Get the file name from URI
        if (fileName == null || !fileName.endsWith(".csv")) {
            // Validate the file type
            tvUploadStatus.setText("Invalid file type. Please select a CSV file.");
            Toast.makeText(requireContext(), "Invalid file type. Please select a CSV file.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Open an InputStream to read the file
            InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                dbHelper.importChargepointsFromCSV(inputStream); // Import CSV data into the database
                inputStream.close(); // Close the InputStream

                // Update the UI to indicate success
                tvUploadStatus.setText("CSV file uploaded successfully.");
                Toast.makeText(requireContext(), "CSV file uploaded successfully.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // Handle errors during file reading
            e.printStackTrace();
            tvUploadStatus.setText("Error reading the file.");
            Toast.makeText(requireContext(), "Error reading the file.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Retrieve the file name from a URI.
     *
     * @param uri The URI of the file.
     * @return The file name as a String, or null if unavailable.
     */
    private String getFileName(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        }
        return null;
    }

    /**
     * Check if the database contains any data and update the UI accordingly.
     */
    private void checkDatabaseStatus() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        if (dbHelper.hasData()) {
            // If the database has data, update the status message
            tvUploadStatus.setText("CSV file has been uploaded.");
        } else {
            // If no data exists, prompt the user to upload a file
            tvUploadStatus.setText("No CSV file uploaded.");
        }
    }
}
