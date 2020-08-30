package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.V1ApiDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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

    public List<UsersTable> getUsers(int offset, Integer limit) {
        return v1ApiDao.get().getUsers(offset, limit);
    }

    public <V> Map<Long, List<V>> mapListToMap(List<Map.Entry<Long, V>> map){
        Map<Long, List<V>> returnMap = new HashMap<>();
        map.forEach(entry -> {
            returnMap.computeIfAbsent(entry.getKey(), userId ->new ArrayList<>()).add(entry.getValue());
        });
        return returnMap;
    }

    public Map<Long, List<String>> getStarredPlugins(List<Long> userIds) {
        return mapListToMap(v1ApiDao.get().getStarredPlugins(userIds));
    }

    public Map<Long, List<Role>> getUsersGlobalRoles(List<Long> userIds) {
        Map<Long, List<Role>> returnMap = new HashMap<>();
        v1ApiDao.get().getUsersGlobalRoles(userIds).forEach(entry -> {
            returnMap.computeIfAbsent(entry.getKey(), userId -> new ArrayList<>())
                    .add(Role.fromId(entry.getValue().getId()));
        });
        return returnMap;
    }

    public Map<Long, List<ProjectChannelsTable>> getProjectsChannels(List<Long> projectIds) {
        return mapListToMap(v1ApiDao.get().getProjectsChannels(projectIds));
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
        return mapListToMap(v1ApiDao.get().getVersionsTags(versionIds));
    }
}
