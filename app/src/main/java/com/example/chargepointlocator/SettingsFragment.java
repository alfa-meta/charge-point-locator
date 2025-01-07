package com.example.chargepointlocator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100;

    private TextView tvUploadStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button btnUploadCSV = view.findViewById(R.id.btnUploadCSV);
        tvUploadStatus = view.findViewById(R.id.tvUploadStatus);

        checkDatabaseStatus();

        btnUploadCSV.setOnClickListener(v -> checkStoragePermission());

        return view;
    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Check for MANAGE_EXTERNAL_STORAGE for Android 12+
            if (!Environment.isExternalStorageManager()) {
                showManageExternalStoragePermissionDialog();
            } else {
                openFilePicker();
            }
        } else {
            // For Android 11 and below
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
                );
            } else {
                // Permission already granted, proceed with file picker
                openFilePicker();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void showManageExternalStoragePermissionDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app requires access to manage all files to upload CSV files. Please grant the permission.")
            .setPositiveButton("Grant", (dialog, which) -> {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .create()
            .show();
    }



    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_PICK_CSV);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                processCSVFile(fileUri);
            }
        }
    }

    public void processCSVFile(Uri fileUri) {
        String fileName = getFileName(fileUri);
        if (fileName == null || !fileName.endsWith(".csv")) {
            tvUploadStatus.setText("Invalid file type. Please select a CSV file.");
            Toast.makeText(requireContext(), "Invalid file type. Please select a CSV file.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                dbHelper.importChargepointsFromCSV(inputStream);
                inputStream.close();

                tvUploadStatus.setText("CSV file uploaded successfully.");
                Toast.makeText(requireContext(), "CSV file uploaded successfully.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvUploadStatus.setText("Error reading the file.");
            Toast.makeText(requireContext(), "Error reading the file.", Toast.LENGTH_LONG).show();
        }
    }

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
            tvUploadStatus.setText("CSV file has been uploaded.");
        } else {
            tvUploadStatus.setText("No CSV file uploaded.");
        }
    }
}
