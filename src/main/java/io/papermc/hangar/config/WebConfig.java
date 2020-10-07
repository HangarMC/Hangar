package io.papermc.hangar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.TemplateException;
import io.papermc.hangar.controller.converters.ColorHexConverter;
import io.papermc.hangar.controller.converters.StringToEnumConverterFactory;
import io.papermc.hangar.security.UserLockExceptionResolver;
import io.papermc.hangar.util.Routes;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private final ObjectMapper mapper;

    public WebConfig(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public FreeMarkerViewResolver freemarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        return resolver;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:templates");
        try {
            freeMarkerConfigurer.afterPropertiesSet();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
        freeMarkerConfigurer.getConfiguration().setOutputEncoding("UTF-8");
        freeMarkerConfigurer.getConfiguration().setLogTemplateExceptions(false);
        freeMarkerConfigurer.getConfiguration().setAPIBuiltinEnabled(true);
        freeMarkerConfigurer.getConfiguration().setObjectWrapper(new Java8ObjectWrapper(freemarker.template.Configuration.getVersion()));
        freeMarkerConfigurer.getConfiguration().setTemplateExceptionHandler((te, env, out) -> {
            String message = te.getMessage();
            if (message.contains("org.springframework.web.servlet.support.RequestContext.getMessage")) {
                System.out.println("[Template Error, most likely missing key] " + message);
                te.getCause().printStackTrace();
            } else if (message.contains(" see cause exception in the Java stack trace.")) {
                te.printStackTrace();
            } else {
                System.out.println("[Template Error] " + message);
            }
        });
        return freeMarkerConfigurer;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/public/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new VersionResourceResolver());
    }

    @Override
    public void configureMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(mapper));
        super.addDefaultHttpMessageConverters(converters);
    }

    @Bean
    public ErrorViewResolver errorViewResolver() {
        return (request, status, model) -> {
            if (status == HttpStatus.GATEWAY_TIMEOUT || status == HttpStatus.REQUEST_TIMEOUT) {
                return new ModelAndView("errors/timeout");
            } else if (status == HttpStatus.NOT_FOUND) {
                return new ModelAndView("errors/notFound");
            } else if (status == HttpStatus.FORBIDDEN) {
                return Routes.USERS_LOGIN.getRedirect("", "", request.getRequestURI());
            } else {
                return new ModelAndView("errors/error");
            }
        };
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
        registry.addConverter(new ColorHexConverter());
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
    public UserLockExceptionResolver userLockExceptionResolver() {
        return new UserLockExceptionResolver();
    }
}
