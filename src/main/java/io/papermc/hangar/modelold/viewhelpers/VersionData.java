package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.modelold.PlatformVersionsTable;
import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.model.api.project.version.Dependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.modelold.generated.PlatformDependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VersionData {

    private final ProjectData p;
    private final ProjectVersionsTable v;
    private final ProjectChannelsTable c;
    private final String approvedBy;
    private final Map<Platform, Map<Dependency, String>> dependencies;
    private final Map<Platform, List<PlatformVersionsTable>> versionPlatformDependencyTables;

    public VersionData(ProjectData p, ProjectVersionsTable v, ProjectChannelsTable c, String approvedBy, Map<Platform, Map<Dependency, String>> dependencies, Map<Platform, List<PlatformVersionsTable>> versionPlatformDependencyTables) {
        this.p = p;
        this.v = v;
        this.c = c;
        this.approvedBy = approvedBy;
        this.dependencies = dependencies;
        this.versionPlatformDependencyTables = versionPlatformDependencyTables;
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

    public Map<Platform, Map<Dependency, String>> getDependencies() {
        return dependencies;
    }

    public Map<PlatformDependency, Map<Dependency, String>> getFormattedDependencies() {
        return versionPlatformDependencyTables.entrySet().stream().collect(HashMap::new, (hashMap, platformListEntry) -> hashMap.put(new PlatformDependency(io.papermc.hangar.modelold.Platform.valueOf(platformListEntry.getKey().name()), platformListEntry.getValue().stream().map(PlatformVersionsTable::getVersion).collect(Collectors.toList())), this.dependencies.get(platformListEntry.getKey())), HashMap::putAll);
    }

    public boolean isRecommended() {
        final Long recommendedVersionId = p.getProject().getRecommendedVersionId();
        return recommendedVersionId != null && recommendedVersionId == v.getId();
    }
}
