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
import com.google.android.gms.maps.model.LatLngBounds;
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
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        Cursor cursor = databaseHelper.getAllChargePoints();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        boolean hasPoints = false;

        if (cursor.moveToFirst()) {
            do {
                String referenceID = cursor.getString(cursor.getColumnIndex("referenceID"));
                String town = cursor.getString(cursor.getColumnIndex("town"));
                String county = cursor.getString(cursor.getColumnIndex("county"));
                String postcode = cursor.getString(cursor.getColumnIndex("postcode"));
                String chargeDeviceStatus = cursor.getString(cursor.getColumnIndex("chargeDeviceStatus"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

                LatLng position = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(referenceID)
                        .snippet("Town: " + town + ", County: " + county +
                                "\nPostcode: " + postcode + "\nStatus: " + chargeDeviceStatus));

                boundsBuilder.include(position);
                hasPoints = true;
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (hasPoints) {
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 5));
        }
    }

}
