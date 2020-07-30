package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.model.generated.Version;

public class VersionData {

    private ProjectData p;
    private ProjectVersionsTable v;
//    private Channel c;
    private String approvedBy;  // Reviewer if present
    //dependencies: Seq[(Dependency, Option[Model[Project]])]

    public VersionData(ProjectData p, ProjectVersionsTable v, String approvedBy) {
        this.p = p;
        this.v = v;
        this.approvedBy = approvedBy;
    }

    public ProjectData getP() {
        return p;
    }

    public ProjectVersionsTable getV() {
        return v;
    }
}
