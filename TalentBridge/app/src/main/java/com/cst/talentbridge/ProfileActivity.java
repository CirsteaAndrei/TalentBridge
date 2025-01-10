package com.cst.talentbridge;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextView studentName;
    private LinearLayout skillsContainer;
    private Button editSkillsButton;
    private FirebaseFirestore db;

    private List<String> predefinedSkills = new ArrayList<>();
    private List<String> selectedSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        studentName = findViewById(R.id.studentName);
        skillsContainer = findViewById(R.id.skillsContainer);
        editSkillsButton = findViewById(R.id.editSkillsButton);

        db = FirebaseFirestore.getInstance();

        // Get current user's ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load profile data from Firestore
        loadProfileData(userId);

        // Load predefined skills from Firestore
        loadSkillsFromFirestore();

        // Edit skills button click
        editSkillsButton.setOnClickListener(v -> showSkillEditingDialog(userId));
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
                                Log.e("ProfileActivity", "Error updating skills", e);
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}