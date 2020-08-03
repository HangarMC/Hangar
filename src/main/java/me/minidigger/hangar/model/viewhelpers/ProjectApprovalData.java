package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.ProjectNamespace;

public class ProjectApprovalData {

    private ProjectNamespace namespace;
    private Visibility visibility;
    private String comment;
    private String changeRequester;

    public ProjectApprovalData(final ProjectNamespace namespace, final Visibility visibility, final String comment, final String changeRequester) {
        this.namespace = namespace;
        this.visibility = visibility;
        this.comment = comment;
        this.changeRequester = changeRequester;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public String getComment() {
        return comment;
    }

    public String getChangeRequester() {
        return changeRequester;
    }
}
