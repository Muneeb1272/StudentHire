package com.hirenest.app;

public class JobModel {

    String title, company, location, salary,
            type, jobId, description, requirements;

    public JobModel(String title, String company,
                    String location, String salary,
                    String type) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getSalary() { return salary; }
    public String getType() { return type; }
    public String getJobId() { return jobId; }
    public String getDescription() { return description; }
    public String getRequirements() { return requirements; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public void setDescription(String description) { this.description = description; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
}