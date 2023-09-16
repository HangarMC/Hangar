package io.papermc.hangar.util;

import io.papermc.hangar.HangarApplication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class RequestUtil {

    private RequestUtil() {
    }

    private static final String ATTR = "HangarIP";

    public static String getRemoteAddress(final HttpServletRequest request) {
        if (HangarApplication.TEST_MODE) {
            return "::1";
        }

        final Object attribute = request.getAttribute(ATTR);
        if (attribute instanceof String ip) {
            return ip;
        }

        final String header = request.getHeader("X-Forwarded-For");
        final String ipHeader = request.getHeader("x-real-ip");
        final String cfHeader = request.getHeader("cf-connecting-ip");
        final String ip;
        if (cfHeader != null) {
            ip = cfHeader;
        } else if (ipHeader != null) {
            ip = ipHeader;
        } else if (header != null) {
            ip = header;
        } else {
            ip = request.getRemoteAddr();
        }

        request.setAttribute(ATTR, ip);
        return ip;
    }

    public static InetAddress getRemoteInetAddress(final HttpServletRequest request) {
        try {
            return InetAddress.getByName(getRemoteAddress(request));
        } catch (final UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public static String appendParam(final String url, final String paramName, final String paramValue) {
        return UriComponentsBuilder.fromUriString(url).queryParam(paramName, paramValue).build().toUriString();
    }
}
