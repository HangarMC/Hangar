package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.common.ReviewAction;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class HangarReview extends Model {

    private final OffsetDateTime endedAt;
    private final String userName;
    private final long userId;
    private final List<HangarReviewMessage> messages;

    public HangarReview(OffsetDateTime createdAt, OffsetDateTime endedAt, String userName, long userId) {
        super(createdAt);
        this.endedAt = endedAt;
        this.userName = userName;
        this.userId = userId;
        this.messages = new ArrayList<>();
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public String getUserName() {
        return userName;
    }

    public long getUserId() {
        return userId;
    }

    public List<HangarReviewMessage> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "HangarReview{" +
                "endedAt=" + endedAt +
                ", userName='" + userName + '\'' +
                ", userId=" + userId +
                ", messages=" + messages +
                "} " + super.toString();
    }

    public static class HangarReviewMessage extends Model {

        private final String message;
        private final ObjectNode args;
        private final ReviewAction action;

        public HangarReviewMessage(OffsetDateTime createdAt, String message, JSONB args, @EnumByOrdinal ReviewAction action) {
            super(createdAt);
            this.message = message;
            this.args = (ObjectNode) args.getJson();
            this.action = action;
        }

        public String getMessage() {
            return message;
        }

        public ObjectNode getArgs() {
            return args;
        }

        public ReviewAction getAction() {
            return action;
        }

        @Override
        public String toString() {
            return "HangarReviewMessage{" +
                    "message='" + message + '\'' +
                    ", args=" + args +
                    ", action=" + action +
                    "} " + super.toString();
        }
    }
}
