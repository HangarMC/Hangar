package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarStatsDAO;
import io.papermc.hangar.db.dao.internal.table.stats.ProjectVersionDownloadsDAO;
import io.papermc.hangar.db.dao.internal.table.stats.ProjectViewsDAO;
import io.papermc.hangar.model.db.ProjectIdentified;
import io.papermc.hangar.model.db.VersionIdentified;
import io.papermc.hangar.model.db.stats.ProjectVersionDownloadIndividualTable;
import io.papermc.hangar.model.db.stats.ProjectViewIndividualTable;
import io.papermc.hangar.model.internal.admin.DayStats;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StatService extends HangarService {

    private static final String STAT_TRACKING_COOKIE = "hangar_stats";

    private final HangarStatsDAO hangarStatsDAO;
    private final ProjectViewsDAO projectViewsDAO;
    private final ProjectVersionDownloadsDAO projectVersionDownloadsDAO;

    @Autowired
    public StatService(HangarDao<HangarStatsDAO> hangarStatsDAO, HangarDao<ProjectViewsDAO> projectViewsDAO, HangarDao<ProjectVersionDownloadsDAO> projectVersionDownloadsDAO) {
        this.hangarStatsDAO = hangarStatsDAO.get();
        this.projectViewsDAO = projectViewsDAO.get();
        this.projectVersionDownloadsDAO = projectVersionDownloadsDAO.get();
    }

    public List<DayStats> getStats(LocalDate from, LocalDate to) {
        return hangarStatsDAO.getStats(from, to);
    }

    public void addProjectView(ProjectIdentified projectIdentified) {
        Long userId = getHangarUserId();
        InetAddress address = RequestUtil.getRemoteInetAddress(request);
        Optional<String> existingCookie = projectViewsDAO.getIndividualView(userId, address).map(ProjectViewIndividualTable::getCookie);
        String cookie = existingCookie.orElse(Optional.ofNullable(WebUtils.getCookie(request, STAT_TRACKING_COOKIE)).map(Cookie::getValue).orElse(UUID.randomUUID().toString()));
        projectViewsDAO.insert(new ProjectViewIndividualTable(address, cookie, userId, projectIdentified.getProjectId()));
        setCookie(cookie);
    }

    public <T extends VersionIdentified & ProjectIdentified> void addVersionDownload(T versionIdentified) {
        Long userId = getHangarUserId();
        InetAddress address = RequestUtil.getRemoteInetAddress(request);
        Optional<String> existingCookie = projectVersionDownloadsDAO.getIndividualView(userId, address).map(ProjectVersionDownloadIndividualTable::getCookie);
        String cookie = existingCookie.orElse(Optional.ofNullable(WebUtils.getCookie(request, STAT_TRACKING_COOKIE)).map(Cookie::getValue).orElse(UUID.randomUUID().toString()));
        projectVersionDownloadsDAO.insert(new ProjectVersionDownloadIndividualTable(address, cookie, userId, versionIdentified.getProjectId(), versionIdentified.getVersionId()));
        setCookie(cookie);
    }

    private void setCookie(String cookieValue) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from(STAT_TRACKING_COOKIE, cookieValue)
                        .secure(config.security.isSecure())
                        .path("/")
                        .maxAge((long) (60 * 60 * 24 * 356.24 * 1000))
                        .sameSite("Strict")
                        .build().toString()
        );
    }

    private void processStats(String individualTable, String dayTable, String statColumn, boolean includeVersionId) {
        hangarStatsDAO.fillStatsUserIdsFromOthers(individualTable);
        hangarStatsDAO.processStatsMain(individualTable, dayTable, statColumn, true, includeVersionId);
        hangarStatsDAO.processStatsMain(individualTable, dayTable, statColumn, false, includeVersionId);
        hangarStatsDAO.deleteOldIndividual(individualTable);
    }

    public void processVersionDownloads() {
        processStats("project_versions_downloads_individual", "project_versions_downloads", "downloads", true);
    }

    public void processProjectViews() {
        processStats("project_views_individual", "project_views", "views", false);
    }
}
