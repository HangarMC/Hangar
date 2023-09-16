package io.papermc.hangar;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.UserActionLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public abstract class HangarComponent {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected HangarConfig config;

    @Lazy // UserActionLogService is a HangarComponent too...
    @Autowired
    protected UserActionLogService actionLogger;

    protected final Optional<HangarPrincipal> getOptionalHangarPrincipal() {
        return this.getHangarPrincipal0().get();
    }

    protected final @NotNull Permission getGlobalPermissions() {
        return this.getHangarPrincipal0().get().map(HangarPrincipal::getGlobalPermissions).orElse(PermissionService.DEFAULT_SIGNED_OUT_PERMISSIONS);
    }

    protected final HangarPrincipal getHangarPrincipal() {
        return this.getHangarPrincipal0().get().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication principal found"));
    }

    protected final @Nullable Long getHangarUserId() {
        return this.getHangarPrincipal0().get().map(HangarPrincipal::getId).orElse(null);
    }

    private MemoizingSupplier<Optional<HangarPrincipal>> getHangarPrincipal0() {
        if (HangarApplication.TEST_MODE) {
            return MemoizingSupplier.of(() -> HangarApplication.TEST_PRINCIPAL);
        }
        return MemoizingSupplier.of(() -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(HangarAuthenticationToken.class::isInstance)
            .map(HangarAuthenticationToken.class::cast)
            .map(HangarAuthenticationToken::getPrincipal));
    }
}
