package io.papermc.hangar.controller;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.security.HangarPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class HangarController {

    @Autowired
    protected HangarConfig hangarConfig;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    protected final HangarPrincipal getHangarPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof HangarAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication principal found");
        }
        return (HangarPrincipal) authentication.getPrincipal();
    }
}
