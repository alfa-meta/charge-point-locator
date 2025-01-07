package com.example.chargepointlocator;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = inflater.inflate(R.layout.fragment_chargepoint_database, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChargePoints);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<ChargePoint> chargePoints = fetchChargePoints();
        ChargePointAdapter adapter = new ChargePointAdapter(requireContext(), chargePoints);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private ArrayList<ChargePoint> fetchChargePoints() {
        ArrayList<ChargePoint> chargePoints = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllChargePoints();
        if (cursor.moveToFirst()) {
            do {
                String connectorID = cursor.getString(cursor.getColumnIndex("connectorID"));
                String connectorType = cursor.getString(cursor.getColumnIndex("connectorType"));
                String referenceID = cursor.getString(cursor.getColumnIndex("referenceID"));
                String town = cursor.getString(cursor.getColumnIndex("town"));
                String county = cursor.getString(cursor.getColumnIndex("county"));
                String postcode = cursor.getString(cursor.getColumnIndex("postcode"));
                String chargeDeviceStatus = cursor.getString(cursor.getColumnIndex("chargeDeviceStatus"));

                chargePoints.add(new ChargePoint(connectorID, connectorType, referenceID, town, county, postcode, chargeDeviceStatus));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chargePoints;
    }
}
