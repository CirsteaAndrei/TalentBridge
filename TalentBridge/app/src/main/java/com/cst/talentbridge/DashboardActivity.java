package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "Firestore";
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI elements
        Button jobsButton = findViewById(R.id.jobsButton);
        Button appliedButton = findViewById(R.id.appliedButton);
        recyclerView = findViewById(R.id.recyclerView);

//         Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        ImageButton statisticsButton = findViewById(R.id.statisticsButton);
        statisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        jobAdapter = new JobAdapter(this, jobList, false, null);
        recyclerView.setAdapter(jobAdapter);

        db = FirebaseFirestore.getInstance();

        // Default View: Show Available Jobs
        fetchStudentSkillsAndFilterJobs();
        setButtonStyle(jobsButton, appliedButton);

        // Handle button clicks for "Jobs" and "Applied"
        jobsButton.setOnClickListener(v -> {
            setButtonStyle(jobsButton, appliedButton);
            fetchStudentSkillsAndFilterJobs(); // Show available jobs
        });

        appliedButton.setOnClickListener(v -> {
            setButtonStyle(appliedButton, jobsButton);
            fetchAppliedJobs(); // Show applied jobs
        });
    }

    // Helper method to set button styles
    private void setButtonStyle(Button selectedButton, Button unselectedButton) {
        selectedButton.setBackgroundResource(R.drawable.button_filled);
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));

        unselectedButton.setBackgroundResource(R.drawable.button_outline);
        unselectedButton.setTextColor(getResources().getColor(R.color.blue_700));
    }



    private void seedJobsAndSkillsToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // List of predefined skills
        List<String> predefinedSkills = List.of(
                "HTML", "CSS", "JavaScript", "React", "Java", "Python", "SQL",
                "Photoshop", "Illustrator", "Creativity", "R", "Machine Learning", "Pandas"
        );

        // List of predefined jobs
        List<Map<String, Object>> jobs = new ArrayList<>();

        jobs.add(createJob("Frontend Developer Intern",
                "Assist in designing and developing responsive web applications using React and CSS. Work closely with the UX/UI team.",
                List.of("HTML", "CSS", "JavaScript", "React")));

        jobs.add(createJob("Backend Developer Intern",
                "Work on building and maintaining APIs and database management systems. Collaborate with the frontend team.",
                List.of("Java", "Python", "SQL")));

        jobs.add(createJob("Graphic Design Intern",
                "Create engaging visual content for marketing and branding campaigns. Use tools like Adobe Photoshop and Illustrator.",
                List.of("Photoshop", "Illustrator", "Creativity")));

        jobs.add(createJob("Data Scientist Intern",
                "Analyze large datasets to extract insights and create predictive models. Present findings to the team.",
                List.of("Python", "R", "Machine Learning", "Pandas")));

        // Seed skills
        for (String skill : predefinedSkills) {
            db.collection("skills")
                    .document(skill) // Use skill name as document ID
                    .set(createSkill(skill))
                    .addOnSuccessListener(aVoid ->
                            Log.d("SeedSkills", "Skill added: " + skill))
                    .addOnFailureListener(e ->
                            Log.w("SeedSkills", "Error adding skill", e));
        }

        // Seed jobs
        for (Map<String, Object> job : jobs) {
            db.collection("jobs")
                    .add(job)
                    .addOnSuccessListener(documentReference ->
                            Log.d("SeedJobs", "Job added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e ->
                            Log.w("SeedJobs", "Error adding job", e));
        }
    }

    // Helper method to create a job map
    private Map<String, Object> createJob(String title, String description, List<String> requiredSkills) {
        Map<String, Object> job = new HashMap<>();
        job.put("title", title);
        job.put("description", description);
        job.put("requiredSkills", requiredSkills);
        return job;
    }

    // Helper method to create a skill map
    private Map<String, Object> createSkill(String name) {
        Map<String, Object> skill = new HashMap<>();
        skill.put("name", name);
        return skill;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only refresh jobs when necessary
        if (recyclerView.getAdapter() == null) {
            fetchStudentSkillsAndFilterJobs();
        }
    }

    private void fetchStudentSkillsAndFilterJobs() {
        // Get current user's ID from FirebaseAuth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch student's applied jobs first
        db.collection("students").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the list of applied jobs
                        List<String> appliedJobs = (List<String>) documentSnapshot.get("applied_jobs");
                        if (appliedJobs == null) {
                            appliedJobs = new ArrayList<>();
                        }

                        // Fetch student's skills
                        List<String> studentSkills = (List<String>) documentSnapshot.get("skills");
                        if (studentSkills != null) {
                            Log.d(TAG, "Student skills: " + studentSkills);
                            fetchJobsAndFilter(studentSkills, appliedJobs); // Pass applied jobs
                        } else {
                            Log.w(TAG, "No skills found for the student");
                        }
                    } else {
                        Log.w(TAG, "Student profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching student profile", e);
                });
    }

    // Fetch only applied jobs for the student
    private void fetchAppliedJobs() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("students").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> appliedJobs = (List<String>) documentSnapshot.get("applied_jobs");
                        if (appliedJobs != null && !appliedJobs.isEmpty()) {
                            fetchJobsByIds(appliedJobs);
                        } else {
                            jobList.clear();
                            jobAdapter.notifyDataSetChanged(); // Show empty list
                            Log.w(TAG, "No applied jobs found");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching applied jobs", e));
    }

    // Fetch jobs by their IDs (used for applied jobs)
    private void fetchJobsByIds(List<String> jobIds) {
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                jobList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String jobId = document.getId();
                    if (jobIds.contains(jobId)) {
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String companyId = document.getString("company_id");
                        List<String> requiredSkills = (List<String>) document.get("requiredSkills");

                        if (title != null && description != null && requiredSkills != null) {
                            jobList.add(new Job(jobId, title, description, requiredSkills, companyId));
                        }
                    }
                }
                jobAdapter.notifyDataSetChanged(); // Refresh RecyclerView
            } else {
                Log.w(TAG, "Error fetching jobs", task.getException());
            }
        });
    }

    private void fetchJobsAndFilter(List<String> studentSkills, List<String> appliedJobs) {
        // Fetch jobs from Firestore
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                jobList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String jobId = document.getId();
                    String title = document.getString("title");
                    String description = document.getString("description");
                    String companyId = document.getString("company_id");
                    List<String> requiredSkills = (List<String>) document.get("requiredSkills");

                    if (title != null && description != null && requiredSkills != null) {
                        // Check if student's skills match the job's required skills
                        boolean matches = false;
                        for (String skill : studentSkills) {
                            if (requiredSkills.contains(skill)) {
                                matches = true;
                                break;
                            }
                        }

                        // Add job only if it matches skills and is NOT applied
                        if (matches && !appliedJobs.contains(jobId)) {
                            jobList.add(new Job(jobId, title, description, requiredSkills, companyId));
                        }
                    }
                }
                jobAdapter.notifyDataSetChanged(); // Notify the adapter about data changes
            } else {
                Log.w(TAG, "Error fetching jobs", task.getException());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String appliedJobId = data.getStringExtra("jobId");

            // Safely remove the applied job from the list
            if (jobList != null) {
                jobList.removeIf(job -> job.getId().equals(appliedJobId));
                if (jobAdapter != null) {
                    jobAdapter.notifyDataSetChanged();
                }
            }
        }
    }




}
