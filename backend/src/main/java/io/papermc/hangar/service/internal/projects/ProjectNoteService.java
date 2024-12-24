package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectNotesDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectNotesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectNoteTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.projects.HangarProjectNote;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectNoteService extends HangarComponent {

    private final ProjectNotesDAO projectNotesDAO;
    private final HangarProjectNotesDAO hangarProjectNotesDAO;

    @Autowired
    public ProjectNoteService(final ProjectNotesDAO projectNotesDAO, final HangarProjectNotesDAO hangarProjectNotesDAO) {
        this.projectNotesDAO = projectNotesDAO;
        this.hangarProjectNotesDAO = hangarProjectNotesDAO;
    }

    public List<HangarProjectNote> getNotes(final ProjectTable project) {
        return this.hangarProjectNotesDAO.getProjectNotes(project.getId());
    }

    public void addNote(final long projectId, final String msg) {
        if (msg.length() > 500) {
            throw new HangarApiException("Note is too long");
        }
        this.projectNotesDAO.insert(new ProjectNoteTable(projectId, msg, this.getHangarPrincipal().getId()));
    }
}
