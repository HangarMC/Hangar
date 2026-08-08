package io.papermc.hangar.components.images.service;

import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the resolver applies the block check to the addresses it hands back, using IP literals so
 * no real DNS is involved.
 */
class SsrfProtectedDnsResolverTest {

    private final SsrfProtectedDnsResolver resolver = new SsrfProtectedDnsResolver();

    @Test
    void blocksInternalLiteral() {
        assertThrows(UnknownHostException.class, () -> this.resolver.resolve("127.0.0.1"));
        assertThrows(UnknownHostException.class, () -> this.resolver.resolve("64:ff9b::a9fe:a9fe")); // NAT64 -> 169.254.169.254
    }

    @Test
    void allowsPublicLiteral() {
        assertDoesNotThrow(() -> this.resolver.resolve("93.184.216.34"));
    }
}
