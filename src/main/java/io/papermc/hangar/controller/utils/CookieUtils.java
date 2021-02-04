package io.papermc.hangar.controller.utils;

import org.apache.commons.lang3.builder.Builder;

import javax.servlet.http.Cookie;

public class CookieUtils {

    private CookieUtils() { }

    public static CookieBuilder builder(String name, String value) {
        return new CookieBuilder(name, value);
    }

    public static class CookieBuilder implements Builder<Cookie> {

        private final Cookie cookie;

        private CookieBuilder(String name, String value) {
            this.cookie = new Cookie(name, value);
        }

        public CookieBuilder withComment(String purpose) {
            cookie.setComment(purpose);
            return this;
        }

        public CookieBuilder setDomain(String domain) {
            cookie.setDomain(domain);
            return this;
        }

        public CookieBuilder setMaxAge(int expiry) {
            cookie.setMaxAge(expiry);
            return this;
        }

        public CookieBuilder setPath(String uri) {
            cookie.setPath(uri);
            return this;
        }

        public CookieBuilder setSecure(boolean flag) {
            cookie.setSecure(flag);
            return this;
        }

        public CookieBuilder setValue(String newValue) {
            cookie.setValue(newValue);
            return this;
        }

        public CookieBuilder setHttpOnly(boolean httpOnly) {
            cookie.setHttpOnly(httpOnly);
            return this;
        }

        @Override
        public Cookie build() {
            return cookie;
        }
    }
}
