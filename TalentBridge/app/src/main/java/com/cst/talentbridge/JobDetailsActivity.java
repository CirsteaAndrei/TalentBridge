package com.cst.talentbridge;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class JobDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        // Get data from the Intent
        String title = getIntent().getStringExtra("jobTitle");
        String description = getIntent().getStringExtra("jobDescription");

        // Set the data to TextViews
        TextView jobTitle = findViewById(R.id.jobTitle);
        TextView jobDescription = findViewById(R.id.jobDescription);

        jobTitle.setText(title);
        jobDescription.setText(description);
    }
}
