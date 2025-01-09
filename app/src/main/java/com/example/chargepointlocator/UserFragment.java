package com.example.chargepointlocator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * UserFragment is responsible for displaying a list of users from the database
 * in a table format. It inflates the layout, queries the database for user data,
 * and dynamically populates a TableLayout with the results.
 */
public class UserFragment extends Fragment {

    // Helper class to manage database creation and version management
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout from XML
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize the DatabaseHelper to access the database
        databaseHelper = new DatabaseHelper(getContext());

        // Locate the TableLayout in the inflated layout to display user data
        TableLayout userTable = rootView.findViewById(R.id.userTable);

        // Fetch all user records from the database
        Cursor cursor = fetchAllUsers();

        // Check if the cursor contains data and move it to the first row
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve user details from the current row
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Create a new TableRow to represent the user
                TableRow tableRow = new TableRow(getContext());

                // Create a TextView for the user's ID and configure it
                TextView idTextView = new TextView(getContext());
                idTextView.setText(String.valueOf(id));
                idTextView.setPadding(8, 8, 8, 8);

                // Create a TextView for the user's name and configure it
                TextView nameTextView = new TextView(getContext());
                nameTextView.setText(name);
                nameTextView.setPadding(8, 8, 8, 8);

                // Create a TextView for the user's username and configure it
                TextView usernameTextView = new TextView(getContext());
                usernameTextView.setText(username);
                usernameTextView.setPadding(8, 8, 8, 8);

                // Add the TextViews to the TableRow
                tableRow.addView(idTextView);
                tableRow.addView(nameTextView);
                tableRow.addView(usernameTextView);

                // Add the TableRow to the TableLayout
                userTable.addView(tableRow);
            } while (cursor.moveToNext()); // Move to the next row
            cursor.close(); // Close the cursor to free resources
        }

        // Return the populated view to be displayed
        return rootView;
    }

    /**
     * Queries the database to fetch all user records.
     *
     * @return A Cursor containing the user data.
     */
    private Cursor fetchAllUsers() {
        // Get a readable instance of the database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Query the "users" table and return the results
        return db.query("users", null, null, null, null, null, null);
    }
}
