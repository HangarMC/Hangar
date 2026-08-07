package io.papermc.hangar.components.images.service;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Resolves through the JDK default resolver but rejects any result pointing at an internal address,
 * so the address that is validated is exactly the one the client connects to. Validating at connect
 * time (rather than once up front) closes the DNS-rebinding / multi-record gap: a host that resolves
 * to a public address on one lookup and an internal one on the next cannot slip through.
 */
public final class SsrfProtectedAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(final EventExecutor executor) {
        return new Resolver(executor, DefaultAddressResolverGroup.INSTANCE.getResolver(executor));
    }

    private static boolean isBlocked(final InetSocketAddress address) {
        return !address.isUnresolved() && InternalAddresses.isBlocked(address.getAddress());
    }

    private record Resolver(EventExecutor executor, AddressResolver<InetSocketAddress> delegate) implements AddressResolver<InetSocketAddress> {

        @Override
        public boolean isSupported(final SocketAddress address) {
            return this.delegate.isSupported(address);
        }

        @Override
        public boolean isResolved(final SocketAddress address) {
            return this.delegate.isResolved(address);
        }

        @Override
        public Future<InetSocketAddress> resolve(final SocketAddress address) {
            return this.resolve(address, this.executor.newPromise());
        }

        @Override
        public Future<InetSocketAddress> resolve(final SocketAddress address, final Promise<InetSocketAddress> promise) {
            this.delegate.resolve(address).addListener((FutureListener<InetSocketAddress>) future -> {
                if (!future.isSuccess()) {
                    promise.setFailure(future.cause());
                    return;
                }
                final InetSocketAddress resolved = future.getNow();
                if (isBlocked(resolved)) {
                    promise.setFailure(new UnknownHostException("Refused to connect to internal address " + resolved));
                } else {
                    promise.trySuccess(resolved);
                }
            });
            return promise;
        }

        @Override
        public Future<List<InetSocketAddress>> resolveAll(final SocketAddress address) {
            return this.resolveAll(address, this.executor.newPromise());
        }

        @Override
        public Future<List<InetSocketAddress>> resolveAll(final SocketAddress address, final Promise<List<InetSocketAddress>> promise) {
            this.delegate.resolveAll(address).addListener((FutureListener<List<InetSocketAddress>>) future -> {
                if (!future.isSuccess()) {
                    promise.setFailure(future.cause());
                    return;
                }
                final List<InetSocketAddress> resolved = future.getNow();
                for (final InetSocketAddress candidate : resolved) {
                    if (isBlocked(candidate)) {
                        promise.setFailure(new UnknownHostException("Refused to connect to internal address " + candidate));
                        return;
                    }
                }
                promise.trySuccess(resolved);
            });
            return promise;
        }

        @Override
        public void close() {
            this.delegate.close();
        }
    }
}
