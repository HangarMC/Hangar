package io.papermc.hangar.model.viewhelpers;

import org.jdbi.v3.core.mapper.Nested;

import io.papermc.hangar.model.generated.ProjectNamespace;

import java.time.OffsetDateTime;

public class ReviewActivity extends Activity {

    private OffsetDateTime endedAt;
    private Review id;

    public ReviewActivity() {
        //
    }

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

    public void setEndedAt(OffsetDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public void setId(Review id) {
        this.id = id;
    }
}
