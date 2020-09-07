package io.papermc.hangar.security;

import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class HangarAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private Supplier<Optional<UsersTable>> currentUser;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        if (currentUser.get().isPresent()) {
            response.sendRedirect(Routes.SHOW_HOME.getRouteUrl());
        } else {
            response.sendRedirect(Routes.USERS_LOGIN.getRouteUrl("", "", request.getRequestURI()));
        }
    }
}
