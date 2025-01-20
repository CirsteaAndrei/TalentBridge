package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CompanyDashboardActivity extends AppCompatActivity {
    private RecyclerView companyJobsRecyclerView;
    private JobAdapter jobAdapter;
    private List<Job> companyJobList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_dashboard);

        companyJobsRecyclerView = findViewById(R.id.companyJobsRecyclerView);
        FloatingActionButton addJobButton = findViewById(R.id.addJobButton);

        db = FirebaseFirestore.getInstance();

        companyJobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobAdapter = new JobAdapter(this, companyJobList, true, this::deleteJob);
        companyJobsRecyclerView.setAdapter(jobAdapter);

        fetchCompanyJobs();

        addJobButton.setOnClickListener(v -> {
            Intent intent = new Intent(CompanyDashboardActivity.this, AddJobActivity.class);
            startActivity(intent);
        });

        ImageButton statisticsButton = findViewById(R.id.statisticsButton);
        statisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(CompanyDashboardActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(CompanyDashboardActivity.this, CompanyProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void fetchCompanyJobs() {
        String companyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("jobs")
                .whereEqualTo("company_id", companyId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("CompanyJobs", "Error fetching jobs", e);
                        return;
                    }
                    companyJobList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        if (job != null) {
                            job.setId(doc.getId()); // Set the document ID
                            job.setCompanyId(companyId); // Explicitly set the companyId
                            companyJobList.add(job);
                        }
                    }
                    jobAdapter.notifyDataSetChanged();
                });
    }

    private void deleteJob(String jobId, int position) {
        db.collection("jobs").document(jobId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Safely remove the item from the list and update the UI
                    if (position >= 0 && position < companyJobList.size() && companyJobList.get(position).getId().equals(jobId)) {
                        companyJobList.remove(position);
                        jobAdapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback: Search for the jobId in the list
                        int indexToRemove = -1;
                        for (int i = 0; i < companyJobList.size(); i++) {
                            if (companyJobList.get(i).getId().equals(jobId)) {
                                indexToRemove = i;
                                break;
                            }
                        }

                        if (indexToRemove != -1) {
                            companyJobList.remove(indexToRemove);
                            jobAdapter.notifyItemRemoved(indexToRemove);
                            Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                        // Remove the "Job not found" toast entirely since we are confident the job is deleted
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete job", Toast.LENGTH_SHORT).show();
                    Log.e("CompanyDashboard", "Error deleting job", e);
                });
    }



}
