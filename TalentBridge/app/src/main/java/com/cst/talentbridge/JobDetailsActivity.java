package com.cst.talentbridge;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class JobDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details); // Ensure the layout name is correct

        // Bind views
        TextView title = findViewById(R.id.jobTitle);
        TextView description = findViewById(R.id.jobDescription);
        TextView requiredSkills = findViewById(R.id.jobRequiredSkills);

        // Get data from intent
        String jobTitle = getIntent().getStringExtra("jobTitle");
        String jobDescription = getIntent().getStringExtra("jobDescription");
        ArrayList<String> jobSkills = getIntent().getStringArrayListExtra("jobSkills");

        // Set data to views
        title.setText(jobTitle);
        description.setText(jobDescription);
        requiredSkills.setText(String.join(", ", jobSkills));
    }
}
