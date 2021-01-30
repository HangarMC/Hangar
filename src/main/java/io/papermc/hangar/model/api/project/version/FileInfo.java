package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Named;

public class FileInfo implements Named {

    private final String name;
    private final long sizeBytes;
    private final String md5Hash;

    public FileInfo(String name, long sizeBytes, String md5Hash) {
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.md5Hash = md5Hash;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", md5Hash='" + md5Hash + '\'' +
                '}';
    }
}
