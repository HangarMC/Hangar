package io.papermc.hangar.components.images.service;

import java.lang.reflect.Method;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The image proxy must reject internal targets expressed both as plain IPv4 literals and as IPv6
 * transition addresses embedding an internal IPv4 destination, while still allowing public hosts.
 */
class ImageProxyServiceSsrfTest {

    private Method parseAndValidate;
    private ImageProxyService service;

    @BeforeEach
    void setUp() throws Exception {
        this.service = new ImageProxyService(null);
        this.parseAndValidate = ImageProxyService.class.getDeclaredMethod("parseAndValidate", String.class);
        this.parseAndValidate.setAccessible(true);
    }

    private URI validate(final String url) throws Exception {
        return (URI) this.parseAndValidate.invoke(this.service, url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://127.0.0.1/",
        "http://10.0.0.1/",
        "http://192.168.1.1/",
        "http://169.254.169.254/latest/meta-data/", // link-local, reached by unwrapping the vectors below
        // NAT64 well-known prefix 64:ff9b::/96
        "http://[64:ff9b::7f00:1]/",    // 127.0.0.1
        "http://[64:ff9b::a9fe:a9fe]/", // 169.254.169.254 (cloud metadata)
        "http://[64:ff9b::a00:1]/",     // 10.0.0.1
        // 6to4 2002::/16
        "http://[2002:7f00:1::]/",      // 127.0.0.1
        "http://[2002:a00:1::]/",       // 10.0.0.1
        // IPv4-compatible and IPv4-mapped
        "http://[::127.0.0.1]/",
        "http://[::ffff:127.0.0.1]/",
    })
    void shouldBlockInternalTargets(final String url) throws Exception {
        assertNull(this.validate(url), "guard must reject internal target: " + url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://93.184.216.34/", // example.com (public IPv4)
        "http://[2606:2800:220:1:248:1893:25c8:1946]/", // public IPv6
        "http://[64:ff9b::101:101]/", // NAT64-wrapped 1.1.1.1 is public, must not be over-blocked
    })
    void shouldAllowPublicTargets(final String url) throws Exception {
        assertNotNull(this.validate(url), "guard must allow public target: " + url);
    }
}
