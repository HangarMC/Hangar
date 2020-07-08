package me.minidigger.hangar.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import me.minidigger.hangar.service.HeaderResponseStatusException;

public class AuthUtils {

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
