package com.cst.talentbridge;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextView studentName, studentEmail, studentUniversity, studentFaculty;
    private ImageView profileImage;
    private LinearLayout skillsContainer;
    private Button editSkillsButton;
    private FirebaseFirestore db;

    private List<String> predefinedSkills = new ArrayList<>();
    private List<String> selectedSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileImage = findViewById(R.id.userPhoto);
        studentName = findViewById(R.id.studentName);
        studentEmail = findViewById(R.id.studentEmail);
        studentUniversity = findViewById(R.id.studentUniversity);
        studentFaculty = findViewById(R.id.studentFaculty);
        skillsContainer = findViewById(R.id.skillsContainer);
        editSkillsButton = findViewById(R.id.editSkillsButton);

        // Initialize buttons
        MaterialButton logOutButton = findViewById(R.id.logOutButton);
        MaterialButton deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Set default profile photo
        profileImage.setImageResource(R.drawable.default_profile_image);

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                redirectToDashboard();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        // Get current user's ID from Firebase Auth
        String studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Load profile data from Firestore
        loadProfileData(studentId);

        // Load predefined skills from Firestore
        loadSkillsFromFirestore();

        // Edit skills button click
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!studentId.equals(currentUserId)) {
            editSkillsButton.setVisibility(android.view.View.GONE);
        } else {
            // Edit skills button click
            String finalStudentId = studentId;
            editSkillsButton.setOnClickListener(v -> showSkillEditingDialog(finalStudentId));
        }

        // Log Out button functionality
        logOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Delete Account button functionality
        deleteAccountButton.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser().getUid();

            // Confirm before deleting the account
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete user from Firestore
                        db.collection("students").document(userId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Delete user from FirebaseAuth
                                    auth.getCurrentUser().delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to delete account from FirebaseAuth", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete account from Firestore", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }


    private void redirectToDashboard() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("companies").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User is a company
                        startActivity(new Intent(ProfileActivity.this, CompanyDashboardActivity.class));
                    } else {
                        // User is a student
                        startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error determining user role", Toast.LENGTH_SHORT).show();
                    // Fallback to the student dashboard
                    startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                });
    }

    private void loadProfileData(String userId) {
        db.collection("students").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve and display student data
                String name = documentSnapshot.getString("name");
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                String university = documentSnapshot.getString("university");
                String faculty = documentSnapshot.getString("faculty");
                List<String> skills = (List<String>) documentSnapshot.get("skills");

                if (name != null) studentName.setText(name);
                if (email != null) studentEmail.setText(email);
                if (university != null) studentUniversity.setText(university);
                if (faculty != null) studentFaculty.setText(faculty);

                if (skills != null) {
                    selectedSkills.clear();
                    selectedSkills.addAll(skills);
                    displaySkills();
                }
            } else {
                Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSkillsFromFirestore() {
        db.collection("skills").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                predefinedSkills.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String skill = document.getString("name");
                    if (skill != null) {
                        predefinedSkills.add(skill);
                    }
                }
                displaySkills(); // Display user's skills after loading predefined skills
            }
        });
    }

    private void displaySkills() {
        skillsContainer.removeAllViews();
        for (String skill : selectedSkills) {
            TextView skillView = new TextView(this);
            skillView.setText(skill);
            skillView.setTextColor(Color.BLACK);
            skillView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            skillsContainer.addView(skillView);
        }
    }

    private void showSkillEditingDialog(String userId) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);

        final List<CheckBox> checkBoxes = new ArrayList<>();
        for (String skill : predefinedSkills) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(skill);
            checkBox.setChecked(selectedSkills.contains(skill));
            dialogLayout.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Skills")
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    selectedSkills.clear();
                    for (CheckBox checkBox : checkBoxes) {
                        if (checkBox.isChecked()) {
                            selectedSkills.add(checkBox.getText().toString());
                        }
                    }
                    displaySkills();

                    // Save updated skills to Firestore
                    db.collection("students").document(userId)
                            .update("skills", selectedSkills)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Skills updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update skills", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
