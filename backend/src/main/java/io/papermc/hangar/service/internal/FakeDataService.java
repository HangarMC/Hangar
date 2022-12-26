package io.papermc.hangar.service.internal;

import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class FakeDataService extends HangarComponent {

    private final Faker faker = new Faker();

    private final UserDAO userDAO;
    private final GlobalRoleService globalRoleService;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final ProjectsDAO projectsDAO;

    public FakeDataService(UserDAO userDAO, GlobalRoleService globalRoleService, ProjectService projectService, ProjectFactory projectFactory, ProjectsDAO projectsDAO) {
        this.userDAO = userDAO;
        this.globalRoleService = globalRoleService;
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.projectsDAO = projectsDAO;
    }

    @Transactional
    public void generate(int users, int projectsPerUser) {
        HangarAuthenticationToken oldAuth = (HangarAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        try {
            for (int udx = 0; udx < users; udx++) {
                UserTable user = createUser();
                SecurityContextHolder.getContext().setAuthentication(HangarAuthenticationToken.createVerifiedToken(new HangarPrincipal(user.getUserId(), user.getName(), false, Permission.All), oldAuth.getCredentials()));
                for (int pdx = 0; pdx < projectsPerUser; pdx++) {
                    createProject(user.getUserId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(oldAuth);
            projectService.refreshHomeProjects();
        }
    }

    public UserTable createUser() {
        UserTable userTable = userDAO.create(UUID.randomUUID(),
            normalize(faker.simpsons().character()) + faker.random().nextInt(500),
            faker.internet().safeEmailAddress(),
            faker.chuckNorris().fact(),
            "en",
            List.of(),
            false,
            "dark");
        globalRoleService.addRole(new GlobalRoleTable(userTable.getId(), GlobalRole.DUMMY));
        return userTable;
    }

    public void createProject(long ownerId) {
        ProjectLicense licence = new ProjectLicense(config.getLicenses().get(faker.random().nextInt(config.getLicenses().size())), null);
        Set<String> keyWords = new HashSet<>();
        for (int i = 0; i < faker.random().nextInt(2, 5); i++) {
            keyWords.add(faker.marketing().buzzwords());
        }
        ProjectSettings settings = new ProjectSettings(faker.internet().domainName(),
            null,
            null,
            null,
            null,
            licence,
            new ProjectDonationSettings(false, "d"),
            keyWords,
            false,
            "# Sponsored by " + faker.beer().style() + " " + faker.beer().name());
        String projectName = normalize(faker.funnyName().name() + "_" + faker.minecraft().animalName());
        String quote = faker.theItCrowd().quotes();
        NewProjectForm newProject = new NewProjectForm(settings,
            Category.values()[faker.random().nextInt(Category.values().length)],
            quote.substring(0, Math.min(quote.length(), 254)),
            ownerId,
            projectName.substring(0, Math.min(projectName.length(), 24)),
            "# " + projectName + "\n\n" + "> " + faker.leagueOfLegends().quote());
        ProjectTable projectTable = projectFactory.createProject(newProject);

        projectTable.setVisibility(Visibility.PUBLIC);
        projectTable.setCreatedAt(faker.date().past(100 * 365, TimeUnit.DAYS).toLocalDateTime().atOffset(ZoneOffset.UTC));
        projectsDAO.update(projectTable);
    }

    private String normalize(String input) {
        return input.replace(" ", "_").replace("\"", "").replace("'", "").replace(".", "");
    }
}
