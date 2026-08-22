package io.papermc.hangar.config;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesAnnotationIntrospector;
import io.papermc.hangar.components.images.service.SsrfProtectedDnsResolver;
import io.papermc.hangar.components.index.webhook.WebhookMessageConverter;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.config.jackson.HangarAnnotationIntrospector;
import io.papermc.hangar.security.annotations.ratelimit.RateLimitInterceptor;
import io.sentry.spring7.SentryTaskDecorator;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private static final Logger interceptorLogger = LoggerFactory.getLogger("http-client-logger");

    private static final Duration timeout = Duration.ofSeconds(45);

    private final HangarConfig hangarConfig;
    private final ObjectMapper mapper;
    private final RateLimitInterceptor rateLimitInterceptor;

    private final List<Converter<?, ?>> converters;
    private final List<ConverterFactory<?, ?>> converterFactories;
    private final List<HandlerMethodArgumentResolver> resolvers;

    @Autowired
    public WebConfig(final HangarConfig hangarConfig, final ObjectMapper mapper, final RateLimitInterceptor rateLimitInterceptor, final List<Converter<?, ?>> converters, final List<ConverterFactory<?, ?>> converterFactories, final List<HandlerMethodArgumentResolver> resolvers) {
        this.hangarConfig = hangarConfig;
        this.mapper = mapper;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.converters = converters;
        this.converterFactories = converterFactories;
        this.resolvers = resolvers;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(this.rateLimitInterceptor).addPathPatterns("/**");
    }

    @Override
    protected void addCorsMappings(final CorsRegistry registry) {
        final CorsRegistration corsRegistration = registry.addMapping("/api/internal/**");
        corsRegistration.allowedOrigins(this.hangarConfig.baseUrl());
        corsRegistration.allowedMethods("GET", "HEAD", "POST", "DELETE");
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }

    @Bean
    public Filter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Bean
    public Filter identifyFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
                response.setHeader("Server", "Hangar");
                filterChain.doFilter(request, response);
            }
        };
    }

    @Override
    protected void addFormatters(final FormatterRegistry registry) {
        this.converters.forEach(registry::addConverter);
        this.converterFactories.forEach(registry::addConverterFactory);
    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        // TODO kinda wack, but idk a better way rn
        final ParameterNamesAnnotationIntrospector sAnnotationIntrospector = (ParameterNamesAnnotationIntrospector) this.mapper.getSerializationConfig().getAnnotationIntrospector().allIntrospectors().stream().filter(ParameterNamesAnnotationIntrospector.class::isInstance).findFirst().orElseThrow();
        this.mapper.setAnnotationIntrospectors(
            AnnotationIntrospector.pair(sAnnotationIntrospector, new HangarAnnotationIntrospector()),
            this.mapper.getDeserializationConfig().getAnnotationIntrospector()
        );
        // order is important!
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(this.mappingJackson2HttpMessageConverter(this.mapper));
        converters.add(new WebhookMessageConverter(this.mapper));
        this.addDefaultHttpMessageConverters(converters);
    }

    @Override
    protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new RequestMappingHandlerAdapter() {
            @Override
            public void afterPropertiesSet() {
                super.afterPropertiesSet();

                for (final HttpMessageConverter<?> messageConverter : this.getMessageConverters()) {
                    if (messageConverter instanceof StringHttpMessageConverter stringConverter) {
                        stringConverter.setDefaultCharset(StandardCharsets.UTF_8);
                    }
                }

                final List<HandlerMethodArgumentResolver> existingResolvers = new ArrayList<>(Objects.requireNonNull(this.getArgumentResolvers()));
                existingResolvers.addAll(0, WebConfig.this.resolvers);
                this.setArgumentResolvers(existingResolvers);
            }
        };
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(final ObjectMapper mapper) {
        return new MappingJackson2HttpMessageConverter(mapper);
    }

    @Bean
    public RestTemplate restTemplate(final List<HttpMessageConverter<?>> messageConverters, final RestTemplateBuilder builder) {
        // RestTemplateBuilder is immutable, every call has to be reassigned
        RestTemplateBuilder result = builder;
        if (interceptorLogger.isDebugEnabled()) {
            final ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
            result = result
                .requestFactory(() -> factory)
                .interceptors(new LoggingInterceptor());
        }

        result = result.defaultHeader("User-Agent", "Hangar <hangar@papermc.io>");
        result = result.clientSettings((s) -> s.withConnectTimeout(timeout).withReadTimeout(timeout));

        this.addDefaultHttpMessageConverters(messageConverters);
        result = result.messageConverters(messageConverters);

        return result.build();
    }

    @Bean
    public CloseableHttpClient imageProxyHttpClient() {
        // Only used by the image proxy: the resolver blocks internal addresses at connect time (SSRF guard)
        final PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDnsResolver(new SsrfProtectedDnsResolver())
            .setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(timeout))
                .setSocketTimeout(Timeout.of(timeout))
                .build())
            .build();
        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(timeout))
                .setResponseTimeout(Timeout.of(timeout))
                .build())
            // don't follow redirects, the redirect target would bypass the up-front validation
            .disableRedirectHandling()
            // forward the bytes (and Content-Encoding) untouched instead of decompressing in the proxy
            .disableContentCompression()
            .setUserAgent("Hangar <hangar@papermc.io>")
            .build();
    }

    @Bean
    public RestClient restClient(final List<HttpMessageConverter<?>> messageConverters, RestClient.Builder builder) {
        if (interceptorLogger.isDebugEnabled()) {
            builder.requestInterceptor(new LoggingInterceptor());
        }

        builder.defaultHeader("User-Agent", "Hangar <hangar@papermc.io>");

        this.addDefaultHttpMessageConverters(messageConverters);
        builder.messageConverters(s -> s.addAll(messageConverters));

        return builder.build();
    }

    @Bean
    SimpleAsyncTaskSchedulerCustomizer configureSentryTaskDecorator() {
        return (s) -> s.setTaskDecorator(new SentryTaskDecorator());
    }

    static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(final HttpRequest req, final byte [] reqBody, final ClientHttpRequestExecution ex) throws IOException {
            if (interceptorLogger.isDebugEnabled()) {
                interceptorLogger.debug("Request {}, body {}, headers {}", req.getMethod() + " " + req.getURI(), new String(reqBody, StandardCharsets.UTF_8), req.getHeaders());
            }
            final ClientHttpResponse response = ex.execute(req, reqBody);
            if (interceptorLogger.isDebugEnabled()) {
                final int code = response.getStatusCode().value();
                final HttpStatus status = HttpStatus.resolve(code);

                final InputStreamReader isr = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
                final String body = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));

                interceptorLogger.debug("Response {}, body {}, headers {}", (status != null ? status : code), body, response.getHeaders());
            }
            return response;
        }
    }
}
