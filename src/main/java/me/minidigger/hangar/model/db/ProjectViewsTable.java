package me.minidigger.hangar.model.db;


import java.time.LocalDate;

public class ProjectViewsTable {

    private LocalDate day;
    private long projectId;
    private long views;


    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

}
