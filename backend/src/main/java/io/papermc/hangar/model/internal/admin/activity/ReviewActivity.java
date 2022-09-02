package io.papermc.hangar.model.internal.admin.activity;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;
import java.util.List;

public class ReviewActivity extends Activity {

    private final OffsetDateTime endedAt;
    private final String versionString;
    private final List<Platform> platforms;

    public ReviewActivity(@Nested ProjectNamespace namespace, OffsetDateTime endedAt, String versionString, List<Platform> platforms) {
        super(namespace);
        this.endedAt = endedAt;
        this.versionString = versionString;
        this.platforms = platforms;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public String getVersionString() {
        return versionString;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    @Override
    public String toString() {
        return "ReviewActivity{" +
                "endedAt=" + endedAt +
                ", versionString='" + versionString + '\'' +
                ", platforms=" + platforms +
                "} " + super.toString();
    }
}
