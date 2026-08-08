package io.papermc.hangar.components.images.controller;

import io.papermc.hangar.components.images.service.ImageProxyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/internal/image")
public class ImageProxyController {

    private final ImageProxyService imageProxyService;

    public ImageProxyController(final ImageProxyService imageProxyService) {
        this.imageProxyService = imageProxyService;
    }

    @GetMapping("/**")
    public StreamingResponseBody proxy(final HttpServletRequest request, final HttpServletResponse response) {
        final String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        final String url = request.getRequestURI() + query;
        final ImageProxyService.ProxiedImage image = this.imageProxyService.proxyImage(url, request);
        // forward headers
        image.headers().forEach((name, values) -> response.setHeader(name, values.getFirst()));
        response.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data:;"); // no xss for you sir
        return outputStream -> {
            try (image) {
                // stream the body straight through, it is never fully loaded into memory
                image.body().transferTo(outputStream);
            }
        };
    }
}
