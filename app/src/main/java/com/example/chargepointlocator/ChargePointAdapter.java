package com.example.chargepointlocator;

import android.content.Context;
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

public class ChargePointAdapter extends RecyclerView.Adapter<ChargePointAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ChargePoint> chargePoints;
    private final SparseBooleanArray expandedItems = new SparseBooleanArray(); // To track expanded states

    public ChargePointAdapter(Context context, ArrayList<ChargePoint> chargePoints) {
        this.context = context;
        this.chargePoints = chargePoints;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chargepoint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChargePoint chargePoint = chargePoints.get(position);

        // Bind visible details
        holder.connectorID.setText("Connector ID: " + chargePoint.getConnectorID());
        holder.connectorType.setText("Connector Type: " + chargePoint.getConnectorType());
        holder.textLocationDetails.setText(String.format("%s, %s, %s",
                chargePoint.getTown(), chargePoint.getCounty(), chargePoint.getPostcode()));
        holder.textStatus.setText("Status: " + chargePoint.getChargeDeviceStatus());

        // Set text color based on status
        if ("In service".equalsIgnoreCase(chargePoint.getChargeDeviceStatus())) {
            holder.textStatus.setTextColor(context.getResources().getColor(R.color.gruvbox_green, context.getTheme())); // Green color
        } else if ("Out service".equalsIgnoreCase(chargePoint.getChargeDeviceStatus())) {
            holder.textStatus.setTextColor(context.getResources().getColor(R.color.gruvbox_red, context.getTheme())); // Red color
        }

        // Expand/Collapse details
        boolean isExpanded = expandedItems.get(position, false);
        holder.expandedDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Update expanded content
        holder.referenceID.setText("Reference ID: " + chargePoint.getReferenceID());
        holder.fullLocation.setText(String.format("Location: %s, %s", chargePoint.getLatitude(), chargePoint.getLongitude()));

        // Set click listener for toggling expanded state
        holder.itemView.setOnClickListener(v -> {
            if (isExpanded) {
                expandedItems.put(position, false);
                collapseView(holder.expandedDetails);
            } else {
                expandedItems.put(position, true);
                expandView(holder.expandedDetails);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return chargePoints.size();
    }

    private void expandView(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(300);
        view.startAnimation(animation);
    }

    private void collapseView(View view) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(300);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView connectorID, connectorType, textLocationDetails, textStatus;
        TextView referenceID, fullLocation; // New TextViews for expanded content
        LinearLayout expandedDetails; // Container for expanded details

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            connectorID = itemView.findViewById(R.id.textConnectorID);
            connectorType = itemView.findViewById(R.id.textConnectorType);
            textLocationDetails = itemView.findViewById(R.id.textLocationDetails);
            textStatus = itemView.findViewById(R.id.textStatus);

            // Initialize expanded details
            expandedDetails = itemView.findViewById(R.id.expandedDetails);
            referenceID = itemView.findViewById(R.id.textReferenceID);
            fullLocation = itemView.findViewById(R.id.textFullLocation);
        }
    }
}

