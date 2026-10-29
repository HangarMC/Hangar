package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.api.project.Project;
import org.jdbi.v3.core.mapper.Nested;

public record ProjectData(@Nested Project project, @Nested HangarProject.HangarProjectInfo info) {
}
