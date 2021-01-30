package io.papermc.hangar.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RequestUtil {

    private RequestUtil() { }

    public static String getRemoteAddress(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header == null) {
            return request.getRemoteAddr();
        } else {
            return header; // header.split(',').headOption.map(_.trim).getOrElse(request.remoteAddress) ?
        }
    }

    public static InetAddress getRemoteInetAddress(HttpServletRequest request) {
        try {
            return InetAddress.getByName(getRemoteAddress(request));
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public static <T> Optional<T> requirePathParams(List<String> requestedParams, Function<String[], T> tFunction) {
        Map<String, String> pathParams = getCurrentPathParams();
        if (pathParams.keySet().containsAll(requestedParams)) {
            return Optional.ofNullable(tFunction.apply(requestedParams.toArray(new String[0])));
        }
        return Optional.empty();
    }

    public static Map<String, String> getCurrentPathParams() {
        return getPathParams(getCurrentRequest());
    }

    public static Map<String, String> getPathParams(HttpServletRequest request) {
        return Optional.ofNullable((Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).orElse(new HashMap<>());
    }

    @NotNull
    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new IllegalStateException("Cannot get http servlet request out of HTTP context");
        }
        return (HttpServletRequest) requestAttributes;
    }
}
