package com.cst.talentbridge;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private LinearLayout pieChartContainer; // LinearLayout to hold multiple PieChartViews
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Ensure the container layout matches your XML ID
        pieChartContainer = findViewById(R.id.pieChartContainer);

        db = FirebaseFirestore.getInstance();

        // Fetch skills data
        fetchSkillsData();
    }

    private void fetchSkillsData() {
        Map<String, Integer> studentSkillsCount = new HashMap<>();
        Map<String, Integer> jobSkillsCount = new HashMap<>();

        // Fetch students collection
        db.collection("students")
                .get()
                .addOnCompleteListener(studentTask -> {
                    if (studentTask.isSuccessful()) {
                        for (QueryDocumentSnapshot studentDoc : studentTask.getResult()) {
                            if (studentDoc.contains("skills")) {
                                for (String skill : (Iterable<String>) studentDoc.get("skills")) {
                                    studentSkillsCount.put(skill, studentSkillsCount.getOrDefault(skill, 0) + 1);
                                }
                            }
                        }

                        // Fetch jobs collection
                        db.collection("jobs")
                                .get()
                                .addOnCompleteListener(jobTask -> {
                                    if (jobTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot jobDoc : jobTask.getResult()) {
                                            if (jobDoc.contains("requiredSkills")) {
                                                for (String skill : (Iterable<String>) jobDoc.get("requiredSkills")) {
                                                    jobSkillsCount.put(skill, jobSkillsCount.getOrDefault(skill, 0) + 1);
                                                }
                                            }
                                        }

                                        // Process the skills data and update the charts
                                        generatePieChartsForSkills(studentSkillsCount, jobSkillsCount);
                                    } else {
                                        Log.e(TAG, "Error fetching jobs data", jobTask.getException());
                                    }
                                });
                    } else {
                        Log.e(TAG, "Error fetching students data", studentTask.getException());
                    }
                });
    }

    private void generatePieChartsForSkills(Map<String, Integer> studentSkillsCount, Map<String, Integer> jobSkillsCount) {
        // Combine all unique skills from both student and job collections
        Map<String, Integer> combinedSkills = new HashMap<>(studentSkillsCount);
        for (Map.Entry<String, Integer> entry : jobSkillsCount.entrySet()) {
            combinedSkills.putIfAbsent(entry.getKey(), 0); // Ensure all job skills are present
        }

        // Loop through each skill and create a pie chart
        for (String skill : combinedSkills.keySet()) {
            int studentsWithSkill = studentSkillsCount.getOrDefault(skill, 0);
            int jobsWithSkill = jobSkillsCount.getOrDefault(skill, 0);

            // Only add charts for skills that have some data
            if (studentsWithSkill == 0 && jobsWithSkill == 0) continue;

            // Create a title TextView for each skill
            TextView skillTitle = new TextView(this);
            skillTitle.setText(String.format("Skill: %s", skill));
            skillTitle.setTextSize(18);
            skillTitle.setPadding(0, 16, 0, 8);
            skillTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Create a PieChartView for each skill
            PieChartView pieChartView = new PieChartView(this);
            pieChartView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400
            ));
            pieChartView.setData(
                    new float[]{studentsWithSkill, jobsWithSkill},
                    new int[]{getColor(R.color.blue_500), getColor(R.color.gray)},
                    new String[]{"Students", "Jobs"}
            );

            // Add the title and pie chart to the container
            pieChartContainer.addView(skillTitle);
            pieChartContainer.addView(pieChartView);
        }
    }
}
