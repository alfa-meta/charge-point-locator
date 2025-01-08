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
public class LoginActivityTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        dbHelper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        dbHelper.addUser("test@email.com", "password", "Test John Doe"); // Add a test user
    }

    @After
    public void tearDown() {
        dbHelper.close();
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .deleteDatabase("ChargePointLocatorDb.db");
    }

    @Test
    public void testLoginSuccess_NavigationToMainActivity() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init();

            // Enter valid email and password
            onView(withId(R.id.emailEditText)).perform(replaceText("test@email.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("password"));
            onView(withId(R.id.loginButton)).perform(click());

            // Verify MainActivity is launched
            Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));

            Intents.release();
        }
    }

    @Test
    public void testLoginInvalidCredentials_NoNavigation() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init();

            // Enter invalid email and password
            onView(withId(R.id.emailEditText)).perform(replaceText("wrong@email.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("wrongpassword"));
            onView(withId(R.id.loginButton)).perform(click());

            // Verify MainActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release();
        }
    }

    @Test
    public void testEmptyFields_NoNavigation() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init();

            // Leave fields empty and click login
            onView(withId(R.id.emailEditText)).perform(replaceText(""));
            onView(withId(R.id.passwordEditText)).perform(replaceText(""));
            onView(withId(R.id.loginButton)).perform(click());

            // Verify MainActivity is not launched
            Intents.assertNoUnverifiedIntents();

            Intents.release();
        }
    }

    @Test
    public void testNavigationToRegisterActivity() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            Intents.init();

            // Click on register button
            onView(withId(R.id.registerButton)).perform(click());

            // Verify that RegisterActivity is launched
            Intents.intended(IntentMatchers.hasComponent(RegisterActivity.class.getName()));

            Intents.release();
        }
    }
}
