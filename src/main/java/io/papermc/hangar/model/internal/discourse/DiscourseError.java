package io.papermc.hangar.model.internal.discourse;

import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Map;

public class DiscourseError extends RuntimeException {

    public static class StatusError extends DiscourseError {
        private final HttpStatus status;
        private final String message;

        public StatusError(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class RateLimitError extends DiscourseError {
        private final Duration duration;

        public RateLimitError(Duration duration) {
            this.duration = duration;
        }

        public Duration getDuration() {
            return duration;
        }
    }

    public static class NotAvailableError extends DiscourseError {
        @Override
        public String getMessage() {
            return "Discourse ins't available";
        }
    }

    public static class UnknownError extends DiscourseError {

        private final String message;
        private final String descriptor;
        private final Map<String, Object> extras;

        public UnknownError(String message, String descriptor, Map<String, Object> extras) {
            super();
            this.message = message;
            this.descriptor = descriptor;
            this.extras = extras;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public Map<String, Object> getExtras() {
            return extras;
        }
    }
 }
