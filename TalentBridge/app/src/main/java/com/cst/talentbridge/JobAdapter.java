package com.cst.talentbridge;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;

    public JobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.job_item, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        // Bind job details to views
        holder.title.setText(job.getTitle());
        holder.description.setText(job.getDescription());
        holder.skills.setText("Skills: " + String.join(", ", job.getRequiredSkills()));

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            // Log to ensure click works
            Log.d("JobAdapter", "Clicked on: " + job.getTitle());

            // Start JobDetailsActivity with job details
            Intent intent = new Intent(context, JobDetailsActivity.class);
            intent.putExtra("jobTitle", job.getTitle());
            intent.putExtra("jobDescription", job.getDescription());
            intent.putStringArrayListExtra("jobSkills", new ArrayList<>(job.getRequiredSkills()));
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, skills;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.jobTitle); // Correct id
            description = itemView.findViewById(R.id.jobDescription); // Correct id
            skills = itemView.findViewById(R.id.jobRequiredSkills); // Correct id from job_item.xml
        }
    }

}

