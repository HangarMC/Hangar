package io.papermc.hangar.components.images.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageProxyService {

    private final WebClient webClient;

    public ImageProxyService(final WebClient webClient) {
        this.webClient = webClient;
    }

    public ClientResponse proxyImage(final String imageUrl, @Nullable final HttpServletRequest request) {
        final URI uri = this.parseAndValidate(this.cleanUrl(imageUrl));
        if (uri != null) {
            try {
                ClientResponse clientResponse = this.webClient.get()
                    .uri(uri)
                    .headers((headers) -> this.passHeaders(headers, request))
                    .exchange().block(); // Block the request, we don't get the body at this point!
                if (clientResponse == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered an error whilst trying to load url " + imageUrl);
                }
                // check status code
                if (!clientResponse.statusCode().is2xxSuccessful()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encountered " + clientResponse.statusCode().value() + " while trying to load " + imageUrl);
                }
                // block large stuff
                if (this.contentTooLarge(clientResponse)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image you are trying too proxy is too large");
                }
                // check content type
                if (!this.validContentType(clientResponse)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad content type");
                }
                return clientResponse;
            } catch (final WebClientRequestException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered " + ex.getClass().getSimpleName() + " while trying to load " + imageUrl, ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad target");
        }
    }

    private void passHeaders(final HttpHeaders headers, @Nullable final HttpServletRequest request) {
        if (request == null) {
            return;
        }
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
            .replace(":///", "://");
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

    private URI parseAndValidate(final String url) {
        try {
            final URI parsedUrl = new URI(url);
            // valid proto
            if (!parsedUrl.getScheme().equals("http") && !parsedUrl.getScheme().equals("https")) {
                return null;
            }
            final InetAddress inetAddress = InetAddress.getByName(parsedUrl.getHost());
            // not local ip
            if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress()) {
                return null;
            }
            return parsedUrl;
        } catch (final Exception e) {
            return null;
        }
    }
}
