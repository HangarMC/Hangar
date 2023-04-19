package io.papermc.hangar.components.images.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        final String url = this.cleanUrl(request.getRequestURI());
        if (this.validTarget(url)) {
            try {
                final ResponseEntity<byte[]> response = this.restTemplate.getForEntity(url, byte[].class);
                if (this.validContentType(response)) {
                    res.setHeader("Content-Security-Policy", "default-src 'self'");
                    return response;
                }
            } catch (final RestClientException ex) {
                return ResponseEntity.internalServerError().body("Encountered " + ex.getClass().getSimpleName() + " while trying to load " + url);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private String cleanUrl(final String url) {
        return url
            .replace("/api/internal/image/", "")
            .replace("https:/", "https://")
            .replace("http:/", "http://")
            .replace(":///", "://");
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
