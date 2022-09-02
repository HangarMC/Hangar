package io.papermc.hangar.model.api.project.version;

public class VersionStats {

    private final long downloads;

    public VersionStats(long downloads) {
        this.downloads = downloads;
    }

    public long getDownloads() {
        return downloads;
    }

    @Override
    public String toString() {
        return "VersionStats{" +
                "downloads=" + downloads +
                '}';
    }
}
