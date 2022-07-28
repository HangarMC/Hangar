package io.papermc.hangar.config;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesAnnotationIntrospector;

import io.papermc.hangar.security.annotations.ratelimit.RateLimitInterceptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.config.jackson.HangarAnnotationIntrospector;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private static Logger interceptorLogger = LoggerFactory.getLogger(LoggingInterceptor.class); // NO-SONAR

    private final HangarConfig hangarConfig;
    private final ObjectMapper mapper;
    private final RateLimitInterceptor rateLimitInterceptor;

    private final List<Converter<?,?>> converters;
    private final List<ConverterFactory<?,?>> converterFactories;
    private final List<HandlerMethodArgumentResolver> resolvers;

    @Autowired
    public WebConfig(HangarConfig hangarConfig, ObjectMapper mapper, RateLimitInterceptor rateLimitInterceptor, List<Converter<?, ?>> converters, List<ConverterFactory<?, ?>> converterFactories, List<HandlerMethodArgumentResolver> resolvers) {
        this.hangarConfig = hangarConfig;
        this.mapper = mapper;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.converters = converters;
        this.converterFactories = converterFactories;
        this.resolvers = resolvers;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        CorsRegistration corsRegistration = registry.addMapping("/api/internal/**");
        if (hangarConfig.isDev()) {
            corsRegistration.allowedOrigins("http://localhost:3333");
        } else {
            corsRegistration.allowedOrigins(hangarConfig.getBaseUrl());
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
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                response.setHeader("Server", "Hangar");
                filterChain.doFilter(request, response);
            }
        };
    }

    @Override
    protected void addFormatters(FormatterRegistry registry) {
        converters.forEach(registry::addConverter);
        converterFactories.forEach(registry::addConverterFactory);
    }

    @Override
    public void configureMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        // TODO kinda wack, but idk a better way rn
        ParameterNamesAnnotationIntrospector sAnnotationIntrospector = (ParameterNamesAnnotationIntrospector) mapper.getSerializationConfig().getAnnotationIntrospector().allIntrospectors().stream().filter(ParameterNamesAnnotationIntrospector.class::isInstance).findFirst().get();
        mapper.setAnnotationIntrospectors(
                AnnotationIntrospector.pair(sAnnotationIntrospector, new HangarAnnotationIntrospector()),
                mapper.getDeserializationConfig().getAnnotationIntrospector()
        );
        converters.add(new MappingJackson2HttpMessageConverter(mapper));
        super.addDefaultHttpMessageConverters(converters);
    }

    @NotNull
    @Override
    protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new RequestMappingHandlerAdapter() {
            @Override
            public void afterPropertiesSet() {
                super.afterPropertiesSet();
                List<HandlerMethodArgumentResolver> existingResolvers = new ArrayList<>(Objects.requireNonNull(getArgumentResolvers()));
                existingResolvers.addAll(0, resolvers);
                this.setArgumentResolvers(existingResolvers);
            }
        };
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper mapper) {
        return new MappingJackson2HttpMessageConverter(mapper);
    }

    @Bean
    public RestTemplate restTemplate(List<HttpMessageConverter<?>> messageConverters) {
        RestTemplate restTemplate;
        if (interceptorLogger.isDebugEnabled()) {
            ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
            restTemplate = new RestTemplate(factory);
            restTemplate.setInterceptors(List.of(new LoggingInterceptor()));
        } else {
            restTemplate = new RestTemplate();
        }
        super.addDefaultHttpMessageConverters(messageConverters);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {
            if (interceptorLogger.isDebugEnabled()) {
                interceptorLogger.debug("Request {}, body {}, headers {}", req.getMethod() + " " + req.getURI(), new String(reqBody, StandardCharsets.UTF_8), req.getHeaders());
            }
            ClientHttpResponse response = ex.execute(req, reqBody);
            if (interceptorLogger.isDebugEnabled()) {
                int code = response.getRawStatusCode();
                HttpStatus status = HttpStatus.resolve(code);

                InputStreamReader isr = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
                String body = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));

                interceptorLogger.debug("Response {}, body {}, headers {}", (status != null ? status : code), body, response.getHeaders());
            }
            return response;
        }
    }

    @Bean
    public StandardEvaluationContext standardEvaluationContext(ApplicationContext applicationContext) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return evaluationContext;
    }
}
