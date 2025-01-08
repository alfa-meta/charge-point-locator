package com.example.chargepointlocator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper is a helper class to manage SQLite database creation, version management,
 * and operations for user and charge point data in the ChargePointLocator app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database details
    private static final String DATABASE_NAME = "ChargePointLocatorDb.db";
    private static final int DATABASE_VERSION = 6;

    // Users table and columns
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";

    // Charge points table and columns
    private static final String TABLE_CHARGEPOINTS = "chargepoints";
    private static final String COLUMN_LOCATION_ID = "location_id";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Creates the tables for users and charge points.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_NAME + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_CHARGEPOINTS_TABLE = "CREATE TABLE " + TABLE_CHARGEPOINTS + " ("
                + COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "referenceID TEXT, "
                + "latitude REAL, "
                + "longitude REAL, "
                + "town TEXT, "
                + "county TEXT, "
                + "postcode TEXT, "
                + "chargeDeviceStatus TEXT, "
                + "connectorID TEXT, "
                + "connectorType TEXT, "
                + "UNIQUE(latitude, longitude))"; // Ensures unique charge points by location
        db.execSQL(CREATE_CHARGEPOINTS_TABLE);
    }

    /**
     * Called when the database version is updated.
     * Drops old tables and recreates them to reflect the new structure.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHARGEPOINTS);
        onCreate(db);
    }

    /**
     * Adds a new user to the database with a hashed password.
     */
    public boolean addUser(String username, String password, String name) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false; // Handle hashing errors

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_NAME, name);

        long result = db.insert(TABLE_USERS, null, values); // Insert user data
        db.close();
        return result != -1; // Return true if insertion succeeds
    }

    /**
     * Verifies if a user exists by checking their username and hashed password.
     */
    public boolean checkUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false; // Handle hashing errors

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, hashedPassword},
                null, null, null);

        boolean exists = cursor.getCount() > 0; // Check if a matching user exists
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Imports charge points from a CSV file, ensuring data integrity with transactions.
     */
    public void importChargepointsFromCSV(InputStream inputStream) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction(); // Start a transaction for better performance and integrity
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 9) { // Validate row data
                    try {
                        // Parse and assign default values for each column
                        String latitude = tokens[1].trim().isEmpty() ? "0" : tokens[1].trim();
                        String longitude = tokens[2].trim().isEmpty() ? "0" : tokens[2].trim();
                        String referenceID = tokens[0].trim().isEmpty() ? "Unknown" : tokens[0].trim();
                        String town = tokens[3].trim().isEmpty() ? "" : tokens[3].trim();
                        String county = tokens[4].trim().isEmpty() ? "" : tokens[4].trim();
                        String postcode = tokens[5].trim().isEmpty() ? "" : tokens[5].trim();
                        String chargeDeviceStatus = tokens[6].trim().isEmpty() ? "Out of Service" : tokens[6].trim();
                        String connectorID = tokens[7].trim().isEmpty() ? "" : tokens[7].trim();
                        String connectorType = tokens[8].trim().isEmpty() ? "Unknown" : tokens[8].trim();

                        // Insert data into charge points table
                        String sql = "INSERT OR REPLACE INTO " + TABLE_CHARGEPOINTS +
                                " (latitude, longitude, referenceID, town, county, postcode, chargeDeviceStatus, connectorID, connectorType) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        db.execSQL(sql, new Object[]{
                                Double.parseDouble(latitude),
                                Double.parseDouble(longitude),
                                referenceID,
                                town,
                                county,
                                postcode,
                                chargeDeviceStatus,
                                connectorID,
                                connectorType
                        });
                    } catch (NumberFormatException e) {
                        e.printStackTrace(); // Handle invalid data rows
                    }
                }
            }
            db.setTransactionSuccessful(); // Commit the transaction
        } catch (IOException e) {
            e.printStackTrace(); // Handle file read errors
        } finally {
            db.endTransaction(); // End the transaction
        }
    }

    /**
     * Retrieves all charge points from the database.
     */
    public Cursor getAllChargePoints() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                "referenceID, " +
                "latitude, " +
                "longitude, " +
                "COALESCE(town, '') AS town, " +
                "COALESCE(county, '') AS county, " +
                "COALESCE(postcode, '') AS postcode, " +
                "COALESCE(chargeDeviceStatus, 'Out of Service') AS chargeDeviceStatus, " +
                "COALESCE(connectorID, '') AS connectorID, " +
                "COALESCE(connectorType, 'Unknown') AS connectorType " +
                "FROM " + TABLE_CHARGEPOINTS;
        return db.rawQuery(query, null); // Return cursor for charge point data
    }

    /**
     * Adds or updates a charge point in the database.
     */
    public void addChargePoint(String referenceID, double latitude, double longitude, String town, String county, String postcode, String status, String connectorID, String connectorType) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use the ON CONFLICT DO UPDATE clause to handle duplicate locations
        String sql = "INSERT INTO " + TABLE_CHARGEPOINTS + " (latitude, longitude, referenceID, town, county, postcode, chargeDeviceStatus, connectorID, connectorType) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(latitude, longitude) DO UPDATE SET "
                + "referenceID = excluded.referenceID, "
                + "town = excluded.town, "
                + "county = excluded.county, "
                + "postcode = excluded.postcode, "
                + "chargeDeviceStatus = excluded.chargeDeviceStatus, "
                + "connectorID = excluded.connectorID, "
                + "connectorType = excluded.connectorType";
        db.execSQL(sql, new Object[]{latitude, longitude, referenceID, town, county, postcode, status, connectorID, connectorType});
        db.close();
    }

    /**
     * Hashes a password using MD5 and a salt defined in BuildConfig.
     */
    private String hashPassword(String password) {
        try {
            String salt = BuildConfig.SALT; // Use salt from BuildConfig
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((salt + password).getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // Return hashed password
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Handle hashing errors
            return null;
        }
    }

    /**
     * Checks if the charge points table has any data.
     */
    public boolean hasData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_CHARGEPOINTS;
        Cursor cursor = db.rawQuery(query, null);

        boolean hasData = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                hasData = count > 0; // True if there are rows in the table
            }
            cursor.close();
        }
        db.close();
        return hasData;
    }

    /**
     * Retrieves the full name of a user based on their username.
     */
    public String getCurrentUserFullName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String fullName = null;

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_NAME},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            }
            cursor.close();
        }
        db.close();
        return fullName; // Return user's full name
    }

    /**
     * Deletes a charge point from the database based on its reference ID.
     */
    public void deleteChargePoint(String referenceID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("chargePoints", "referenceID = ?", new String[]{referenceID});
        db.close();
    }

    /**
     * Retrieves a list of unique charger types from the database.
     */
    @SuppressLint("Range")
    public List<String> getUniqueChargerTypes() {
        List<String> chargerTypes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT connectorType FROM chargepoints WHERE connectorType IS NOT NULL AND connectorType != ''";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String connectorType = cursor.getString(cursor.getColumnIndex("connectorType"));
                if (connectorType != null && !connectorType.isEmpty()) {
                    chargerTypes.add(connectorType);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return chargerTypes; // Return list of charger types
    }
}
