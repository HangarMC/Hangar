package io.papermc.hangar.model.internal.discourse;

import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Map;

public class DiscourseError extends RuntimeException {

    public static class StatusError extends DiscourseError {
        private final HttpStatus status;
        private final String message;

        public StatusError(final HttpStatus status, final String message) {
            this.status = status;
            this.message = message;
        }

        public HttpStatus getStatus() {
            return this.status;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }

    public static class RateLimitError extends DiscourseError {
        private final Duration duration;

        public RateLimitError(final Duration duration) {
            this.duration = duration;
        }

        public Duration getDuration() {
            return this.duration;
        }
    }

    public static class NotAvailableError extends DiscourseError {
        @Override
        public String getMessage() {
            return "Discourse isn't available";
        }
    }

    public static class UnknownError extends DiscourseError {

        private final String message;
        private final String descriptor;
        private final Map<String, Object> extras;

        public UnknownError(final String message, final String descriptor, final Map<String, Object> extras) {
            this.message = message;
            this.descriptor = descriptor;
            this.extras = extras;
        }

        @Override
        public String getMessage() {
            return this.message;
        }

        public String getDescriptor() {
            return this.descriptor;
        }

        public Map<String, Object> getExtras() {
            return this.extras;
        }
    }

    public static class NotProcessable extends DiscourseError {

        private final String message;

        public NotProcessable(final String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }
}
