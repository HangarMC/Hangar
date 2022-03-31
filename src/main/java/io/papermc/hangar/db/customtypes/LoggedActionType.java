package io.papermc.hangar.db.customtypes;

import io.papermc.hangar.db.customtypes.LoggedActionType.AbstractContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LoggedActionType<C extends AbstractContext<C>> {

    public static final Map<String, LoggedActionType<? extends AbstractContext<?>>> loggedActionTypes = new HashMap<>();

    public static final LoggedActionType<ProjectContext> PROJECT_VISIBILITY_CHANGE = new LoggedActionType<>(LoggedAction.PROJECT_VISIBILITY_CHANGE, "ProjectVisibilityChange", "The project visibility state was changed");
    public static final LoggedActionType<ProjectContext> PROJECT_RENAMED = new LoggedActionType<>(LoggedAction.PROJECT_RENAMED, "ProjectRename", "The project was renamed");
    public static final LoggedActionType<ProjectContext> PROJECT_FLAGGED = new LoggedActionType<>(LoggedAction.PROJECT_FLAGGED, "ProjectFlagged", "The project got flagged");
    public static final LoggedActionType<ProjectContext> PROJECT_SETTINGS_CHANGED = new LoggedActionType<>(LoggedAction.PROJECT_SETTINGS_CHANGED, "ProjectSettingsChanged", "The project settings were changed");
    public static final LoggedActionType<ProjectContext> PROJECT_MEMBER_REMOVED = new LoggedActionType<>(LoggedAction.PROJECT_MEMBER_REMOVED, "ProjectMemberRemoved", "A Member was removed from the project");
    public static final LoggedActionType<ProjectContext> PROJECT_ICON_CHANGED = new LoggedActionType<>(LoggedAction.PROJECT_ICON_CHANGED, "ProjectIconChanged", "The project icon was changed");
    public static final LoggedActionType<ProjectContext> PROJECT_FLAG_RESOLVED = new LoggedActionType<>(LoggedAction.PROJECT_FLAG_RESOLVED, "ProjectFlagResolved", "The flag was resolved");

    public static final LoggedActionType<ProjectPageContext> PROJECT_PAGE_CREATED = new LoggedActionType<>(LoggedAction.PROJECT_PAGE_CREATED, "ProjectPageCreated", "A project page was created");
    public static final LoggedActionType<ProjectPageContext> PROJECT_PAGE_DELETED = new LoggedActionType<>(LoggedAction.PROJECT_PAGE_DELETED, "ProjectPageDeleted", "A project page was deleted");
    public static final LoggedActionType<ProjectPageContext> PROJECT_PAGE_EDITED = new LoggedActionType<>(LoggedAction.PROJECT_PAGE_EDITED, "ProjectPageEdited", "A project page was edited");

    public static final LoggedActionType<VersionContext> VERSION_DELETED = new LoggedActionType<>(LoggedAction.VERSION_DELETED, "VersionDeleted", "The version was deleted");
    public static final LoggedActionType<VersionContext> VERSION_UPLOADED = new LoggedActionType<>(LoggedAction.VERSION_UPLOADED, "VersionUploaded", "A new version was uploaded");
    public static final LoggedActionType<VersionContext> VERSION_DESCRIPTION_CHANGED = new LoggedActionType<>(LoggedAction.VERSION_DESCRIPTION_CHANGED, "VersionDescriptionEdited", "The version description was edited");
    public static final LoggedActionType<VersionContext> VERSION_REVIEW_STATE_CHANGED = new LoggedActionType<>(LoggedAction.VERSION_REVIEW_STATE_CHANGED, "VersionReviewStateChanged", "The versions review state was changed");

    public static final LoggedActionType<UserContext> USER_TAGLINE_CHANGED = new LoggedActionType<>(LoggedAction.USER_TAGLINE_CHANGED, "UserTaglineChanged", "The user tagline changed");
    public static final LoggedActionType<UserContext> USER_LOCKED = new LoggedActionType<>(LoggedAction.USER_LOCKED, "UserLocked", "This user is locked");
    public static final LoggedActionType<UserContext> USER_UNLOCKED = new LoggedActionType<>(LoggedAction.USER_UNLOCKED, "UserUnlocked", "This use is unlocked");
    public static final LoggedActionType<UserContext> USER_APIKEY_CREATE = new LoggedActionType<>(LoggedAction.USER_APIKEY_CREATE, "UserApikeyCreate", "An apikey was created");
    public static final LoggedActionType<UserContext> USER_APIKEY_DELETE = new LoggedActionType<>(LoggedAction.USER_APIKEY_DELETE, "UserApikeyDelete", "An apikey was deleted");

    public static final LoggedActionType<OrganizationContext> ORG_MEMBERS_ADDED = new LoggedActionType<>(LoggedAction.ORG_MEMBERS_ADDED, "OrganizationMembersAdded", "Users were added to an organization");
    public static final LoggedActionType<OrganizationContext> ORG_MEMBER_REMOVED = new LoggedActionType<>(LoggedAction.ORG_MEMBER_REMOVED, "OrganizationMemberRemoved", "User was removed from an organization");
    public static final LoggedActionType<OrganizationContext> ORG_MEMBER_ROLES_UPDATED = new LoggedActionType<>(LoggedAction.ORG_MEMBER_ROLES_UPDATED, "OrganizationMemberRolesUpdated", "Organization members roles were updated");

    private final LoggedAction value;
    private final String name;
    private C actionContext;
    private final String description;

    private LoggedActionType(LoggedAction value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
        loggedActionTypes.put(value.getValue(), this);
    }

    private LoggedActionType(LoggedActionType<C> actionType, C actionContext) {
        this.value = actionType.value;
        this.name = actionType.name;
        this.description = actionType.description;
        this.actionContext = actionContext;
    }

    public LoggedAction getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public C getActionContext() {
        return actionContext;
    }

    public String getDescription() {
        return description;
    }

    public LoggedActionType<C> with(C actionContext) {
        return new LoggedActionType<>(this, actionContext);
    }

    public static class ProjectContext extends AbstractContext<ProjectContext> {

        private final long projectId;

        private ProjectContext(long projectId) {
            super(0);
            this.projectId = projectId;
        }

        public long getProjectId() {
            return projectId;
        }

        public static ProjectContext of(long projectId) {
            return new ProjectContext(projectId);
        }
    }

    public static class VersionContext extends AbstractContext<VersionContext> {

        private final long projectId;
        private final long versionId;

        private VersionContext(long projectId, long versionId) {
            super(1);
            this.projectId = projectId;
            this.versionId = versionId;
        }

        public long getProjectId() {
            return projectId;
        }

        public long getVersionId() {
            return versionId;
        }

        public static VersionContext of(long projectId, long versionId) {
            return new VersionContext(projectId, versionId);
        }
    }

    public static class ProjectPageContext extends AbstractContext<ProjectPageContext> {

        private final long projectId;
        private final long pageId;

        private ProjectPageContext(long projectId, long pageId) {
            super(2);
            this.projectId = projectId;
            this.pageId = pageId;
        }

        public long getProjectId() {
            return projectId;
        }

        public long getPageId() {
            return pageId;
        }

        public static ProjectPageContext of(long projectId, long pageId) {
            return new ProjectPageContext(projectId, pageId);
        }
    }

    public static class UserContext extends AbstractContext<UserContext> {

        private final long userId;

        private UserContext(long userId) {
            super(3);
            this.userId = userId;
        }

        public long getUserId() {
            return userId;
        }

        public static UserContext of(long userId) {
            return new UserContext(userId);
        }
    }

    public static class OrganizationContext extends AbstractContext<OrganizationContext> {

        private final long orgId;

        private OrganizationContext(long orgId) {
            super(4);
            this.orgId = orgId;
        }

        public long getOrganizationId() {
            return orgId;
        }

        public static OrganizationContext of(long orgId) {
            return new OrganizationContext(orgId);
        }
    }

    public abstract static class AbstractContext<C> {

        protected int value;

        protected AbstractContext(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Nullable
    public static LoggedActionType<? extends AbstractContext<?>> getLoggedActionType(String name) {
        return loggedActionTypes.get(name);
    }
}
