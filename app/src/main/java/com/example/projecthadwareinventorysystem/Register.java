package com.example.projecthadwareinventorysystem;

import android.annotation.SuppressLint;
import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.view.View; // Import View
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Import Toast

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {
    EditText regFirstName, regLastName, regEmail, regStaffNumber, regPassword;
    Button regRegister;
    MyDBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new MyDBHelper(this); // Initialize MyDBHelper

        regFirstName = findViewById(R.id.regFirstName);
        regLastName = findViewById(R.id.regLastName);
        regEmail = findViewById(R.id.regEmail);
        regStaffNumber = findViewById(R.id.regStaffNumber);
        regPassword = findViewById(R.id.regPassword);
        regRegister = findViewById(R.id.regRegister);

        regRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    // This method handles the user registration process.
    private void registerUser() {
        String firstName = regFirstName.getText().toString().trim();
        String lastName = regLastName.getText().toString().trim();
        String email = regEmail.getText().toString().trim();
        String staffID = regStaffNumber.getText().toString().trim(); // See note above about S_StaffID
        String password = regPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            regFirstName.setError("First name is required");
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            regLastName.setError("Last name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            regEmail.setError("Email is required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("Enter a valid email");
            return;
        }
        // Add validation for staffID
        if (TextUtils.isEmpty(staffID)) {
            regStaffNumber.setError("Staff ID is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            regPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) { // Minimum password length
            regPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Check if email already exists
        if (dbHelper.checkEmailExists(email)) {
            Toast.makeText(Register.this, "Email already registered.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user to database
        boolean isInserted = dbHelper.addStaff(firstName, lastName, email,  password);

        if (isInserted) {
            Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            // navigate to Login screen or Main screen
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
