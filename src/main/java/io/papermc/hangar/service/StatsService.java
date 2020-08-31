package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectStatsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatsService {

    private final HangarDao<ProjectStatsDao> projectStatsDao;

    @Autowired
    public StatsService(HangarDao<ProjectStatsDao> projectStatsDao) {
        this.projectStatsDao = projectStatsDao;
    }

    public List<String> getReviewStats(LocalDate from, LocalDate to){
        Map<LocalDate, Integer> databaseData = projectStatsDao.get().getNumberOfReviewPerDay(from, to);
        return getDaysBetween(from, to).map(date -> databaseData.getOrDefault(date, 0))
                .map(count -> "\"" + count + "\"").collect(Collectors.toList());
    }

    private Stream<LocalDate> getDaysBetween(LocalDate from, LocalDate to){
        return from.datesUntil(to.plusDays(1));
    }

    public List<String> getStringListOfDates(LocalDate from, LocalDate to){
        return getDaysBetween(from, to)
                .map(date -> "\"" + date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) + "\"")
                .collect(Collectors.toList());
    }
}
