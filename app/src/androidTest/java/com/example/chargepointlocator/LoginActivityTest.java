package com.example.chargepointlocator;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// Specifies that this test will run with the AndroidJUnit4 test runner
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private DatabaseHelper dbHelper; // Database helper instance for managing the test database

    @Before
    public void setUp() {
        // Initialize the database helper and add a test user before each test
        dbHelper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        dbHelper.addUser("test@email.com", "password", "Test John Doe"); // Add a test user
    }

    @After
    public void tearDown() {
        // Clean up resources and delete the database after each test
        dbHelper.close();
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .deleteDatabase("ChargePointLocatorDb.db");
    }

    @Test
    public void testLoginSuccess_NavigationToMainActivity() {
        // Test case for successful login and navigation to MainActivity
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init(); // Initialize Espresso intents

            // Enter valid email and password
            onView(withId(R.id.emailEditText)).perform(replaceText("test@email.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("password"));
            onView(withId(R.id.loginButton)).perform(click());

            // Verify that MainActivity is launched
            Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));

            Intents.release(); // Release Espresso intents
        }
    }

    @Test
    public void testLoginInvalidCredentials_NoNavigation() {
        // Test case for invalid login credentials where navigation should not happen
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init(); // Initialize Espresso intents

            // Enter invalid email and password
            onView(withId(R.id.emailEditText)).perform(replaceText("wrong@email.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("wrongpassword"));
            onView(withId(R.id.loginButton)).perform(click());

            // Verify that MainActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release(); // Release Espresso intents
        }
    }

    @Test
    public void testEmptyFields_NoNavigation() {
        // Test case for empty fields where navigation should not happen
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init(); // Initialize Espresso intents

            // Leave email and password fields empty and click login
            onView(withId(R.id.emailEditText)).perform(replaceText(""));
            onView(withId(R.id.passwordEditText)).perform(replaceText(""));
            onView(withId(R.id.loginButton)).perform(click());

            // Verify that MainActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release(); // Release Espresso intents
        }
    }

    @Test
    public void testNavigationToRegisterActivity() {
        // Test case for navigation to the RegisterActivity when register button is clicked
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init(); // Initialize Espresso intents

            // Click the register button
            onView(withId(R.id.registerButton)).perform(click());

            // Verify that RegisterActivity is launched
            Intents.intended(IntentMatchers.hasComponent(RegisterActivity.class.getName()));

            Intents.release(); // Release Espresso intents
        }
    }
}
