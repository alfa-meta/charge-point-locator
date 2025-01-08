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

    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(requireContext());
    }

    @SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_chargepoint_database, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewChargePoints);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the "Add New Location" button
        Button addNewLocationButton = view.findViewById(R.id.addNewLocationButton);
        addNewLocationButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddChargePointFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Initialize filter UI
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
                filterLayout.setVisibility(View.GONE);
                toggleFilterButton.setText("Filters");
            } else {
                filterLayout.setVisibility(View.VISIBLE);
                toggleFilterButton.setText("Hide Filters");
            }
        });

        // Close filter layout
        closeFilterButton.setOnClickListener(v -> {
            filterLayout.setVisibility(View.GONE);
            toggleFilterButton.setText("Filters");
        });

        // Apply filters
        applyFilterButton.setOnClickListener(v -> {
            String town = searchTown.getText().toString().trim();
            String county = searchCounty.getText().toString().trim();
            String chargerType = chargerTypeSpinner.getSelectedItem() != null
                    ? chargerTypeSpinner.getSelectedItem().toString()
                    : "All Types";
            boolean onlyInService = statusSwitch.isChecked();

            // Update RecyclerView with filtered data
            ArrayList<ChargePoint> filteredChargePoints = filterChargePoints(town, county, chargerType, onlyInService);
            ChargePointAdapter adapter = new ChargePointAdapter(requireContext(), filteredChargePoints, databaseHelper);
            recyclerView.setAdapter(adapter);
        });

        // Populate spinner
        populateChargerTypeSpinner(chargerTypeSpinner);

        // Fetch and set adapter
        ArrayList<ChargePoint> chargePoints = fetchChargePoints();
        ChargePointAdapter adapter = new ChargePointAdapter(requireContext(), chargePoints, databaseHelper);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @SuppressLint("Range")
    private ArrayList<ChargePoint> filterChargePoints(String town, String county, String chargerType, boolean onlyInService) {
        ArrayList<ChargePoint> filteredChargePoints = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Build the base query
        StringBuilder query = new StringBuilder("SELECT * FROM chargepoints WHERE 1=1");
        List<String> queryArgs = new ArrayList<>();

        // Add filters
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

                filteredChargePoints.add(new ChargePoint(latitude, longitude, connectorID, connectorType, referenceID, townResult, countyResult, postcode, chargeDeviceStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return filteredChargePoints;
    }


    private void populateChargerTypeSpinner(Spinner chargerTypeSpinner) {
        List<String> chargerTypes = databaseHelper.getUniqueChargerTypes();
        chargerTypes.add(0, "All Types");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, chargerTypes);
        adapter.setDropDownViewResource(R.layout.spinner_item); // Use the same custom layout for dropdown
        chargerTypeSpinner.setAdapter(adapter);
    }



    @SuppressLint("Range")
    private ArrayList<ChargePoint> fetchChargePoints() {
        ArrayList<ChargePoint> chargePoints = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllChargePoints();
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

                chargePoints.add(new ChargePoint(latitude, longitude, connectorID, connectorType, referenceID, town, county, postcode, chargeDeviceStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chargePoints;
    }
}
