package com.example.chargepointlocator;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        dbHelper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .deleteDatabase("ChargePointLocatorDb.db"); // Reset the database for clean tests
    }

    @After
    public void tearDown() {
        dbHelper.close();
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .deleteDatabase("ChargePointLocatorDb.db");
    }

    @Test
    public void testSuccessfulRegistration_NavigationToLoginActivity() {
        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            Intents.init();

            // Fill in valid registration details
            onView(withId(R.id.nameEditText)).perform(replaceText("John Doe"));
            onView(withId(R.id.emailEditText)).perform(replaceText("johndoe@example.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
            onView(withId(R.id.registerButton)).perform(click());

            // Verify LoginActivity is launched
            Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));

            Intents.release();
        }
    }

    @Test
    public void testRegistrationWithExistingUser_ShowsToast() {
        dbHelper.addUser("existinguser@example.com", "password123", "Existing User");

        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            Intents.init();

            // Fill in details for an existing user
            onView(withId(R.id.nameEditText)).perform(replaceText("Existing User"));
            onView(withId(R.id.emailEditText)).perform(replaceText("existinguser@example.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
            onView(withId(R.id.registerButton)).perform(click());

            // Check that LoginActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release();
        }
    }

    @Test
    public void testEmptyFields_ShowsToast() {
        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            Intents.init();

            // Leave fields empty and click register
            onView(withId(R.id.nameEditText)).perform(replaceText(""));
            onView(withId(R.id.emailEditText)).perform(replaceText(""));
            onView(withId(R.id.passwordEditText)).perform(replaceText(""));
            onView(withId(R.id.registerButton)).perform(click());

            // Verify LoginActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release();
        }
    }

    @Test
    public void testInvalidEmail_ShowsToast() {
        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            Intents.init();

            // Fill in an invalid email
            onView(withId(R.id.nameEditText)).perform(replaceText("John Doe"));
            onView(withId(R.id.emailEditText)).perform(replaceText("invalidemail"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
            onView(withId(R.id.registerButton)).perform(click());

            // Verify LoginActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release();
        }
    }

    @Test
    public void testNavigationToLoginActivity() {
        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            Intents.init();

            // Click on the "Go to Login" button
            onView(withId(R.id.goToLoginButton)).perform(click());

            // Verify LoginActivity is launched
            Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));

            Intents.release();
        }
    }
}
