package io.papermc.hangar.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.exceptions.HangarApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class HangarAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Autowired
    public HangarAuthenticationEntryPoint(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException failed) throws IOException {
        final HttpStatus status;
        if (failed instanceof CredentialsExpiredException) {
            status = HttpStatus.FORBIDDEN;
        } else if (failed instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(this.mapper.writeValueAsString(new HangarApiException(status, failed.getMessage())));
    }
}
