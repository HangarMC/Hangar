package io.papermc.hangar.service.internal.defaults;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.globaldata.dao.AnnouncementTable;
import io.papermc.hangar.components.globaldata.dao.GlobalDataDAO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static io.papermc.hangar.components.observability.TransactionUtil.withTransaction;

@Component
public class PopulationService extends HangarComponent {

    private static final Logger log = LoggerFactory.getLogger(PopulationService.class);

    private final RolesDAO rolesDAO;
    private final PlatformVersionDAO platformVersionDAO;
    private final GlobalDataDAO globalDataDAO;
    private final RestClient restClient;

    public PopulationService(final RolesDAO rolesDAO, final PlatformVersionDAO platformVersionsDao, final GlobalDataDAO globalDataDAO, final RestClient restClient) {
        this.rolesDAO = rolesDAO;
        this.platformVersionDAO = platformVersionsDao;
        this.globalDataDAO = globalDataDAO;
        this.restClient = restClient;
    }

    @EventListener
    public void populateTables(final ContextRefreshedEvent event) {
        try {
            withTransaction("task", "PopulationService#populateTables()", () -> {
                this.populateRoles();
                this.populatePlatformVersions();
                this.populateAnnouncements();
            });
        } catch (Exception ex) {
            log.warn("Error while populating tables", ex);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void populateRoles() {
        GlobalRole.values();
        ProjectRole.values();
        OrganizationRole.values();

        final RoleTable admin = this.rolesDAO.getById(1);
        if (admin != null && admin.getPermission().has(Permission.All)) {
            log.info("The 'roles' table is already populated");
            return;
        }

        log.info("Populating 'roles' table with initial values");
        for (final Role<?> role : Role.ID_ROLES.values()) {
            this.rolesDAO.insert(RoleTable.fromRole(role));
        }
    }

    private void populatePlatformVersions() {
        record FillResponseData(Data data) {
            record Data(List<Project> projects) {
                record Project(String id, List<Version> versions) {
                    record Version(String id) {
                    }
                }
            }
        }

        var fillResponse = this.restClient.post().uri("https://fill.papermc.io/graphql").header("Hangar/1.0 (https://hangar.papermc.io)").body(Map.of("query", """
            {
              projects {
                id
                versions {
                  id
                }
              }
            }
            """)).retrieve().toEntity(FillResponseData.class);
        if (!fillResponse.getStatusCode().is2xxSuccessful() || !fillResponse.hasBody()) {
            throw new RuntimeException("Failed to populate platform versions, Fill returned " + fillResponse.getStatusCode() + ": " + fillResponse.getBody());
        }

        List<PlatformVersionTable> tables = new ArrayList<>();
        for (final var project : Objects.requireNonNull(fillResponse.getBody()).data().projects()) {
            switch (project.id) {
                case "paper" -> project.versions.stream().map(v -> new PlatformVersionTable(Platform.PAPER, v.id)).forEach(tables::add);
                case "waterfall" -> project.versions.stream().map(v -> new PlatformVersionTable(Platform.WATERFALL, v.id)).forEach(tables::add);
                case "velocity" -> project.versions.stream().map(v -> new PlatformVersionTable(Platform.VELOCITY, v.id)).forEach(tables::add);
            }
        }

        // https://regex101.com/r/JdIutj/1
        var pattern = Pattern.compile("^\\d+.\\d+(.\\d)?$");
        tables.removeIf(t -> !pattern.matcher(t.getVersion()).matches());

        int result = this.platformVersionDAO.insertAll(tables);
        if (result > 0) {
            log.info("Populated 'platform_versions' table with {} new versions", result);
        } else {
            log.info("No new versions were added to the 'platform_versions' table");
        }
    }

    private void populateAnnouncements() {
        if (config.dev()) {
            try {
                if (!this.globalDataDAO.getAnnouncements().isEmpty()) {
                    log.info("The 'announcements' table is already populated");
                    return;
                }
                this.globalDataDAO.insertAnnouncement(new AnnouncementTable("This is a local server for testing purposes. There is a public staging instance at <a href=\"https://hangar.papermc.dev\" style=\"text-decoration: underline\">https://hangar.papermc.dev</a> and the production site can be found at <a href=\"https://hangar.papermc.io\" style=\"text-decoration: underline\">https://hangar.papermc.io</a>.", "#2f4476", 1));
                log.info("Inserted local development announcement into 'announcements' table");
            } catch (Exception ex) {
                log.warn("Failed to populate announcement table: {}:  {}", ex.getClass().getName(), ex.getMessage());
            }
        }
    }
}
