package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformService extends HangarService {

    private final PlatformVersionDAO platformVersionDAO;

    @Autowired
    public PlatformService(HangarDao<PlatformVersionDAO> platformVersionDAO) {
        this.platformVersionDAO = platformVersionDAO.get();
    }

    public List<String> getVersionsForPlatform(Platform platform) {
        return platformVersionDAO.getVersionsForPlatform(platform);
    }
}
