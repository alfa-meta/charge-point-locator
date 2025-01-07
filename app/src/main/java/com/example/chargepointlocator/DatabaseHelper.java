package com.example.chargepointlocator;

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

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ChargePointLocatorDb.db";
    private static final int DATABASE_VERSION = 5;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String TABLE_CHARGEPOINTS = "chargepoints";
    private static final String COLUMN_LOCATION_ID = "location_id";
    private static final String SALT = "BigSaltForSecurityReasons111";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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
                + "UNIQUE(latitude, longitude))"; // Add a UNIQUE constraint on latitude and longitude
        db.execSQL(CREATE_CHARGEPOINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHARGEPOINTS);
        onCreate(db);
    }

    // Add a user with hashed password
    public boolean addUser(String username, String password, String name) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_NAME, name);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // Returns true if insertion is successful
    }

    // Check if user exists with hashed password
    public boolean checkUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, hashedPassword},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Import chargepoints from a CSV file
    public void importChargepointsFromCSV(InputStream inputStream) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 9) { // Ensure there are at least 9 columns
                    try {
                        // Use INSERT OR REPLACE for upsert
                        String sql = "INSERT OR REPLACE INTO " + TABLE_CHARGEPOINTS + " (latitude, longitude, referenceID, town, county, postcode, chargeDeviceStatus, connectorID, connectorType) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        db.execSQL(sql, new Object[]{
                                Double.parseDouble(tokens[1].trim()), // latitude
                                Double.parseDouble(tokens[2].trim()), // longitude
                                tokens[0].trim(),                   // referenceID
                                tokens[3].trim(),                   // town
                                tokens[4].trim(),                   // county
                                tokens[5].trim(),                   // postcode
                                tokens[6].trim(),                   // chargeDeviceStatus
                                tokens[7].trim(),                   // connectorID
                                tokens[8].trim()                    // connectorType
                        });
                    } catch (NumberFormatException e) {
                        e.printStackTrace(); // Skip invalid rows
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // Fetch all charge points from the database
    public Cursor getAllChargePoints() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CHARGEPOINTS, null, null, null, null, null, null);
    }

    public void addChargePoint(String referenceID, double latitude, double longitude, String town, String county, String postcode, String status, String connectorID, String connectorType) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use the ON CONFLICT DO UPDATE clause for precise control
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


    // Hash password with salt using MD5
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((SALT + password).getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}