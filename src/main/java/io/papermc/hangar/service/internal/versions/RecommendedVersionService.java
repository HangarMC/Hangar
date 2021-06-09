package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.RecommendedProjectVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.RecommendedProjectVersionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RecommendedVersionService extends HangarComponent {

    private final RecommendedProjectVersionsDAO recommendedProjectVersionsDAO;

    @Autowired
    public RecommendedVersionService(RecommendedProjectVersionsDAO recommendedProjectVersionsDAO) {
        this.recommendedProjectVersionsDAO = recommendedProjectVersionsDAO;
    }

    public void setRecommendedVersion(long projectId, long versionId, Platform platform) {
        recommendedProjectVersionsDAO.delete(projectId, platform);
        recommendedProjectVersionsDAO.insert(new RecommendedProjectVersionTable(versionId, projectId, platform));
    }

    public Map<Platform, String> getRecommendedVersions(long projectId) {
        return recommendedProjectVersionsDAO.getRecommendedVersions(projectId);
    }

    public Map<Platform, String> getRecommendedVersions(String owner, String slug) {
        return recommendedProjectVersionsDAO.getRecommendedVersions(owner, slug);
    }

    public String fixVersionString(String author, String slug, String versionString, Platform platform) {
        if (!"recommended".equals(versionString)) {
            return versionString;
        }
        Map<Platform, String> recommendedVersions = getRecommendedVersions(author, slug);
        String recommendedVersion = recommendedVersions.get(platform);
        if (recommendedVersion != null) {
            return recommendedVersion;
        }
        throw new HangarApiException(HttpStatus.NOT_FOUND, "No recommended version found");
    }
}
