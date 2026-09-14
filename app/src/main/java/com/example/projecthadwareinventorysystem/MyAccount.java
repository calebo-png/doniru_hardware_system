package com.example.projecthadwareinventorysystem;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyAccount extends AppCompatActivity {
    TextView edtNameAcc, edtEmailAcc;
    Spinner spinnerDept;
    MyDBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtNameAcc = findViewById(R.id.edtNameAcc);
        edtEmailAcc = findViewById(R.id.edtEmailAcc);
        spinnerDept = findViewById(R.id.spinnerDept); // Initialize the Spinner

        dbHelper = new MyDBHelper(this);

        // --- Spinner Setup ---
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.department_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerDept.setAdapter(adapter);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Dashboard.EXTRA_STAFF_EMAIL_TO_DISPLAY)) {
            String staffEmailToDisplay = intent.getStringExtra(Dashboard.EXTRA_STAFF_EMAIL_TO_DISPLAY);
            if (staffEmailToDisplay != null && !staffEmailToDisplay.isEmpty()) {
                loadStaffData(staffEmailToDisplay);
            } else {
                Log.e("MyAccount", "Received null or empty staff email from Dashboard.");
                Toast.makeText(this, "Error: Could not load staff details (invalid email).", Toast.LENGTH_LONG).show();
                setFieldsToNA();
            }
        } else {
            Log.e("MyAccount", "Error: Staff email not found in Intent from Dashboard.");
            Toast.makeText(this, "Error: Staff details not available.", Toast.LENGTH_LONG).show();
            setFieldsToNA();
        }

    }

    private void loadStaffData(String email) {
        Bundle staffDetails = dbHelper.getStaffDetailsBundleByEmail(email);
        Log.d("MyAccount", "Loading data for email: " + email);

        if (staffDetails != null) {
            Log.d("MyAccount", "StaffDetails Bundle is NOT null.");
            String firstName = staffDetails.getString("first_name", "N/A");
            String lastName = staffDetails.getString("last_name", "N/A");
            String staffEmail = staffDetails.getString("email", "N/A");
            String department = staffDetails.getString("department", "");

            Log.d("MyAccount", "Retrieved from Bundle - Name: " + firstName + " " + lastName + ", Email: " + staffEmail + ", Dept: " + department);

            String fullName = (firstName + " " + lastName).trim();
            if (fullName.equals("N/A N/A") || fullName.isEmpty() || fullName.equals("N/A")) {
                fullName = "N/A";
            }

            edtNameAcc.setText(fullName);
            edtEmailAcc.setText(staffEmail);

            if (department != null && !department.isEmpty()) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerDept.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(department);
                    if (position >= 0) { // Check if the item exists in the adapter
                        spinnerDept.setSelection(position);
                    } else {
                        Log.w("MyAccount", "Department '" + department + "' not found in Spinner. Defaulting to first item.");
                    }
                }
            } else {
                Log.d("MyAccount", "Department from DB is null or empty. Spinner will show default selection.");
            }

        } else {
            Log.e("MyAccount", "No staff details bundle found in DB for email: " + email);
            Toast.makeText(this, "Could not retrieve staff details.", Toast.LENGTH_LONG).show();
            setFieldsToNA();
        }
    }

    private void setFieldsToNA() {
        edtNameAcc.setText("N/A");
        edtEmailAcc.setText("N/A");
        if (spinnerDept != null && spinnerDept.getAdapter() != null && spinnerDept.getAdapter().getCount() > 0) {
            spinnerDept.setSelection(0); // Select the first item as a default "N/A" state
        }
    }
}
