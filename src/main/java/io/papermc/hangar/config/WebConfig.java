package io.papermc.hangar.config;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesAnnotationIntrospector;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.config.jackson.HangarAnnotationIntrospector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private final HangarConfig hangarConfig;
    private final ObjectMapper mapper;

    private final List<Converter<?,?>> converters;
    private final List<ConverterFactory<?,?>> converterFactories;
    private final List<HandlerMethodArgumentResolver> resolvers;

    @Autowired
    public WebConfig(HangarConfig hangarConfig, ObjectMapper mapper, List<Converter<?, ?>> converters, List<ConverterFactory<?, ?>> converterFactories, List<HandlerMethodArgumentResolver> resolvers) {
        this.hangarConfig = hangarConfig;
        this.mapper = mapper;
        this.converters = converters;
        this.converterFactories = converterFactories;
        this.resolvers = resolvers;
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/internal/**").allowedOrigins(hangarConfig.isDev() ? "http://localhost:3000" : hangarConfig.getBaseUrl());
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }

    @Bean
    public Filter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
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
        RestTemplate restTemplate = new RestTemplate();
        super.addDefaultHttpMessageConverters(messageConverters);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    @Bean
    public StandardEvaluationContext standardEvaluationContext(ApplicationContext applicationContext) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return evaluationContext;
    }
}
