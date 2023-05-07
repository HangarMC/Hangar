package io.papermc.hangar.components.images.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/internal/image")
public class ImageProxyController {

    private final RestTemplate restTemplate;

    public ImageProxyController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/**")
    public ResponseEntity<?> proxy(final HttpServletRequest request, final HttpServletResponse res) {
        final String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        final String url = this.cleanUrl(request.getRequestURI() + query);
        if (this.validTarget(url)) {
            try {
                final ResponseEntity<byte[]> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(this.passHeaders(request)), byte[].class);
                if (this.validContentType(response)) {
                    res.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data:;");
                    return response;
                }
            } catch (final RestClientException ex) {
                return ResponseEntity.internalServerError().body("Encountered " + ex.getClass().getSimpleName() + " while trying to load " + url);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private HttpHeaders passHeaders(final HttpServletRequest request) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", request.getHeader("User-Agent") + " Hangar/1.0");
        headers.set("Accept", request.getHeader("Accept"));
        headers.set("Accept-Encoding", request.getHeader("Accept-Encoding"));
        headers.set("Accept-Language", request.getHeader("Accept-Language"));
        return headers;
    }

    private String cleanUrl(final String url) {
        return url
            .replace("/api/internal/image/", "")
            .replace("https:/", "https://")
            .replace("http:/", "http://")
            .replace(":///", "://")
            .replace("%7F", "\u007F")
            .replace("%20", " "); // I hate everything about this, but it fixes #1187
    }

    private boolean validContentType(final ResponseEntity<byte[]> response) {
        final MediaType contentType = response.getHeaders().getContentType();
        return contentType != null && contentType.getType().equals("image");
    }

    private boolean validTarget(final String url) {
        try {
            final URL parsedUrl = new URL(url);
            // valid proto
            if (!parsedUrl.getProtocol().equals("http") && !parsedUrl.getProtocol().equals("https")) {
                return false;
            }
            final InetAddress inetAddress = InetAddress.getByName(parsedUrl.getHost());
            // not local ip
            if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress()) {
                return false;
            }
        } catch (final MalformedURLException | UnknownHostException e) {
            return false;
        }

        return true;
    }
}
