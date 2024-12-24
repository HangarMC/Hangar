package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.model.internal.projects.HangarProjectNote;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(HangarProjectNote.class)
public interface HangarProjectNotesDAO {

    @SqlQuery("""
        SELECT pn.*, (SELECT u.name FROM users u WHERE u.id = pn.user_id) AS name
        FROM project_notes pn
        WHERE pn.project_id = :projectId
        """)
    List<HangarProjectNote> getProjectNotes(long projectId);
}
