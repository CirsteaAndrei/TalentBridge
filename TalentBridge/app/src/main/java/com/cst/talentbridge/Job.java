package com.cst.talentbridge;

public class Job {
    private String title;
    private String description;

    public Job() {
        // Default constructor required for calls to DataSnapshot.getValue(Job.class)
    }

    public Job(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
