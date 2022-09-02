package io.papermc.hangar.model.internal.admin.activity;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class FlagActivity extends Activity {

    private final OffsetDateTime resolvedAt;

    public FlagActivity(@Nested ProjectNamespace namespace, OffsetDateTime resolvedAt) {
        super(namespace);
        this.resolvedAt = resolvedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    @Override
    public String toString() {
        return "FlagActivity{" +
                "resolvedAt=" + resolvedAt +
                "} " + super.toString();
    }
}
