package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChargePointDatabaseFragment extends Fragment {

    private RecyclerView recyclerView; // RecyclerView to display charge points
    private DatabaseHelper databaseHelper; // Helper class for database operations

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the database helper
        databaseHelper = new DatabaseHelper(requireContext());
    }

    @SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_chargepoint_database, container, false);

        // Initialize RecyclerView and set layout manager
        recyclerView = view.findViewById(R.id.recyclerViewChargePoints);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the "Add New Location" button
        Button addNewLocationButton = view.findViewById(R.id.addNewLocationButton);
        addNewLocationButton.setOnClickListener(v -> {
            // Navigate to the AddChargePointFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddChargePointFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Initialize filter UI components
        EditText searchTown = view.findViewById(R.id.search_town);
        EditText searchCounty = view.findViewById(R.id.search_county);
        Spinner chargerTypeSpinner = view.findViewById(R.id.charger_type_spinner);
        Switch statusSwitch = view.findViewById(R.id.status_switch);
        Button applyFilterButton = view.findViewById(R.id.apply_filter_button);
        Button closeFilterButton = view.findViewById(R.id.close_filter_button);
        Button toggleFilterButton = view.findViewById(R.id.toggle_filter_button);
        LinearLayout filterLayout = view.findViewById(R.id.filter_layout);

        // Toggle filter layout visibility
        toggleFilterButton.setOnClickListener(v -> {
            if (filterLayout.getVisibility() == View.VISIBLE) {
                filterLayout.setVisibility(View.GONE); // Hide filters
                toggleFilterButton.setText("Filters");
            } else {
                filterLayout.setVisibility(View.VISIBLE); // Show filters
                toggleFilterButton.setText("Hide Filters");
            }
        });

        // Close filter layout when the close button is clicked
        closeFilterButton.setOnClickListener(v -> {
            filterLayout.setVisibility(View.GONE);
            toggleFilterButton.setText("Filters");
        });

        // Apply filters when the apply button is clicked
        applyFilterButton.setOnClickListener(v -> {
            // Retrieve filter inputs
            String town = searchTown.getText().toString().trim();
            String county = searchCounty.getText().toString().trim();
            String chargerType = chargerTypeSpinner.getSelectedItem() != null
                    ? chargerTypeSpinner.getSelectedItem().toString()
                    : "All Types";
            boolean onlyInService = statusSwitch.isChecked();

            // Filter charge points based on input and update RecyclerView
            ArrayList<ChargePoint> filteredChargePoints = filterChargePoints(town, county, chargerType, onlyInService);
            ChargePointAdapter adapter = new ChargePointAdapter(requireContext(), filteredChargePoints, databaseHelper);
            recyclerView.setAdapter(adapter);
        });

        // Populate the charger type spinner with data from the database
        populateChargerTypeSpinner(chargerTypeSpinner);

        // Fetch all charge points from the database and set adapter
        ArrayList<ChargePoint> chargePoints = fetchChargePoints();
        ChargePointAdapter adapter = new ChargePointAdapter(requireContext(), chargePoints, databaseHelper);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @SuppressLint("Range")
    private ArrayList<ChargePoint> filterChargePoints(String town, String county, String chargerType, boolean onlyInService) {
        ArrayList<ChargePoint> filteredChargePoints = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Build the SQL query dynamically based on filters
        StringBuilder query = new StringBuilder("SELECT * FROM chargepoints WHERE 1=1");
        List<String> queryArgs = new ArrayList<>();

        // Add filters to the query
        if (!town.isEmpty()) {
            query.append(" AND LOWER(town) LIKE ?");
            queryArgs.add("%" + town.toLowerCase() + "%");
        }
        if (!county.isEmpty()) {
            query.append(" AND LOWER(county) LIKE ?");
            queryArgs.add("%" + county.toLowerCase() + "%");
        }
        if (!chargerType.equals("All Types")) {
            query.append(" AND connectorType = ?");
            queryArgs.add(chargerType);
        }
        if (onlyInService) {
            query.append(" AND chargeDeviceStatus = ?");
            queryArgs.add("In Service");
        }

        // Execute the query
        Cursor cursor = db.rawQuery(query.toString(), queryArgs.toArray(new String[0]));

        // Iterate through the result set and create ChargePoint objects
        if (cursor.moveToFirst()) {
            do {
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String connectorID = cursor.getString(cursor.getColumnIndex("connectorID"));
                String connectorType = cursor.getString(cursor.getColumnIndex("connectorType"));
                String referenceID = cursor.getString(cursor.getColumnIndex("referenceID"));
                String townResult = cursor.getString(cursor.getColumnIndex("town"));
                String countyResult = cursor.getString(cursor.getColumnIndex("county"));
                String postcode = cursor.getString(cursor.getColumnIndex("postcode"));
                String chargeDeviceStatus = cursor.getString(cursor.getColumnIndex("chargeDeviceStatus"));

                // Add ChargePoint object to the filtered list
                filteredChargePoints.add(new ChargePoint(latitude, longitude, connectorID, connectorType, referenceID, townResult, countyResult, postcode, chargeDeviceStatus));
            } while (cursor.moveToNext());
        }
        cursor.close(); // Close the cursor to release resources

        return filteredChargePoints;
    }

    private void populateChargerTypeSpinner(Spinner chargerTypeSpinner) {
        // Fetch unique charger types from the database
        List<String> chargerTypes = databaseHelper.getUniqueChargerTypes();
        chargerTypes.add(0, "All Types"); // Add default option

        // Set adapter for the spinner with a custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, chargerTypes);
        adapter.setDropDownViewResource(R.layout.spinner_item); // Set dropdown layout
        chargerTypeSpinner.setAdapter(adapter);
    }

    @SuppressLint("Range")
    private ArrayList<ChargePoint> fetchChargePoints() {
        ArrayList<ChargePoint> chargePoints = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllChargePoints();

        // Iterate through all records and create ChargePoint objects
        if (cursor.moveToFirst()) {
            do {
                String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String connectorID = cursor.getString(cursor.getColumnIndex("connectorID"));
                String connectorType = cursor.getString(cursor.getColumnIndex("connectorType"));
                String referenceID = cursor.getString(cursor.getColumnIndex("referenceID"));
                String town = cursor.getString(cursor.getColumnIndex("town"));
                String county = cursor.getString(cursor.getColumnIndex("county"));
                String postcode = cursor.getString(cursor.getColumnIndex("postcode"));
                String chargeDeviceStatus = cursor.getString(cursor.getColumnIndex("chargeDeviceStatus"));

                // Add ChargePoint object to the list
                chargePoints.add(new ChargePoint(latitude, longitude, connectorID, connectorType, referenceID, town, county, postcode, chargeDeviceStatus));
            } while (cursor.moveToNext());
        }
        cursor.close(); // Close the cursor to release resources

        return chargePoints;
    }
}