package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CompanyProfileActivity extends AppCompatActivity {
    private TextView companyName;
    private TextView companyDescription;
    private TextView companyEmail;
    private TextView companyLocation;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);

        // Initialize views
        companyName = findViewById(R.id.companyName);
        companyDescription = findViewById(R.id.companyDescription);
        companyEmail = findViewById(R.id.companyEmail);
        companyLocation = findViewById(R.id.companyLocation);

        // Buttons
        MaterialButton logOutButton = findViewById(R.id.logOutButton);
        MaterialButton deleteAccountButton = findViewById(R.id.deleteAccountButton);

        db = FirebaseFirestore.getInstance();

        loadCompanyProfile(FirebaseAuth.getInstance().getUid());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(CompanyProfileActivity.this, CompanyDashboardActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                return true;
            }
            return false;
        });
        // Log out functionality
        logOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Delete account functionality
        deleteAccountButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    db.collection("companies").document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                FirebaseAuth.getInstance().getCurrentUser().delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to delete user account", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete company profile", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show());
    }


    private void loadCompanyProfile(String userId) {
        db.collection("companies").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve and display company data
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");
                String email = documentSnapshot.getString("email");
                String location = documentSnapshot.getString("location");

                if (name != null) {
                    companyName.setText(name);
                }

                if (description != null) {
                    companyDescription.setText(description);
                }

                if (email != null) {
                    companyEmail.setText(email);
                }

                if (location != null) {
                    companyLocation.setText(location);
                }
            } else {
                Toast.makeText(this, "Company profile not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }
}
