package com.cst.talentbridge;

import java.util.List;

public class Job {
    private String title;
    private String description;
    private List<String> requiredSkills;

    public Job() {
        // Default constructor required for calls to DataSnapshot.getValue(Job.class)
    }

    public Job(String title, String description, List<String> requiredSkills) {
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }
}
