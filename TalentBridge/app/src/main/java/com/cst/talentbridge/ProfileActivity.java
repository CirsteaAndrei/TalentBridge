package com.cst.talentbridge;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView studentName;
    private LinearLayout skillsContainer;
    private Button editSkillsButton;
    private FirebaseFirestore db;

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
        List<String> skills = Arrays.asList("Java", "Android");

        for (String skill : skills) {
            TextView skillView = new TextView(this);
            skillView.setText(skill);
            skillsContainer.addView(skillView);
        }

        // Handle edit skills
        editSkillsButton.setOnClickListener(v -> {
            // Logic to open a skill editing dialog or screen
        });
    }
}
