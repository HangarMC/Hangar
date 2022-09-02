package io.papermc.hangar.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.exceptions.HangarApiException;
import org.apache.pdfbox.util.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class HangarAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Autowired
    public HangarAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        HttpStatus status;
        if (failed instanceof CredentialsExpiredException) {
            status = HttpStatus.FORBIDDEN;
        } else if (failed instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.getWriter().write(mapper.writeValueAsString(new HangarApiException(status, failed.getMessage())));
    }
}
