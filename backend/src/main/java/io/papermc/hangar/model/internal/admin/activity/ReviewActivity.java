package io.papermc.hangar.model.internal.admin.activity;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.core.mapper.Nested;

public class ReviewActivity extends Activity {

    private final OffsetDateTime endedAt;
    private final String versionString;
    private final List<Platform> platforms;

    public ReviewActivity(@Nested final ProjectNamespace namespace, final OffsetDateTime endedAt, final String versionString, final List<Platform> platforms) {
        super(namespace);
        this.endedAt = endedAt;
        this.versionString = versionString;
        this.platforms = platforms;
    }

    public OffsetDateTime getEndedAt() {
        return this.endedAt;
    }

    public String getVersionString() {
        return this.versionString;
    }

    public List<Platform> getPlatforms() {
        return this.platforms;
    }

    @Override
    public String toString() {
        return "ReviewActivity{" +
            "endedAt=" + this.endedAt +
            ", versionString='" + this.versionString + '\'' +
            ", platforms=" + this.platforms +
            "} " + super.toString();
    }
}
