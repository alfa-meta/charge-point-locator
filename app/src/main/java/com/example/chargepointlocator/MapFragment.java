package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private DatabaseHelper databaseHelper;
    private EditText searchTown, searchCounty;
    private Spinner chargerTypeSpinner;
    private Switch statusSwitch;
    private Button applyFilterButton, toggleFilterButton, closeFilterButton;
    private LinearLayout filterLayout;
    private GoogleMap googleMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize views
        searchTown = view.findViewById(R.id.search_town);
        searchCounty = view.findViewById(R.id.search_county);
        chargerTypeSpinner = view.findViewById(R.id.charger_type_spinner);
        statusSwitch = view.findViewById(R.id.status_switch);
        applyFilterButton = view.findViewById(R.id.apply_filter_button);
        toggleFilterButton = view.findViewById(R.id.toggle_filter_button);
        closeFilterButton = view.findViewById(R.id.close_filter_button); // New Close Filters button
        filterLayout = view.findViewById(R.id.filter_layout);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(requireContext());

        // Set up the MapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        applyFilterButton.setOnClickListener(v -> applyFilters());

        // Toggle filter layout visibility
        toggleFilterButton.setOnClickListener(v -> toggleFilterLayout());

        // Close filter layout
        closeFilterButton.setOnClickListener(v -> closeFilterLayout());

        // Populate Spinner
        populateChargerTypeSpinner();

        return view;
    }

    private void toggleFilterLayout() {
        if (filterLayout.getVisibility() == View.VISIBLE) {
            filterLayout.setVisibility(View.GONE);
            toggleFilterButton.setText("Filters");
        } else {
            filterLayout.setVisibility(View.VISIBLE);
            toggleFilterButton.setText("Hide Filters");
        }
    }

    private void closeFilterLayout() {
        filterLayout.setVisibility(View.GONE);
        toggleFilterButton.setText("Filters");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Load all points by default
        loadChargePoints(null, null, null, false);
    }

    private void applyFilters() {
        String town = searchTown.getText().toString().trim();
        String county = searchCounty.getText().toString().trim();
        String chargerType = chargerTypeSpinner.getSelectedItem() != null
                ? chargerTypeSpinner.getSelectedItem().toString()
                : "All Types"; // Default value
        boolean onlyInService = statusSwitch.isChecked();

        // Clear existing markers
        googleMap.clear();

        // Load filtered charge points
        loadChargePoints(town, county, chargerType, onlyInService);
    }

    @SuppressLint("Range")
    private void loadChargePoints(String town, String county, String chargerType, boolean onlyInService) {
        Cursor cursor = databaseHelper.getAllChargePoints();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        boolean hasPoints = false;

        if (cursor.moveToFirst()) {
            do {
                String referenceID = cursor.getString(cursor.getColumnIndex("referenceID"));
                String chargeTown = cursor.getString(cursor.getColumnIndex("town"));
                String chargeCounty = cursor.getString(cursor.getColumnIndex("county"));
                String chargeType = cursor.getString(cursor.getColumnIndex("connectorType"));
                String chargeStatus = cursor.getString(cursor.getColumnIndex("chargeDeviceStatus"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

                // Apply filters with null checks
                if ((town == null || town.isEmpty() || (chargeTown != null && chargeTown.equalsIgnoreCase(town))) &&
                        (county == null || county.isEmpty() || (chargeCounty != null && chargeCounty.equalsIgnoreCase(county))) &&
                        (chargerType == null || chargerType.equals("All Types") || (chargeType != null && chargeType.equalsIgnoreCase(chargerType))) &&
                        (!onlyInService || (chargeStatus != null && chargeStatus.equalsIgnoreCase("In Service")))) {

                    LatLng position = new LatLng(latitude, longitude);
                    googleMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(referenceID)
                            .snippet("Town: " + chargeTown + ", County: " + chargeCounty +
                                    "\nType: " + chargeType + "\nStatus: " + chargeStatus)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    chargeStatus != null && chargeStatus.equalsIgnoreCase("In Service") ?
                                            BitmapDescriptorFactory.HUE_GREEN :
                                            BitmapDescriptorFactory.HUE_RED)));

                    boundsBuilder.include(position);
                    hasPoints = true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (hasPoints) {
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            Toast.makeText(requireContext(), "Could not find what you are looking for", Toast.LENGTH_SHORT).show();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.0, -2.0), 6));
        }
    }

    private void populateChargerTypeSpinner() {
        // Fetch unique charger types from the DatabaseHelper
        List<String> chargerTypes = databaseHelper.getUniqueChargerTypes();
        chargerTypes.add(0, "All Types"); // Add a default option at the beginning

        // Create an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, // Layout for the dropdown
                chargerTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        chargerTypeSpinner.setAdapter(adapter);
    }
}
