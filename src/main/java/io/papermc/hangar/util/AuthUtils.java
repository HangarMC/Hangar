package io.papermc.hangar.util;

import io.papermc.hangar.exceptions.HangarApiException;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated(forRemoval = true)
public class AuthUtils {

    private static final Pattern API_KEY_HEADER_PATTERN = Pattern.compile("(?<=apikey=\").*(?=\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SESSION_HEADER_PATTERN = Pattern.compile("(?<=session=\").*(?=\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private AuthUtils() { }

    public static RuntimeException unAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.WWW_AUTHENTICATE, "HangarApi");
        return new HangarApiException(HttpStatus.UNAUTHORIZED, headers);
    }

    public static RuntimeException unAuth(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.WWW_AUTHENTICATE, "HangarApi");
        return new HangarApiException(HttpStatus.UNAUTHORIZED, message, headers);
    }

    public static OffsetDateTime expiration(Duration expirationDuration, Long userChoice) {
        long durationSeconds = expirationDuration.toSeconds();

        if (userChoice == null) {
            return OffsetDateTime.now().plusSeconds(durationSeconds);
        } else if (userChoice <= durationSeconds) {
            return OffsetDateTime.now().plusSeconds(userChoice);
        } else {
            return null;
        }
    }

    public static AuthCredentials parseAuthHeader(boolean requireHeader) {
        return parseAuthHeader(null, requireHeader);
    }

    public static AuthCredentials parseAuthHeader(@Nullable HttpServletRequest request, boolean requireHeader) {
        String authHeader = request == null ? ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getHeader(HttpHeaders.AUTHORIZATION) : request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean missingAuthHeader = authHeader == null || authHeader.isBlank() || !authHeader.startsWith("HangarApi");
        if (missingAuthHeader && requireHeader) {
            throw AuthUtils.unAuth("Invalid or no header found");
        } else if (missingAuthHeader) {
            return new AuthCredentials(null, null);
        }
        return AuthUtils.AuthCredentials.parseHeader(authHeader);
    }

    public static class AuthCredentials {
        private final String apiKey;
        private final String session;

        private AuthCredentials(String apiKey, String session) {
            this.apiKey = apiKey;
            this.session = session;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getSession() {
            return session;
        }

        public static AuthCredentials parseHeader(String authHeader) {
            Matcher apiKeyMatcher = API_KEY_HEADER_PATTERN.matcher(authHeader);
            Matcher sessionMatcher = SESSION_HEADER_PATTERN.matcher(authHeader);
            String apiKey = null;
            String sessionKey = null;
            if (apiKeyMatcher.find()) {
                apiKey = apiKeyMatcher.group();
            } else if (sessionMatcher.find()) {
                sessionKey = sessionMatcher.group();
            } else {
                throw AuthUtils.unAuth("Invalid Authorization header format");
            }
            return new AuthCredentials(apiKey, sessionKey);
        }
    }
}
