package io.papermc.hangar.config;

import io.papermc.hangar.controller.extras.pagination.FilterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SwaggerConfig {

    static {
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = true;
    }

    @Bean
    OpenApiCustomizer apiInfo() {
        return (openApi) -> openApi
            .info(new Info().title("Hangar API")
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
                .version("1.0"));
    }

    // TODO fix
    //.directModelSubstitute(LocalDate.class, java.sql.Date.class)
    //.directModelSubstitute(OffsetDateTime.class, Date.class)

    @Bean
    public CustomScanner customScanner(final FilterRegistry filterRegistry) {
        return new CustomScanner(filterRegistry);
    }

    static class CustomScanner implements OperationCustomizer {

        private final FilterRegistry filterRegistry;

        public CustomScanner(final FilterRegistry filterRegistry) {
            this.filterRegistry = filterRegistry;
        }


        @Override
        public Operation customize(final Operation operation, final HandlerMethod handlerMethod) {
            final ApplicableSorters sorters = handlerMethod.getMethodAnnotation(ApplicableSorters.class);
            if (sorters != null) {
                operation.addParametersItem(new Parameter()
                    .name("sort")
                    .in("query")
                    .description("Used to sort the result"));
                // TODO fix
                //.query(q -> q.style(ParameterStyle.SIMPLE).model(m -> m.scalarModel(ScalarType.STRING)).enumerationFacet(e -> e.allowedValues(Arrays.asList(sorters.value()).stream().map(SorterRegistry::getName).collect(Collectors.toSet())))).build());
            }
            final ApplicableFilters filters = handlerMethod.getMethodAnnotation(ApplicableFilters.class);
            if (filters != null) {
                for (final var clazz : filters.value()) {
                    final var filter = this.filterRegistry.get(clazz);

                    operation.addParametersItem(new Parameter()
                        .name(filter.getSingleQueryParam()) // TODO multi-param filters
                        .in("query")
                        .description(filter.getDescription()));
                    // TODO fix
                    // .query(q -> q.style(ParameterStyle.SIMPLE).model(m -> m.scalarModel(ScalarType.STRING))).build());
                }
            }

            return operation;
        }
    }
}
