package me.minidigger.hangar.db.customtypes;

import me.minidigger.hangar.db.customtypes.LoggedActionType.IntEntry;

public class LoggedActionType<C extends IntEntry> {

    public static final LoggedActionType<ProjectContext> PROJECT_VISIBILITY_CHANGE = new LoggedActionType<>("project_visibility_change");


    private final String value;
    private final String name;
    private C actionContext;
    private final String description;

    private LoggedActionType(String value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    public static class ProjectContext extends IntEntry {

        private final long projectId;

        private ProjectContext(long projectId) {
            super(0);
            this.projectId = projectId;
        }

        public long getProjectId() {
            return projectId;
        }

        public static UserContext of(long projectId) {
            return new UserContext(projectId);
        }
    }

    public static class VersionContext extends IntEntry {

        private final long versionId;

        private VersionContext(long versionId) {
            super(1);
            this.versionId = versionId;
        }

        public long getVersionId() {
            return versionId;
        }

        public static VersionContext of(long versionId) {
            return new VersionContext(versionId);
        }
    }

    public static class ProjectPageContext extends IntEntry {

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

    public static class UserContext extends IntEntry {

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

    public static class OrganizationContext extends IntEntry {

        private final long orgId;

        private OrganizationContext(long orgId) {
            super(4);
            this.orgId = orgId;
        }

        public long getOrgId() {
            return orgId;
        }

        public static OrganizationContext of(long orgId) {
            return new OrganizationContext(orgId);
        }
    }

    abstract static class IntEntry {

        public IntEntry(int value) {
            this.value = value;
        }

        protected int value;

        public int getValue() {
            return value;
        }
    }
}
