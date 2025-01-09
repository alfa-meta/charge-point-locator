package com.example.chargepointlocator;

import static org.junit.Assert.*;
import android.content.Context;
import android.database.Cursor;
import org.junit.runner.RunWith;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Unit tests for the DatabaseHelper class.
 * These tests validate the core functionality of the database helper methods,
 * ensuring data integrity and correct behavior for common operations.
 */
@RunWith(JUnit4.class)
public class DatabaseHelperTest {

    private DatabaseHelper dbHelper;
    private Context context;

    /**
     * Set up resources before each test.
     * Initializes the database helper and test context.
     */
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Clean up resources after each test.
     * Closes the database and deletes the test database file to maintain isolation between tests.
     */
    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("ChargePointLocatorDb.db"); // Clean up database
    }

    /**
     * Test to verify the addUser method.
     * Ensures a new user can be successfully added to the database.
     */
    @Test
    public void testAddUser() {
        boolean result = dbHelper.addUser("testuser", "testpassword", "Test Name");
        assertTrue("User should be added successfully", result);
    }

    /**
     * Test to verify the checkUser method for valid credentials.
     * Ensures the method returns true when the user exists with correct credentials.
     */
    @Test
    public void testCheckUser_ValidUser() {
        dbHelper.addUser("testuser", "testpassword", "Test Name");
        boolean result = dbHelper.checkUser("testuser", "testpassword");
        assertTrue("User should exist with valid credentials", result);
    }

    /**
     * Test to verify the checkUser method for invalid credentials.
     * Ensures the method returns false when the user does not exist or credentials are incorrect.
     */
    @Test
    public void testCheckUser_InvalidUser() {
        dbHelper.addUser("testuser", "testpassword", "Test Name");
        boolean result = dbHelper.checkUser("invaliduser", "wrongpassword");
        assertFalse("User should not exist with invalid credentials", result);
    }

    /**
     * Test to verify the importChargepointsFromCSV method.
     * Ensures CSV data is correctly parsed and imported into the database.
     */
    @Test
    public void testImportChargepointsFromCSV() {
        String csvData = "ID,51.5074,-0.1278,London,Greater London,SW1A 1AA,Available,123,Type1\n" +
                "ID2,53.4808,-2.2426,Manchester,Greater Manchester,M1 1AE,Busy,456,Type2";
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

        dbHelper.importChargepointsFromCSV(inputStream);

        Cursor cursor = dbHelper.getAllChargePoints();
        assertNotNull("Cursor should not be null", cursor);
        assertTrue("Database should contain imported chargepoints", cursor.getCount() > 0);

        cursor.close();
    }

    /**
     * Test to verify the getAllChargePoints method.
     * Ensures all charge points are retrieved correctly from the database.
     */
    @Test
    public void testGetAllChargePoints() {
        dbHelper.addChargePoint("ID1", 51.5074, -0.1278, "London", "Greater London", "SW1A 1AA", "Available", "123", "Type1");
        dbHelper.addChargePoint("ID2", 53.4808, -2.2426, "Manchester", "Greater Manchester", "M1 1AE", "Busy", "456", "Type2");

        Cursor cursor = dbHelper.getAllChargePoints();
        assertNotNull("Cursor should not be null", cursor);
        assertEquals("Should return 2 chargepoints", 2, cursor.getCount());

        cursor.close();
    }

    /**
     * Test to verify the hasData method when no data exists.
     * Ensures the method returns false when the database is empty.
     */
    @Test
    public void testHasData_NoData() {
        boolean result = dbHelper.hasData();
        assertFalse("Database should initially have no data", result);
    }

    /**
     * Test to verify the hasData method when data exists.
     * Ensures the method returns true when data is present in the database.
     */
    @Test
    public void testHasData_WithData() {
        dbHelper.addChargePoint("ID1", 51.5074, -0.1278, "London", "Greater London", "SW1A 1AA", "Available", "123", "Type1");
        boolean result = dbHelper.hasData();
        assertTrue("Database should have data after insertion", result);
    }

    /**
     * Test to verify the getCurrentUserFullName method.
     * Ensures the method correctly retrieves the full name of a user by their username.
     */
    @Test
    public void testGetCurrentUserFullName() {
        dbHelper.addUser("testuser", "testpassword", "Test Name");
        String fullName = dbHelper.getCurrentUserFullName("testuser");
        assertEquals("Full name should match the one added", "Test Name", fullName);
    }

    /**
     * Test to verify the deleteChargePoint method.
     * Ensures a specific charge point can be successfully deleted from the database.
     */
    @Test
    public void testDeleteChargePoint() {
        dbHelper.addChargePoint("ID1", 51.5074, -0.1278, "London", "Greater London", "SW1A 1AA", "Available", "123", "Type1");
        dbHelper.deleteChargePoint("ID1");

        Cursor cursor = dbHelper.getAllChargePoints();
        assertNotNull("Cursor should not be null", cursor);
        assertEquals("Chargepoint should be deleted", 0, cursor.getCount());

        cursor.close();
    }

    /**
     * Test to verify the getUniqueChargerTypes method.
     * Ensures the method returns a list of unique charger types in the database.
     */
    @Test
    public void testGetUniqueChargerTypes() {
        dbHelper.addChargePoint("ID1", 51.5074, -0.1278, "London", "Greater London", "SW1A 1AA", "Available", "123", "Type1");
        dbHelper.addChargePoint("ID2", 53.4808, -2.2426, "Manchester", "Greater Manchester", "M1 1AE", "Busy", "456", "Type2");

        List<String> uniqueTypes = dbHelper.getUniqueChargerTypes();
        assertNotNull("Unique charger types should not be null", uniqueTypes);
        assertEquals("Should return 2 unique charger types", 2, uniqueTypes.size());
        assertTrue("List should contain Type1", uniqueTypes.contains("Type1"));
        assertTrue("List should contain Type2", uniqueTypes.contains("Type2"));
    }
}
