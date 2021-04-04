package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarStatsDAO;
import io.papermc.hangar.model.internal.admin.DayStats;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatService extends HangarService {

    private final HangarStatsDAO hangarStatsDAO;

    @Autowired
    public StatService(HangarDao<HangarStatsDAO> hangarStatsDAO) {
        this.hangarStatsDAO = hangarStatsDAO.get();
    }

    public List<DayStats> getStats(LocalDate from, LocalDate to) {
        return hangarStatsDAO.getStats(from, to);
    }
}
