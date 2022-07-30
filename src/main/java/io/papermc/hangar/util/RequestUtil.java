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

        String headers = Collections.list(request.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                h -> Collections.list(request.getHeaders(h))
            )).toString();
        System.out.println("getRemoteAddress from " + headers);

        String header = request.getHeader("X-Forwarded-For");
        String ipHeader = request.getHeader("x-real-ip");
        String cfHeader = request.getHeader("cf-connecting-ip");
        String ip;
        if (cfHeader != null) {
            System.out.println("found cf " + cfHeader);
            ip = cfHeader;
        } else if (ipHeader != null) {
            System.out.println("found ip " + ipHeader);
            ip = ipHeader;
        } else if (header != null) {
            System.out.println("found " + header);
            ip = header;
        } else {
            System.out.println("fall back to " + request.getRemoteAddr());
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
