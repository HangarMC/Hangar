package io.papermc.hangar.components.images.controller;

import io.papermc.hangar.components.images.service.ImageProxyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/internal/image")
public class ImageProxyController {

    private final ImageProxyService imageProxyService;

    public ImageProxyController(final ImageProxyService imageProxyService) {
        this.imageProxyService = imageProxyService;
    }

    @GetMapping("/**")
    public StreamingResponseBody proxy(final HttpServletRequest request, final HttpServletResponse res) {
        final String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        final String url = request.getRequestURI() + query;
        ClientResponse clientResponse = null;
        try {
            clientResponse = this.imageProxyService.proxyImage(url, request);
            // forward headers
            for (final Map.Entry<String, List<String>> stringListEntry : clientResponse.headers().asHttpHeaders().entrySet()) {
                res.setHeader(stringListEntry.getKey(), stringListEntry.getValue().getFirst());
            }
            // Ask to have the body put into a stream of data buffers
            final Flux<DataBuffer> body = clientResponse.body(BodyExtractors.toDataBuffers());
            res.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data:;"); // no xss for you sir
            return o -> {
                // Write the data buffers into the outputstream as they come!
                final Flux<DataBuffer> flux = DataBufferUtils
                    .write(body, o)
                    .publish()
                    .autoConnect(2);
                flux.subscribe(DataBufferUtils.releaseConsumer()); // Release the consumer as we are using exchange, prevent memory leaks!
                try {
                    flux.blockLast(); // Wait until the last block has been passed and then tell Spring to close the stream!
                } catch (final RuntimeException ex) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered " + ex.getClass().getSimpleName() + " while trying to load " + url);
                }
            };
        } finally {
            if (clientResponse != null) {
                clientResponse.releaseBody();
            }
        }
    }
}
