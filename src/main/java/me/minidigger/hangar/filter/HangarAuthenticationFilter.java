package me.minidigger.hangar.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

import me.minidigger.hangar.security.HangarAuthentication;

public class HangarAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return null;
        }

        if (!header.startsWith("HangarApi ")) {
            return null;
        }

//        String key = header.replace("HangarApi ", "");
//        return new HangarAuthentication(key);
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

//    class HangarAuthenticationManager implements AuthenticationManager {
//
//        @Override
//        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//            if (authentication.getPrincipal() instanceof HangarAuthentication) {
//                authentication.setAuthenticated(true);
//                HangarAuthentication auth = (HangarAuthentication) authentication.getPrincipal();
//                auth.setUserId(-1);
//                // TODO auth
//            }
//
//            return authentication;
//        }
//    }
}
