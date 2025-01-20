package com.cst.talentbridge;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private LinearLayout barChartContainer; // LinearLayout to hold bar charts
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        barChartContainer = findViewById(R.id.barChartContainer);

        db = FirebaseFirestore.getInstance();

        // Fetch data for statistics
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

                                        // Generate bar charts
                                        generateBarCharts(studentSkillsCount, jobSkillsCount);
                                    } else {
                                        Log.e(TAG, "Error fetching jobs data", jobTask.getException());
                                    }
                                });
                    } else {
                        Log.e(TAG, "Error fetching students data", studentTask.getException());
                    }
                });
    }

    private void generateBarCharts(Map<String, Integer> studentSkillsCount, Map<String, Integer> jobSkillsCount) {
        barChartContainer.removeAllViews();

        for (String skill : studentSkillsCount.keySet()) {
            int studentsWithSkill = studentSkillsCount.getOrDefault(skill, 0);
            int jobsWithSkill = jobSkillsCount.getOrDefault(skill, 0);

            // Skip if both counts are zero
            if (studentsWithSkill == 0 && jobsWithSkill == 0) continue;

            // Add Skill Title
            TextView skillTitle = new TextView(this);
            skillTitle.setText(skill);
            skillTitle.setTextSize(20);
            skillTitle.setTextColor(Color.BLACK);
            skillTitle.setPadding(0, 20, 0, 10);
            skillTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            barChartContainer.addView(skillTitle);

            // Add Bar Chart View
            CustomBarChartView barChartView = new CustomBarChartView(this);
            barChartView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400
            ));
            barChartView.setData(studentsWithSkill, jobsWithSkill);
            barChartContainer.addView(barChartView);
        }
    }

}
