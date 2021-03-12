package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.common.ReviewAction;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class HangarReview extends Model {

    private final OffsetDateTime endedAt;
    private final String userName;
    private final List<HangarReviewMessage> messages;

    public HangarReview(OffsetDateTime createdAt, OffsetDateTime endedAt, String userName) {
        super(createdAt);
        this.endedAt = endedAt;
        this.userName = userName;
        this.messages = new ArrayList<>();
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public String getUserName() {
        return userName;
    }

    public List<HangarReviewMessage> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "HangarReview{" +
                "endedAt=" + endedAt +
                ", userName='" + userName + '\'' +
                ", messages=" + messages +
                "} " + super.toString();
    }

    public static class HangarReviewMessage extends Model {

        private final String message;
        private final ReviewAction action;

        public HangarReviewMessage(OffsetDateTime createdAt, String message, @EnumByOrdinal ReviewAction action) {
            super(createdAt);
            this.message = message;
            this.action = action;
        }

        public String getMessage() {
            return message;
        }

        public ReviewAction getAction() {
            return action;
        }

        @Override
        public String toString() {
            return "HangarReviewMessage{" +
                    "message='" + message + '\'' +
                    ", reviewAction=" + action +
                    "} " + super.toString();
        }
    }
}
