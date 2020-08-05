package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.generated.ProjectNamespace;

import java.time.OffsetDateTime;

public class FlagActivity extends Activity {

    private OffsetDateTime resolvedAt;

    public FlagActivity(OffsetDateTime resolvedAt, ProjectNamespace project) {
        super(project);
        this.resolvedAt = resolvedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }
}
