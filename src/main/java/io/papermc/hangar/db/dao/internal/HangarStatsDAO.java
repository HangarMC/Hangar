package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.admin.DayStats;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@UseClasspathSqlLocator
public interface HangarStatsDAO {

    @SqlQuery
    @RegisterConstructorMapper(DayStats.class)
    List<DayStats> getStats(LocalDate startDate, LocalDate endDate);
}
