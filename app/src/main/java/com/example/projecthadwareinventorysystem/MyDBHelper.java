package com.example.projecthadwareinventorysystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle; // <<<<<<<< ADD THIS IMPORT
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HardwareInventory.db";
    private static final int DATABASE_VERSION = 3;

    // --- Staff_table ---
    public static final String TABLE_STAFF = "Staff_table";
    public static final String COLUMN_S_STAFF_ID = "S_StaffID";
    public static final String COLUMN_S_FIRST_NAME = "S_FirstName";
    public static final String COLUMN_S_LAST_NAME = "S_LastName";
    public static final String COLUMN_S_EMAIL = "S_Email";
    public static final String COLUMN_S_PASSWORD = "S_Password";
    public static final String COLUMN_S_IMAGE_URI = "S_ImageUri";

    //  Inventory_table
    public static final String TABLE_INVENTORY = "Inventory_table";
    public static final String COLUMN_INV_ID = "Inv_ID";
    public static final String COLUMN_INV_NAME = "Inv_Name";
    public static final String COLUMN_INV_MODEL = "Inv_Model";
    public static final String COLUMN_INV_SERIAL_NO = "Inv_SerialNo";
    public static final String COLUMN_INV_STATUS = "Inv_Status";


    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This method is called when the database is created for the first time.
        Log.d("MyDBHelper", "onCreate: Creating tables...");

        String CREATE_STAFF_TABLE = "CREATE TABLE " + TABLE_STAFF + "("
                + COLUMN_S_STAFF_ID + " TEXT PRIMARY KEY," //
                + COLUMN_S_FIRST_NAME + " TEXT NOT NULL,"
                + COLUMN_S_LAST_NAME + " TEXT NOT NULL,"
                + COLUMN_S_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_S_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_S_IMAGE_URI + " TEXT"
                + ")";
        db.execSQL(CREATE_STAFF_TABLE);
        Log.d("MyDBHelper", "onCreate: " + TABLE_STAFF + " created.");

        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "("
                + COLUMN_INV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_INV_NAME + " TEXT NOT NULL,"
                + COLUMN_INV_MODEL + " TEXT,"
                + COLUMN_INV_SERIAL_NO + " TEXT UNIQUE NOT NULL,"
                + COLUMN_INV_STATUS + " TEXT"
                + ")";
        db.execSQL(CREATE_INVENTORY_TABLE);
        Log.d("MyDBHelper", "onCreate: " + TABLE_INVENTORY + " created.");
    }
    // Handles database upgrades by dropping and recreating tables if the schema changes,
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("MyDBHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ".");
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STAFF);
            Log.d("MyDBHelper", "onUpgrade: Dropped " + TABLE_STAFF + ". It will be recreated with the new schema.");


            String CREATE_STAFF_TABLE = "CREATE TABLE " + TABLE_STAFF + "("
                    + COLUMN_S_STAFF_ID + " TEXT PRIMARY KEY,"
                    + COLUMN_S_FIRST_NAME + " TEXT NOT NULL,"
                    + COLUMN_S_LAST_NAME + " TEXT NOT NULL,"
                    + COLUMN_S_EMAIL + " TEXT UNIQUE NOT NULL,"
                    + COLUMN_S_PASSWORD + " TEXT NOT NULL,"
                    + COLUMN_S_IMAGE_URI + " TEXT"
                    + ")";
            db.execSQL(CREATE_STAFF_TABLE);
        }
        if (oldVersion < 2 && newVersion >= 2) {

            Log.d("MyDBHelper", "onUpgrade: Creating " + TABLE_INVENTORY + " as it did not exist in version " + oldVersion);
            String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "("
                    + COLUMN_INV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INV_NAME + " TEXT NOT NULL,"
                    + COLUMN_INV_MODEL + " TEXT,"
                    + COLUMN_INV_SERIAL_NO + " TEXT UNIQUE NOT NULL,"
                    + COLUMN_INV_STATUS + " TEXT"
                    + ")";
            db.execSQL(CREATE_INVENTORY_TABLE);
            Log.d("MyDBHelper", "onUpgrade: " + TABLE_INVENTORY + " created during upgrade.");
        }
    }

    // Staff Methods
     //Adds a new staff member to the Staff_table.Passwords should ideally be hashed before storing for security.
    public boolean addStaff(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_S_FIRST_NAME, firstName);
        values.put(COLUMN_S_LAST_NAME, lastName);
        values.put(COLUMN_S_EMAIL, email);
        values.put(COLUMN_S_PASSWORD, password); // Consider hashing passwords

        long result = db.insert(TABLE_STAFF, null, values);
        db.close();
        return result != -1;
    }

    // Checks if a staff member exists with the given email and password.
    public boolean checkStaff(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_S_STAFF_ID};
        String selection = COLUMN_S_EMAIL + " = ? AND " + COLUMN_S_PASSWORD + " = ?";
        String[] selectionArgs = {email, password}; // In real apps, compare hashed passwords
        Cursor cursor = db.query(TABLE_STAFF, columns, selection, selectionArgs, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

    // Checks if a staff member exists with the given email.
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_S_STAFF_ID};
        String selection = COLUMN_S_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_STAFF, columns, selection, selectionArgs, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

    // Retrieves staff details (first name, last name, email) by email and returns them in a Bundle.
    public Bundle getStaffDetailsBundleByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Bundle staffDetailsBundle = null;

        try {
            // Define only the columns you need: FirstName, LastName, Email
            String[] columns = {
                    COLUMN_S_FIRST_NAME,
                    COLUMN_S_LAST_NAME,
                    COLUMN_S_EMAIL
            };
            String selection = COLUMN_S_EMAIL + " = ?";
            String[] selectionArgs = {email};

            cursor = db.query(TABLE_STAFF, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int firstNameIndex = cursor.getColumnIndex(COLUMN_S_FIRST_NAME);
                int lastNameIndex = cursor.getColumnIndex(COLUMN_S_LAST_NAME);
                int emailIndex = cursor.getColumnIndex(COLUMN_S_EMAIL);

                // Adjust the condition to check only for the columns we are fetching
                if (firstNameIndex != -1 && lastNameIndex != -1 && emailIndex != -1) {
                    staffDetailsBundle = new Bundle();
                    // staffDetailsBundle.putString("staff_id", cursor.getString(idIndex)); // <<<< REMOVE
                    staffDetailsBundle.putString("first_name", cursor.getString(firstNameIndex));
                    staffDetailsBundle.putString("last_name", cursor.getString(lastNameIndex));
                    staffDetailsBundle.putString("email", cursor.getString(emailIndex));
                } else {
                    Log.e("MyDBHelper", "One or more required columns (FirstName, LastName, Email) not found for email: " + email);
                }
            }
        } catch (Exception e) {
            Log.e("MyDBHelper", "Error getting staff details bundle by email: " + email, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close(); // Important to close the database connection
        }
        return staffDetailsBundle;
    }


    // Inventory Methods

    // Adds a new inventory item to the Inventory_table.
    public boolean addInventoryItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_NAME, item.getName());
        values.put(COLUMN_INV_MODEL, item.getModel());
        values.put(COLUMN_INV_SERIAL_NO, item.getSerial());
        values.put(COLUMN_INV_STATUS, item.getStatus());

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_INVENTORY, null, values);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e("MyDBHelper", "Failed to insert inventory item, possibly due to duplicate serial number: " + item.getSerial(), e);
        } finally {
            db.close(); // Ensure DB is closed
        }
        return result != -1;
    }

    // Deletes an inventory item from the Inventory_table based on its serial number.
    public boolean deleteInventoryItem(String serialNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        try {
            result = db.delete(TABLE_INVENTORY, COLUMN_INV_SERIAL_NO + " = ?", new String[]{serialNo});
        } catch (Exception e) {
            Log.e("MyDBHelper", "Error deleting inventory item with serial: " + serialNo, e);
        } finally {
            db.close(); // Ensure DB is closed
        }
        return result > 0;
    }

    // Updates an existing inventory item in the database.
    public boolean updateInventoryItem(String originalSerialNo, InventoryItem updatedItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_NAME, updatedItem.getName());
        values.put(COLUMN_INV_MODEL, updatedItem.getModel());
        values.put(COLUMN_INV_SERIAL_NO, updatedItem.getSerial());
        values.put(COLUMN_INV_STATUS, updatedItem.getStatus());

        int result = 0;
        try {
            result = db.update(TABLE_INVENTORY, values, COLUMN_INV_SERIAL_NO + " = ?", new String[]{originalSerialNo});
            if (result == 0) {
                Log.w("MyDBHelper", "No rows updated for original serial: " + originalSerialNo + ". Item might not exist or serial already changed.");
            }
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e("MyDBHelper", "Failed to update inventory item, new serial '" + updatedItem.getSerial() + "' might already exist.", e);
            return false;
        } catch (Exception e) {
            Log.e("MyDBHelper", "Error updating inventory item with original serial: " + originalSerialNo, e);
            return false;
        } finally {
            db.close();
        }
        return result > 0;
    }


    // Retrieves all inventory items from the database, ordered by name.
    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_INVENTORY, null, null, null, null, null, COLUMN_INV_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(COLUMN_INV_NAME);
                int modelIndex = cursor.getColumnIndex(COLUMN_INV_MODEL);
                int serialIndex = cursor.getColumnIndex(COLUMN_INV_SERIAL_NO);
                int statusIndex = cursor.getColumnIndex(COLUMN_INV_STATUS);

                if (nameIndex == -1 || modelIndex == -1 || serialIndex == -1 || statusIndex == -1) {
                    Log.e("MyDBHelper", "One or more column names not found in inventory table cursor.");
                    return itemList; // Return empty list
                }

                do {
                    String name = cursor.getString(nameIndex);
                    String model = cursor.getString(modelIndex);
                    String serial = cursor.getString(serialIndex);
                    String status = cursor.getString(statusIndex);
                    itemList.add(new InventoryItem(name, model, serial, status));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("MyDBHelper", "Error while trying to get inventory items from database", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close(); // Ensure DB is closed
        }
        return itemList;
    }

    // Retrieves a specific inventory item from the database using its serial number.
    public InventoryItem getInventoryItemBySerial(String serialNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        InventoryItem item = null;

        try {
            String[] columns = {COLUMN_INV_NAME, COLUMN_INV_MODEL, COLUMN_INV_SERIAL_NO, COLUMN_INV_STATUS};
            String selection = COLUMN_INV_SERIAL_NO + " = ?";
            String[] selectionArgs = {serialNo};

            cursor = db.query(TABLE_INVENTORY, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(COLUMN_INV_NAME);
                int modelIndex = cursor.getColumnIndex(COLUMN_INV_MODEL);
                int serialIndex = cursor.getColumnIndex(COLUMN_INV_SERIAL_NO);
                int statusIndex = cursor.getColumnIndex(COLUMN_INV_STATUS);

                if (nameIndex != -1 && modelIndex != -1 && serialIndex != -1 && statusIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    String model = cursor.getString(modelIndex);
                    String serial = cursor.getString(serialIndex);
                    String status = cursor.getString(statusIndex);
                    item = new InventoryItem(name, model, serial, status);
                } else {
                    Log.e("MyDBHelper", "Error fetching columns for inventory item by serial: " + serialNo);
                }
            }
        } catch (Exception e) {
            Log.e("MyDBHelper", "Error getting inventory item by serial " + serialNo, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close(); // Ensure DB is closed
        }
        return item;
    }
}

