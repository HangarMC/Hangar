package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.ProjectFlagsTable;
import me.minidigger.hangar.model.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectFlag {

    @Nested
    private ProjectFlagsTable flag;
    private String reportedBy;
    private String resolvedBy;
    private String projectOwnerName;
    private String projectSlug;
    private Visibility projectVisibility;

    public ProjectFlag() { }

    public ProjectFlag with(ProjectFlagsTable flag) {
        this.flag = flag;
        return this;
    }

    public ProjectFlagsTable getFlag() {
        return flag;
    }

    public void setFlag(ProjectFlagsTable flag) {
        this.flag = flag;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getProjectOwnerName() {
        return projectOwnerName;
    }

    public void setProjectOwnerName(String projectOwnerName) {
        this.projectOwnerName = projectOwnerName;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    @EnumByOrdinal
    public Visibility getProjectVisibility() {
        return projectVisibility;
    }

    @EnumByOrdinal
    public void setProjectVisibility(Visibility projectVisibility) {
        this.projectVisibility = projectVisibility;
    }

    public String getProjectNamespace() {
        return projectOwnerName + "/" + projectSlug;
    }
}
