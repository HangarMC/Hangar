package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.Table;
import java.util.Arrays;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionDownloadTable extends Table {

    private final long versionId;
    private final Long fileSize;
    private final String hash;
    private final String fileName;
    private final String externalUrl;
    private final Platform[] platforms;
    private final Platform downloadPlatform;

    public ProjectVersionDownloadTable(final long versionId, final Long fileSize, final String hash, final String fileName, final String externalUrl, final Platform[] platforms, final Platform downloadPlatform) {
        this.versionId = versionId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.externalUrl = externalUrl;
        this.platforms = platforms;
        this.downloadPlatform = downloadPlatform;
    }

    @JdbiConstructor
    public ProjectVersionDownloadTable(final long id, final long versionId, final Long fileSize, final String hash, final String fileName, final String externalUrl, final Platform[] platforms, final @EnumByOrdinal Platform downloadPlatform) {
        super(id);
        this.versionId = versionId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.externalUrl = externalUrl;
        this.platforms = platforms;
        this.downloadPlatform = downloadPlatform;
    }

    public long getVersionId() {
        return this.versionId;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public String getHash() {
        return this.hash;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getExternalUrl() {
        return this.externalUrl;
    }

    public Platform[] getPlatforms() {
        return platforms;
    }

    @EnumByOrdinal
    public Platform getDownloadPlatform() {
        return downloadPlatform;
    }

    @Override
    public String toString() {
        return "ProjectVersionDownloadTable{" +
            "versionId=" + versionId +
            ", fileSize=" + fileSize +
            ", hash='" + hash + '\'' +
            ", fileName='" + fileName + '\'' +
            ", externalUrl='" + externalUrl + '\'' +
            ", platforms='" + Arrays.toString(platforms) + '\'' +
            ", downloadPlatform='" + downloadPlatform + '\'' +
            ", id=" + id +
            '}';
    }
}
