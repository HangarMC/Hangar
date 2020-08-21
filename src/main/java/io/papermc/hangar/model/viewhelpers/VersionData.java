package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.Dependency;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class VersionData {

    private final ProjectData p;
    private final ProjectVersionsTable v;
    private final ProjectChannelsTable c;
    private final String approvedBy;
    private final Map<Dependency, ProjectsTable> dependencies;

    public VersionData(ProjectData p, ProjectVersionsTable v, ProjectChannelsTable c, String approvedBy, Map<Dependency, ProjectsTable> dependencies) {
        this.p = p;
        this.v = v;
        this.c = c;
        this.approvedBy = approvedBy;
        this.dependencies = dependencies;
    }

    public ProjectData getP() {
        return p;
    }

    public ProjectVersionsTable getV() {
        return v;
    }

    public ProjectChannelsTable getC() {
        return c;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Set<Dependency> getDependencies() {
        return dependencies.keySet();
    }

    public Map<Dependency, ProjectsTable> getFilteredDependencies() {
        List<String> platformIds = Arrays.stream(Platform.getValues()).map(Platform::getDependencyId).collect(Collectors.toList());
        return dependencies.entrySet().stream().filter(entry -> !platformIds.contains(entry.getKey().getPluginId())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public boolean isRecommended() {
        return p.getProject().getRecommendedVersionId() == v.getId();
    }
}
