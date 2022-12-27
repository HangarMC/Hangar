package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.db.projects.ProjectNoteTable;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class HangarProjectNote extends ProjectNoteTable {

    private final String userName;

    @JdbiConstructor
    public HangarProjectNote(final OffsetDateTime createdAt, final long id, final long projectId, final String message, final Long userId, final @Nullable String name) {
        super(createdAt, id, projectId, message, userId);
        this.userName = name;
    }

    public String getUserName() {
        return this.userName;
    }

    @Override
    public String toString() {
        return "HangarProjectNote{" +
                "userName='" + this.userName + '\'' +
                "} " + super.toString();
    }
}
