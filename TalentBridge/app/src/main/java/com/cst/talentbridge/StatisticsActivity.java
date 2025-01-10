package com.cst.talentbridge;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // Add the legend to the top
        addLegend();

        // Fetch skills data
        fetchSkillsData();
    }

    private void addLegend() {
        // Create a horizontal layout for the legend
        LinearLayout legendLayout = new LinearLayout(this);
        legendLayout.setOrientation(LinearLayout.HORIZONTAL);
        legendLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        legendLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        legendLayout.setPadding(0, 16, 0, 16);

        // Add "Students" color box and label
        LinearLayout studentLegend = createLegendItem("Students", getColor(R.color.blue_500));
        legendLayout.addView(studentLegend);

        // Add space between legends
        TextView spacer = new TextView(this);
        spacer.setText("     "); // Add some spacing
        legendLayout.addView(spacer);

        // Add "Jobs" color box and label
        LinearLayout jobLegend = createLegendItem("Jobs", getColor(R.color.black));
        legendLayout.addView(jobLegend);

        // Add the legend layout to the container
        pieChartContainer.addView(legendLayout);
    }

    private LinearLayout createLegendItem(String label, int color) {
        LinearLayout legendItem = new LinearLayout(this);
        legendItem.setOrientation(LinearLayout.HORIZONTAL);
        legendItem.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Color box
        TextView colorBox = new TextView(this);
        colorBox.setBackgroundColor(color);
        colorBox.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
        colorBox.setPadding(8, 8, 8, 8);

        // Label
        TextView legendLabel = new TextView(this);
        legendLabel.setText(label);
        legendLabel.setTextSize(16);
        legendLabel.setTextColor(Color.BLACK);
        legendLabel.setPadding(16, 0, 0, 0);

        // Add color box and label to the legend item
        legendItem.addView(colorBox);
        legendItem.addView(legendLabel);

        return legendItem;
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
        Map<String, Integer> combinedSkills = new HashMap<>(studentSkillsCount);
        for (Map.Entry<String, Integer> entry : jobSkillsCount.entrySet()) {
            combinedSkills.putIfAbsent(entry.getKey(), 0);
        }

        LinearLayout pieChartContainer = findViewById(R.id.pieChartContainer);
        LinearLayout currentRow = null;
        int itemCount = 0;

        for (String skill : combinedSkills.keySet()) {
            int studentsWithSkill = studentSkillsCount.getOrDefault(skill, 0);
            int jobsWithSkill = jobSkillsCount.getOrDefault(skill, 0);

            if (studentsWithSkill == 0 && jobsWithSkill == 0) continue;

            if (itemCount % 2 == 0) {
                // Create a new horizontal row for every 2 items
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                pieChartContainer.addView(currentRow);
            }

            // Create a container for each pie chart
            LinearLayout chartContainer = new LinearLayout(this);
            chartContainer.setOrientation(LinearLayout.VERTICAL);
            chartContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1 // Weight to distribute evenly
            ));
            chartContainer.setPadding(8, 8, 8, 8);

            // Add title for the skill
            TextView skillTitle = new TextView(this);
            skillTitle.setText(String.format("Skill: %s", skill));
            skillTitle.setTextSize(16);
            skillTitle.setTextColor(Color.BLACKil
            );
            skillTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            chartContainer.addView(skillTitle);

            // Add a label for the legend above the chart
            TextView legendLabel = new TextView(this);
            legendLabel.setTextSize(14);
            legendLabel.setTextColor(getColor(R.color.gray));
            legendLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            chartContainer.addView(legendLabel);

            // Create PieChartView
            PieChartView pieChartView = new PieChartView(this);
            pieChartView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400
            ));
            pieChartView.setData(
                    new float[]{studentsWithSkill, jobsWithSkill},
                    new int[]{getColor(R.color.blue_500), getColor(R.color.gray)},
                    new String[]{"Students("+ studentsWithSkill + ")", "Jobs(" + jobsWithSkill + ")"}
            );
            chartContainer.addView(pieChartView);

            // Add chart container to the current row
            if (currentRow != null) currentRow.addView(chartContainer);

            itemCount++;
        }
    }



}