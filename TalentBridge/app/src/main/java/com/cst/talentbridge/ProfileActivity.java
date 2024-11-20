package com.cst.talentbridge;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        // Load profile data (mock for now)
        studentName.setText("John Doe");
        selectedSkills.add("Java"); // Example of user's current skills

        // Load predefined skills from Firestore
        loadSkillsFromFirestore();

        // Edit skills button click
        editSkillsButton.setOnClickListener(v -> showSkillEditingDialog());
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
        }
    }

    private void showSkillEditingDialog() {
        // Create a dialog for editing skills
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
                    Toast.makeText(this, "Skills updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
