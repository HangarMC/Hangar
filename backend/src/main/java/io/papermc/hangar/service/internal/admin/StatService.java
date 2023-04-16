package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarStatsDAO;
import io.papermc.hangar.db.dao.internal.table.stats.ProjectVersionDownloadStatsDAO;
import io.papermc.hangar.db.dao.internal.table.stats.ProjectViewsDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.stats.ProjectVersionDownloadIndividualTable;
import io.papermc.hangar.model.db.stats.ProjectViewIndividualTable;
import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.identified.VersionIdentified;
import io.papermc.hangar.model.internal.admin.DayStats;
import io.papermc.hangar.util.RequestUtil;
import jakarta.servlet.http.Cookie;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

@Service
public class StatService extends HangarComponent {

    private static final String STAT_TRACKING_COOKIE = "hangar_stats";

    private final HangarStatsDAO hangarStatsDAO;
    private final ProjectViewsDAO projectViewsDAO;
    private final ProjectVersionDownloadStatsDAO projectVersionDownloadStatsDAO;

    @Autowired
    public StatService(final HangarStatsDAO hangarStatsDAO, final ProjectViewsDAO projectViewsDAO, final ProjectVersionDownloadStatsDAO projectVersionDownloadStatsDAO) {
        this.hangarStatsDAO = hangarStatsDAO;
        this.projectViewsDAO = projectViewsDAO;
        this.projectVersionDownloadStatsDAO = projectVersionDownloadStatsDAO;
    }

    public List<DayStats> getStats(final LocalDate from, final LocalDate to) {
        return this.hangarStatsDAO.getStats(from, to);
    }

    @Transactional
    public void addProjectView(final ProjectIdentified projectIdentified) {
        final Long userId = this.getHangarUserId();
        final InetAddress address = RequestUtil.getRemoteInetAddress(this.request);
        final Optional<String> existingCookie = this.projectViewsDAO.getIndividualView(userId, address).map(ProjectViewIndividualTable::getCookie);
        final String cookie = existingCookie.orElseGet(() -> {
            final Cookie value = WebUtils.getCookie(this.request, STAT_TRACKING_COOKIE);
            if (value != null) {
                return value.getValue();
            }
            return UUID.randomUUID().toString();
        });
        this.projectViewsDAO.insert(new ProjectViewIndividualTable(address, cookie, userId, projectIdentified.getProjectId()));
        this.setCookie(cookie);
    }

    @Transactional
    public <T extends VersionIdentified & ProjectIdentified> void addVersionDownload(final T versionIdentified, final Platform platform) {
        final Long userId = this.getHangarUserId();
        final InetAddress address = RequestUtil.getRemoteInetAddress(this.request);
        final Optional<String> existingCookie = this.projectVersionDownloadStatsDAO.getIndividualDownload(userId, address).map(ProjectVersionDownloadIndividualTable::getCookie);
        final String cookie = existingCookie.orElse(Optional.ofNullable(WebUtils.getCookie(this.request, STAT_TRACKING_COOKIE)).map(Cookie::getValue).orElse(UUID.randomUUID().toString()));
        this.projectVersionDownloadStatsDAO.insert(new ProjectVersionDownloadIndividualTable(address, cookie, userId, versionIdentified.getProjectId(), versionIdentified.getVersionId(), platform));
        this.setCookie(cookie);
    }

    private void setCookie(final String cookieValue) {
        this.response.addHeader(HttpHeaders.SET_COOKIE,
            ResponseCookie.from(STAT_TRACKING_COOKIE, cookieValue)
                .secure(this.config.security.secure())
                .path("/")
                .maxAge((long) (60 * 60 * 24 * 356.24 * 1000))
                .sameSite("Strict")
                .build().toString()
        );
    }

    @Transactional
    public void processVersionDownloads() {
        this.processStats("project_versions_downloads_individual", "project_versions_downloads", "downloads", true);
    }

    @Transactional
    public void processProjectViews() {
        this.processStats("project_views_individual", "project_views", "views", false);
    }

    private void processStats(final String individualTable, final String dayTable, final String statColumn, final boolean downloads) {
        this.hangarStatsDAO.fillStatsUserIdsFromOthers(individualTable);
        this.hangarStatsDAO.processStatsMain(individualTable, dayTable, statColumn, true, downloads);
        this.hangarStatsDAO.processStatsMain(individualTable, dayTable, statColumn, false, downloads);
        this.hangarStatsDAO.deleteOldIndividual(individualTable);
    }
}
