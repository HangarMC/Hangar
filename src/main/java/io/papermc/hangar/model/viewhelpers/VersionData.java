package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.generated.PlatformDependency;

import java.util.HashMap;
import java.util.Map;

public class VersionData {

    private final ProjectData p;
    private final ProjectVersionsTable v;
    private final ProjectChannelsTable c;
    private final String approvedBy;
    private final Map<Platform, Map<Dependency, String>> dependencies;

    public VersionData(ProjectData p, ProjectVersionsTable v, ProjectChannelsTable c, String approvedBy, Map<Platform, Map<Dependency, String>> dependencies) {
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

    public Map<Platform, Map<Dependency, String>> getDependencies() {
        return dependencies;
    }

    public Map<PlatformDependency, Map<Dependency, String>> getFormattedDependencies() {
        return this.v.getPlatforms().stream().collect(HashMap::new, (hashMap, platformDependency) -> hashMap.put(platformDependency, this.dependencies.get(platformDependency.getPlatform())), HashMap::putAll);
    }

    public boolean isRecommended() {
        final Long recommendedVersionId = p.getProject().getRecommendedVersionId();
        return recommendedVersionId != null && recommendedVersionId == v.getId();
    }
}
