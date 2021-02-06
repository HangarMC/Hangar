package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.security.HangarPrincipal;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public abstract class HangarService {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected HangarConfig hangarConfig;

    protected final HangarPrincipal getHangarPrincipal() {
        return _getHangarPrincipal().get().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication principal found"));
    }

    @Nullable
    protected final Long getHangarUserId() {
        return _getHangarPrincipal().get().map(HangarPrincipal::getId).orElse(null);
    }

    private MemoizingSupplier<Optional<HangarPrincipal>> _getHangarPrincipal() {
        return MemoizingSupplier.of(() -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(HangarAuthenticationToken.class::isInstance)
                .map(HangarAuthenticationToken.class::cast)
                .map(HangarAuthenticationToken::getPrincipal));
    }
}
