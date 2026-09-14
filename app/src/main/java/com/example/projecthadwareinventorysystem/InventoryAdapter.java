package com.example.projecthadwareinventorysystem;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final List<InventoryItem> inventoryList;
    private final List<InventoryItem> inventoryListFull;
    private final OnItemLongClickListener longClickListener;
    private Context context; // Add context field

    // Interface for long click
    public interface OnItemLongClickListener {
        void onItemLongClick(InventoryItem item, int position);
    }

    // Constructor to initialize the adapter with a list of items and a long click listener.
    public InventoryAdapter(List<InventoryItem> itemList, OnItemLongClickListener longClickListener) {
        this.inventoryList = new ArrayList<>(itemList);
        this.inventoryListFull = new ArrayList<>(itemList);
        this.longClickListener = longClickListener;

        if (longClickListener instanceof Context) {
            this.context = (Context) longClickListener;
        }
    }

    //  method is called when the RecyclerView needs a new ViewHolder to represent an item.
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure context is available for inflating layout and getting resources
        if (this.context == null) {
            this.context = parent.getContext();
        }
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_row, parent, false);
        return new InventoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem currentItem = inventoryList.get(position); // Use renamed list
        holder.itemName.setText(currentItem.getName());
        holder.itemModel.setText(currentItem.getModel());
        holder.itemSerial.setText(currentItem.getSerial());
        holder.itemStatus.setText(currentItem.getStatus());

        // Set status color
        String status = currentItem.getStatus().toLowerCase(Locale.getDefault());
        int colorId;

        // Make sure context is not null before trying to get colors
        if (context != null) {
            switch (status) {
                case "available":
                    colorId = ContextCompat.getColor(context, R.color.status_available);
                    break;
                case "maintenance":
                    colorId = ContextCompat.getColor(context, R.color.status_maintenance);
                    break;
                case "checked out": // Assuming "Checked Out" is the status string
                    colorId = ContextCompat.getColor(context, R.color.status_checked_out);
                    break;
                default:
                    colorId = ContextCompat.getColor(context, R.color.status_default); // Or use android.R.color.black
                    break;
            }
            holder.itemStatus.setTextColor(colorId); // Apply color to itemStatus TextView
        } else {
            // Fallback or log error if context is null
            holder.itemStatus.setTextColor(Color.BLACK); // Default fallback
        }


        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                // Use holder.getAdapterPosition() for position in case of list changes
                longClickListener.onItemLongClick(currentItem, holder.getAdapterPosition());
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList.size(); // Use renamed list
    }

    // Method to update the full list (e.g., when data is loaded from DB)
    public void updateItemList(List<InventoryItem> newItemList) {
        this.inventoryListFull.clear();
        this.inventoryListFull.addAll(newItemList);
        // Apply the current filter (or show all if no filter)
        this.inventoryList.clear();
        this.inventoryList.addAll(new ArrayList<>(this.inventoryListFull));
        notifyDataSetChanged();
}


    // Method to filter the list based on search query
    public void filterList(List<InventoryItem> filteredList) {
        inventoryList.clear();
        inventoryList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // Method to get the current list being displayed by the adapter
    public List<InventoryItem> getCurrentItemList() {
        return new ArrayList<>(inventoryList); // Return a copy of the currently displayed list
    }

    // ViewHolder class to hold the views for each item in the RecyclerView.
    static class InventoryViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView itemName;
        TextView itemModel;
        TextView itemSerial;
        TextView itemStatus;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_inv_name);
            itemModel = itemView.findViewById(R.id.item_inv_model);
            itemSerial = itemView.findViewById(R.id.item_inv_serial);
            itemStatus = itemView.findViewById(R.id.item_inv_status);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }
}