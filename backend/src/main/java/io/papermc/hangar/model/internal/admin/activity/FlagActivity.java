package io.papermc.hangar.model.internal.admin.activity;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.Nested;

public class FlagActivity extends Activity {

    private final OffsetDateTime resolvedAt;

    public FlagActivity(@Nested final ProjectNamespace namespace, final OffsetDateTime resolvedAt) {
        super(namespace);
        this.resolvedAt = resolvedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    @Override
    public String toString() {
        return "FlagActivity{" +
            "resolvedAt=" + this.resolvedAt +
            "} " + super.toString();
    }
}
