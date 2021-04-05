package io.papermc.hangar.controller.extras.filters;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.extras.filters.ContentSecurityPolicyFilter.CSP.PolicyBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    public final HangarConfig hangarConfig;

    private final String cspHeader;

    // TODO update for new frontend (probably dont even want this anymore)
    @Autowired
    public ContentSecurityPolicyFilter(HangarConfig hangarConfig) {
        this.hangarConfig = hangarConfig;
        PolicyBuilder builder = new PolicyBuilder();

        builder.default_src(CSP.SELF, "https://google-analytics.com", "https://fonts.gstatic.com", "https://fonts.googleapis.com", "data: papermc.io paper.readthedocs.io") // move the data one back to img
                .style_src(CSP.SELF, "https://fonts.googleapis.com", "cdn.jsdelivr.net", CSP.UNSAFE_INLINE)
                .font_src("fonts.gstatic.com", "cdn.jsdelivr.net")
                .script_src(CSP.SELF, "'nonce-{nonce}'", CSP.UNSAFE_INLINE) // unsafe inline is ignored by browsers that support nonces, just added for backwards compat
//                .img_src(CSP.SELF, hangarConfig.getAuthUrl(), "https://www.google-analytics.com", "https://www.gravatar.com", "data: papermc.io paper.readthedocs.io") // ppl can use images in descriptions, we would need an image proxy or smth
                .img_src(CSP.SELF, "https:")
                .manifest_src(CSP.SELF)
                .prefetch_src(CSP.SELF, "https://fonts.googleapis.com") // isnt implemented yet -> default_src
                .connect_src(CSP.SELF, "https://www.google-analytics.com", "https://stats.g.doubleclick.net")
                .media_src(CSP.SELF)
                .object_src(CSP.NONE)
                .frame_ancestor(CSP.NONE)
                .base_uri(CSP.NONE)
                .block_all_mixed_content();

        if (hangarConfig.isDev()) {
            String webpack = "http://localhost:8081";
            String webSocket = "ws://*:8081";
            String socketIo = "http://*:8081";
            builder.default_src(webpack, socketIo, webSocket)
                    .style_src(webpack, socketIo, webSocket)
                    .script_src(webpack, socketIo, webSocket, CSP.UNSAFE_EVAL)
                    .img_src(webpack, socketIo, webSocket)
                    .prefetch_src(webpack)
                    .connect_src(socketIo, webSocket)
                    .manifest_src("http://localhost:8081/manifest/manifest.json")
                    .media_src(webpack);
        }

        cspHeader = builder.build();
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String nonce = RandomStringUtils.randomAlphanumeric(64);
        response.addHeader(CSP.Header.REPORT_ONLY,cspHeader.replace("{nonce}", nonce));
        request.setAttribute("nonce", nonce);
        filterChain.doFilter(request, response);
    }

    /*
     * Copyright (C) 2013 salesforce.com, inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *         http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    /**
     * This class provides functionality for building Content Security Policy 1.0
     * (CSP) HTTP response headers as described in <a href="http://www.w3.org/TR/CSP/">
     * the W3C Content Security Policy 1.0 spec</a>.
     *
     * Use PolicyBuilder to build a header value to attach to an HTTP response.
     *
     * Example Usage:
     *
     * To set a header that <em>only reports</em> all resource loading:
     * <code>
     PolicyBuilder p = new PolicyBuilder();
     p
     .default_src(CSP.NONE)
     .report_uri(CSPReporterServlet.URL);
     response.setHeader(CSP.Header.REPORT_ONLY, p.build());
     * </code>
     *
     * To set a header that <em>disallows</em> loading of scripts <em>except</em>
     * from social network widgets:
     * <code>
     PolicyBuilder p = new PolicyBuilder();
     p
     .script_src(
     "https://apis.google.com",
     "https://platform.twitter.com")
     .frame_src(
     "https://plusone.google.com",
     "https://facebook.com",
     "https://platform.twitter.com");
     response.setHeader(CSP.Header.SECURE, p.build());
     * </code>
     */
    public static class CSP {

        private CSP() {
            // This class is only static methods and should not be instantiated
        }

        public static final class Header {
            private Header() {
                // This class is only static methods and should not be instantiated
            }

            public static final String SECURE = "Content-Security-Policy";
            public static final String REPORT_ONLY = "Content-Security-Policy-Report-Only";
        }

        public enum Directive {
            DEFAULT("default-src"),
            SCRIPT("script-src"),
            OBJECT("object-src"),
            STYLE("style-src"),
            IMG("img-src"),
            MEDIA("media-src"),
            FRAME_ANCESTOR("frame-ancestors"),  // Sites allowed to frame this page
            FRAME_SRC("frame-src"),  // Sites this page is allowed to frame
            FONT("font-src"),
            CONNECT("connect-src"),
            SANDBOX("sandbox"),
            REPORT_URI("report-uri"),
            PLUGIN_TYPES("plugin-types"),
            BASE_URI("base-uri"),
            REFERRER("referrer"),
            MANIFEST("manifest-src"),
            PREFETCH("prefetch-src"),
            UPGRADE_INSECURE_REQUESTS("upgrade-insecure-requests"),
            BLOCK_ALL_MIXED_CONTENT("block-all-mixed-content");

            private String directive;

            Directive(String directive) {
                this.directive = directive;
            }

            @Override
            public String toString() {
                return directive;
            }
        }

        /**
         * Special value for allowing a resource type from the same domain as
         * served the initial response.
         */
        public static final String SELF = "'self'";

        /**
         * Special value for disallowing a resource type from any domain.
         */
        public static final String NONE = "'none'";

        /**
         * Special value for allowing a resource type from any domain.
         */
        public static final String ALL = "*";

        /**
         * Special value for allowing inline resource inclusion (such as
         * &lt;style&gt; or &lt;script&gt;).
         *
         * Not recommended.
         */
        public static final String UNSAFE_INLINE = "'unsafe-inline'";

        /**
         * Special value for allowing <code>eval()</code> of JavaScript.
         *
         * Not recommended.
         */
        public static final String UNSAFE_EVAL = "'unsafe-eval'";

        /**
         * Fluent interface for building Content Security Policy headers.
         *
         * See {@link CSP} for example usage.
         */
        public static class PolicyBuilder {
            private static final Set<String> DIRECTIVE_WITHOUT_VALUE_PLACEHOLDER = Collections.singleton("");

            /**
             * This uses {@link java.util.Set}s to enforce unique elements in
             * source-list and ancestor-source-list directive values.
             *
             * Implementation note: {@link java.util.LinkedHashSet} maintains output
             * parity for the sake of existing tests that expect output in a specific order.
             */
            private final EnumMap<Directive, Set<String>> directives = new EnumMap<>(Directive.class);

            public PolicyBuilder default_src(String... sources) {
                return extend(Directive.DEFAULT, sources);
            }

            public PolicyBuilder script_src(String... sources) {
                return extend(Directive.SCRIPT, sources);
            }

            public PolicyBuilder object_src(String... sources) {
                return extend(Directive.OBJECT, sources);
            }

            public PolicyBuilder style_src(String... sources) {
                return extend(Directive.STYLE, sources);
            }

            public PolicyBuilder img_src(String... sources) {
                return extend(Directive.IMG, sources);
            }

            public PolicyBuilder media_src(String... sources) {
                return extend(Directive.MEDIA, sources);
            }

            public PolicyBuilder frame_src(String... sources) {
                return extend(Directive.FRAME_SRC, sources);
            }

            public PolicyBuilder frame_ancestor(String... sources) {
                return extend(Directive.FRAME_ANCESTOR, sources);
            }

            public PolicyBuilder font_src(String... sources) {
                return extend(Directive.FONT, sources);
            }

            public PolicyBuilder connect_src(String... sources) {
                return extend(Directive.CONNECT, sources);
            }

            public PolicyBuilder manifest_src(String... sources) {
                return extend(Directive.MANIFEST, sources);
            }

            public PolicyBuilder prefetch_src(String... sources) {
                return extend(Directive.PREFETCH, sources);
            }

            public PolicyBuilder sandbox(String... flags) {
                directives.put(Directive.SANDBOX, new LinkedHashSet<>(Arrays.asList(flags)));
                return this;
            }

            public PolicyBuilder report_uri(String... uris) {
                // If the Report URI is empty or null, don't add report-uri directive at all
                Set<String> reportURIs = Arrays.stream(uris).filter(StringUtils::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
                if (!reportURIs.isEmpty()) {
                    directives.put(Directive.REPORT_URI, reportURIs);
                }
                return this;
            }

            public PolicyBuilder plugin_types(String... pluginTypes) {
                return extend(Directive.PLUGIN_TYPES, pluginTypes);
            }

            public PolicyBuilder base_uri(String... uris) {
                return extend(Directive.BASE_URI, uris);
            }

            public PolicyBuilder referrer(String referrer) {
                directives.put(Directive.REFERRER, new LinkedHashSet<>(Collections.singletonList(referrer)));
                return this;
            }

            public PolicyBuilder upgrade_insecure_requests() {
                // upgrade-insecure-requests does not have an associated value
                directives.put(Directive.UPGRADE_INSECURE_REQUESTS, DIRECTIVE_WITHOUT_VALUE_PLACEHOLDER);
                return this;
            }

            public PolicyBuilder block_all_mixed_content() {
                // block-all-mixed-content does not have an associated value
                directives.put(Directive.BLOCK_ALL_MIXED_CONTENT, DIRECTIVE_WITHOUT_VALUE_PLACEHOLDER);
                return this;
            }

            private PolicyBuilder extend(Directive directive, String... sources) {
                Set<String> set = directives.get(directive);
                if (set == null) {
                    directives.put(directive, new LinkedHashSet<>(Arrays.asList(sources)));
                } else {
                    set.addAll(Arrays.asList(sources));
                }
                return this;
            }

            public String build() {
                StringBuilder sb = new StringBuilder();

                for (Directive dir : directives.keySet()) {
                    Set<String> values = directives.get(dir);
                    if (!values.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append("; ");
                        }

                        sb.append(dir);

                        for (String value : values) {
                            sb.append(' ').append(value);
                        }
                    }
                }
                return sb.toString();
            }
        }
    }
}
