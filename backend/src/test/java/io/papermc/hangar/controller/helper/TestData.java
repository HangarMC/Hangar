package io.papermc.hangar.controller.helper;

import io.papermc.hangar.HangarApplication;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.components.index.IndexService;
import io.papermc.hangar.components.scheduler.SchedulerService;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.model.api.project.ProjectLicense;
import io.papermc.hangar.model.api.project.settings.ProjectSettings;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.PendingVersionFile;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.APIKeyService;
import io.papermc.hangar.service.internal.organizations.OrganizationFactory;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import io.papermc.hangar.service.internal.versions.VersionService;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;

@TestConfiguration
public class TestData {
    private static final Logger logger = LoggerFactory.getLogger(TestData.class);

    public static UserTable USER_NORMAL;
    public static UserTable USER_MEMBER;
    public static UserTable USER_ADMIN;
    public static UserTable USER_BANNED;

    public static String KEY_ADMIN;
    public static String KEY_PROJECT_ONLY;
    public static String KEY_SEE_HIDDEN;

    public static OrganizationTable ORG;

    public static ProjectTable PROJECT;
    public static ProjectTable PRIVATE_PROJECT;

    public static ProjectPageTable PAGE_PARENT;
    public static ProjectPageTable PAGE_CHILD;

    public static ProjectVersionTable VERSION;
    public static ProjectVersionTable VERSION_HIDDEN;

    @Autowired
    private AuthService authService;
    @Autowired
    private APIKeyService apiKeyService;
    @Autowired
    private GlobalRoleService globalRoleService;
    @Autowired
    private OrganizationFactory organizationFactory;
    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private ProjectFactory projectFactory;
    @Autowired
    private ProjectPageService projectPageService;
    @Autowired
    private UserService userService;
    @Autowired
    private VersionFactory versionFactory;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private ProjectVersionsDAO projectVersionsDAO;
    @Autowired
    private SchedulerService schedulerService;

    @EventListener(ApplicationStartedEvent.class)
    public void prepare() {
        HangarApplication.TEST_MODE = true;
        logger.info("Preparing test data...");
        logger.info("Creating some test users...");
        USER_NORMAL = this.authService.registerUser(new SignupForm("TestUser", "testuser@papermc.io", "W45nNUefrsB8ucQeiKDdbEQijH5KP", true, null));
        USER_MEMBER = this.authService.registerUser(new SignupForm("TestMember", "testmember@papermc.io", "W45nNUefrsB8ucQeiKDdbEQijH5KP", true, null));
        USER_ADMIN = this.authService.registerUser(new SignupForm("TestAdmin", "testadmin@papermc.io", "W45nNUefrsB8ucQeiKDdbEQijH5KP", true, null));
        USER_BANNED = this.authService.registerUser(new SignupForm("TestBanned", "testbanned@papermc.io", "W45nNUefrsB8ucQeiKDdbEQijH5KP", true, null));

        USER_NORMAL.setEmailVerified(true);
        USER_MEMBER.setEmailVerified(true);
        USER_ADMIN.setEmailVerified(true);
        USER_BANNED.setEmailVerified(true);
        USER_BANNED.setLocked(true);
        this.userDAO.update(USER_NORMAL);
        this.userDAO.update(USER_MEMBER);
        this.userDAO.update(USER_ADMIN);
        this.userDAO.update(USER_BANNED);

        this.globalRoleService.addRole(new GlobalRoleTable(USER_ADMIN.getUserId(), GlobalRole.HANGAR_ADMIN));

        HangarApplication.TEST_PRINCIPAL = Optional.of(new HangarPrincipal(USER_ADMIN.getUserId(), USER_ADMIN.getName(), USER_ADMIN.getEmail(), false, Permission.All, null, 2, true));

        logger.info("Creating some test orgs...");
        ORG = this.organizationFactory.createOrganization("PaperMC");
        this.organizationMemberService.addNewAcceptedByDefaultMember(OrganizationRole.ORGANIZATION_DEVELOPER.create(ORG.getOrganizationId(), null, USER_MEMBER.getUserId(), true));

        logger.info("Creating some test projects...");
        PROJECT = this.projectFactory.createProject(new NewProjectForm(new ProjectSettings(List.of(), List.of(), new ProjectLicense(null, null, "MIT"), List.of(), null),
            Category.CHAT, "", ORG.getUserId(), "TestProject", "# Test", null));
        PAGE_PARENT = this.projectPageService.createPage(PROJECT.getProjectId(), "TestParentPage", "testparentpage", "# TestParentPage", true, null, false);
        PAGE_CHILD = this.projectPageService.createPage(PROJECT.getProjectId(), "TestChildPage", "testparentpage/testchild", "# TestChildPage", true, PAGE_PARENT.getId(), false);

        PRIVATE_PROJECT = this.projectFactory.createProject(new NewProjectForm(new ProjectSettings(List.of(), List.of(), new ProjectLicense(null, null, "MIT"), List.of(), null),
            Category.CHAT, "", ORG.getUserId(), "PrivateProject", "# PrivateProject", null));

        logger.info("Creating some test versions...");
        VERSION = this.versionFactory.publishPendingVersion(PROJECT.getProjectId(), new PendingVersion("1.0", Map.of(), new EnumMap<>(Map.of(Platform.PAPER, new TreeSet<>(Set.of("1.8")))), "# 1.0", List.of(new PendingVersionFile(List.of(Platform.PAPER), null, "https://google.com")), "Release", "Release channel", null, Set.of()));
        VERSION_HIDDEN = this.versionFactory.publishPendingVersion(PROJECT.getProjectId(), new PendingVersion("2.0", Map.of(), new EnumMap<>(Map.of(Platform.PAPER, new TreeSet<>(Set.of("1.8")))), "# 2.0", List.of(new PendingVersionFile(List.of(Platform.PAPER), null, "https://google.com")), "Release", "Release channel", null, Set.of()));
        VERSION_HIDDEN.setVisibility(Visibility.NEEDSAPPROVAL);
        this.projectVersionsDAO.update(VERSION_HIDDEN);

        logger.info("Creating test api keys...");
        KEY_ADMIN = this.apiKeyService.createApiKey(USER_ADMIN, new CreateAPIKeyForm("Admin", Set.of(NamedPermission.values())), Permission.All);
        KEY_PROJECT_ONLY = this.apiKeyService.createApiKey(USER_NORMAL, new CreateAPIKeyForm("Project Only", Set.of(NamedPermission.CREATE_PROJECT)), Permission.All);
        KEY_SEE_HIDDEN = this.apiKeyService.createApiKey(USER_NORMAL, new CreateAPIKeyForm("See Hidden", Set.of(NamedPermission.SEE_HIDDEN)), Permission.All);

        this.userService.toggleStarred(USER_NORMAL.getUserId(), PROJECT.getProjectId(), true);
        this.userService.toggleWatching(USER_NORMAL.getUserId(), PROJECT.getProjectId(), true);

        //noinspection deprecation
        this.schedulerService.runAllJobs();

        HangarApplication.TEST_PRINCIPAL = Optional.empty();
        HangarApplication.TEST_MODE = false;
    }
}
