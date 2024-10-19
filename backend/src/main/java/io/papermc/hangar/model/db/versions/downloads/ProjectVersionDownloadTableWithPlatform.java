package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.common.Platform;

public class ProjectVersionDownloadTableWithPlatform extends ProjectVersionDownloadTable {

    private final Platform platform;

    public ProjectVersionDownloadTableWithPlatform(final long id, final long versionId, final Long fileSize, final String hash, final String fileName, final String externalUrl, final Platform platform) {
        super(id, versionId, fileSize, hash, fileName, externalUrl);
        this.platform = platform;
    }

    public Platform getPlatform() {
        return this.platform;
    }
}
