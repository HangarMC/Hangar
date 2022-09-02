package io.papermc.hangar.model.db.versions.reviews;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.common.ReviewAction;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVersionReviewMessageTable extends Table {

    private final long reviewId;
    private String message;
    private JSONB args;
    private final ReviewAction action;

    @JdbiConstructor
    public ProjectVersionReviewMessageTable(OffsetDateTime createdAt, long id, long reviewId, String message, JSONB args, @EnumByOrdinal ReviewAction action) {
        super(createdAt, id);
        this.reviewId = reviewId;
        this.message = message;
        this.args = args;
        this.action = action;
    }

    public ProjectVersionReviewMessageTable(long reviewId, String message, JSONB args, ReviewAction action) {
        this.reviewId = reviewId;
        this.message = message;
        this.args = args;
        this.action = action;
    }

    public long getReviewId() {
        return reviewId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONB getArgs() {
        return args;
    }

    public void setArgs(JSONB args) {
        this.args = args;
    }

    @EnumByOrdinal
    public ReviewAction getAction() {
        return action;
    }


    @Override
    public String toString() {
        return "ProjectVersionReviewMessageTable{" +
                "reviewId=" + reviewId +
                ", message='" + message + '\'' +
                ", action=" + action +
                "} " + super.toString();
    }
}
