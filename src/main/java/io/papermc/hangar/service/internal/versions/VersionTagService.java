package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionTagsDAO;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Deprecated(forRemoval = true)
public class VersionTagService extends HangarComponent {

    private final ProjectVersionTagsDAO projectVersionTagsDAO;

    public VersionTagService(ProjectVersionTagsDAO projectVersionTagsDAO) {
        this.projectVersionTagsDAO = projectVersionTagsDAO;
    }

    public ProjectVersionTagTable getTag(long versionId, String name) {
        return projectVersionTagsDAO.getTag(versionId, name);
    }

    public void addTags(Collection<ProjectVersionTagTable> projectVersionTagTables) {
        projectVersionTagsDAO.insertTags(projectVersionTagTables);
    }

    public void updateTag(ProjectVersionTagTable projectVersionTagTable) {
        projectVersionTagsDAO.updateTag(projectVersionTagTable);
    }
}
