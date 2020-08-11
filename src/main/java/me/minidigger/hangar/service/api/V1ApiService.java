package me.minidigger.hangar.service.api;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.api.V1ApiDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class V1ApiService {

    private final HangarDao<V1ApiDao> v1ApiDao;

    @Autowired
    public V1ApiService(HangarDao<V1ApiDao> v1ApiDao) {
        this.v1ApiDao = v1ApiDao;
    }

    public Map<Long, List<ProjectsTable>> getUsersProjects(List<Long> userIds) {
        return v1ApiDao.get().getProjectsForUsers(userIds).stream().collect(Collectors.groupingBy(ProjectsTable::getOwnerId));
    }

    // TODO better way for this?
    public Map<Long, List<String>> getStarredPlugins(List<Long> userIds) {
        return v1ApiDao.get().getStarredPlugins(userIds).stream()
                .collect(
                        Collectors.groupingBy(Entry::getKey)
                ).entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Entry::getKey,
                                longListEntry -> longListEntry.getValue().stream().map(Entry::getValue).collect(Collectors.toList()))
                        );
    }

    public Map<Long, List<Role>> getUsersGlobalRoles(List<Long> userIds) {
        return v1ApiDao.get().getUsersGlobalRoles(userIds).stream()
                .collect(
                        Collectors.groupingBy(Entry::getKey)
                ).entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Entry::getKey,
                                listEntry -> listEntry.getValue().stream().map(entry -> Role.fromId(entry.getValue().getId())).collect(Collectors.toList())
                        )
                );
    }

    public Map<Long, List<ProjectChannelsTable>> getProjectsChannels(List<Long> projectIds) {
        return v1ApiDao.get().getProjectsChannels(projectIds).stream()
                .collect(
                        Collectors.groupingBy(Entry::getKey)
                ).entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Entry::getKey,
                                listEntry -> listEntry.getValue().stream().map(Entry::getValue).collect(Collectors.toList())
                        )
                );
    }

    public Map<Long, ProjectVersionsTable> getProjectsRecommendedVersion(List<Long> projectIds) {
        return v1ApiDao.get().getProjectsRecommendedVersion(projectIds);
    }

    public Map<Long, ProjectChannelsTable> getProjectsRecommendedVersionChannel(List<Long> projectIds) {
        return v1ApiDao.get().getProjectsRecommendedVersionChannel(projectIds);
    }

    public Map<Long, List<Entry<UserProjectRolesTable, UsersTable>>> getProjectsMembers(List<Long> projectIds) {
        return v1ApiDao.get().getProjectsMembers(projectIds).stream()
                .collect(
                        Collectors.groupingBy(entry -> entry.getKey().getProjectId())
                );
    }

    public Map<Long, List<ProjectVersionTagsTable>> getVersionsTags(List<Long> versionIds) {
        return v1ApiDao.get().getVersionsTags(versionIds).stream()
                .collect(
                        Collectors.groupingBy(Entry::getKey)
                ).entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Entry::getKey,
                                listEntry -> listEntry.getValue().stream().map(Entry::getValue).collect(Collectors.toList())
                        )
                );
    }
}
