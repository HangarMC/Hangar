package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.common.ReviewAction;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public class HangarReview extends Model {

    private final OffsetDateTime endedAt;
    private final String userName;
    private final long userId;
    private final List<HangarReviewMessage> messages;

    public HangarReview(final OffsetDateTime createdAt, final OffsetDateTime endedAt, final String userName, final long userId) {
        super(createdAt);
        this.endedAt = endedAt;
        this.userName = userName;
        this.userId = userId;
        this.messages = new ArrayList<>();
    }

    public OffsetDateTime getEndedAt() {
        return this.endedAt;
    }

    public String getUserName() {
        return this.userName;
    }

    public long getUserId() {
        return this.userId;
    }

    public List<HangarReviewMessage> getMessages() {
        return this.messages;
    }

    @Override
    public String toString() {
        return "HangarReview{" +
            "endedAt=" + this.endedAt +
            ", userName='" + this.userName + '\'' +
            ", userId=" + this.userId +
            ", messages=" + this.messages +
            "} " + super.toString();
    }

    public static class HangarReviewMessage extends Model {

        private final String message;
        private final ObjectNode args;
        private final ReviewAction action;

        public HangarReviewMessage(final OffsetDateTime createdAt, final String message, final JSONB args, @EnumByOrdinal final ReviewAction action) {
            super(createdAt);
            this.message = message;
            this.args = (ObjectNode) args.getJson();
            this.action = action;
        }

        public String getMessage() {
            return this.message;
        }

        public ObjectNode getArgs() {
            return this.args;
        }

        public ReviewAction getAction() {
            return this.action;
        }

        @Override
        public String toString() {
            return "HangarReviewMessage{" +
                "message='" + this.message + '\'' +
                ", args=" + this.args +
                ", action=" + this.action +
                "} " + super.toString();
        }
    }
}
