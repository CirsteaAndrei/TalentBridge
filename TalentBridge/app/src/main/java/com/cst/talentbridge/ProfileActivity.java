package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private LinearLayout skillsContainer;
    private FirebaseFirestore db;

    private List<String> predefinedSkills = new ArrayList<>();
    private List<String> selectedSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        skillsContainer = findViewById(R.id.skillsContainer);
        db = FirebaseFirestore.getInstance();

        // Get current user's ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load profile data from Firestore
        loadProfileData(userId);

        // Load predefined skills from Firestore
        loadSkillsFromFirestore();

        // Edit skills button click
        editSkillsButton.setOnClickListener(v -> showSkillEditingDialog(userId));

        // Handle navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                return true; // Stay on ProfileActivity
            }
            return false;
        });
    }
    private void loadProfileData(String userId) {
        db.collection("students").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve and display student data
                String name = documentSnapshot.getString("name");
                List<String> skills = (List<String>) documentSnapshot.get("skills");

                if (name != null) {
                    studentName.setText(name);
                }

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
            Log.e("ProfileActivity", "Error loading profile", e);
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
            } else {
                Log.w(TAG, "Error fetching skills", task.getException());
            }
        });
    }

    private void displaySkills() {
        skillsContainer.removeAllViews();
        for (String skill : selectedSkills) {
            TextView skillView = new TextView(this);
            skillView.setText(skill);
            skillView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            skillsContainer.addView(skillView);

        // Fetch and display user details
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = "USER_ID"; // Replace with the actual user ID logic
        db.collection("students").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        displayUserDetails(document);
                    }
                });
    }

    private void displayUserDetails(DocumentSnapshot document) {
        String name = document.getString("name");
        String university = document.getString("university");
        List<String> skills = (List<String>) document.get("skills");

        // Set user name and university
        TextView studentName = findViewById(R.id.studentName);
        TextView studentUniversity = findViewById(R.id.studentUniversity);
        studentName.setText(name != null ? name : "Unknown");
        studentUniversity.setText(university != null ? university : "Unknown");

        // Display skills
        if (skills != null) {
            for (String skill : skills) {
                addSkillToContainer(skill);
            }
        } else {
            addSkillToContainer("No skills available");
        }
    }

        // Handle edit skills
        editSkillsButton.setOnClickListener(v -> {
            // Logic to open a skill editing dialog or screen
        });
    private void addSkillToContainer(String skill) {
        TextView skillTextView = new TextView(this);
        skillTextView.setText(skill);
        skillTextView.setTextSize(14);
        skillTextView.setTextColor(getColor(R.color.black));
//        skillTextView.setBackground(getDrawable(R.drawable.skill_background)); // Optional, for a styled background
        skillTextView.setPadding(16, 8, 16, 8);
        skillTextView.setGravity(Gravity.CENTER);
        skillTextView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        skillsContainer.addView(skillTextView);
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
                                Log.e("ProfileActivity", "Error updating skills", e);
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
