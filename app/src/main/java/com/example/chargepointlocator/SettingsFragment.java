package com.example.chargepointlocator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private static final String[] REQUIRED_COLUMNS = {
            "referenceID", "latitude", "longitude", "town",
            "county", "postcode", "chargeDeviceStatus", "connectorID", "connectorType"
    };

    private TextView tvUploadStatus;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button btnUploadCSV = view.findViewById(R.id.btnUploadCSV);
        tvUploadStatus = view.findViewById(R.id.tvUploadStatus);

        databaseHelper = new DatabaseHelper(requireContext());

        btnUploadCSV.setOnClickListener(v -> openFilePicker());

        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"*/csv"});
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

    private void processCSVFile(Uri fileUri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Validate columns
            String headerLine = reader.readLine();
            if (headerLine == null || !validateColumns(headerLine)) {
                tvUploadStatus.setText("Invalid CSV format");
                Toast.makeText(requireContext(), "Invalid CSV format. Ensure correct column names.", Toast.LENGTH_LONG).show();
                return;
            }

            // Import data
            databaseHelper.importChargepointsFromCSV(requireContext().getContentResolver().openInputStream(fileUri));
            tvUploadStatus.setText("File successfully uploaded!");
            Toast.makeText(requireContext(), "File successfully uploaded!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            tvUploadStatus.setText("Error processing file");
            Toast.makeText(requireContext(), "Error processing file", Toast.LENGTH_SHORT).show();
            Log.e("SettingsFragment", "Error processing file", e);
        }
    }

    private boolean validateColumns(String headerLine) {
        String[] columns = headerLine.split(",");
        if (columns.length != REQUIRED_COLUMNS.length) {
            return false;
        }

        for (int i = 0; i < REQUIRED_COLUMNS.length; i++) {
            if (!columns[i].trim().equalsIgnoreCase(REQUIRED_COLUMNS[i])) {
                return false;
            }
        }
        return true;
    }
}
