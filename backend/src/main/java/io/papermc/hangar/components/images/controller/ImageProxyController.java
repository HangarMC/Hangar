package io.papermc.hangar.components.images.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/internal/image")
public class ImageProxyController {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public ImageProxyController(final RestTemplate restTemplate, final WebClient webClient) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
    }

    @GetMapping("/**")
    public StreamingResponseBody proxy(final HttpServletRequest request, final HttpServletResponse res) {
        final String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        final String url = this.cleanUrl(request.getRequestURI() + query);
        if (true || this.validTarget(url)) {
            try {
                final ResponseEntity<Resource> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(this.passHeaders(new HttpHeaders(), request)), Resource.class);
                if (this.validContentType(response) && response.getBody() != null) {
                    for (final Map.Entry<String, List<String>> stringListEntry : response.getHeaders().entrySet()) {
                        res.setHeader(stringListEntry.getKey(), stringListEntry.getValue().get(0));
                    }
                    res.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data:;");
                    final InputStream stream = response.getBody().getInputStream();
                    return (os) -> {
                        final byte[] data = new byte[2048];
                        int read;
                        while ((read = stream.read(data)) > 0) {
                            os.write(data, 0, read);
                        }
                        os.flush();
                    };
                }
            } catch (final RestClientException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered " + ex.getClass().getSimpleName() + " while trying to load " + url);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/flux/**")
    public StreamingResponseBody proxy2(final HttpServletRequest request, final HttpServletResponse res) {
        final String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        final String url = this.cleanUrl(request.getRequestURI() + query);
        if (true || this.validTarget(url)) {
            ClientResponse clientResponse = null;
            try {
                clientResponse = this.webClient.get()
                    .uri(url)
                    .headers((headers) -> this.passHeaders(headers, request))
                    .exchange().block(); // Block the request, we don't get the body at this point!
                if(clientResponse == null){
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered an error whilst trying to load url");
                }
                // Go through headers
                for (final Map.Entry<String, List<String>> stringListEntry : clientResponse.headers().asHttpHeaders().entrySet()) {
                    res.setHeader(stringListEntry.getKey(), stringListEntry.getValue().get(0));
                }
                // Ask to have the body put into a stream of data buffers
                final Flux<DataBuffer> body = clientResponse.body(BodyExtractors.toDataBuffers());
                //if (this.validContentType(response)) {
                res.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data:;");
                return (o) -> {
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
                //}
            } catch (final RestClientException ex) {
                if(clientResponse != null){
                    clientResponse.releaseBody(); // Just in case...
                }
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered " + ex.getClass().getSimpleName() + " while trying to load " + url);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    private HttpHeaders passHeaders(final HttpHeaders headers, final HttpServletRequest request) {
        headers.set("User-Agent", request.getHeader("User-Agent") + " Hangar/1.0");
        headers.set("Accept", request.getHeader("Accept"));
        headers.set("Accept-Encoding", request.getHeader("Accept-Encoding"));
        headers.set("Accept-Language", request.getHeader("Accept-Language"));
        return headers;
    }

    private String cleanUrl(final String url) {
        return url
            .replace("/api/internal/image/flux/", "")
            .replace("/api/internal/image/", "")
            .replace("https:/", "https://")
            .replace("http:/", "http://")
            .replace(":///", "://")
            .replace("%7F", "\u007F")
            .replace("%20", " "); // I hate everything about this, but it fixes #1187
    }

    private boolean validContentType(final ResponseEntity<Resource> response) {
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
