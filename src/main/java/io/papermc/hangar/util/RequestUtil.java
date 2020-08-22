package io.papermc.hangar.util;

import org.springframework.http.HttpHeaders;

import java.net.InetAddress;
import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

    public static String getRemoteAddress(HttpServletRequest request) {
        String header = request.getHeader("X-Frowarded-For");
        if (header == null) {
            return request.getRemoteAddr();
        } else {
            return header; // header.split(',').headOption.map(_.trim).getOrElse(request.remoteAddress) ?
        }
    }

}
