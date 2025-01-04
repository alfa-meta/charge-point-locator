package com.example.chargepointlocator;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseHelperTest {
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("UserDatabase.db"); // Cleanup database after each test
    }

    @Test
    public void testAddUser() {
        boolean isUserAdded = dbHelper.addUser("testUser", "testPassword", "John Doe");
        assertTrue("User should be added successfully", isUserAdded);
    }

    @Test
    public void testAddDuplicateUser() {
        dbHelper.addUser("testUser", "testPassword", "John Doe");
        boolean isDuplicateUserAdded = dbHelper.addUser("testUser", "testPassword2", "Jane Doe");
        assertFalse("Duplicate user should not be added", isDuplicateUserAdded);
    }

    @Test
    public void testIsUserValid() {
        dbHelper.addUser("testUser", "testPassword", "John Doe");
        boolean isValidUser = dbHelper.checkUser("testUser", "testPassword");
        assertTrue("Valid user should be found", isValidUser);
    }

    @Test
    public void testIsUserInvalid() {
        dbHelper.addUser("testUser", "testPassword", "John Doe");
        boolean isValidUser = dbHelper.checkUser("wrongUser", "wrongPassword");
        assertFalse("Invalid user should not be found", isValidUser);
    }
}
