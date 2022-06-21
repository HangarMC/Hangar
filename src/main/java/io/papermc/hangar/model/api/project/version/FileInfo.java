package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Named;
import org.jdbi.v3.core.mapper.PropagateNull;

public class FileInfo implements Named {

    private final String name;
    private final long sizeBytes;
    private final String md5Hash;

    public FileInfo(@PropagateNull final String name, final long sizeBytes, final String md5Hash) {
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.md5Hash = md5Hash;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public long getSizeBytes() {
        return this.sizeBytes;
    }

    public String getMd5Hash() {
        return this.md5Hash;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
            "name='" + this.name + '\'' +
            ", sizeBytes=" + this.sizeBytes +
            ", md5Hash='" + this.md5Hash + '\'' +
            '}';
    }
}
