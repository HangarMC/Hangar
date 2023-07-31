package io.papermc.hangar.components.images.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/internal/image")
public class ImageProxyController {

    private final WebClient webClient;

    public ImageProxyController(final WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/**")
    public Object proxy(final HttpServletRequest request, final HttpServletResponse res) {
        final String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        final String url = this.cleanUrl(request.getRequestURI() + query);
        if (this.validTarget(url)) {
            ClientResponse clientResponse = null;
            try {
                clientResponse = this.webClient.get()
                    .uri(new URL(url).toURI())
                    .headers((headers) -> this.passHeaders(headers, request))
                    .exchange().block(); // Block the request, we don't get the body at this point!
                if (clientResponse == null) {
                    return ResponseEntity.internalServerError().body("Encountered an error whilst trying to load url");
                }
                // block large stuff
                if (this.contentTooLarge(clientResponse)) {
                    return ResponseEntity.badRequest().body("The image you are trying too proxy is too large");
                }
                // forward headers
                for (final Map.Entry<String, List<String>> stringListEntry : clientResponse.headers().asHttpHeaders().entrySet()) {
                    res.setHeader(stringListEntry.getKey(), stringListEntry.getValue().get(0));
                }
                // Ask to have the body put into a stream of data buffers
                final Flux<DataBuffer> body = clientResponse.body(BodyExtractors.toDataBuffers());
                if (this.validContentType(clientResponse)) {
                    res.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data:;"); // no xss for you sir
                    return (StreamingResponseBody) o -> {
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
                } else {
                    return ResponseEntity.badRequest().body("Bad content type");
                }
            } catch (final WebClientRequestException | MalformedURLException | URISyntaxException ex) {
                return ResponseEntity.internalServerError().body("Encountered " + ex.getClass().getSimpleName() + " while trying to load " + url);
            } finally {
                if (clientResponse != null) {
                    // noinspection ReactiveStreamsUnusedPublisher
                    clientResponse.releaseBody(); // Just in case...
                }
            }
        } else {
            return ResponseEntity.badRequest().body("Bad target");
        }
    }

    private void passHeaders(final HttpHeaders headers, final HttpServletRequest request) {
        headers.set("User-Agent", request.getHeader("User-Agent") + " Hangar/1.0");
        headers.set("Accept", request.getHeader("Accept"));
        headers.set("Accept-Encoding", request.getHeader("Accept-Encoding"));
        headers.set("Accept-Language", request.getHeader("Accept-Language"));
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

    private boolean contentTooLarge(final ClientResponse response) {
        final var contentLength = response.headers().contentLength();
        // not all responses have a length...
        return contentLength.isPresent() && contentLength.getAsLong() > 150_000_000;
    }

    private boolean validContentType(final ClientResponse response) {
        try {
            final var contentType = response.headers().contentType();
            return contentType.isPresent() && contentType.get().getType().equals("image");
        } catch (final InvalidMediaTypeException ignored) {
            return false;
        }
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
