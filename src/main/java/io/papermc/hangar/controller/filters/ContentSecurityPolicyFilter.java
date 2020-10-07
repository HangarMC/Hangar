package io.papermc.hangar.controller.filters;

import io.papermc.hangar.config.hangar.HangarConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    public final HangarConfig hangarConfig;

    private static final String CSP = "default-src 'self' {additional-uris} fonts.googleapis.com; style-src fonts.googleapis.com 'self' {additional-uris} 'unsafe-inline'; font-src fonts.gstatic.com; script-src {additional-uris} 'self' 'nonce-{nonce}' 'unsafe-eval'; img-src 'self' papermc.io paper.readthedocs.io {additional-uris} {auth-uri}; manifest-src {manifest-uri}; prefetch-src {prefetch-uri}; media {prefetch-uri}; object-src 'none'; block-all-mixed-content; frame-ancestors 'none'; base-uri 'none'";


    @Autowired
    public ContentSecurityPolicyFilter(HangarConfig hangarConfig) {
        this.hangarConfig = hangarConfig;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String CSP_NONCE = RandomStringUtils.randomAlphanumeric(64);
//        response.addHeader(
//                "Content-Security-Policy",
//                CSP
//                .replace("{additional-uris}", hangarConfig.isUseWebpack() ? "http://localhost:8081" : "")
//                .replace("{auth-uri}", hangarConfig.getAuthUrl())
//                .replace("{manifest-uri}", hangarConfig.isUseWebpack() ? "http://localhost:8081/manifest/manifest.json" : "'self'")
//                .replace("{prefetch-uri}", hangarConfig.isUseWebpack() ? "http://localhost:8081" : "'self'")
//                .replace("{nonce}", CSP_NONCE));
        // TODO still some changes to make to the header
        filterChain.doFilter(request, response);
    }
}
