package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.model.internal.projects.HangarProjectNote;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarProjectNote.class)
public interface HangarProjectNotesDAO {

    @SqlQuery("SELECT pn.*, u.name" +
        "   FROM project_notes pn" +
        "       LEFT JOIN users u ON pn.user_id = u.id" +
        "   WHERE pn.project_id = :projectId")
    List<HangarProjectNote> getProjectNotes(long projectId);
}
