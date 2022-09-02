package io.papermc.hangar.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RequestUtil {

    private RequestUtil() { }

    private static final String ATTR = "HangarIP";

    public static String getRemoteAddress(HttpServletRequest request) {
        Object attribute = request.getAttribute(ATTR);
        if (attribute instanceof String ip) {
            return ip;
        }

        String header = request.getHeader("X-Forwarded-For");
        String ipHeader = request.getHeader("x-real-ip");
        String cfHeader = request.getHeader("cf-connecting-ip");
        String ip;
        if (cfHeader != null) {
            ip = cfHeader;
        } else if (ipHeader != null) {
            ip = ipHeader;
        } else if (header != null) {
            ip = header;
        } else {
            ip = request.getRemoteAddr();
        }

        request.setAttribute(ATTR,ip);
        return ip;
    }

    public static InetAddress getRemoteInetAddress(HttpServletRequest request) {
        try {
            return InetAddress.getByName(getRemoteAddress(request));
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
