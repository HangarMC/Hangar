package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.common.roles.ProjectRole;
import java.util.List;

public class ProjectMember {

    private final String user;
    private final List<ProjectRole> roles;

    public ProjectMember(final String user, final List<ProjectRole> roles) {
        this.user = user;
        this.roles = roles;
    }

    public String getUser() {
        return this.user;
    }

    public List<ProjectRole> getRoles() {
        return this.roles;
    }
}
