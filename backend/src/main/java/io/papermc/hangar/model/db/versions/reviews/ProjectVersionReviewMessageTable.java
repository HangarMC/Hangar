package io.papermc.hangar.model.db.versions.reviews;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.common.ReviewAction;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionReviewMessageTable extends Table {

    private final long reviewId;
    private String message;
    private JSONB args;
    private final ReviewAction action;

    @JdbiConstructor
    public ProjectVersionReviewMessageTable(final OffsetDateTime createdAt, final long id, final long reviewId, final String message, final JSONB args, @EnumByOrdinal final ReviewAction action) {
        super(createdAt, id);
        this.reviewId = reviewId;
        this.message = message;
        this.args = args;
        this.action = action;
    }

    public ProjectVersionReviewMessageTable(final long reviewId, final String message, final JSONB args, final ReviewAction action) {
        this.reviewId = reviewId;
        this.message = message;
        this.args = args;
        this.action = action;
    }

    public long getReviewId() {
        return this.reviewId;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public JSONB getArgs() {
        return this.args;
    }

    public void setArgs(final JSONB args) {
        this.args = args;
    }

    @EnumByOrdinal
    public ReviewAction getAction() {
        return this.action;
    }


    @Override
    public String toString() {
        return "ProjectVersionReviewMessageTable{" +
            "reviewId=" + this.reviewId +
            ", message='" + this.message + '\'' +
            ", action=" + this.action +
            "} " + super.toString();
    }
}
