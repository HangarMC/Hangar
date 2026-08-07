package io.papermc.hangar.components.images.service;

import java.net.InetAddress;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalAddressesTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "127.0.0.1",
        "10.0.0.1",
        "192.168.1.1",
        "169.254.169.254", // link-local (cloud metadata)
        "100.64.0.1",      // carrier-grade NAT
        "100.127.255.255", // carrier-grade NAT, upper bound
        "fd00::1",         // unique-local IPv6
        "fc00::1",         // unique-local IPv6
        "fe80::1",         // link-local IPv6
        "64:ff9b::7f00:1", // NAT64 -> 127.0.0.1
        "64:ff9b::a9fe:a9fe", // NAT64 -> 169.254.169.254
        "2002:a00:1::",    // 6to4 -> 10.0.0.1
        "::7f00:1",        // IPv4-compatible -> 127.0.0.1
    })
    void shouldBlock(final String host) throws Exception {
        assertTrue(InternalAddresses.isBlocked(InetAddress.getByName(host)), "must block " + host);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "93.184.216.34",   // public IPv4
        "100.63.255.255",  // just below the CGNAT range
        "100.128.0.1",     // just above the CGNAT range
        "2606:2800:220:1:248:1893:25c8:1946", // public IPv6
        "64:ff9b::101:101", // NAT64-wrapped 1.1.1.1 is public
    })
    void shouldAllow(final String host) throws Exception {
        assertFalse(InternalAddresses.isBlocked(InetAddress.getByName(host)), "must allow " + host);
    }
}
