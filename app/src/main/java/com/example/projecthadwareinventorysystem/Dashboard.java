package com.example.projecthadwareinventorysystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {
    LinearLayout linearMyAccount, linearInventory, linearReports;

    Button btnLogout; // Your logout button
    private String loggedInStaffEmail;

    public static final String EXTRA_STAFF_EMAIL_TO_DISPLAY = "com.example.projecthadwareinventorysystem.STAFF_EMAIL_TO_DISPLAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_LOGGED_IN_STAFF_EMAIL)) {
            loggedInStaffEmail = intent.getStringExtra(MainActivity.EXTRA_LOGGED_IN_STAFF_EMAIL);
            Log.d("Dashboard", "Logged in staff email received: " + loggedInStaffEmail);
        } else {
            Log.e("Dashboard", "Error: Logged in staff email not found in Intent from MainActivity.");
        }

        linearMyAccount = findViewById(R.id.linearMyAccount);
        linearInventory = findViewById(R.id.linearInventory);
        linearReports = findViewById(R.id.linearReports);
        btnLogout = findViewById(R.id.btnLogout);

        linearMyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedInStaffEmail != null && !loggedInStaffEmail.isEmpty()) {
                    Intent myAccountIntent = new Intent(Dashboard.this, MyAccount.class);
                    myAccountIntent.putExtra(EXTRA_STAFF_EMAIL_TO_DISPLAY, loggedInStaffEmail);
                    startActivity(myAccountIntent);
                } else {
                    Log.e("Dashboard", "Cannot open MyAccount, staff email is missing or null.");
                    Toast.makeText(Dashboard.this, "Error: User data not found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        linearInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, Inventory.class));
            }
        });

        linearReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, Reports.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(Dashboard.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Yes", proceed with logout
                                Intent intent = new Intent(Dashboard.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                Toast.makeText(Dashboard.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "No", dismiss the dialog and do nothing
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }
}
