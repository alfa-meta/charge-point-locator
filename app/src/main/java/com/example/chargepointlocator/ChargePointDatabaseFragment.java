package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChargePointDatabaseFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(requireContext());
    }

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

        // Fetch and set adapter
        ArrayList<ChargePoint> chargePoints = fetchChargePoints();
        ChargePointAdapter adapter = new ChargePointAdapter(requireContext(), chargePoints, databaseHelper);
        recyclerView.setAdapter(adapter);

        return view;
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
