package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionTagsDAO;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class VersionTagService extends HangarComponent {

    private final ProjectVersionTagsDAO projectVersionTagsDAO;

    public VersionTagService(HangarDao<ProjectVersionTagsDAO> projectVersionTagsDAO) {
        this.projectVersionTagsDAO = projectVersionTagsDAO.get();
    }

    public ProjectVersionTagTable getTag(long versionId, String name) {
        return projectVersionTagsDAO.getTag(versionId, name);
    }

    public void addTags(Collection<ProjectVersionTagTable> projectVersionTagTables) {
        projectVersionTagsDAO.insertTags(projectVersionTagTables);
    }

    public void addTags(ProjectVersionTagTable...projectVersionTagTables) {
        projectVersionTagsDAO.insertTags(projectVersionTagTables);
    }

    public void updateTag(ProjectVersionTagTable projectVersionTagTable) {
        projectVersionTagsDAO.updateTag(projectVersionTagTable);
    }

    public void addUnstableTag(long versionId) {
        addTags(new ProjectVersionTagTable(versionId, "Unstable", TagColor.UNSTABLE));
    }
}
