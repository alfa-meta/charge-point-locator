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

public class AddChargePointFragment extends Fragment {
    private DatabaseHelper databaseHelper;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_chargepoint, container, false);

        databaseHelper = new DatabaseHelper(requireContext());

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

        // Set up the Spinner with binary options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.charge_point_status, R.layout.spinner_item); // Use custom layout for items
        adapter.setDropDownViewResource(R.layout.spinner_item); // Optional: Use the same layout for dropdown items
        statusSpinner.setAdapter(adapter);


        // Programmatically set the color for the selected value
        //statusSpinner.setBackgroundColor(requireContext().getResources().getColor(R.color.gruvbox_bg));
        statusSpinner.setPopupBackgroundDrawable(requireContext().getDrawable(R.color.gruvbox_bg));

        addButton.setOnClickListener(v -> {
            String lat = latitude.getText().toString().trim();
            String lng = longitude.getText().toString().trim();
            String conID = connectorID.getText().toString().trim();
            String conType = connectorType.getText().toString().trim();
            String refID = referenceID.getText().toString().trim();
            String twn = town.getText().toString().trim();
            String cnty = county.getText().toString().trim();
            String post = postcode.getText().toString().trim();
            String stat = statusSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng) || TextUtils.isEmpty(conID) || TextUtils.isEmpty(conType) ||
                    TextUtils.isEmpty(refID) || TextUtils.isEmpty(twn) || TextUtils.isEmpty(cnty) || TextUtils.isEmpty(post)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseHelper.addChargePoint(refID, Double.parseDouble(lat), Double.parseDouble(lng), twn, cnty, post, stat, conID, conType);
            Toast.makeText(requireContext(), "ChargePoint added successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to ChargePointDatabaseFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ChargePointDatabaseFragment())
                .commit();
        });

        return view;
    }
}
