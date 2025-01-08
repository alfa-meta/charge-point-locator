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
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

// Main Fragment class to handle map display and charge point filtering
public class MapFragment extends Fragment implements OnMapReadyCallback {

    // UI components
    private EditText searchTown, searchCounty;
    private Spinner chargerTypeSpinner;
    private Switch statusSwitch;
    private Button applyFilterButton, toggleFilterButton, closeFilterButton;
    private LinearLayout filterLayout;

    // Google Map object
    private GoogleMap googleMap;

    // Database helper for fetching charge point data
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize UI components
        searchTown = view.findViewById(R.id.search_town);
        searchCounty = view.findViewById(R.id.search_county);
        chargerTypeSpinner = view.findViewById(R.id.charger_type_spinner);
        statusSwitch = view.findViewById(R.id.status_switch);
        applyFilterButton = view.findViewById(R.id.apply_filter_button);
        toggleFilterButton = view.findViewById(R.id.toggle_filter_button);
        closeFilterButton = view.findViewById(R.id.close_filter_button); // Close Filters button
        filterLayout = view.findViewById(R.id.filter_layout);

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(requireContext());

        // Setup map fragment and callback for when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up button listeners for filtering actions
        applyFilterButton.setOnClickListener(v -> applyFilters());
        toggleFilterButton.setOnClickListener(v -> toggleFilterLayout());
        closeFilterButton.setOnClickListener(v -> closeFilterLayout());

        // Populate charger type dropdown with available options
        populateChargerTypeSpinner();

        return view;
    }

    // Toggles the visibility of the filter layout
    private void toggleFilterLayout() {
        if (filterLayout.getVisibility() == View.VISIBLE) {
            filterLayout.setVisibility(View.GONE);
            toggleFilterButton.setText("Filters"); // Show "Filters" when hidden
        } else {
            filterLayout.setVisibility(View.VISIBLE);
            toggleFilterButton.setText("Hide Filters"); // Show "Hide Filters" when visible
        }
    }

    // Hides the filter layout and resets the toggle button text
    private void closeFilterLayout() {
        filterLayout.setVisibility(View.GONE);
        toggleFilterButton.setText("Filters");
    }

    // Callback when the map is ready for interaction
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Enable basic map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Load all charge points without any filters by default
        loadChargePoints(null, null, null, false);
    }

    // Applies the filters selected by the user and reloads the map
    private void applyFilters() {
        // Retrieve filter inputs
        String town = searchTown.getText().toString().trim();
        String county = searchCounty.getText().toString().trim();
        String chargerType = chargerTypeSpinner.getSelectedItem() != null
                ? chargerTypeSpinner.getSelectedItem().toString()
                : "All Types"; // Default value if no selection
        boolean onlyInService = statusSwitch.isChecked();

        // Clear existing markers on the map
        googleMap.clear();

        // Load charge points based on the selected filters
        loadChargePoints(town, county, chargerType, onlyInService);
    }

    // Fetches and displays charge points on the map based on filters
    @SuppressLint("Range")
    private void loadChargePoints(String town, String county, String chargerType, boolean onlyInService) {
        // Query the database for charge points
        Cursor cursor = databaseHelper.getAllChargePoints();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasPoints = false;

        // Iterate through the results and filter based on user inputs
        if (cursor.moveToFirst()) {
            do {
                // Retrieve charge point details
                String referenceID = cursor.getString(cursor.getColumnIndex("referenceID"));
                String chargeTown = cursor.getString(cursor.getColumnIndex("town"));
                String chargeCounty = cursor.getString(cursor.getColumnIndex("county"));
                String chargeType = cursor.getString(cursor.getColumnIndex("connectorType"));
                String chargeStatus = cursor.getString(cursor.getColumnIndex("chargeDeviceStatus"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

                // Apply filters
                if ((town == null || town.isEmpty() || (chargeTown != null && chargeTown.equalsIgnoreCase(town))) &&
                        (county == null || county.isEmpty() || (chargeCounty != null && chargeCounty.equalsIgnoreCase(county))) &&
                        (chargerType == null || chargerType.equals("All Types") || (chargeType != null && chargeType.equalsIgnoreCase(chargerType))) &&
                        (!onlyInService || (chargeStatus != null && chargeStatus.equalsIgnoreCase("In Service")))) {

                    // Add marker for the filtered charge point
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

        // Adjust camera bounds to show all charge points or show default location
        if (hasPoints) {
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            Toast.makeText(requireContext(), "Could not find what you are looking for", Toast.LENGTH_SHORT).show();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.0, -2.0), 6)); // Default zoom
        }
    }

    // Populates the charger type spinner with unique values from the database
    private void populateChargerTypeSpinner() {
        // Retrieve unique charger types from the database
        List<String> chargerTypes = databaseHelper.getUniqueChargerTypes();
        chargerTypes.add(0, "All Types"); // Add a default "All Types" option

        // Create an adapter with a custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item, // Default spinner layout
                chargerTypes
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // Set custom text color for the main view
                ((TextView) view).setTextColor(getResources().getColor(R.color.gruvbox_fg));
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                // Set custom text color for dropdown items
                ((TextView) view).setTextColor(getResources().getColor(R.color.gruvbox_fg));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the spinner
        chargerTypeSpinner.setAdapter(adapter);
    }
}
