package io.papermc.hangar.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.papermc.hangar.controller.extras.pagination.FilterRegistry;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.MethodArgumentNotValidExceptionSerializer;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import io.papermc.hangar.model.db.Table;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.OperationService;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;

import static org.springdoc.core.utils.Constants.API_DOCS_URL;
import static org.springdoc.core.utils.Constants.APPLICATION_OPENAPI_YAML;
import static org.springdoc.core.utils.Constants.DEFAULT_API_DOCS_URL_YAML;

@Configuration
public class SwaggerConfig {

    static {
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = true;
    }

    @Bean
    public GroupedOpenApi publicOpenApi(CustomScanner customScanner) {
        return GroupedOpenApi.builder().group("public").pathsToMatch("/api/v1/**").addOpenApiCustomizer((openApi -> {
            openApi.info(new Info().title("Hangar API").version("1.0").description("""
                This page describes the format for the current Hangar REST API as well as general usage guidelines.<br>
                Note that all routes **not** listed here should be considered **internal**, and can change at a moment's notice. **Do not use them**.

                ## Authentication and Authorization
                There are two ways to consume the API: Authenticated or anonymous.

                ### Anonymous
                When using anonymous authentication, you only have access to public information, but you don't need to worry about creating and storing an API key or handing JWTs.

                ### Authenticated
                If you need access to non-public content or actions, you need to create and use API keys.
                These can be created by going to the API keys page via the profile dropdown or by going to your user page and clicking on the key icon.

                API keys allow you to impersonate yourself, so they should be handled like passwords. **Do not share them with anyone else!**

                #### Getting and Using a JWT
                Once you have an API key, you need to authenticate yourself: Send a `POST` request with your API key identifier to `/api/v1/authenticate?apiKey=yourKey`.
                The response will contain your JWT as well as an expiration time.
                Put this JWT into the `Authorization` header of every request and make sure to request a new JWT after the expiration time has passed.

                Please also set a meaningful `User-Agent` header. This allows us to better identify loads and needs for potentially new endpoints.

                ## Misc
                ### Date Formats
                Standard ISO types. Where possible, we use the [OpenAPI format modifier](https://swagger.io/docs/specification/data-models/data-types/#format).

                ### Rate Limits and Caching
                The default rate limit is set at 20 requests every 5 seconds with an initial overdraft for extra leniency.
                Individual endpoints, such as version creation, may have stricter rate limiting.

                If applicable, always cache responses. The Hangar API itself is cached by CloudFlare and internally."""));
            openApi.getComponents().addSecuritySchemes("HangarAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
        }))
        .addOperationCustomizer(customScanner)
        .build();
    }

    @Bean
    public GroupedOpenApi internalOpenApi(CustomScanner customScanner) {
        return GroupedOpenApi.builder().group("internal").pathsToMatch("/api/internal/**").addOperationCustomizer(customScanner).build();
    }

    @Bean
    public GroupedOpenApi combinedOpenApi(CustomScanner customScanner) {
        return GroupedOpenApi.builder().group("combined").pathsToMatch("/api/**")
            .addOpenApiCustomizer(this.requiredByDefaultCustomizer())
            .addOpenApiCustomizer(this.exceptionCustomizer())
            .addOperationCustomizer(customScanner)
            .build();
    }

    public OpenApiCustomizer requiredByDefaultCustomizer() {
        return openAPI -> {
            for (final Schema<?> schema : openAPI.getComponents().getSchemas().values()) {
                if (schema.getProperties() != null) {
                    try {
                        final Class<?> clazz = Class.forName(schema.getName());
                        final Set<String> requiredKeys = new HashSet<>();
                        for (final String key : schema.getProperties().keySet()) {
                            final Field field = ReflectionUtils.findField(clazz, key);

                            if (field != null && (field.getAnnotatedType().isAnnotationPresent(Nullable.class) || field.getAnnotatedType().isAnnotationPresent(jakarta.annotation.Nullable.class))) {
                                continue;
                            }

                            Method method = null;
                            for (final String prefix : List.of("get", "is", "has", "")) {
                                 method = ReflectionUtils.findMethod(clazz, !prefix.isEmpty() ? prefix + key.substring(0, 1).toUpperCase() + key.substring(1) : key );
                                 if (method != null) break;
                            }

                            if (method != null && (method.getAnnotatedReturnType().isAnnotationPresent(Nullable.class) || method.getAnnotatedReturnType().isAnnotationPresent(jakarta.annotation.Nullable.class))) {
                                continue;
                            }

                            requiredKeys.add(key);
                        }
                        schema.required(new ArrayList<>(requiredKeys));
                    } catch (final ClassNotFoundException e) {
                        schema.required(new ArrayList<>(schema.getProperties().keySet()));
                    }
                }
            }
        };
    }

    public OpenApiCustomizer exceptionCustomizer() {
        return openAPI -> {
            for (final Class<?> clazz : List.of(HangarApiException.class, MultiHangarApiException.class, MethodArgumentNotValidExceptionSerializer.HangarValidationException.class)) {
                ModelConverters.getInstance().readAllAsResolvedSchema(clazz).referencedSchemas.forEach(openAPI.getComponents()::addSchemas);
            }
        };
    }

    @Bean
    public CustomScanner customScanner(final FilterRegistry filterRegistry) {
        return new CustomScanner(filterRegistry);
    }

    @Component
    public static class SlugOrIdCustomizer implements ParameterCustomizer {
        @Override
        public Parameter customize(final Parameter parameter, final MethodParameter methodParameter) {
            if (Table.class.isAssignableFrom(methodParameter.getParameterType())) {
                return parameter.schema(new StringSchema());
            }
            return parameter;
        }
    }

    public static class CustomScanner implements OperationCustomizer {

        private final FilterRegistry filterRegistry;

        public CustomScanner(final FilterRegistry filterRegistry) {
            this.filterRegistry = filterRegistry;
        }


        @Override
        public Operation customize(final Operation operation, final HandlerMethod handlerMethod) {
            final ApplicableSorters sorters = handlerMethod.getMethodAnnotation(ApplicableSorters.class);
            if (sorters != null) {
                final StringSchema allowedValues = new StringSchema();
                for (final SorterRegistry sorterRegistry : sorters.value()) {
                    allowedValues.addEnumItem(sorterRegistry.getName());
                }
                operation.addParametersItem(new Parameter()
                    .name("sort")
                    .in("query")
                    .description("Used to sort the result")
                    .style(Parameter.StyleEnum.FORM)
                    .schema(allowedValues)
                );
            } else if (operation.getParameters() != null && operation.getParameters().stream().anyMatch(p -> p.getName().equals("sort"))) {
                throw new RuntimeException("potential swagger issue found, sort param but no @ApplicableSorters annotation?: " + operation.getOperationId());
            }

            final ApplicableFilters filters = handlerMethod.getMethodAnnotation(ApplicableFilters.class);
            if (filters != null) {
                for (final var clazz : filters.value()) {
                    final var filter = this.filterRegistry.get(clazz);

                    for (final String queryParamName : filter.getQueryParamNames()) {
                        final Parameter param = new Parameter()
                            .name(queryParamName)
                            .in("query")
                            .description(filter.getDescription())
                            .style(Parameter.StyleEnum.FORM)
                            .schema(new StringSchema());

                        final String deprecationNote = filter.getDeprecatedQueryParamNames().get(queryParamName);
                        if (deprecationNote != null) {
                            param.deprecated(true).description("Deprecated: " + deprecationNote);
                        }

                        operation.addParametersItem(param);
                    }
                }
            }

            return operation;
        }
    }

    // override /v3/api-docs to return /v3/api-docs/public
    @Bean
    @Primary
    OpenApiWebMvcResource openApiResource(final ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory, final AbstractRequestService requestBuilder,
                                          final GenericResponseService responseBuilder, final OperationService operationParser,
                                          final SpringDocConfigProperties springDocConfigProperties,
                                          final SpringDocProviders springDocProviders, final SpringDocCustomizers springDocCustomizers, final List<GroupedOpenApi> groupedOpenApis) {

        final OpenApiWebMvcResource publicApi = groupedOpenApis.stream()
            .filter(s -> s.getGroup().equals("public"))
            .findFirst()
            .map(groupedOpenApi -> new OpenApiWebMvcResource(groupedOpenApi.getGroup(),
                openAPIBuilderObjectFactory,
                requestBuilder,
                responseBuilder,
                operationParser,
                springDocConfigProperties, springDocProviders,
                new SpringDocCustomizers(Optional.of(groupedOpenApi.getOpenApiCustomizers()), Optional.of(groupedOpenApi.getOperationCustomizers()),
                    Optional.of(groupedOpenApi.getRouterOperationCustomizers()), Optional.of(groupedOpenApi.getOpenApiMethodFilters()))

            )).orElseThrow(RuntimeException::new);

        return new OpenApiWebMvcResource(openAPIBuilderObjectFactory, requestBuilder,
            responseBuilder, operationParser, springDocConfigProperties, springDocProviders, springDocCustomizers) {
            @io.swagger.v3.oas.annotations.Operation(hidden = true)
            @GetMapping(value = API_DOCS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
            @Override
            public byte[] openapiJson(final HttpServletRequest request, @Value(API_DOCS_URL) final String apiDocsUrl, final Locale locale) throws JsonProcessingException {
                return publicApi.openapiJson(request, apiDocsUrl, locale);
            }

            @io.swagger.v3.oas.annotations.Operation(hidden = true)
            @GetMapping(value = DEFAULT_API_DOCS_URL_YAML, produces = APPLICATION_OPENAPI_YAML)
            @Override
            public byte[] openapiYaml(final HttpServletRequest request, @Value(DEFAULT_API_DOCS_URL_YAML) final String apiDocsUrl, final Locale locale) throws JsonProcessingException {
                return publicApi.openapiYaml(request, apiDocsUrl, locale);
            }
        };
    }
}
