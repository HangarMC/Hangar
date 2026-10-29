package io.papermc.hangar.components.images.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;

/**
 * Resolves through the system default resolver but rejects any result pointing at an internal address,
 * so the address that is validated is exactly the one the client connects to. Validating at connect
 * time (rather than once up front) closes the DNS-rebinding / multi-record gap: a host that resolves
 * to a public address on one lookup and an internal one on the next cannot slip through.
 */
public final class SsrfProtectedDnsResolver implements DnsResolver {

    private final DnsResolver delegate = SystemDefaultDnsResolver.INSTANCE;

    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException {
        final InetAddress[] addresses = this.delegate.resolve(host);
        for (final InetAddress address : addresses) {
            if (InternalAddresses.isBlocked(address)) {
                throw new UnknownHostException("Refused to connect to internal address " + address + " for host " + host);
            }
        }
        return addresses;
    }

    @Override
    public String resolveCanonicalHostname(final String host) throws UnknownHostException {
        return this.delegate.resolveCanonicalHostname(host);
    }
}
