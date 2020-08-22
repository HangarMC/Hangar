package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.Dependency;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
        // Value is nullable, so we can't use Collectors#toMap
        Map<Dependency, ProjectsTable> map = new HashMap<>();
        for (Entry<Dependency, ProjectsTable> entry : dependencies.entrySet()) {
            if (Platform.getByDependencyId(entry.getKey().getPluginId()) == null) { // Exclude the platform dependency
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    public boolean isRecommended() {
        final Long recommendedVersionId = p.getProject().getRecommendedVersionId();
        return recommendedVersionId != null && recommendedVersionId == v.getId();
    }
}
