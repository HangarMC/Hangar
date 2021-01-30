package io.papermc.hangar.model.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.OffsetDateTime;

public class ApiSession {

    private final String session;
    private final OffsetDateTime expires;
    private final SessionType type;

    public ApiSession(String session, OffsetDateTime expires, SessionType type) {
        this.session = session;
        this.expires = expires;
        this.type = type;
    }

    public String getSession() {
        return session;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }

    public SessionType getType() {
        return type;
    }

    public enum SessionType {
        KEY("key"),
        USER("user"),
        PUBLIC("public"),
        DEV("dev");

        public static final SessionType[] VALUES = values();

        private final String value;

        SessionType(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static SessionType fromValue(String value) {
            for (SessionType sessionType : SessionType.VALUES) {
                if (sessionType.value.equals(value)) {
                    return sessionType;
                }
            }
            return null;
        }
    }
}
