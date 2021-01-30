package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.PlatformVersionsDao;
import io.papermc.hangar.db.daoold.RoleDao;
import io.papermc.hangar.db.modelold.PlatformVersionsTable;
import io.papermc.hangar.db.modelold.RolesTable;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.modelold.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PopulationService {

    private static final Logger log = LoggerFactory.getLogger(PopulationService.class);

    private final HangarDao<RoleDao> roleDao;
    private final HangarDao<PlatformVersionsDao> platformVersionsDao;

    public PopulationService(HangarDao<RoleDao> roleDao, HangarDao<PlatformVersionsDao> platformVersionsDao) {
        this.roleDao = roleDao;
        this.platformVersionsDao = platformVersionsDao;
    }

    @EventListener
    public void populateTables(ContextRefreshedEvent event) {
        populateRoles();
        populatePlatformVersions();
    }

    private void populateRoles() {
        RolesTable admin = roleDao.get().getById(1);
        if (admin != null && admin.getRole() == Role.HANGAR_ADMIN) {
            log.info("The 'roles' table is already populated");
            return;
        }

        log.info("Populating 'roles' table with initial values");
        for (Role role : Role.values()) {
            roleDao.get().insert(RolesTable.fromRole(role));
        }
    }

    private final List<String> paperVersions = List.of("1.8", "1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16");
    private final List<String> waterfallVersions = List.of("1.11", "1.12", "1.13", "1.14", "1.15", "1.16");
    private final List<String> velocityVersions = List.of("1.0", "1.1");

    private void populatePlatformVersions() {
        Map<Platform, List<String>> platformVersions = platformVersionsDao.get().getVersions();
        if (platformVersions.isEmpty()) {
            log.info("Populating 'platform_versions' table with initial values");
            platformVersionsDao.get().insert(paperVersions.stream().map(v -> new PlatformVersionsTable(Platform.PAPER, v)).collect(Collectors.toList()));
            platformVersionsDao.get().insert(velocityVersions.stream().map(v -> new PlatformVersionsTable(Platform.VELOCITY, v)).collect(Collectors.toList()));
            platformVersionsDao.get().insert(waterfallVersions.stream().map(v -> new PlatformVersionsTable(Platform.WATERFALL, v)).collect(Collectors.toList())); // TODO ok, I have no idea why this throws some duplicate key error. I can manually insert them just fine.
        } else {
            log.info("The 'platform_versions' table is already populated");
        }
    }
}
