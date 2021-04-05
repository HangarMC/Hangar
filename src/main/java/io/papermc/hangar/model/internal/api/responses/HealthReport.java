package io.papermc.hangar.model.internal.api.responses;

import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;

import java.util.List;

public class HealthReport {

    private final List<UnhealthyProject> noTopicProjects;
    private final List<UnhealthyProject> staleProjects;
    private final List<UnhealthyProject> nonPublicProjects;
    private final List<MissingFileCheck> missingFiles;
    private final List<JobTable> erroredJobs;

    public HealthReport(List<UnhealthyProject> noTopicProjects, List<UnhealthyProject> staleProjects, List<UnhealthyProject> nonPublicProjects, List<MissingFileCheck> missingFiles, List<JobTable> erroredJobs) {
        this.noTopicProjects = noTopicProjects;
        this.staleProjects = staleProjects;
        this.nonPublicProjects = nonPublicProjects;
        this.missingFiles = missingFiles;
        this.erroredJobs = erroredJobs;
    }

    public List<UnhealthyProject> getNoTopicProjects() {
        return noTopicProjects;
    }

    public List<UnhealthyProject> getStaleProjects() {
        return staleProjects;
    }

    public List<UnhealthyProject> getNonPublicProjects() {
        return nonPublicProjects;
    }

    public List<MissingFileCheck> getMissingFiles() {
        return missingFiles;
    }

    public List<JobTable> getErroredJobs() {
        return erroredJobs;
    }

    @Override
    public String toString() {
        return "HealthReport{" +
                "noTopicProjects=" + noTopicProjects +
                ", staleProjects=" + staleProjects +
                ", nonPublicProjects=" + nonPublicProjects +
                ", missingFiles=" + missingFiles +
                ", erroredJobs=" + erroredJobs +
                '}';
    }
}
