package io.papermc.hangar.service;

import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.db.daoold.ProjectStatsDao;
import io.papermc.hangar.db.daoold.ProjectStatsTrackerDao;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.Stats;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatsService extends HangarService {

    private final HangarDao<ProjectStatsDao> projectStatsDao;
    private final HangarDao<ProjectStatsTrackerDao> projectStatsTrackerDao;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Autowired
    public StatsService(HangarDao<ProjectStatsDao> projectStatsDao, HangarDao<ProjectStatsTrackerDao> projectStatsTrackerDao, HttpServletRequest request, HttpServletResponse response) {
        this.projectStatsDao = projectStatsDao;
        this.projectStatsTrackerDao = projectStatsTrackerDao;
        this.request = request;
        this.response = response;
    }

    public Stream<LocalDate> getDaysBetween(LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1));
    }

    public List<Stats> getStats(LocalDate from, LocalDate to) {
        return projectStatsDao.get().getStats(from, to);
    }

    public String getStatDays(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getDay));
    }

    public String getReviewStats(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getReviews));
    }

    public String getUploadStats(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getUploads));
    }

    public String getTotalDownloadStats(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getTotalDownloads));
    }

    public String getUnsafeDownloadsStats(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getUnsafeDownloads));
    }

    public String getFlagsOpenedStats(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getFlagsOpened));
    }

    public String getFlagsClosedStats(List<Stats> stats) {
        return getJsonListAsString(stats.stream().map(Stats::getFlagsClosed));
    }

    public <T> String getJsonListAsString(Stream<T> stream) {
        return stream.map(count -> "\"" + count + "\"").collect(Collectors.joining(", ", "[", "]"));
    }

    public static final String COOKIE_NAME = "_stat";

    public void addProjectView(ProjectsTable project) {
        Long userId = currentUser.get().map(UsersTable::getId).orElse(null);
        InetAddress address = RequestUtil.getRemoteInetAddress(request);
        Optional<String> existingCookie = projectStatsTrackerDao.get().getIndividualViewCookie(userId, address);
        String cookie = existingCookie.orElse(Optional.ofNullable(WebUtils.getCookie(request, COOKIE_NAME)).map(Cookie::getValue).orElse(UUID.randomUUID().toString()));
        projectStatsTrackerDao.get().addProjectView(project.getId(), address, cookie, userId);
        setCookie(cookie);
    }

    public void addVersionDownloaded(ProjectVersionsTable versionsTable) {
        Long userId = currentUser.get().map(UsersTable::getId).orElse(null);
        InetAddress address = RequestUtil.getRemoteInetAddress(request);
        Optional<String> existingCookie = projectStatsTrackerDao.get().getIndividualDownloadCookie(userId, address);
        String cookie = existingCookie.orElse(Optional.ofNullable(WebUtils.getCookie(request, COOKIE_NAME)).map(Cookie::getValue).orElse(UUID.randomUUID().toString()));
        projectStatsTrackerDao.get().addVersionDownload(versionsTable.getProjectId(), versionsTable.getId(), address, cookie, userId);
        setCookie(cookie);
    }

    private void setCookie(String cookie) {
        // TODO maybe secure?
        Cookie newCookie = new Cookie(COOKIE_NAME, cookie);
        newCookie.setPath("/");
        newCookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(newCookie);
    }

    private void processStats(String individualTable, String dayTable, String statColumn, boolean includeVersionId) {
        projectStatsTrackerDao.get().fillStatsUserIdsFromOthers(individualTable);
        projectStatsTrackerDao.get().processStatsMain(individualTable, dayTable, statColumn, true, includeVersionId);
        projectStatsTrackerDao.get().processStatsMain(individualTable, dayTable, statColumn, false, includeVersionId);
        projectStatsTrackerDao.get().deleteOldIndividual(individualTable);
    }

    public void processVersionDownloads() {
        processStats("project_versions_downloads_individual", "project_versions_downloads", "downloads", true);
    }

    public void processProjectViews() {
        processStats("project_views_individual", "project_views", "views", false);
    }
}
