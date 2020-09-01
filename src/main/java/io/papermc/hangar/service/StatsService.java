package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectStatsDao;
import io.papermc.hangar.db.model.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatsService {

    private final HangarDao<ProjectStatsDao> projectStatsDao;

    @Autowired
    public StatsService(HangarDao<ProjectStatsDao> projectStatsDao) {
        this.projectStatsDao = projectStatsDao;
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
}
