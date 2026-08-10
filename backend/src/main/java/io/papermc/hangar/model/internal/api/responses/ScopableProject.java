package io.papermc.hangar.model.internal.api.responses;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.util.AvatarUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public record ScopableProject(String name, ProjectNamespace namespace, String avatarUrl) {

    @JdbiConstructor
    public ScopableProject(final String name, @Nested final ProjectNamespace namespace, final @Nullable String avatar, final @Nullable String avatarFallback) {
        this(name, namespace, AvatarUtil.avatarUrl(avatar, avatarFallback));
    }
}
