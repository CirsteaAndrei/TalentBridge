package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class JobDetailsActivity extends AppCompatActivity {

    private static final String TAG = "JobDetailsActivity";

    private TextView jobTitle, jobDescription, jobRequiredSkills, applicantsLabel;
    private Button applyButton;
    private LinearLayout applicantsContainer;
    BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private String userId;
    private String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        jobTitle = findViewById(R.id.jobTitle);
        jobDescription = findViewById(R.id.jobDescription);
        jobRequiredSkills = findViewById(R.id.jobRequiredSkills);
        applyButton = findViewById(R.id.applyButton);
        applicantsLabel = findViewById(R.id.applicantsLabel);
        applicantsContainer = findViewById(R.id.applicantsContainer);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        checkIfJobApplied(userId);
        checkUserRole(userId);

        // Get job details from intent
        jobId = getIntent().getStringExtra("jobId");
        String title = getIntent().getStringExtra("jobTitle");
        String description = getIntent().getStringExtra("jobDescription");
        ArrayList<String> skills = getIntent().getStringArrayListExtra("jobSkills");

        // Populate job details
        if (title != null) jobTitle.setText(title);
        if (description != null) jobDescription.setText(description);
        if (skills != null) jobRequiredSkills.setText(String.join(", ", skills));

        // Determine user role and set up functionality
        checkIfCompanyOwnsJob();
    }

    private void checkIfCompanyOwnsJob() {
        db.collection("jobs").document(jobId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String companyId = documentSnapshot.getString("company_id");

                if (companyId != null && companyId.equals(userId)) {
                    // User is the company owner
                    showApplicantsForJob();
                } else {
                    // User is a student
                    showApplyButton();
                }
            } else {
                Toast.makeText(this, "Job not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching job details", e);
            Toast.makeText(this, "Failed to load job details.", Toast.LENGTH_SHORT).show();
        });
    }

    private void showApplicantsForJob() {
        applicantsLabel.setVisibility(android.view.View.VISIBLE);
        applicantsContainer.setVisibility(android.view.View.VISIBLE);

        db.collection("jobs").document(jobId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> applicantIds = (List<String>) documentSnapshot.get("applicants");

                if (applicantIds != null) {
                    applicantsContainer.removeAllViews();
                    for (String applicantId : applicantIds) {
                        db.collection("students").document(applicantId).get().addOnSuccessListener(applicantDoc -> {
                            if (applicantDoc.exists()) {
                                String applicantName = applicantDoc.getString("name");

                                if (applicantName != null) {
                                    TextView applicantView = new TextView(this);
                                    applicantView.setText(applicantName);
                                    applicantView.setTextColor(getResources().getColor(android.R.color.black));
                                    applicantView.setTextSize(16);
                                    applicantView.setPadding(8, 8, 8, 8);

                                    // Add click listener to open the student's profile
                                    applicantView.setOnClickListener(v -> {
                                        Intent intent = new Intent(JobDetailsActivity.this, ProfileActivity.class);
                                        intent.putExtra("studentId", applicantId);
                                        startActivity(intent);
                                    });

                                    applicantsContainer.addView(applicantView);
                                }
                            }
                        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching student details", e));
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching job details", e));
    }


    private void fetchApplicantsDetails(List<String> applicantIds) {
        applicantsContainer.removeAllViews();

        for (String studentId : applicantIds) {
            db.collection("students").document(studentId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String studentName = documentSnapshot.getString("name");

                    if (studentName != null) {
                        TextView applicantView = new TextView(this);
                        applicantView.setText(studentName);
                        applicantView.setTextColor(getResources().getColor(android.R.color.black));
                        applicantView.setTextSize(16);
                        applicantsContainer.addView(applicantView);
                    }
                } else {
                    Log.w(TAG, "Student not found: " + studentId);
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Error fetching student details", e));
        }
    }

    private void showApplyButton() {
        applyButton.setVisibility(View.VISIBLE);
        applyButton.setOnClickListener(v -> applyForJob());
    }

    private void applyForJob() {
        db.collection("jobs").document(jobId)
                .update("applicants", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Applied successfully", Toast.LENGTH_SHORT).show();
                    applyButton.setEnabled(false);
                    applyButton.setText("Applied");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to apply", Toast.LENGTH_SHORT).show());

        db.collection("students").document(userId)
                .update("applied_jobs", com.google.firebase.firestore.FieldValue.arrayUnion(jobId))
                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(JobDetailsActivity.this, "Successfully Applied", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("jobId", jobId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JobDetailsActivity.this, "Failed to Apply: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfJobApplied(String userId) {
        db.collection("students").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("applied_jobs")) {
                        ArrayList<String> appliedJobs = (ArrayList<String>) documentSnapshot.get("applied_jobs");
                        if (appliedJobs != null && appliedJobs.contains(jobId)) {
                            // If the job is already applied, disable the button and change its text
                            applyButton.setEnabled(false);
                            applyButton.setText("Applied");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JobDetailsActivity.this, "Error checking applied jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserRole(String userId)
    {
        db.collection("students").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User is a student
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    if (item.getItemId() == R.id.nav_dashboard) {
                        startActivity(new Intent(JobDetailsActivity.this, DashboardActivity.class));
                        return true;
                    } else if (item.getItemId() == R.id.nav_profile) {
                        startActivity(new Intent(JobDetailsActivity.this, ProfileActivity.class));
                        return true;
                    }
                    return false;
                });
            } else {
                // Check if user is a company
                db.collection("companies").document(userId).get().addOnSuccessListener(companySnapshot -> {
                    if (companySnapshot.exists()) {
                        // User is a company
                        bottomNavigationView.setOnItemSelectedListener(item -> {
                            if (item.getItemId() == R.id.nav_dashboard) {
                                startActivity(new Intent(JobDetailsActivity.this, CompanyDashboardActivity.class));
                                return true;
                            } else if (item.getItemId() == R.id.nav_profile) {
                                startActivity(new Intent(JobDetailsActivity.this, CompanyProfileActivity.class));
                                return true;
                            }
                            return false;
                        });
                    } else {
                        // Handle case where user is neither a student nor a company (optional)
                        Toast.makeText(JobDetailsActivity.this, "Error: User role not identified.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(JobDetailsActivity.this, "Failed to determine user role.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }
}
