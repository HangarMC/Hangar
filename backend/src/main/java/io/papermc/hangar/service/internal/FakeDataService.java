package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.api.project.ProjectDonationSettings;
import io.papermc.hangar.model.api.project.ProjectLicense;
import io.papermc.hangar.model.api.project.ProjectSettings;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.projects.ProjectService;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FakeDataService extends HangarComponent {

    private final Faker faker = new Faker();

    private final UserDAO userDAO;
    private final GlobalRoleService globalRoleService;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final ProjectsDAO projectsDAO;

    public FakeDataService(final UserDAO userDAO, final GlobalRoleService globalRoleService, final ProjectService projectService, final ProjectFactory projectFactory, final ProjectsDAO projectsDAO) {
        this.userDAO = userDAO;
        this.globalRoleService = globalRoleService;
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.projectsDAO = projectsDAO;
    }

    @Transactional
    public void generate(final int users, final int projectsPerUser) {
        final HangarAuthenticationToken oldAuth = (HangarAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        try {
            for (int udx = 0; udx < users; udx++) {
                final UserTable user = this.createUser();
                SecurityContextHolder.getContext().setAuthentication(HangarAuthenticationToken.createVerifiedToken(new HangarPrincipal(user.getUserId(), user.getName(), false, Permission.All), oldAuth.getCredentials()));
                for (int pdx = 0; pdx < projectsPerUser; pdx++) {
                    this.createProject(user.getUserId());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(oldAuth);
            this.projectService.refreshHomeProjects();
        }
    }

    public UserTable createUser() {
        final UserTable userTable = this.userDAO.create(UUID.randomUUID(),
            this.normalize(this.faker.simpsons().character()) + this.faker.random().nextInt(500),
            this.faker.internet().safeEmailAddress(),
            this.faker.chuckNorris().fact(),
            "en",
            List.of(),
            false,
            "dark");
        this.globalRoleService.addRole(new GlobalRoleTable(userTable.getId(), GlobalRole.DUMMY));
        return userTable;
    }

    public void createProject(final long ownerId) {
        final ProjectLicense licence = new ProjectLicense(this.config.getLicenses().get(this.faker.random().nextInt(this.config.getLicenses().size())), null);
        final Set<String> keyWords = new HashSet<>();
        for (int i = 0; i < this.faker.random().nextInt(2, 5); i++) {
            keyWords.add(this.faker.marketing().buzzwords());
        }
        final ProjectSettings settings = new ProjectSettings(this.faker.internet().domainName(),
            null,
            null,
            null,
            null,
            licence,
            new ProjectDonationSettings(false, "d"),
            keyWords,
            false,
            "# Sponsored by " + this.faker.beer().style() + " " + this.faker.beer().name());
        final String projectName = this.normalize(this.faker.funnyName().name() + "_" + this.faker.minecraft().animalName());
        final String quote = this.faker.theItCrowd().quotes();
        final NewProjectForm newProject = new NewProjectForm(settings,
            Category.values()[this.faker.random().nextInt(Category.values().length)],
            quote.substring(0, Math.min(quote.length(), 254)),
            ownerId,
            projectName.substring(0, Math.min(projectName.length(), 24)),
            "# " + projectName + "\n\n" + "> " + this.faker.leagueOfLegends().quote());
        final ProjectTable projectTable = this.projectFactory.createProject(newProject);

        projectTable.setVisibility(Visibility.PUBLIC);
        projectTable.setCreatedAt(this.faker.date().past(100 * 365, TimeUnit.DAYS).toLocalDateTime().atOffset(ZoneOffset.UTC));
        this.projectsDAO.update(projectTable);
    }

    private String normalize(final String input) {
        return input.replace(" ", "_").replace("\"", "").replace("'", "").replace(".", "");
    }
}
