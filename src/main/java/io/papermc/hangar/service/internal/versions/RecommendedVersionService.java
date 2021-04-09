package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.RecommendedProjectVersionsDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.RecommendedProjectVersionTable;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendedVersionService extends HangarComponent {

    private final RecommendedProjectVersionsDAO recommendedProjectVersionsDAO;

    @Autowired
    public RecommendedVersionService(HangarDao<RecommendedProjectVersionsDAO> recommendedProjectVersionsDAO) {
        this.recommendedProjectVersionsDAO = recommendedProjectVersionsDAO.get();
    }

    public void setRecommendedVersion(long projectId, long versionId, Platform platform) {
        recommendedProjectVersionsDAO.delete(projectId, platform);
        recommendedProjectVersionsDAO.insert(new RecommendedProjectVersionTable(versionId, projectId, platform));
    }

    public Map<Platform, Long> getRecommendedVersions(long projectId) {
        return recommendedProjectVersionsDAO.getRecommendedVersions(projectId).stream().collect(Collectors.toMap(pair -> Platform.values()[pair.getKey().intValue()], Pair::getValue));
    }
}
