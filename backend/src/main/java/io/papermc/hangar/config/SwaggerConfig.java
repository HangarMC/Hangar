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
                .description("""
                    This page describes the format for the current Hangar REST API as well as general usage guidelines.<br>
                    Note that all routes **not** listed here should be considered **internal**, and can change at a moment's notice. **Do not use them**.

                    <h2>Authentication and Authorization</h2>
                    There are two ways to consume the API: Authenticated or anonymous.

                    <h3>Anonymous</h3>
                    When using anonymous authentication, you only have access to public information, but you don't need to worry about creating and storing an API key or handing JWTs.

                    <h3>Authenticated</h3>
                    If you need access to non-public content or actions, you need to create and use API keys.
                    These can be created by going to the API keys page via the profile dropdown or by going to your user page and clicking on the key icon.<br><br>

                    API keys allow you to impersonate yourself, so they should be handled like passwords. **Do not share them with anyone else!**

                    <h4>Getting and Using a JWT</h4>
                    Once you have an API key, you need to authenticate yourself: Send a `POST` request with your API key identifier (a UUID) to `/api/v1/authenticate?apiKey=yourKey`. The response will contain your JWT as well as an expiration time.
                    Put this JWT into the `Authentication` header of every request and make sure to request a new JWT after the expiration time has passed.<br><br>

                    Please also set a meaningful `User-Agent` header. This allows us to better identify loads and needs for potentially new endpoints.

                    <h2>Misc</h2>
                    <h3>Date Formats</h3>
                    Standard ISO types. Where possible, we use the [OpenAPI format modifier](https://swagger.io/docs/specification/data-models/data-types/#format).

                    <h3>Rate Limits and Caching</h3>
                    The default rate limit is set at 20 requests every 5 seconds with an initial overdraft for extra leniency.
                    Individual endpoints, such as version creation, may have stricter rate limiting.<br><br>

                    If applicable, always cache responses. The Hangar API itself is cached by CloudFlare and internally.""")
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
