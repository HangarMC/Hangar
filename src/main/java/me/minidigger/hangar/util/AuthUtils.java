package me.minidigger.hangar.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class AuthUtils {

    private AuthUtils() { }

    public static RuntimeException unAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.WWW_AUTHENTICATE, "HangarApi");
        return new HeaderResponseStatusException(HttpStatus.UNAUTHORIZED, headers);
    }

    public static RuntimeException unAuth(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.WWW_AUTHENTICATE, "HangarApi");
        return new HeaderResponseStatusException(HttpStatus.UNAUTHORIZED, message, headers);
    }
}
