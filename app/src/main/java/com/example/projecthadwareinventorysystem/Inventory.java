package com.example.projecthadwareinventorysystem;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Inventory extends AppCompatActivity implements InventoryAdapter.OnItemLongClickListener {

    private RecyclerView recyclerViewInventory;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryItems; // This will hold the currently displayed list (filtered)
    private List<InventoryItem> allInventoryItems; // This holds the original, unfiltered list from DB
    private SearchView inventorySearchView;
    private FloatingActionButton fabAddDevice;

    private MyDBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI elements: RecyclerView for displaying inventory items,
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        inventorySearchView = findViewById(R.id.inventorySearchView);
        fabAddDevice = findViewById(R.id.fab_add_device);
        dbHelper = new MyDBHelper(this);

        loadInventoryData();

        inventoryAdapter = new InventoryAdapter(inventoryItems != null ? inventoryItems : new ArrayList<>(), this);
        recyclerViewInventory.setAdapter(inventoryAdapter);

        setupSearchListener();
        setupFabClickListener();
    }

    private void loadInventoryData() {
        allInventoryItems = dbHelper.getAllInventoryItems();

        if (allInventoryItems == null) {
            allInventoryItems = new ArrayList<>();
            Log.e("Inventory", "DB returned null list, initialized to empty.");
        }
        Log.d("Inventory", "Loaded " + allInventoryItems.size() + " items from DB.");

        inventoryItems = new ArrayList<>(allInventoryItems);

        if (inventoryAdapter != null) {
            inventoryAdapter.updateItemList(new ArrayList<>(allInventoryItems));
            filter(inventorySearchView.getQuery().toString());
        }
    }

    private void setupFabClickListener() {
        fabAddDevice.setOnClickListener(view -> {
            InventoryItem newItem = new InventoryItem(
                    " Device Name " + (allInventoryItems.size() + 1),
                    "Model Number",
                    "Serial Number " + System.currentTimeMillis(),
                    "Available"
            );
            addNewDevice(newItem);
        });
    }

    // Sets up the search functionality for the inventory list.
    private void setupSearchListener() {
        inventorySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<InventoryItem> filteredList = new ArrayList<>();
        String filterPattern = text.toLowerCase(Locale.getDefault()).trim();

        if (filterPattern.isEmpty()) {
            filteredList.addAll(allInventoryItems);
        } else {
            for (InventoryItem item : allInventoryItems) {
                if (item.getName().toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                        item.getModel().toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                        item.getSerial().toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                        item.getStatus().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        if (inventoryAdapter != null) {
            inventoryAdapter.filterList(filteredList);
        }
        this.inventoryItems = new ArrayList<>(filteredList);
    }

    // Handles long click on an inventory item, showing a context menu for edit/delete.
    @Override
    public void onItemLongClick(InventoryItem item, int position) {
        InventoryItem currentItem = inventoryAdapter.getCurrentItemList().get(position);

        View itemView = recyclerViewInventory.getLayoutManager().findViewByPosition(position);

        if (itemView != null) {
            PopupMenu popup = new PopupMenu(this, itemView);
            popup.getMenuInflater().inflate(R.menu.inventory_item_context_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_edit_item) {
                    editDevice(currentItem);
                    return true;
                } else if (itemId == R.id.action_delete_item) {
                    confirmDeleteDevice(currentItem);
                    return true;
                } else {
                    return false;
                }
            });
            popup.show();
        } else {
            Toast.makeText(this, "Item view not found for menu.", Toast.LENGTH_SHORT).show();
        }
    }

    // Adds a new inventory item to the database and reloads the inventory list.
    private void addNewDevice(InventoryItem newItem) {
        boolean success = dbHelper.addInventoryItem(newItem);
        if (success) {
            Toast.makeText(this, newItem.getName() + " added to DB", Toast.LENGTH_SHORT).show();
            loadInventoryData();
        } else {
            Toast.makeText(this, "Failed to add " + newItem.getName() + ". Serial might exist.", Toast.LENGTH_LONG).show();
        }
    }

    // Presents a dialog to edit the details of an existing inventory item.
    private void editDevice(InventoryItem itemToEdit) {
        // Keep a copy of the original serial number for the DB update call
        final String originalSerial = itemToEdit.getSerial();

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");
        inputName.setText(itemToEdit.getName());

        final EditText inputModel = new EditText(this);
        inputModel.setHint("Model");
        inputModel.setText(itemToEdit.getModel());

        final EditText inputSerial = new EditText(this);
        inputSerial.setHint("Serial Number");
        inputSerial.setText(itemToEdit.getSerial()); // Current serial

        final EditText inputStatus = new EditText(this);
        inputStatus.setHint("Status");
        inputStatus.setText(itemToEdit.getStatus());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputName);
        layout.addView(inputModel);
        layout.addView(inputSerial);
        layout.addView(inputStatus);



        new AlertDialog.Builder(this)
                .setTitle("Edit Device")
                .setView(layout) // Consider using scrollView here if you add it
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = inputName.getText().toString().trim();
                    String newModel = inputModel.getText().toString().trim();
                    String newSerial = inputSerial.getText().toString().trim();
                    String newStatus = inputStatus.getText().toString().trim();

                    if (newName.isEmpty() || newModel.isEmpty() || newSerial.isEmpty() || newStatus.isEmpty()) {
                        Toast.makeText(Inventory.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    InventoryItem updatedItem = new InventoryItem(
                            newName,
                            newModel,
                            newSerial, // The potentially new serial number
                            newStatus
                    );

                    boolean success = dbHelper.updateInventoryItem(originalSerial, updatedItem);

                    if (success) {
                        Toast.makeText(Inventory.this, updatedItem.getName() + " updated ", Toast.LENGTH_SHORT).show();
                        loadInventoryData(); // Re-fetch and update adapter
                    } else {
                        InventoryItem checkItem = dbHelper.getInventoryItemBySerial(newSerial);
                        if (checkItem != null && !newSerial.equals(originalSerial)) {
                            Toast.makeText(Inventory.this, "Failed to update. New Serial Number '" + newSerial + "' might already exist.", Toast.LENGTH_LONG).show();
                        } else if (dbHelper.getInventoryItemBySerial(originalSerial) == null && !newSerial.equals(originalSerial)) {
                            Toast.makeText(Inventory.this, "Failed to update " + itemToEdit.getName() + ". Item not found or new serial exists.", Toast.LENGTH_LONG).show();
                            loadInventoryData(); // Refresh to see current state
                        } else {
                            Toast.makeText(Inventory.this, "Failed to update " + itemToEdit.getName() + ".", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteDevice(InventoryItem itemToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Device")
                .setMessage("Are you sure you want to delete '" + itemToDelete.getName() + "' (SN: " + itemToDelete.getSerial() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteDevice(itemToDelete);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDevice(InventoryItem itemToDelete) {
        boolean success = dbHelper.deleteInventoryItem(itemToDelete.getSerial());
        if (success) {
            Toast.makeText(this, itemToDelete.getName() + " deleted from DB", Toast.LENGTH_SHORT).show();
            loadInventoryData();
        } else {
            Toast.makeText(this, "Error deleting " + itemToDelete.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private int findItemIndexInMasterList(InventoryItem itemToFind) {
        if (itemToFind == null || itemToFind.getSerial() == null || allInventoryItems == null) {
            return -1;
        }
        for (int i = 0; i < allInventoryItems.size(); i++) {
            if (allInventoryItems.get(i) != null && itemToFind.getSerial().equals(allInventoryItems.get(i).getSerial())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
