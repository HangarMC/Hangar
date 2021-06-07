package io.papermc.hangar.config;

import io.papermc.hangar.controller.extras.pagination.FilterRegistry;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterStyle;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Hangar API")
                .description("This page describes the format for the current Hangar REST API, in addition to common questions when using it.<br>" +
                             "Note that all routes **not** listed here should be considered **internal**, and can change at a moment's notice. **Do not use them**." +
                             "<h2>Authentication and Authorization</h2>" +
                             "There are two ways to consume the API: Authenticated or Anonymous." +
                             "<h3>Anonymous</h3>" +
                             "When using anonymous authentication you only get access to public information, but don't need to worry about creating and storing an API key or handing JWTs." +
                             "<h3>Authenticated</h3>" +
                             "If you need access to non-public actions, or want to do something programmatically, you likely want an API key.<br>" +
                             "These can be created by going to your user page and clicking on the key icon.<br>" +
                             "API keys allow you to impersonate yourself, so they should be handled like passwords, **never** share them!" +
                             "<h4>Authentication</h4>" +
                             "Once you have an api key, you need to authenticate yourself. For that you `POST` your API Key to `/api/v1/authenticate?apiKey=yourKey`. The response will contain your JWT.<br>" +
                             "You want to store that JWT and send it with every request. It's valid for a certain amount of time, the token itself contains a field with a timestamp when it will expire. If it expired, you want to reauthenticate yourself." +
                             "<h4>Authorization</h4>" +
                             "Now that you have your JWT, you want to set it in the Authorization header for every request like so `Authorization: HangarAuth your.jwt`. The request will be then executed with the permission scope of your api key." +
                             "<br>" +
                             "While talking about headers. Please also set a useful User-Agent header. This allows us to better identify loads and need for potentially new endpoints." +
                             "<h2>FAQ</h2>" +
                             "<h3>What format do dates have?</h3>" +
                             "Standard ISO types. Where possible, we use the OpenAPI format modifier. You can view its meanings [here](https://swagger.io/docs/specification/data-models/data-types/#format)." +
                             "<h3>Are there rate-limits? What about caching?</h3>" +
                             "There are currently no rate limits. Please don't abuse that fact. If applicable, always cache the responses. The Hangar API itself is cached by CloudFlare and internally.")
                .version("1.0")
                .build();
    }

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.papermc.hangar.controller.api.v1"))
                .build()
                .directModelSubstitute(LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(OffsetDateTime.class, Date.class)
                .apiInfo(apiInfo());
    }

    @Bean
    public CustomScanner customScanner(FilterRegistry filterRegistry) {
        return new CustomScanner(filterRegistry);
    }

    static class CustomScanner implements OperationBuilderPlugin {

        private final FilterRegistry filterRegistry;

        public CustomScanner(FilterRegistry filterRegistry) {
            this.filterRegistry = filterRegistry;
        }

        @Override
        public void apply(OperationContext context) {
            Optional<ApplicableSorters> sorters = context.findAnnotation(ApplicableSorters.class);
            if (sorters.isPresent()) {
                Set<RequestParameter> requestParameters = context.operationBuilder().build().getRequestParameters();
                requestParameters.add(new RequestParameterBuilder()
                        .name("sort")
                        .in(ParameterType.QUERY)
                        .description("Used to sort the result")
                        .query(q -> q.style(ParameterStyle.SIMPLE).model(m -> m.scalarModel(ScalarType.STRING)).enumerationFacet(e -> e.allowedValues(Arrays.asList(sorters.get().value()).stream().map(SorterRegistry::getName).collect(Collectors.toSet())))).build());
                context.operationBuilder().requestParameters(requestParameters);
            }
            Optional<ApplicableFilters> filters = context.findAnnotation(ApplicableFilters.class);
            if (filters.isPresent()) {
                Set<RequestParameter> requestParameters = context.operationBuilder().build().getRequestParameters();

                for (var clazz : filters.get().value()) {
                    var filter = filterRegistry.get(clazz);

                    requestParameters.add(new RequestParameterBuilder()
                            .name(filter.getSingleQueryParam()) // TODO multi-param filters
                            .in(ParameterType.QUERY)
                            .description(filter.getDescription())
                            .query(q -> q.style(ParameterStyle.SIMPLE).model(m -> m.scalarModel(ScalarType.STRING))).build());
                }
                context.operationBuilder().requestParameters(requestParameters);
            }
        }

        @Override
        public boolean supports(DocumentationType documentationType) {
            return true;
        }
    }
}
