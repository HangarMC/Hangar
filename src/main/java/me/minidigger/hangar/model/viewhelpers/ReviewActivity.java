package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.generated.ProjectNamespace;

import java.time.OffsetDateTime;

public class ReviewActivity extends Activity {

    private final OffsetDateTime endedAt;
    private final Review id;

    public ReviewActivity(OffsetDateTime endedAt, Review id, ProjectNamespace project) {
        super(project);
        this.endedAt = endedAt;
        this.id = id;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public Review getId() {
        return id;
    }
}
