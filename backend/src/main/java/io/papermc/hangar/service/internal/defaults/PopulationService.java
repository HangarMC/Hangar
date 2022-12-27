package io.papermc.hangar.service.internal.defaults;

import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.roles.RolesDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.PlatformVersionTable;
import io.papermc.hangar.model.db.roles.RoleTable;
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

    private final RolesDAO rolesDAO;
    private final PlatformVersionDAO platformVersionDAO;

    public PopulationService(final RolesDAO rolesDAO, final PlatformVersionDAO platformVersionsDao) {
        this.rolesDAO = rolesDAO;
        this.platformVersionDAO = platformVersionsDao;
    }

    @EventListener
    public void populateTables(final ContextRefreshedEvent event) {
        this.populateRoles();
        this.populatePlatformVersions();
    }

    private void populateRoles() {
        final RoleTable admin = this.rolesDAO.getById(1);
        if (admin != null && admin.getPermission().has(Permission.All)) {
            log.info("The 'roles' table is already populated");
            return;
        }

        GlobalRole.values();
        ProjectRole.values();
        OrganizationRole.values();
        log.info("Populating 'roles' table with initial values");
        for (final Role<?> role : Role.ID_ROLES.values()) {
            this.rolesDAO.insert(RoleTable.fromRole(role));
        }
    }

    private final List<String> paperVersions = List.of("1.8", "1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19");
    private final List<String> waterfallVersions = List.of("1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19");
    private final List<String> velocityVersions = List.of("1.0", "1.1", "3.0", "3.1");

    private void populatePlatformVersions() {
        final Map<Platform, List<String>> platformVersions = this.platformVersionDAO.getVersions();
        if (platformVersions.isEmpty()) {
            log.info("Populating 'platform_versions' table with initial values");
            this.platformVersionDAO.insertAll(this.paperVersions.stream().map(v -> new PlatformVersionTable(Platform.PAPER, v)).collect(Collectors.toList()));
            this.platformVersionDAO.insertAll(this.velocityVersions.stream().map(v -> new PlatformVersionTable(Platform.VELOCITY, v)).collect(Collectors.toList()));
            this.platformVersionDAO.insertAll(this.waterfallVersions.stream().map(v -> new PlatformVersionTable(Platform.WATERFALL, v)).collect(Collectors.toList()));
        } else {
            log.info("The 'platform_versions' table is already populated");
        }
    }
}
