package com.example.chargepointlocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChargePointAdapter extends RecyclerView.Adapter<ChargePointAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ChargePoint> chargePoints;

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
        holder.connectorID.setText("Connector ID: " + chargePoint.getConnectorID());
        holder.connectorType.setText("Connector Type: " + chargePoint.getConnectorType());
        holder.textLocationDetails.setText(String.format("%s, %s, %s",
                chargePoint.getTown(), chargePoint.getCounty(), chargePoint.getPostcode()));
        holder.textStatus.setText("Status: " + chargePoint.getChargeDeviceStatus());

        if ("Out service".equals(chargePoint.getChargeDeviceStatus())) {
            holder.textStatus.setTextColor(context.getResources().getColor(R.color.gruvbox_red)); // Red color
        } else {
            holder.textStatus.setTextColor(context.getResources().getColor(R.color.gruvbox_aqua)); // Default color
        }
    }

    @Override
    public int getItemCount() {
        return chargePoints.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView connectorID, connectorType, textLocationDetails, textStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            connectorID = itemView.findViewById(R.id.textConnectorID);
            connectorType = itemView.findViewById(R.id.textConnectorType);
            textLocationDetails = itemView.findViewById(R.id.textLocationDetails);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
