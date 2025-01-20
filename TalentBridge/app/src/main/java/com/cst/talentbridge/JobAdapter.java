package com.cst.talentbridge;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    private boolean isCompany;
    private DeleteCallback deleteCallback;

    public JobAdapter(Context context, List<Job> jobList, boolean isCompany, DeleteCallback deleteCallback) {
        this.context = context;
        this.jobList = jobList;
        this.isCompany = isCompany;
        this.deleteCallback = deleteCallback;
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

        holder.title.setText(job.getTitle());
        holder.skills.setText("Skills: " + String.join(", ", job.getRequiredSkills()));

        String companyId = job.getCompanyId();
        if (companyId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("companies").document(companyId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String companyName = documentSnapshot.getString("name");
                        holder.companyName.setText(companyName != null ? companyName : "Unknown Company");
                    })
                    .addOnFailureListener(e -> holder.companyName.setText("Error fetching company"));
        } else {
            holder.companyName.setText("Unknown Company");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailsActivity.class);
            intent.putExtra("jobId", job.getId());
            intent.putExtra("jobTitle", job.getTitle());
            intent.putExtra("jobDescription", job.getDescription());
            intent.putStringArrayListExtra("jobSkills", new ArrayList<>(job.getRequiredSkills()));
            intent.putExtra("companyName", holder.companyName.getText().toString());
            context.startActivity(intent);
        });

        if (isCompany) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> deleteCallback.deleteJob(job.getId(), position));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView title, companyName, skills;
        Button deleteButton;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.jobTitle);
            companyName = itemView.findViewById(R.id.jobCompany);
            skills = itemView.findViewById(R.id.jobRequiredSkills);
            deleteButton = itemView.findViewById(R.id.deleteJobButton);
        }
    }
}
