package com.cst.talentbridge;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddJobActivity extends AppCompatActivity {
    private EditText jobTitleField, jobDescriptionField, jobSkillsField;
    private Button addJobButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        jobTitleField = findViewById(R.id.jobTitleField);
        jobDescriptionField = findViewById(R.id.jobDescriptionField);
        jobSkillsField = findViewById(R.id.jobSkillsField);
        addJobButton = findViewById(R.id.addJobButton);

        db = FirebaseFirestore.getInstance();

        addJobButton.setOnClickListener(v -> addJob());
    }

    private void addJob() {
        String title = jobTitleField.getText().toString().trim();
        String description = jobDescriptionField.getText().toString().trim();
        String skills = jobSkillsField.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || skills.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> skillList = Arrays.asList(skills.split(","));
        Map<String, Object> job = new HashMap<>();
        job.put("title", title);
        job.put("description", description);
        job.put("requiredSkills", skillList);
        job.put("company_id", FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("jobs")
                .add(job)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Job added successfully", Toast.LENGTH_SHORT).show();
                    addMissingSkillsToDatabase(skillList); // Add missing skills
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding job", Toast.LENGTH_SHORT).show();
                    Log.e("AddJob", "Error adding job", e);
                });
    }

    private void addMissingSkillsToDatabase(List<String> skillList) {
        db.collection("skills").get()
                .addOnSuccessListener(querySnapshot -> {
                    // Collect existing skills
                    Map<String, Boolean> existingSkills = new HashMap<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        existingSkills.put(document.getId().toLowerCase(), true);
                    }

                    // Add missing skills to Firestore
                    for (String skill : skillList) {
                        String trimmedSkill = skill.trim().toLowerCase();
                        if (!existingSkills.containsKey(trimmedSkill)) {
                            Map<String, Object> skillData = new HashMap<>();
                            skillData.put("name", skill.trim());
                            db.collection("skills").document(trimmedSkill)
                                    .set(skillData)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("AddJob", "Skill added to database: " + skill.trim()))
                                    .addOnFailureListener(e ->
                                            Log.e("AddJob", "Error adding skill: " + skill.trim(), e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AddJob", "Error fetching existing skills", e));
    }
}
