package io.papermc.hangar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.TemplateException;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.extras.converters.ColorHexConverter;
import io.papermc.hangar.controller.extras.converters.OffsetDateTimeConverter;
import io.papermc.hangar.controller.extras.converters.StringToEnumConverterFactory;
import io.papermc.hangar.securityold.UserLockExceptionResolver;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.Filter;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private final HangarConfig hangarConfig;
    private final ObjectMapper mapper;

    @Autowired
    public WebConfig(HangarConfig hangarConfig, ObjectMapper mapper) {
        this.hangarConfig = hangarConfig;
        this.mapper = mapper;
    }

    // TODO remove after freemarker is gone
    @Bean
    public FreeMarkerViewResolver freemarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        return resolver;
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/internal/**").allowedOrigins(hangarConfig.isUseWebpack() ? "http://localhost:3000" : "https://hangar.minidigger.me");
    }

    // TODO remove after freemarker is gone
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
        freeMarkerConfigurer.getConfiguration().setOutputFormat(HTMLOutputFormat.INSTANCE);
        freeMarkerConfigurer.getConfiguration().setObjectWrapper(new Java8ObjectWrapper(freemarker.template.Configuration.getVersion()));
        freeMarkerConfigurer.getConfiguration().setTemplateExceptionHandler((te, env, out) -> {
            String message = te.getMessage();
            if (message.contains("org.springframework.web.servlet.support.RequestContext.getMessage")) {
                System.out.println("[Template Error, most likely missing key] " + message);
                if (te.getCause() != null) {
                    te.getCause().printStackTrace();
                } else {
                    te.printStackTrace();
                }
            } else if (message.contains(" see cause exception in the Java stack trace.")) {
                te.printStackTrace();
            } else {
                System.out.println("[Template Error] " + message);
            }
        });
        return freeMarkerConfigurer;
    }

    // TODO remove after freemarker is gone
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        VersionResourceResolver resolver = new VersionResourceResolver()
                .addContentVersionStrategy("/**");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/public/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(resolver)
                .addTransformer(new CssLinkResourceTransformer());
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
    public void configureMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(mapper));
        super.addDefaultHttpMessageConverters(converters);
    }

    // TODO maybe? remove after freemarker is gone
//    @Bean
//    public ErrorViewResolver errorViewResolver() {
//        return (request, status, model) -> {
//            if (status == HttpStatus.GATEWAY_TIMEOUT || status == HttpStatus.REQUEST_TIMEOUT) {
//                return new ModelAndView("errors/timeout");
//            } else if (status == HttpStatus.NOT_FOUND) {
//                return new ModelAndView("errors/notFound");
//            } else if (status == HttpStatus.FORBIDDEN) {
//                return Routes.USERS_LOGIN.getRedirect("", "", request.getRequestURI());
//            } else {
//                return new ModelAndView("errors/error");
//            }
//        };
//    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
        registry.addConverter(new ColorHexConverter());
        registry.addConverter(new OffsetDateTimeConverter());
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
