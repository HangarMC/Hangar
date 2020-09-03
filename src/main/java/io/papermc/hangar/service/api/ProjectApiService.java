package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.ProjectsApiDao;
import io.papermc.hangar.model.generated.ProjectMember;
import io.papermc.hangar.model.generated.ProjectStatsDay;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ProjectApiService {

    private final HangarDao<ProjectsApiDao> projectApiDao;

    public ProjectApiService(HangarDao<ProjectsApiDao> projectApiDao) {
        this.projectApiDao = projectApiDao;
    }

    public List<ProjectMember> getProjectMembers(String pluginId, long limit, long offset) {
        return projectApiDao.get().projectMembers(pluginId, limit, offset);
    }

    public Map<String, ProjectStatsDay> getProjectStats(String pluginId, LocalDate fromDate, LocalDate toDate) {
        return projectApiDao.get().projectStats(pluginId, fromDate, toDate);
    }
}
