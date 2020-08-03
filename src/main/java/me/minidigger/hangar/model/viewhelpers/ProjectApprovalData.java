package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.ProjectNamespace;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

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

    public ProjectApprovalData() { }

    @Nested("pn")
    public ProjectNamespace getNamespace() {
        return namespace;
    }

    @Nested("pn")
    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getChangeRequester() {
        return changeRequester;
    }

    public void setChangeRequester(String changeRequester) {
        this.changeRequester = changeRequester;
    }
}
