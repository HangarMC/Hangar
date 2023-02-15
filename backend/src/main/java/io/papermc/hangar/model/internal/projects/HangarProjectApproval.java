package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public record HangarProjectApproval(long projectId,
                                    @Nested("pn") ProjectNamespace namespace,
                                    @EnumByOrdinal Visibility visibility,
                                    String comment,
                                    String changeRequester) {
}
