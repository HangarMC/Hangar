package io.papermc.hangar.service.internal;

import dev.samstevens.totp.qr.QrDataFactory;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.model.credential.PasswordCredential;
import io.papermc.hangar.components.auth.model.credential.TotpCredential;
import io.papermc.hangar.components.auth.service.CredentialsService;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.api.project.ProjectLicense;
import io.papermc.hangar.model.api.project.settings.Link;
import io.papermc.hangar.model.api.project.settings.LinkSection;
import io.papermc.hangar.model.api.project.settings.ProjectSettings;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FakeDataService extends HangarComponent {

    private final Faker faker = new Faker();

    private final UserDAO userDAO;
    private final GlobalRoleService globalRoleService;
    private final ProjectFactory projectFactory;
    private final ProjectsDAO projectsDAO;
    private final CredentialsService credentialsService;
    private final PasswordEncoder passwordEncoder;
    private final QrDataFactory qrDataFactory;
    private final AvatarService avatarService;

    public FakeDataService(final UserDAO userDAO, final GlobalRoleService globalRoleService, final ProjectFactory projectFactory, final ProjectsDAO projectsDAO, final CredentialsService credentialsService, final PasswordEncoder passwordEncoder, final QrDataFactory qrDataFactory, final AvatarService avatarService) {
        this.userDAO = userDAO;
        this.globalRoleService = globalRoleService;
        this.projectFactory = projectFactory;
        this.projectsDAO = projectsDAO;
        this.credentialsService = credentialsService;
        this.passwordEncoder = passwordEncoder;
        this.qrDataFactory = qrDataFactory;
        this.avatarService = avatarService;
    }

    public void generate(final int users, final int projectsPerUser) {
        final HangarAuthenticationToken oldAuth = (HangarAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        try {
            for (int udx = 0; udx < users; udx++) {
                final UserTable user;
                try {
                    user = this.createUser();
                } catch (final Exception ignored) {
                    continue;
                }
                SecurityContextHolder.getContext().setAuthentication(HangarAuthenticationToken.createVerifiedToken(new HangarPrincipal(user.getUserId(), user.getName(), user.getEmail(), false, Permission.All, null, 2, true), oldAuth.getCredentials()));
                for (int pdx = 0; pdx < projectsPerUser; pdx++) {
                    try {
                        this.createProject(user.getUserId());
                    } catch (final Exception ignored) {
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(oldAuth);
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
            "dark",
            true,
            this.avatarService.getDefaultAvatarUrl(),
            new JSONB(Map.of()));
        this.globalRoleService.addRole(new GlobalRoleTable(userTable.getId(), GlobalRole.DUMMY));
        return userTable;
    }

    public void createProject(final long ownerId) {
        final String type = this.config.getLicenses().get(this.faker.random().nextInt(this.config.getLicenses().size()));
        final ProjectLicense licence = new ProjectLicense(null, null, type);
        final Set<String> keyWords = new HashSet<>();
        for (int i = 0; i < this.faker.random().nextInt(2, 5); i++) {
            String keyword = this.faker.marketing().buzzwords().replace(" ", "-").replace("'", "").replace(".", "");
            keyword = keyword.length() > 16 ? keyword.substring(0, 16) : keyword;
            keyWords.add(keyword);
        }
        final ProjectSettings settings = new ProjectSettings(
            List.of(
                new LinkSection(0, "top", "Top", List.of(
                    new Link(0, "Wiki", "https://github.com"),
                    new Link(0, "Homepage", this.faker.internet().domainName())
                )),
                new LinkSection(0, "sidebar", "Donations", List.of(
                    new Link(0, "Paypal", "https://paypal.com"),
                    new Link(0, "OpenCollective", "https://opencollective.com")
                ))
            ),
            List.of(),
            licence,
            keyWords,
            "# Sponsored by " + this.faker.beer().style() + " " + this.faker.beer().name());
        final String projectName = this.normalize(this.faker.funnyName().name() + "_" + this.faker.minecraft().animalName());
        final String quote = this.faker.theItCrowd().quotes();
        final NewProjectForm newProject = new NewProjectForm(settings,
            Category.VALID_CATEGORIES.get(this.faker.random().nextInt(Category.VALID_CATEGORIES.size())),
            quote.substring(0, Math.min(quote.length(), 254)),
            ownerId,
            projectName.substring(0, Math.min(projectName.length(), 24)),
            "# " + projectName + "\n\n" + "> " + this.faker.leagueOfLegends().quote(), null);
        final ProjectTable projectTable = this.projectFactory.createProject(newProject);

        projectTable.setVisibility(Visibility.PUBLIC);
        projectTable.setCreatedAt(this.faker.date().past(100 * 365, TimeUnit.DAYS).toLocalDateTime().atOffset(ZoneOffset.UTC));
        this.projectsDAO.update(projectTable);
    }

    private String normalize(final String input) {
        return input.replace(" ", "_").replace("\"", "").replace("'", "").replace(".", "");
    }

    public void generateE2EData() {
        final UserTable admin = this.userDAO.create(UUID.randomUUID(), "e2e_admin",
            "e2e_admin@hangar.papermc.io",
            "Used for E2E tests",
            "en",
            List.of(),
            false,
            "dark",
            true,
            this.avatarService.getDefaultAvatarUrl(),
            new JSONB(Map.of()));
        this.globalRoleService.addRole(new GlobalRoleTable(admin.getId(), GlobalRole.HANGAR_ADMIN));

        final UserTable user = this.userDAO.create(UUID.randomUUID(), "e2e_user",
            "e2e_user@hangar.papermc.io",
            "Used for E2E tests",
            "en",
            List.of(),
            false,
            "dark",
            true,
            this.avatarService.getDefaultAvatarUrl(),
            new JSONB(Map.of()));

        final String password = this.config.e2e.password();
        final String totpSecret = this.config.e2e.totpSecret();
        this.credentialsService.registerCredential(admin.getUserId(), new PasswordCredential(this.passwordEncoder.encode(password)));
        this.credentialsService.registerCredential(user.getUserId(), new PasswordCredential(this.passwordEncoder.encode(password)));

        this.credentialsService.registerCredential(admin.getUserId(), new TotpCredential(this.qrDataFactory.newBuilder()
            .label(admin.getName())
            .secret(totpSecret)
            .issuer("Hangar")
            .build().getUri()));
        this.credentialsService.registerCredential(user.getUserId(), new TotpCredential(this.qrDataFactory.newBuilder()
            .label(user.getName())
            .secret(totpSecret)
            .issuer("Hangar")
            .build().getUri()));
    }
}
