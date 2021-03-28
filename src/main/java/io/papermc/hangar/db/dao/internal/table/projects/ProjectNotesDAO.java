package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectNoteTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectNoteTable.class)
public interface ProjectNotesDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO project_notes (created_at, project_id, message, user_id) VALUES (:now, :projectId, :message, :userId)")
    void insert(@BindBean ProjectNoteTable projectNoteTable);
}
