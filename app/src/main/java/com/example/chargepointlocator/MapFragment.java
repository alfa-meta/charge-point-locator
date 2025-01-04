package com.example.chargepointlocator;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(requireContext());

        // Set up the MapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Retrieve locations from the database
        Cursor cursor = databaseHelper.getAllChargepoints();
        if (cursor.moveToFirst()) {
            do {
                String locationName = cursor.getString(cursor.getColumnIndex("location_name"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

                // Add a marker for each location
                LatLng position = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(locationName));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Set initial camera position (e.g., focus on the first location or a default position)
        if (googleMap.getCameraPosition() != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 5));
        }
    }
}
