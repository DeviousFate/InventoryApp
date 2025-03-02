package com.example.inventoryapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 101;
    private static final String TAG = "DashboardActivity";

    // UI elements for adding items
    private EditText etItemName, etQuantity;
    private Button btnAdd, btnTestSMS;

    // UI elements for operations by ID
    private EditText etSearchId;
    private Button btnSearch, btnRemove, btnUpdate;

    private GridView gridView;
    private DBHelper dbHelper;
    private ArrayList<String> inventoryList;
    private ArrayList<Integer> itemIds;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI elements for adding new items
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        btnAdd = findViewById(R.id.btnAdd);
        btnTestSMS = findViewById(R.id.btnTestSMS);

        // Initialize UI elements for operations (search, remove, update)
        etSearchId = findViewById(R.id.etSearchId);
        btnSearch = findViewById(R.id.btnSearch);
        btnRemove = findViewById(R.id.btnRemove);
        btnUpdate = findViewById(R.id.btnUpdate);

        gridView = findViewById(R.id.gridView);

        // Initialize DB helper and lists
        dbHelper = new DBHelper(this);
        inventoryList = new ArrayList<>();
        itemIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, inventoryList);
        gridView.setAdapter(adapter);

        // Load current inventory
        loadInventoryItems();

        // Add new inventory item (with duplicate check)
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addInventoryItem();
            }
        });

        // Test SMS Alert functionality
        btnTestSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMSAlert("Test Item", 1);
            }
        });

        checkSMSPermission();

        // Search for an item by unique ID
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = etSearchId.getText().toString().trim();
                if (idStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Please enter an ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                int searchId;
                try {
                    searchId = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardActivity.this, "Invalid ID format", Toast.LENGTH_SHORT).show();
                    return;
                }
                Cursor cursor = dbHelper.getInventoryItemById(searchId);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(DBHelper.COLUMN_ITEM_NAME);
                    int quantityIndex = cursor.getColumnIndex(DBHelper.COLUMN_QUANTITY);
                    if (nameIndex == -1 || quantityIndex == -1) {
                        Toast.makeText(DashboardActivity.this, "Column not found", Toast.LENGTH_SHORT).show();
                    } else {
                        String itemName = cursor.getString(nameIndex);
                        int quantity = cursor.getInt(quantityIndex);
                        Toast.makeText(DashboardActivity.this,
                                "Found: " + itemName + " - Quantity: " + quantity,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "No item found with that ID", Toast.LENGTH_SHORT).show();
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        });

        // Remove an item by unique ID
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = etSearchId.getText().toString().trim();
                if (idStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Please enter an ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                int id;
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardActivity.this, "Invalid ID format", Toast.LENGTH_SHORT).show();
                    return;
                }
                int result = dbHelper.deleteInventoryItem(id);
                if (result > 0) {
                    Toast.makeText(DashboardActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                    loadInventoryItems();
                } else {
                    Toast.makeText(DashboardActivity.this, "Item not found or could not be removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update an item by unique ID
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = etSearchId.getText().toString().trim();
                if (idStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Please enter an ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                int id;
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardActivity.this, "Invalid ID format", Toast.LENGTH_SHORT).show();
                    return;
                }
                Cursor cursor = dbHelper.getInventoryItemById(id);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(DBHelper.COLUMN_ITEM_NAME);
                    int quantityIndex = cursor.getColumnIndex(DBHelper.COLUMN_QUANTITY);
                    if (nameIndex == -1 || quantityIndex == -1) {
                        Toast.makeText(DashboardActivity.this, "Column not found", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return;
                    }
                    String currentName = cursor.getString(nameIndex);
                    int currentQuantity = cursor.getInt(quantityIndex);
                    cursor.close();
                    showUpdateDialog(id, currentName, currentQuantity);
                } else {
                    Toast.makeText(DashboardActivity.this, "No item found with that ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Load all inventory items and refresh the grid view.
    private void loadInventoryItems() {
        inventoryList.clear();
        itemIds.clear();
        Cursor cursor = dbHelper.getAllInventoryItems();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ITEM_ID));
                String itemName = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_QUANTITY));
                inventoryList.add("ID: " + id + "\n" + itemName + ": " + quantity);
                itemIds.add(id);
                // Send SMS alert if inventory is low
                if (quantity < 5) {
                    sendSMSAlert(itemName, quantity);
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    // Add a new inventory item. Check for duplicates by name (case-insensitive) before adding.
    private void addInventoryItem() {
        String itemName = etItemName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        if (itemName.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter item name and quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a number", Toast.LENGTH_SHORT).show();
            return;
        }
        // Use the new duplicate-check function
        checkDuplicateAndAddOrUpdate(itemName, quantity);
        etItemName.setText("");
        etQuantity.setText("");
    }

    // Check for duplicate item name (case-insensitive). If found, prompt the user whether to update or add new.
    private void checkDuplicateAndAddOrUpdate(final String itemName, final int quantity) {
        Cursor cursor = dbHelper.getInventoryItemByName(itemName);
        if (cursor != null && cursor.moveToFirst()) {
            // Duplicate found – use the first found record.
            final int existingId = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ITEM_ID));
            final int existingQuantity = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_QUANTITY));
            cursor.close();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Duplicate Item Found");
            builder.setMessage("An item with the name '" + itemName + "' already exists (Quantity: " + existingQuantity + ").\n" +
                    "Would you like to update the existing item (adding the new quantity) or add a separate new item?");
            builder.setPositiveButton("Update Existing", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Update: add new quantity to existing quantity.
                    int newQuantity = existingQuantity + quantity;
                    int result = dbHelper.updateInventoryItem(existingId, itemName, newQuantity);
                    if (result > 0) {
                        Toast.makeText(DashboardActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                        loadInventoryItems();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Add New", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    long result = dbHelper.addInventoryItem(itemName, quantity);
                    if (result != -1) {
                        Toast.makeText(DashboardActivity.this, "New item added", Toast.LENGTH_SHORT).show();
                        loadInventoryItems();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Error adding new item", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNeutralButton("Cancel", null);
            builder.show();
        } else {
            if (cursor != null) {
                cursor.close();
            }
            // No duplicate found; add item directly.
            long result = dbHelper.addInventoryItem(itemName, quantity);
            if (result != -1) {
                Toast.makeText(DashboardActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                loadInventoryItems();
            } else {
                Toast.makeText(DashboardActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Display a dialog for updating an inventory item.
    private void showUpdateDialog(final int itemId, String currentName, int currentQuantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Item");

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_item, null);
        final EditText etUpdateName = dialogView.findViewById(R.id.etUpdateName);
        final EditText etUpdateQuantity = dialogView.findViewById(R.id.etUpdateQuantity);
        etUpdateName.setText(currentName);
        etUpdateQuantity.setText(String.valueOf(currentQuantity));
        builder.setView(dialogView);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = etUpdateName.getText().toString().trim();
                String newQuantityStr = etUpdateQuantity.getText().toString().trim();
                if (newName.isEmpty() || newQuantityStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                int newQuantity;
                try {
                    newQuantity = Integer.parseInt(newQuantityStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardActivity.this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }
                int updateResult = dbHelper.updateInventoryItem(itemId, newName, newQuantity);
                if (updateResult > 0) {
                    Toast.makeText(DashboardActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                    loadInventoryItems();
                } else {
                    Toast.makeText(DashboardActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Request SMS permission if not already granted.
    private void checkSMSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. Alerts will not be sent.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "SMS permission denied by the user.");
            }
        }
    }

    // Send an SMS alert if inventory is low.
    private void sendSMSAlert(String itemName, int quantity) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            String phoneNumber = "1234567890"; // Replace with a valid number or user-configurable value
            String message = "Alert: Low inventory for " + itemName + ". Only " + quantity + " left.";
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "SMS alert sent", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "SMS failed, please try again later!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending SMS", e);
            }
        } else {
            Log.d(TAG, "SMS permission not granted; skipping alert.");
        }
    }
}