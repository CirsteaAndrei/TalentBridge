package com.cst.talentbridge;

import java.util.List;

public class Job {
    private String id; // Unique identifier for the job
    private String title;
    private String description;
    private List<String> requiredSkills;
    private boolean isApplied; // Field to track if the job is applied
    private String companyId;

    public Job() {
        // Default constructor required for calls to DataSnapshot.getValue(Job.class)
    }

    public Job(String id, String title, String description, List<String> requiredSkills, String companyId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.companyId = companyId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public boolean isApplied() {
        return isApplied;
    }

    public void setApplied(boolean applied) {
        isApplied = applied;
    }
    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
