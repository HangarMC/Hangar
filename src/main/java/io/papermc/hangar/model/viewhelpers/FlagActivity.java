package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.generated.ProjectNamespace;

import java.time.OffsetDateTime;

public class FlagActivity extends Activity {

    private OffsetDateTime resolvedAt;

    public FlagActivity() {
        //
    }

    public FlagActivity(OffsetDateTime resolvedAt, ProjectNamespace project) {
        super(project);
        this.resolvedAt = resolvedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
