package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public final class ProjectVersionDownloadTable extends Table {

    private final long versionId;
    private final Long fileSize;
    private final String hash;
    private final String fileName;
    private final String externalUrl;

    public ProjectVersionDownloadTable(final long versionId, final Long fileSize, final String hash, final String fileName, final String externalUrl) {
        this.versionId = versionId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.externalUrl = externalUrl;
    }

    @JdbiConstructor
    public ProjectVersionDownloadTable(final long id, final long versionId, final Long fileSize, final String hash, final String fileName, final String externalUrl) {
        super(id);
        this.versionId = versionId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.externalUrl = externalUrl;
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

    @Override
    public String toString() {
        return "ProjectVersionDownloadTable{" +
            "versionId=" + versionId +
            ", fileSize=" + fileSize +
            ", hash='" + hash + '\'' +
            ", fileName='" + fileName + '\'' +
            ", externalUrl='" + externalUrl + '\'' +
            ", id=" + id +
            '}';
    }
}
