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

    public HealthReport(final List<UnhealthyProject> noTopicProjects, final List<UnhealthyProject> staleProjects, final List<UnhealthyProject> nonPublicProjects, final List<MissingFileCheck> missingFiles, final List<JobTable> erroredJobs) {
        this.noTopicProjects = noTopicProjects;
        this.staleProjects = staleProjects;
        this.nonPublicProjects = nonPublicProjects;
        this.missingFiles = missingFiles;
        this.erroredJobs = erroredJobs;
    }

    public List<UnhealthyProject> getNoTopicProjects() {
        return this.noTopicProjects;
    }

    public List<UnhealthyProject> getStaleProjects() {
        return this.staleProjects;
    }

    public List<UnhealthyProject> getNonPublicProjects() {
        return this.nonPublicProjects;
    }

    public List<MissingFileCheck> getMissingFiles() {
        return this.missingFiles;
    }

    public List<JobTable> getErroredJobs() {
        return this.erroredJobs;
    }

    @Override
    public String toString() {
        return "HealthReport{" +
                "noTopicProjects=" + this.noTopicProjects +
                ", staleProjects=" + this.staleProjects +
                ", nonPublicProjects=" + this.nonPublicProjects +
                ", missingFiles=" + this.missingFiles +
                ", erroredJobs=" + this.erroredJobs +
                '}';
    }
}
