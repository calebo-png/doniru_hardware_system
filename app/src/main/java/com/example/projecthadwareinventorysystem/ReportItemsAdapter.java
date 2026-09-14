package com.example.projecthadwareinventorysystem;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
// ReportItemsAdapter is an adapter class for the RecyclerViews are used in Android to display large sets of data efficiently.
public class ReportItemsAdapter extends RecyclerView.Adapter<ReportItemsAdapter.ReportViewHolder> {

    private List<InventoryItem> reportItems;
    private final Context context;

    public ReportItemsAdapter(Context context, List<InventoryItem> reportItems) {
        this.context = context;
        this.reportItems = reportItems;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_row, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        InventoryItem item = reportItems.get(position);
        holder.itemName.setText(item.getName());
        holder.itemModel.setText(item.getModel());
        holder.itemSerial.setText("SN: " + item.getSerial());
        holder.itemStatus.setText(item.getStatus());

        // Set status badge color
        String status = item.getStatus().toLowerCase(Locale.getDefault());
        int colorResId;
        switch (status) {
            case "available":
                colorResId = R.color.status_available;
                break;
            case "maintenance":
                colorResId = R.color.status_maintenance;
                break;
            case "checked out":
                colorResId = R.color.status_checked_out;
                break;
            default:
                colorResId = R.color.status_default;
                break;
        }
        // Get the background drawable and set its color
        GradientDrawable statusBackground = (GradientDrawable) holder.itemStatus.getBackground();
        statusBackground.setColor(ContextCompat.getColor(context, colorResId));
        if (status.equals("checked out")) {
            holder.itemStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            holder.itemStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    //  returns the total number of items in the data set held by the adapter.
    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    // Updates the list of items in the adapter and refreshes the RecyclerView.
    public void updateData(List<InventoryItem> newItems) {
        this.reportItems = newItems;
        notifyDataSetChanged();
    }

    // ViewHolder class to hold the views for each item in the RecyclerView
    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemModel, itemSerial, itemStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.report_item_name);
            itemModel = itemView.findViewById(R.id.report_item_model);
            itemSerial = itemView.findViewById(R.id.report_item_serial);
            itemStatus = itemView.findViewById(R.id.report_item_status);
        }
    }
}
