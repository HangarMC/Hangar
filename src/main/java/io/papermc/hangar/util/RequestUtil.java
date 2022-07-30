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

    public static String getRemoteAddress(HttpServletRequest request) {
        String headers = Collections.list(request.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                h -> Collections.list(request.getHeaders(h))
            )).toString();
        System.out.println("getRemoteAddress from " + headers);

        String header = request.getHeader("X-Forwarded-For");
        if (header == null) {
            System.out.println("fall back to " + request.getRemoteAddr());
            return request.getRemoteAddr();
        } else {
            System.out.println("found " + header);
            return header;
        }
    }

    public static InetAddress getRemoteInetAddress(HttpServletRequest request) {
        try {
            return InetAddress.getByName(getRemoteAddress(request));
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
