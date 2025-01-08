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

public class UserFragment extends Fragment {
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());

        // Find the TableLayout where we'll dynamically add user data
        TableLayout userTable = rootView.findViewById(R.id.userTable);

        // Fetch all users
        Cursor cursor = fetchAllUsers();

        // Loop through the cursor and add user details to the table
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Create a new row for each user
                TableRow tableRow = new TableRow(getContext());

                // Create and configure TextViews for each column
                TextView idTextView = new TextView(getContext());
                idTextView.setText(String.valueOf(id));
                idTextView.setPadding(8, 8, 8, 8);

                TextView nameTextView = new TextView(getContext());
                nameTextView.setText(name);
                nameTextView.setPadding(8, 8, 8, 8);

                TextView usernameTextView = new TextView(getContext());
                usernameTextView.setText(username);
                usernameTextView.setPadding(8, 8, 8, 8);

                // Add TextViews to the row
                tableRow.addView(idTextView);
                tableRow.addView(nameTextView);
                tableRow.addView(usernameTextView);

                // Add the row to the table
                userTable.addView(tableRow);
            } while (cursor.moveToNext());
            cursor.close(); // Close the cursor
        }
        return rootView;
    }

    // Fetch all users from the database
    private Cursor fetchAllUsers() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return db.query("users", null, null, null, null, null, null);
    }
}
