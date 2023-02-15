package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.common.roles.ProjectRole;
import java.util.List;

public record ProjectMember(String user, List<ProjectRole> roles) {
}
