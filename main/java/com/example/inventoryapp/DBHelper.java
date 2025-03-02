package com.example.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// DBHelper manages creation and version management of our database.
public class DBHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "MyAppDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_INVENTORY = "inventory";

    // Columns for Users table
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Columns for Inventory table
    public static final String COLUMN_ITEM_ID = "id";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_QUANTITY = "quantity";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USERNAME + " TEXT UNIQUE," +
                COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Inventory table
        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "(" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ITEM_NAME + " TEXT," +
                COLUMN_QUANTITY + " INTEGER" + ")";
        db.execSQL(CREATE_INVENTORY_TABLE);
    }

    // Called when database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // Check if a user with provided username and password exists
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(query, new String[] {username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Add a new user to the database
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    // Add an inventory item to the database
    public long addInventoryItem(String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_QUANTITY, quantity);
        long result = db.insert(TABLE_INVENTORY, null, values);
        db.close();
        return result;
    }

    // Update an existing inventory item
    public int updateInventoryItem(int id, String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_QUANTITY, quantity);
        int result = db.update(TABLE_INVENTORY, values, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    // Delete an inventory item
    public int deleteInventoryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    // Retrieve all inventory items for display
    public Cursor getAllInventoryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_INVENTORY;
        return db.rawQuery(query, null);
    }

    // Retrieve a specific inventory item using its unique ID.
    public Cursor getInventoryItemById(int id) {
        // Get a readable instance of the database.
        SQLiteDatabase db = this.getReadableDatabase();
        // Build the query to select the row with the specified ID.
        String query = "SELECT * FROM " + TABLE_INVENTORY + " WHERE " + COLUMN_ITEM_ID + "=?";
        // Execute the query and return the cursor.
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    // Retrieve an inventory item by name (case-insensitive)
    public Cursor getInventoryItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_INVENTORY + " WHERE LOWER(" + COLUMN_ITEM_NAME + ") = LOWER(?)";
        return db.rawQuery(query, new String[]{name});
    }


}
