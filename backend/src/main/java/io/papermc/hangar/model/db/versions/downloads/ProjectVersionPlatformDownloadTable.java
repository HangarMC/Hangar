package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public final class ProjectVersionPlatformDownloadTable extends Table {

    private final long versionId;
    private final Platform platform;
    private final long downloadId;

    public ProjectVersionPlatformDownloadTable(final long versionId, final Platform platform, final long downloadId) {
        this.versionId = versionId;
        this.platform = platform;
        this.downloadId = downloadId;
    }

    @JdbiConstructor
    public ProjectVersionPlatformDownloadTable(final long id, final long versionId, @EnumByOrdinal final Platform platform, final long downloadId) {
        super(id);
        this.versionId = versionId;
        this.platform = platform;
        this.downloadId = downloadId;
    }

    public long getVersionId() {
        return this.versionId;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return this.platform;
    }

    public long getDownloadId() {
        return this.downloadId;
    }
}
