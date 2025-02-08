package io.papermc.hangar.model.internal.projects;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.config.jackson.RequiresPermission;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectChannel;
import io.papermc.hangar.model.api.project.version.PlatformVersionDownload;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.internal.Joinable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.mapper.Nested;

public class HangarProject extends Project implements Joinable<ProjectRoleTable>, ProjectIdentified {

    private final List<JoinableMember<ProjectRoleTable>> members;
    private final String lastVisibilityChangeComment;
    private final String lastVisibilityChangeUserName;
    private final HangarProjectInfo info;
    private final Collection<HangarProjectPage> pages;
    private final ExtendedProjectPage mainPage;
    private final List<PinnedVersion> pinnedVersions;
    private final Map<Platform, Version> mainChannelVersions;

    public HangarProject(final Project project, final List<JoinableMember<ProjectRoleTable>> members, final String lastVisibilityChangeComment, final String lastVisibilityChangeUserName, final HangarProjectInfo info, final Collection<HangarProjectPage> pages, final List<PinnedVersion> pinnedVersions, final Map<Platform, Version> mainChannelVersions, final ExtendedProjectPage mainPage) {
        super(project);
        this.members = members;
        this.lastVisibilityChangeComment = lastVisibilityChangeComment;
        this.lastVisibilityChangeUserName = lastVisibilityChangeUserName;
        this.info = info;
        this.pages = pages;
        this.pinnedVersions = pinnedVersions;
        this.mainChannelVersions = mainChannelVersions;
        this.mainPage = mainPage;
    }

    @Override
    public long getProjectId() {
        return this.id;
    }

    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.PROJECT;
    }

    @Override
    public List<JoinableMember<ProjectRoleTable>> getMembers() {
        return this.members;
    }

    public String getLastVisibilityChangeComment() {
        return this.lastVisibilityChangeComment;
    }

    public String getLastVisibilityChangeUserName() {
        return this.lastVisibilityChangeUserName;
    }

    public HangarProjectInfo getInfo() {
        return this.info;
    }

    public Collection<HangarProjectPage> getPages() {
        return this.pages;
    }

    public List<PinnedVersion> getPinnedVersions() {
        return this.pinnedVersions;
    }

    public Map<Platform, Version> getMainChannelVersions() {
        return this.mainChannelVersions;
    }

    public ExtendedProjectPage getMainPage() {
        return this.mainPage;
    }

    @Override
    public String toString() {
        return "HangarProject{" +
            "id=" + this.id +
            ", members=" + this.members +
            ", lastVisibilityChangeComment='" + this.lastVisibilityChangeComment + '\'' +
            ", lastVisibilityChangeUserName='" + this.lastVisibilityChangeUserName + '\'' +
            ", info=" + this.info +
            ", pages=" + this.pages +
            ", mainPage=" + this.mainPage +
            ", pinnedVersions=" + this.pinnedVersions +
            "} " + super.toString();
    }

    public record HangarProjectInfo(int publicVersions, int flagCount, int noteCount, long starCount, long watcherCount) {

        @RequiresPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
        public int flagCount() {
            return this.flagCount;
        }

        @RequiresPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
        public int noteCount() {
            return this.noteCount;
        }
    }


    public record PinnedVersion(long versionId,
                                HangarProject.PinnedVersion.Type type,
                                String name,
                                ProjectChannel channel,
                                Map<Platform, PlatformVersionDownload> downloads,
                                Map<Platform, Set<String>> platformDependencies,
                                Map<Platform, List<String>> platformDependenciesFormatted) {
            public PinnedVersion(final long versionId,
                                 final Type type,
                                 final String name,
                                 @Nested("pc") final ProjectChannel channel,
                                 final Map<Platform, PlatformVersionDownload> downloads,
                                 final Map<Platform, Set<String>> platformDependencies,
                                 final Map<Platform, List<String>> platformDependenciesFormatted) {
                this.versionId = versionId;
                this.type = type;
                this.name = name;
                this.channel = channel;
                this.downloads = downloads;
                this.platformDependencies = platformDependencies;
                this.platformDependenciesFormatted = platformDependenciesFormatted;
            }

            @EnumByName
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            public enum Type {
                CHANNEL,
                VERSION
            }
        }
}
