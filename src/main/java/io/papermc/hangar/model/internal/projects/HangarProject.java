package io.papermc.hangar.model.internal.projects;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.config.jackson.RequiresPermission;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectChannel;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.internal.Joinable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.model.internal.versions.HangarVersion;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

public class HangarProject extends Project implements Joinable<ProjectRoleTable>, ProjectIdentified {

    private final long id;
    private final ProjectOwner owner;
    private final List<JoinableMember<ProjectRoleTable>> members;
    private final String lastVisibilityChangeComment;
    private final String lastVisibilityChangeUserName;
    private final HangarProjectInfo info;
    private final Collection<HangarProjectPage> pages;
    private final List<PinnedVersion> pinnedVersions;
    private final Map<Platform, HangarVersion> mainChannelVersions;

    public HangarProject(final Project project, final long id, final ProjectOwner owner, final List<JoinableMember<ProjectRoleTable>> members, final String lastVisibilityChangeComment, final String lastVisibilityChangeUserName, final HangarProjectInfo info, final Collection<HangarProjectPage> pages, final List<PinnedVersion> pinnedVersions, final Map<Platform, HangarVersion> mainChannelVersions) {
        super(project);
        this.id = id;
        this.owner = owner;
        this.members = members;
        this.lastVisibilityChangeComment = lastVisibilityChangeComment;
        this.lastVisibilityChangeUserName = lastVisibilityChangeUserName;
        this.info = info;
        this.pages = pages;
        this.pinnedVersions = pinnedVersions;
        this.mainChannelVersions = mainChannelVersions;
    }

    public long getId() {
        return id;
    }

    @Override
    public long getProjectId() {
        return id;
    }

    @Override
    public ProjectOwner getOwner() {
        return owner;
    }

    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.PROJECT;
    }

    @Override
    public List<JoinableMember<ProjectRoleTable>> getMembers() {
        return members;
    }

    public String getLastVisibilityChangeComment() {
        return lastVisibilityChangeComment;
    }

    public String getLastVisibilityChangeUserName() {
        return lastVisibilityChangeUserName;
    }

    public HangarProjectInfo getInfo() {
        return info;
    }

    public Collection<HangarProjectPage> getPages() {
        return pages;
    }

    public List<PinnedVersion> getPinnedVersions() {
        return this.pinnedVersions;
    }

    public Map<Platform, HangarVersion> getMainChannelVersions() {
        return mainChannelVersions;
    }

    @Override
    public String toString() {
        return "HangarProject{" +
            "id=" + this.id +
            ", owner=" + this.owner +
            ", members=" + this.members +
            ", lastVisibilityChangeComment='" + this.lastVisibilityChangeComment + '\'' +
            ", lastVisibilityChangeUserName='" + this.lastVisibilityChangeUserName + '\'' +
            ", info=" + this.info +
            ", pages=" + this.pages +
            ", pinnedVersions=" + this.pinnedVersions +
            "} " + super.toString();
    }

    public static class HangarProjectInfo {

        private final int publicVersions;
        private final int flagCount;
        private final int noteCount;
        private final long starCount;
        private final long watcherCount;

        public HangarProjectInfo(final int publicVersions, final int flagCount, final int noteCount, final long starCount, final long watcherCount) {
            this.publicVersions = publicVersions;
            this.flagCount = flagCount;
            this.noteCount = noteCount;
            this.starCount = starCount;
            this.watcherCount = watcherCount;
        }

        public int getPublicVersions() {
            return publicVersions;
        }

        @RequiresPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
        public int getFlagCount() {
            return flagCount;
        }

        @RequiresPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
        public int getNoteCount() {
            return noteCount;
        }

        public long getStarCount() {
            return starCount;
        }

        public long getWatcherCount() {
            return watcherCount;
        }

        @Override
        public String toString() {
            return "HangarProjectInfo{" +
                "publicVersions=" + publicVersions +
                ", flagCount=" + flagCount +
                ", noteCount=" + noteCount +
                ", starCount=" + starCount +
                ", watcherCount=" + watcherCount +
                '}';
        }
    }


    public static class PinnedVersion {
        private final long versionId;
        private final Type type;
        private final String name;
        private final ProjectChannel channel;
        private final FileInfo fileInfo;
        private final String externalUrl;
        private final Map<Platform, Set<String>> platformDependencies;

        public PinnedVersion(long versionId, Type type, String name, @Nested("pc") ProjectChannel channel,
                             @Nested("fi") @Nullable FileInfo fileInfo, @Nullable String externalUrl) {
            this.versionId = versionId;
            this.type = type;
            this.name = name;
            this.channel = channel;
            this.fileInfo = fileInfo;
            this.externalUrl = externalUrl;
            this.platformDependencies = new EnumMap<>(Platform.class);
        }

        public long getVersionId() {
            return versionId;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Map<Platform, Set<String>> getPlatformDependencies() {
            return platformDependencies;
        }

        public ProjectChannel getChannel() {
            return channel;
        }

        public FileInfo getFileInfo() {
            return fileInfo;
        }

        public String getExternalUrl() {
            return externalUrl;
        }

        @Override
        public String toString() {
            return "PinnedVersion[" +
                "versionId=" + versionId + ", " +
                "type=" + type + ", " +
                "name=" + name + ", " +
                "platforms=" + platformDependencies + ", " +
                "channel=" + channel + ", " +
                "fileInfo=" + fileInfo + ", " +
                "externalUrl=" + externalUrl + ']';
        }


        @EnumByName
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public enum Type {
            CHANNEL,
            VERSION
        }
    }
}
