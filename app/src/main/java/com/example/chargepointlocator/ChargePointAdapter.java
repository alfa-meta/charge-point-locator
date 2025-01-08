package com.example.chargepointlocator;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import android.view.animation.AlphaAnimation;

// Adapter class for managing the list of ChargePoint items in a RecyclerView
public class ChargePointAdapter extends RecyclerView.Adapter<ChargePointAdapter.ViewHolder> {

    // Context for accessing application resources
    private final Context context;

    // List of charge points to display in the RecyclerView
    private final ArrayList<ChargePoint> chargePoints;

    // Tracks which items are expanded (visible or collapsed)
    private final SparseBooleanArray expandedItems = new SparseBooleanArray();

    // Database helper for managing charge point data
    private final DatabaseHelper databaseHelper;

    // Constructor initializes context, data, and database helper
    public ChargePointAdapter(Context context, ArrayList<ChargePoint> chargePoints, DatabaseHelper databaseHelper) {
        this.context = context;
        this.chargePoints = chargePoints;
        this.databaseHelper = databaseHelper;
    }

    // Inflates the layout for each item in the RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chargepoint, parent, false);
        return new ViewHolder(view);
    }

    // Binds data to each ViewHolder (item in the RecyclerView)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChargePoint chargePoint = chargePoints.get(position);

        // Set basic details in the item view
        holder.connectorID.setText("Connector ID: " + chargePoint.getConnectorID());
        holder.connectorType.setText("Connector Type: " + chargePoint.getConnectorType());
        holder.textLocationDetails.setText(String.format("%s, %s, %s",
                chargePoint.getTown(), chargePoint.getCounty(), chargePoint.getPostcode()));
        holder.textStatus.setText("Status: " + chargePoint.getChargeDeviceStatus());

        // Change text color based on the status of the charge point
        if ("In service".equalsIgnoreCase(chargePoint.getChargeDeviceStatus())) {
            holder.textStatus.setTextColor(context.getResources().getColor(R.color.gruvbox_green, context.getTheme())); // Green for "In service"
        } else if ("Out service".equalsIgnoreCase(chargePoint.getChargeDeviceStatus())) {
            holder.textStatus.setTextColor(context.getResources().getColor(R.color.gruvbox_red, context.getTheme())); // Red for "Out service"
        }

        // Handle visibility of expanded content (details)
        boolean isExpanded = expandedItems.get(position, false);
        holder.expandedDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Populate expanded content if visible
        holder.referenceID.setText("Reference ID: " + chargePoint.getReferenceID());
        holder.fullLocation.setText(String.format("Location: %s, %s", chargePoint.getLatitude(), chargePoint.getLongitude()));

        // Click listener to toggle expand/collapse state of the item
        holder.itemView.setOnClickListener(v -> {
            if (isExpanded) {
                expandedItems.put(position, false); // Collapse the item
                collapseView(holder.expandedDetails);
            } else {
                expandedItems.put(position, true); // Expand the item
                expandView(holder.expandedDetails);
            }
            notifyItemChanged(position); // Notify RecyclerView about the change
        });

        // Listener for delete button to remove the charge point
        holder.deleteButton.setOnClickListener(v -> {
            deleteChargePoint(position);
        });
    }

    // Deletes a charge point from the database and updates the list
    private void deleteChargePoint(int position) {
        if (position >= 0 && position < chargePoints.size()) {
            ChargePoint chargePoint = chargePoints.get(position);

            // Remove the charge point from the database
            databaseHelper.deleteChargePoint(chargePoint.getReferenceID());

            // Remove the charge point from the local list
            chargePoints.remove(position);

            // Notify the adapter about the removed item
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, chargePoints.size()); // Adjust positions of subsequent items
        } else {
            Log.e("ChargePointAdapter", "Invalid position: " + position);
        }
    }

    // Returns the number of items in the list
    @Override
    public int getItemCount() {
        return chargePoints.size();
    }

    // Expands a view with an animation
    private void expandView(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f); // Fade-in animation
        animation.setDuration(300); // Duration in milliseconds
        view.startAnimation(animation);
    }

    // Collapses a view with an animation
    private void collapseView(View view) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f); // Fade-out animation
        animation.setDuration(300); // Duration in milliseconds
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    // ViewHolder class to hold references to the views in each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView connectorID, connectorType, textLocationDetails, textStatus;
        TextView referenceID, fullLocation; // Additional TextViews for expanded content
        LinearLayout expandedDetails; // Layout containing expanded details
        View deleteButton; // Button for deleting a charge point

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views for basic details
            connectorID = itemView.findViewById(R.id.textConnectorID);
            connectorType = itemView.findViewById(R.id.textConnectorType);
            textLocationDetails = itemView.findViewById(R.id.textLocationDetails);
            textStatus = itemView.findViewById(R.id.textStatus);

            // Initialize views for expanded details
            expandedDetails = itemView.findViewById(R.id.expandedDetails);
            referenceID = itemView.findViewById(R.id.textReferenceID);
            fullLocation = itemView.findViewById(R.id.textFullLocation);

            // Initialize delete button
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
