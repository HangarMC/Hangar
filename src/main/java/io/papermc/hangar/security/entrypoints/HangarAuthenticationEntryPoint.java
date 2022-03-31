package io.papermc.hangar.security.entrypoints;

import io.papermc.hangar.util.Routes;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HangarAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            response.sendRedirect(Routes.SHOW_HOME.getRouteUrl());
        } else {
            response.sendRedirect(Routes.USERS_LOGIN.getRouteUrl("", "", request.getRequestURI()));
        }
    }
}
