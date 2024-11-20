package com.cst.talentbridge;

import android.os.Bundle;
<<<<<<< Updated upstream
import android.widget.Button;
=======
import android.view.Gravity;
import android.view.ViewGroup;
>>>>>>> Stashed changes
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

<<<<<<< Updated upstream
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
=======
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

>>>>>>> Stashed changes
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout skillsContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        skillsContainer = findViewById(R.id.skillsContainer);
        db = FirebaseFirestore.getInstance();

<<<<<<< Updated upstream
        // Load profile data (mock for now)
        studentName.setText("John Doe");
        List<String> skills = Arrays.asList("Java", "Android");

        for (String skill : skills) {
            TextView skillView = new TextView(this);
            skillView.setText(skill);
            skillsContainer.addView(skillView);
=======
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
>>>>>>> Stashed changes
        }

<<<<<<< Updated upstream
        // Handle edit skills
        editSkillsButton.setOnClickListener(v -> {
            // Logic to open a skill editing dialog or screen
        });
=======
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
>>>>>>> Stashed changes
    }
}
