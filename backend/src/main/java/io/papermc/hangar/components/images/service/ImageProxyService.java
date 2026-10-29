package io.papermc.hangar.components.images.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;
import java.util.Set;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageProxyService {

    private static final long MAX_CONTENT_LENGTH = 150_000_000;

    /**
     * Headers that must not be forwarded from the upstream response: hop-by-hop headers (RFC 9110)
     * that would confuse the servlet container, and headers that would let an upstream image host
     * mess with our domain (cookies) or weaken the CSP the controller sets itself.
     */
    private static final Set<String> SKIPPED_RESPONSE_HEADERS = Set.of(
        HttpHeaders.CONNECTION, "keep-alive", HttpHeaders.PROXY_AUTHENTICATE, HttpHeaders.PROXY_AUTHORIZATION,
        HttpHeaders.TE, HttpHeaders.TRAILER, HttpHeaders.TRANSFER_ENCODING, HttpHeaders.UPGRADE,
        HttpHeaders.SET_COOKIE, "content-security-policy"
    );

    private final CloseableHttpClient httpClient;

    public ImageProxyService(final CloseableHttpClient imageProxyHttpClient) {
        this.httpClient = imageProxyHttpClient;
    }

    /**
     * Requests the given image url and returns once the upstream response head has been validated.
     * The body is <b>not</b> read into memory; it stays a live stream that the caller must consume
     * and then close (try-with-resources on the returned object closes the upstream response).
     */
    public ProxiedImage proxyImage(final String imageUrl, @Nullable final HttpServletRequest request) {
        final URI uri = this.parseAndValidate(this.cleanUrl(imageUrl));
        if (uri == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad target");
        }
        final HttpGet get = new HttpGet(uri);
        this.passHeaders(get, request);
        final CloseableHttpResponse response;
        try {
            // returns as soon as the response head is in; the body keeps streaming from the socket
            response = this.httpClient.execute(get);
        } catch (final IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered " + ex.getClass().getSimpleName() + " while trying to load " + imageUrl, ex);
        }
        try {
            // check status code
            if (response.getCode() < 200 || response.getCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encountered " + response.getCode() + " while trying to load " + imageUrl);
            }
            // block large stuff
            if (this.contentTooLarge(response)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image you are trying too proxy is too large");
            }
            // check content type
            if (!this.validContentType(response)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad content type");
            }
            final HttpEntity entity = response.getEntity();
            return new ProxiedImage(this.extractHeaders(response), entity != null ? entity.getContent() : InputStream.nullInputStream(), response);
        } catch (final IOException ex) {
            this.closeQuietly(response);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encountered " + ex.getClass().getSimpleName() + " while trying to load " + imageUrl, ex);
        } catch (final RuntimeException ex) {
            this.closeQuietly(response);
            throw ex;
        }
    }

    private void closeQuietly(final CloseableHttpResponse response) {
        try {
            response.close();
        } catch (final IOException ignored) {
        }
    }

    private void passHeaders(final HttpGet get, @Nullable final HttpServletRequest request) {
        if (request == null) {
            return;
        }
        final String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            get.setHeader("User-Agent", userAgent + " Hangar/1.0");
        }
        this.passHeader(get, request, "Accept");
        this.passHeader(get, request, "Accept-Encoding");
        this.passHeader(get, request, "Accept-Language");
    }

    private void passHeader(final HttpGet get, final HttpServletRequest request, final String name) {
        final String value = request.getHeader(name);
        if (value != null) {
            get.setHeader(name, value);
        }
    }

    private HttpHeaders extractHeaders(final CloseableHttpResponse response) {
        final HttpHeaders headers = new HttpHeaders();
        for (final Header header : response.getHeaders()) {
            if (!SKIPPED_RESPONSE_HEADERS.contains(header.getName().toLowerCase(Locale.ROOT))) {
                headers.add(header.getName(), header.getValue());
            }
        }
        return headers;
    }

    private String cleanUrl(final String url) {
        return url
            .replace("/api/internal/image/", "")
            .replace("https:/", "https://")
            .replace("http:/", "http://")
            .replace(":///", "://");
    }

    private boolean contentTooLarge(final CloseableHttpResponse response) {
        // not all responses have a length...
        final Header contentLength = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        if (contentLength == null) {
            return false;
        }
        try {
            return Long.parseLong(contentLength.getValue().trim()) > MAX_CONTENT_LENGTH;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    private boolean validContentType(final CloseableHttpResponse response) {
        final Header contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return false;
        }
        try {
            return MediaType.parseMediaType(contentType.getValue()).getType().equals("image");
        } catch (final InvalidMediaTypeException ignored) {
            return false;
        }
    }

    private URI parseAndValidate(final String url) {
        try {
            final URI parsedUrl = new URI(url);
            // valid proto
            if (parsedUrl.getScheme() == null || (!parsedUrl.getScheme().equals("http") && !parsedUrl.getScheme().equals("https"))) {
                return null;
            }
            // reject internal targets up front; the http client's resolver re-checks at connect time
            if (parsedUrl.getHost() == null || InternalAddresses.isBlocked(InetAddress.getByName(parsedUrl.getHost()))) {
                return null;
            }
            return parsedUrl;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * A validated upstream image response. {@link #body()} streams straight from the upstream socket;
     * closing this object releases the upstream connection.
     */
    public record ProxiedImage(HttpHeaders headers, InputStream body, CloseableHttpResponse upstream) implements Closeable {

        @Override
        public void close() throws IOException {
            this.upstream.close();
        }
    }
}
