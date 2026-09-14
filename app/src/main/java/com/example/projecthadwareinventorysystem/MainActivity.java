package com.example.projecthadwareinventorysystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    EditText edtEmail, editPassword;
    Button btnLogin, btnSignUp;
    MyDBHelper dbHelper;

    // Define a key for the email extra
    public static final String EXTRA_LOGGED_IN_STAFF_EMAIL = "com.example.projecthadwareinventorysystem.LOGGED_IN_STAFF_EMAIL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new MyDBHelper(this);

        edtEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.editPassword); // If this ID is edtPassword in XML, keep it. If it's editPassword, then it's fine.
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });
    }

    // Method to handle user login
    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validate email and password fields
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email");
            edtEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return;
        }

        // Check credentials against the database
        boolean isAuthenticated = dbHelper.checkStaff(email, password);

        if (isAuthenticated) {
            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

            // Navigate to Dashboard Activity and pass the email
            Intent intent = new Intent(MainActivity.this, Dashboard.class);
            intent.putExtra(EXTRA_LOGGED_IN_STAFF_EMAIL, email); // Pass the email
            startActivity(intent);
            finish(); // Optional: Close LoginActivity so user can't go back
        } else {
            Toast.makeText(MainActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
        }
    }
}
