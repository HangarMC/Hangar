package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public record ProjectMember(String user, long userId,
                            @Schema(description = "The member's title within the project, chosen when they were added", example = "Maintainer") String title,
                            @Schema(description = "What the member is allowed to do in this project") List<NamedPermission> permissions) {

    @JdbiConstructor
    public ProjectMember(final String user, final long userId, final String title, final Permission permissions) {
        this(user, userId, title, permissions.toNamed());
    }
}
