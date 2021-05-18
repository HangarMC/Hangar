package io.papermc.hangar;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.UserActionLogService;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public abstract class HangarComponent {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected HangarConfig config;

    @Autowired
    protected UserActionLogService actionLogger;

    protected final Optional<HangarPrincipal> getOptionalHangarPrincipal() {
        return _getHangarPrincipal().get();
    }

    @NotNull
    protected final Permission getGlobalPermissions() {
        return _getHangarPrincipal().get().map(HangarPrincipal::getGlobalPermissions).orElse(PermissionService.DEFAULT_SIGNED_OUT_PERMISSIONS);
    }

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
