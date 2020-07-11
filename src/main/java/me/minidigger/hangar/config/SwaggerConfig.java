package me.minidigger.hangar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Hangar API")
                .description(" This page describes the format for the current Hangar REST API, in addition to common questions when using it. " +
                             "Note that anything that starts with `_` should be considered internal, and can change at a moment's notice. Do not use it. " +
                             "## Authentication and authorization There are two ways to consume the API. Keyless, and using an API key. " +
                             "### Keyless When using keyless authentication you only get access to public information, but don't need to worry about creating and storing an API key. " +
                             "### API Keys If you need access to non-public actions, or want to do something programatically, you likely want an API key. " +
                             "These can be created by going to your user page and clicking on the key icon. " +
                             "### Authentication Once you know how you want to authenticate you need to create a session. You can do this by `POST`ing to `/authenticate`. " +
                             "If you're using keyless authentication that's it. If you have an API key, you need to specify it in the Authorization header like so `Authorization: HangarApi apikey=\"foobar\"`. " +
                             "### Authorization Once you do that you should receive an session. This is valid for a pre-defined set of time. When it expires, you need to authenticate again. " +
                             "To use it, set it in the Authorization header like so `Authorization: HangarApi session=\"noisses\"`. For more info about authentication, see [here](#/Authentification/authenticate). " +
                             "## FAQ " +
                             "### Can I just change v1 to v2 and be done with the transition to the new API? " +
                             "No, not at all. The new API is wildly different from the old API. You won't even get out the door. " +
                             "### Why do I need to create a new session when I just want to get some public info? " +
                             "We're working on a session-less authentification for public endpoints. " +
                             "### What format does dates have? " +
                             "Standard ISO types. Where possible we use the OpenAPI format modifier. You can view it's meanings [here](https://swagger.io/docs/specification/data-models/data-types/#format).")
                .license("Unlicence")
                .licenseUrl("http://unlicense.org")
                .termsOfServiceUrl("")
                .version("2.0")
                .contact(new Contact("MiniDigger", "https://minidigger.me", "admin@minidigger.me"))
                .build();
    }

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("me.minidigger.hangar"))
                .build()
                .directModelSubstitute(LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(OffsetDateTime.class, Date.class)
                .apiInfo(apiInfo());
    }

}
