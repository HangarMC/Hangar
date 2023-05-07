package io.papermc.hangar.config;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesAnnotationIntrospector;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.config.jackson.HangarAnnotationIntrospector;
import io.papermc.hangar.security.annotations.ratelimit.RateLimitInterceptor;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private static final Logger interceptorLogger = LoggerFactory.getLogger(LoggingInterceptor.class); // NO-SONAR

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
        if (this.hangarConfig.isDev()) {
            corsRegistration.allowedOrigins("http://localhost:3333");
        } else {
            corsRegistration.allowedOrigins(this.hangarConfig.getBaseUrl());
        }
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
    public void configureMessageConverters(final @NotNull List<HttpMessageConverter<?>> converters) {
        // TODO kinda wack, but idk a better way rn
        final ParameterNamesAnnotationIntrospector sAnnotationIntrospector = (ParameterNamesAnnotationIntrospector) this.mapper.getSerializationConfig().getAnnotationIntrospector().allIntrospectors().stream().filter(ParameterNamesAnnotationIntrospector.class::isInstance).findFirst().orElseThrow();
        this.mapper.setAnnotationIntrospectors(
            AnnotationIntrospector.pair(sAnnotationIntrospector, new HangarAnnotationIntrospector()),
            this.mapper.getDeserializationConfig().getAnnotationIntrospector()
        );
        converters.add(new MappingJackson2HttpMessageConverter(this.mapper));
        this.addDefaultHttpMessageConverters(converters);
    }

    @Override
    protected @NotNull RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new RequestMappingHandlerAdapter() {
            @Override
            public void afterPropertiesSet() {
                super.afterPropertiesSet();
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
        if (interceptorLogger.isDebugEnabled()) {
            final ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
            builder
                .requestFactory(() -> factory)
                .interceptors(new LoggingInterceptor());
        }

        builder.defaultHeader("User-Agent", "Hangar <hangar@papermc.io>");
        builder.setConnectTimeout(timeout);
        builder.setReadTimeout(timeout);

        this.addDefaultHttpMessageConverters(messageConverters);
        builder.messageConverters(messageConverters);

        return builder.build();
    }

    @Bean
    public WebClient webClient(final WebClient.Builder builder) {
        final HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.toMillis())
            .responseTimeout(timeout)
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(timeout.toMillis(), TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(timeout.toMillis(), TimeUnit.MILLISECONDS)));
        builder.clientConnector(new ReactorClientHttpConnector(httpClient));

        final int size = 16 * 1024 * 1024;
        builder.exchangeStrategies(ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
            .build());

        builder.defaultHeader("User-Agent", "Hangar <hangar@papermc.io>");

        return builder.build();
    }

    @Override
    protected void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(this.taskExecutor());
    }

    @Bean
    protected ConcurrentTaskExecutor taskExecutor() {
        return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(10));
    }

    static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(final HttpRequest req, final byte[] reqBody, final ClientHttpRequestExecution ex) throws IOException {
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
