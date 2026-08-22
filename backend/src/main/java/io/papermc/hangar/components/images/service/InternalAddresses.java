package io.papermc.hangar.components.images.service;

import org.jspecify.annotations.Nullable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Address checks shared by the image proxy's up-front URL validation and its connect-time resolver.
 */
public final class InternalAddresses {

    private static final byte[] NAT64_PREFIX = {0x00, 0x64, (byte) 0xFF, (byte) 0x9B, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] IPV4_COMPATIBLE_PREFIX = new byte[12];

    private InternalAddresses() {
    }

    /**
     * Whether an address must not be proxied to: loopback, private (RFC 1918), link-local (cloud
     * metadata), unique-local IPv6, carrier-grade NAT, or an IPv6 transition address that embeds any
     * of those.
     */
    public static boolean isBlocked(final InetAddress address) {
        if (isLocal(address)) {
            return true;
        }
        // IPv6 transition addresses embed an IPv4 target the checks above see as global
        final InetAddress embedded = extractEmbeddedIpv4(address);
        return embedded != null && isLocal(embedded);
    }

    private static boolean isLocal(final InetAddress address) {
        return address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isSiteLocalAddress()   // 10/8, 172.16/12, 192.168/16
            || address.isLinkLocalAddress()   // 169.254/16 (cloud metadata) and fe80::/10
            || isUniqueLocalIpv6(address)      // fc00::/7
            || isCarrierGradeNat(address);     // 100.64/10
    }

    private static boolean isUniqueLocalIpv6(final InetAddress address) {
        return address instanceof Inet6Address && (address.getAddress()[0] & 0xFE) == 0xFC;
    }

    private static boolean isCarrierGradeNat(final InetAddress address) {
        if (!(address instanceof Inet4Address)) {
            return false;
        }
        final byte[] bytes = address.getAddress();
        return (bytes[0] & 0xFF) == 100 && (bytes[1] & 0xFF) >= 64 && (bytes[1] & 0xFF) <= 127;
    }

    @Nullable
    private static InetAddress extractEmbeddedIpv4(final InetAddress address) {
        if (!(address instanceof Inet6Address)) {
            return null;
        }
        final byte[] bytes = address.getAddress();
        try {
            // 6to4 2002:WWXX:YYZZ:: -> bytes 2-5
            if ((bytes[0] & 0xFF) == 0x20 && (bytes[1] & 0xFF) == 0x02) {
                return InetAddress.getByAddress(Arrays.copyOfRange(bytes, 2, 6));
            }
            // NAT64 64:ff9b::/96 and IPv4-compatible ::a.b.c.d -> low 32 bits
            if (Arrays.equals(bytes, 0, 12, NAT64_PREFIX, 0, 12) || Arrays.equals(bytes, 0, 12, IPV4_COMPATIBLE_PREFIX, 0, 12)) {
                return InetAddress.getByAddress(Arrays.copyOfRange(bytes, 12, 16));
            }
        } catch (final UnknownHostException ignored) {
            // a 4-byte address never throws; satisfy the checked signature and treat as no embedded IPv4
        }
        return null;
    }
}
