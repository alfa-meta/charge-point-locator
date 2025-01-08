package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * A Fragment that allows the user to add a new charge point entry into the database.
 * This class handles user input, validates the data, and saves the charge point details to the database.
 */
public class AddChargePointFragment extends Fragment {
    private DatabaseHelper databaseHelper; // Helper for database operations

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_add_chargepoint, container, false);

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(requireContext());

        // Find all views in the layout
        EditText latitude = view.findViewById(R.id.editLatitude);
        EditText longitude = view.findViewById(R.id.editLongitude);
        EditText connectorID = view.findViewById(R.id.editConnectorID);
        EditText connectorType = view.findViewById(R.id.editConnectorType);
        EditText referenceID = view.findViewById(R.id.editReferenceID);
        EditText town = view.findViewById(R.id.editTown);
        EditText county = view.findViewById(R.id.editCounty);
        EditText postcode = view.findViewById(R.id.editPostcode);
        Spinner statusSpinner = view.findViewById(R.id.spinnerStatus);
        Button addButton = view.findViewById(R.id.addChargePointButton);

        // Set up the Spinner (dropdown) with options from resources
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.charge_point_status, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        statusSpinner.setAdapter(adapter);

        // Customize the popup background for the Spinner
        statusSpinner.setPopupBackgroundDrawable(requireContext().getDrawable(R.color.gruvbox_bg));

        // Set an OnClickListener for the Add button
        addButton.setOnClickListener(v -> {
            // Retrieve and trim user input
            String lat = latitude.getText().toString().trim();
            String lng = longitude.getText().toString().trim();
            String conID = connectorID.getText().toString().trim();
            String conType = connectorType.getText().toString().trim();
            String refID = referenceID.getText().toString().trim();
            String twn = town.getText().toString().trim();
            String cnty = county.getText().toString().trim();
            String post = postcode.getText().toString().trim();
            String stat = statusSpinner.getSelectedItem().toString();

            // Validate input fields
            if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng) || TextUtils.isEmpty(conID) || TextUtils.isEmpty(conType) ||
                    TextUtils.isEmpty(refID) || TextUtils.isEmpty(twn) || TextUtils.isEmpty(cnty) || TextUtils.isEmpty(post)) {
                // Show error message if any field is empty
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Construct a confirmation message with the entered details
            String confirmationMessage = "Please confirm the entered details:\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Connector ID: " + conID + "\n" +
                    "Connector Type: " + conType + "\n" +
                    "Reference ID: " + refID + "\n" +
                    "Town: " + twn + "\n" +
                    "County: " + cnty + "\n" +
                    "Postcode: " + post + "\n" +
                    "Status: " + stat;

            // Display a confirmation dialog to the user
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Details") // Dialog title
                    .setMessage(confirmationMessage) // Dialog message with details
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Save the charge point details to the database
                        databaseHelper.addChargePoint(refID, Double.parseDouble(lat), Double.parseDouble(lng), twn, cnty, post, stat, conID, conType);
                        Toast.makeText(requireContext(), "ChargePoint added successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate back to the ChargePointDatabaseFragment
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ChargePointDatabaseFragment())
                                .commit();
                    })
                    .setNegativeButton("Edit", (dialog, which) -> {
                        // Dismiss the dialog to allow the user to make changes
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
        return view; // Return the constructed view
    }
}
