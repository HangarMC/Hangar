package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class VersionService extends HangarService {

    private final ProjectVersionsDAO projectVersionsDAO;

    @Autowired
    public VersionService(HangarDao<ProjectVersionsDAO> projectVersionDAO) {
        this.projectVersionsDAO = projectVersionDAO.get();
    }

    public void addUnstableTag(long versionId) {
        projectVersionsDAO.insertTags(Set.of(
                new ProjectVersionTagTable(versionId, "Unstable", null, TagColor.UNSTABLE)
        ));
    }
}
