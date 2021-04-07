package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectNotesDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectNotesDAO;
import io.papermc.hangar.model.db.projects.ProjectNoteTable;
import io.papermc.hangar.model.internal.projects.HangarProjectNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectNoteService extends HangarComponent {

    private final ProjectNotesDAO projectNotesDAO;
    private final HangarProjectNotesDAO hangarProjectNotesDAO;

    @Autowired
    public ProjectNoteService(HangarDao<ProjectNotesDAO> projectNotesDAO, HangarDao<HangarProjectNotesDAO> hangarProjectNotesDAO) {
        this.projectNotesDAO = projectNotesDAO.get();
        this.hangarProjectNotesDAO = hangarProjectNotesDAO.get();
    }

    public List<HangarProjectNote> getNotes(long projectId) {
        return hangarProjectNotesDAO.getProjectNotes(projectId);
    }

    public void addNote(long projectId, String msg) {
        projectNotesDAO.insert(new ProjectNoteTable(projectId, msg, getHangarPrincipal().getId()));
    }
}
