package io.papermc.hangar.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectVersionDao;
import io.papermc.hangar.db.dao.api.V1ApiDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class V1ApiService {

    private final HangarDao<V1ApiDao> v1ApiDao;
    private final HangarDao<ProjectVersionDao> projectVersionDao;
    private final ObjectMapper mapper;

    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectVersionsTable> projectVersionsTable;

    @Autowired
    public V1ApiService(HangarDao<V1ApiDao> v1ApiDao, HangarDao<ProjectVersionDao> projectVersionDao, ObjectMapper mapper, Supplier<ProjectsTable> projectsTable, Supplier<ProjectVersionsTable> projectVersionsTable) {
        this.v1ApiDao = v1ApiDao;
        this.projectVersionDao = projectVersionDao;
        this.mapper = mapper;
        this.projectsTable = projectsTable;
        this.projectVersionsTable = projectVersionsTable;
    }

    public List<Long> projectIdPreSearch(String q, List<Integer> categories, ProjectSortingStrategy strategy, long limit, long offset) {
        return v1ApiDao.get().apiIdSearch(q, categories, strategy.getSql(), limit, offset);
    }

    public List<ProjectsTable> getProjects(String q, List<Integer> categories, ProjectSortingStrategy strategy, long limit, long offset) {
        return v1ApiDao.get().getProjects(q, categories, strategy.getSql(), limit, offset);
    }

    public Map<Long, List<ProjectsTable>> getUsersProjects(List<Long> userIds) {
        return v1ApiDao.get().getProjectsForUsers(userIds).stream().collect(Collectors.groupingBy(ProjectsTable::getOwnerId));
    }

    public List<UsersTable> getUsers(int offset, Integer limit) {
        return v1ApiDao.get().getUsers(offset, limit);
    }

    public ArrayNode getVersionList(String channels, long limit, long offset, boolean onlyPublic) {
        List<String> channelList = new ArrayList<>();
        if (channels != null) {
            channelList = Arrays.asList(channels.split(","));
        }

        ProjectsTable projectsTable = this.projectsTable.get();
        List<ProjectVersionsTable> versions = v1ApiDao.get().getProjectVersions(channelList, projectsTable.getOwnerName(), projectsTable.getSlug(), limit, offset, onlyPublic);
        List<Long> versionIds = versions.stream().map(ProjectVersionsTable::getId).collect(Collectors.toList());
        Map<Long, ProjectChannelsTable> versionChannels = v1ApiDao.get().getProjectVersionChannels(versionIds);
        Map<Long, List<ProjectVersionTagsTable>> versionTags = mapListToMap(v1ApiDao.get().getVersionsTags(versionIds));
        return writeVersions(projectsTable, versions, versionChannels, versionTags);
    }

    public ObjectNode getVersion() {
        ProjectsTable project = projectsTable.get();
        ProjectVersionsTable versionsTable = projectVersionsTable.get();
        ProjectChannelsTable channelsTable = v1ApiDao.get().getProjectVersionChannels(List.of(versionsTable.getId())).get(versionsTable.getId());
        List<ProjectVersionTagsTable> tags = mapListToMap(v1ApiDao.get().getVersionsTags(List.of(versionsTable.getId()))).get(versionsTable.getId());
        return writeVersion(project, versionsTable, channelsTable, tags);
    }

    public <V> Map<Long, List<V>> mapListToMap(List<Map.Entry<Long, V>> map) {
        Map<Long, List<V>> returnMap = new HashMap<>();
        map.forEach(entry -> {
            returnMap.computeIfAbsent(entry.getKey(), userId -> new ArrayList<>()).add(entry.getValue());
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

    public ObjectNode getVersionTags(String author, String slug) {
        ProjectVersionsTable projectVersion = projectVersionsTable.get();
        List<ProjectVersionTagsTable> tags = mapListToMap(v1ApiDao.get().getVersionsTags(List.of(projectVersion.getId()))).get(projectVersion.getId());
        ObjectNode obj = mapper.createObjectNode()
                .put("author", author)
                .put("slug", slug)
                .put("version", projectVersion.getVersionString());

        ArrayNode tagsArray = mapper.createArrayNode();
        tags.forEach(tag -> tagsArray.add(mapper.valueToTree(tag)));
        obj.set("tags", tagsArray);
        return obj;
    }

    private ArrayNode writeVersions(ProjectsTable project, List<ProjectVersionsTable> projectVersions, Map<Long, ProjectChannelsTable> projectChannels, Map<Long, List<ProjectVersionTagsTable>> tags) {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (ProjectVersionsTable pvt : projectVersions) {
            arrayNode.add(writeVersion(project, pvt, projectChannels.get(pvt.getId()), tags.get(pvt.getId())));
        }
        return arrayNode;
    }

    private ObjectNode writeVersion(ProjectsTable project, ProjectVersionsTable v, ProjectChannelsTable channel, List<ProjectVersionTagsTable> tags) {
        ObjectNode objectNode = mapper.createObjectNode()
                .put("id", v.getId())
                .put("createdAt", v.getCreatedAt().toString())
                .put("name", v.getVersionString())
                .put("author", project.getOwnerName())
                .put("slug", project.getSlug())
                .put("fileSize", v.getFileSize())
                .put("md5", v.getHash())
                .put("staffApproved", v.getReviewState().isChecked())
                .put("reviewState", v.getReviewState().toString())
                .put("href", "/" + project.getOwnerName() + "/" + project.getSlug() + "/versions/" + v.getVersionString())
                .put("downloads", 0)
                .put("description", v.getDescription());
        objectNode.set("channel", mapper.valueToTree(channel));
        objectNode.set("dependencies", Dependency.from(v.getDependencies()).stream().collect(Collector.of(mapper::createArrayNode, (array, dep) -> {
            ObjectNode depObj = mapper.createObjectNode()
                    //TODO dependency identification
                    .put("pluginId", dep.getPluginId())
                    .put("version", dep.getVersion());
            array.add(depObj);
        }, (ignored1, ignored2) -> {
            throw new UnsupportedOperationException();
        })));

        if (v.getVisibility() != Visibility.PUBLIC) {
            ObjectNode visObj = mapper.createObjectNode()
                    .put("type", v.getVisibility().getName())
                    .put("css", v.getVisibility().getCssClass());
            objectNode.set("visibility", visObj);
        }
        objectNode.set("tags", tags.stream().collect(Collector.of(mapper::createArrayNode, (array, tag) -> array.add(mapper.valueToTree(tag)), (t1, t2) -> {
            throw new UnsupportedOperationException();
        })));
        return objectNode;
    }
}
