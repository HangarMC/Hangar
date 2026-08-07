package io.papermc.hangar.components.images.service;

import io.netty.resolver.AddressResolver;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the resolver applies the block check to the address it hands back, using IP literals so
 * no real DNS is involved.
 */
class SsrfProtectedAddressResolverGroupTest {

    private final AddressResolver<InetSocketAddress> resolver =
        new SsrfProtectedAddressResolverGroup().getResolver(GlobalEventExecutor.INSTANCE);

    private boolean resolves(final String host) throws Exception {
        return this.resolver.resolve(InetSocketAddress.createUnresolved(host, 80)).await().isSuccess();
    }

    @Test
    void blocksInternalLiteral() throws Exception {
        assertFalse(this.resolves("127.0.0.1"));
        assertFalse(this.resolves("64:ff9b::a9fe:a9fe")); // NAT64 -> 169.254.169.254
    }

    @Test
    void allowsPublicLiteral() throws Exception {
        assertTrue(this.resolves("93.184.216.34"));
    }
}
