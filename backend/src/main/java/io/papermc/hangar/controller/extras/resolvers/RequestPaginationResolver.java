package io.papermc.hangar.controller.extras.resolvers;

import io.papermc.hangar.controller.extras.ApiUtils;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.FilterRegistry;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.controller.extras.pagination.annotations.ConfigurePagination;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.requests.RequestPagination;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class RequestPaginationResolver implements HandlerMethodArgumentResolver {

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final FilterRegistry filterRegistry;
    private final StandardEvaluationContext evaluationContext;

    // need lazy here to avoid circular dep issue
    @Autowired
    public RequestPaginationResolver(@Lazy final FilterRegistry filterRegistry, final StandardEvaluationContext evaluationContext) {
        this.filterRegistry = filterRegistry;
        this.evaluationContext = evaluationContext;
    }

    @Override
    public boolean supportsParameter(final @NotNull MethodParameter parameter) {
        return RequestPagination.class.isAssignableFrom(parameter.getParameterType());
    }

    private RequestPagination create(final @Nullable Long requestOffset, final @Nullable Long requestLimit, final @Nullable ConfigurePagination settings) {
        final long offset = ApiUtils.offsetOrZero(requestOffset);
        if (settings == null) {
            return new RequestPagination(ApiUtils.limitOrDefault(requestLimit), offset);
        }
        final long maxLimit;
        if (settings.maxLimit() != -1) {
            maxLimit = settings.maxLimit();
        } else if (!settings.maxLimitString().isBlank()) {
            final Expression expression = EXPRESSION_PARSER.parseExpression(settings.maxLimitString());
            maxLimit = Objects.requireNonNull(expression.getValue(this.evaluationContext, requestLimit, Long.class), "SpEL must evaluate to a long");
        } else {
            maxLimit = ApiUtils.DEFAULT_MAX_LIMIT;
        }
        final long limit;
        if (requestLimit == null) {
            final long defaultLimit;
            if (settings.defaultLimit() != -1) {
                defaultLimit = settings.defaultLimit();
            } else if (!settings.defaultLimitString().isBlank()) {
                final Expression expression = EXPRESSION_PARSER.parseExpression(settings.defaultLimitString());
                defaultLimit = Objects.requireNonNull(expression.getValue(this.evaluationContext, requestLimit, Long.class), "SpEL must evaluate to a long");
            } else {
                defaultLimit = ApiUtils.DEFAULT_LIMIT;
            }
            limit = defaultLimit;
        } else {
            limit = requestLimit;
        }
        return new RequestPagination(Math.min(maxLimit, limit), offset);

    }

    @Override
    public RequestPagination resolveArgument(final @NotNull MethodParameter parameter, final ModelAndViewContainer mavContainer, final @NotNull NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        final RequestPagination pagination = this.create(
            ApiUtils.mapParameter(webRequest, "offset", this::parseLong),
            ApiUtils.mapParameter(webRequest, "limit", this::parseLong),
            parameter.getParameterAnnotation(ConfigurePagination.class)
        );

        // find filters
        final Set<String> paramNames = new HashSet<>(webRequest.getParameterMap().keySet());
        final Class<? extends Filter<? extends Filter.FilterInstance, ?>>[] applicableFilters = Optional.ofNullable(parameter.getMethodAnnotation(ApplicableFilters.class)).map(ApplicableFilters::value).orElse(null);
        if (applicableFilters != null) {
            for (final Class<? extends Filter<? extends Filter.FilterInstance, ?>> filterClass : applicableFilters) {
                final Filter<? extends Filter.FilterInstance, ?> f = this.filterRegistry.get(filterClass);
                if (f.supports(webRequest)) {
                    // construct key, will be used in cache later
                    String key = f.getSingleQueryParam() + "-";
                    final Object value = f.getValue(webRequest);
                    if (value instanceof String[] array) {
                        key += Arrays.toString(array);
                    } else {
                        key += value.toString();
                    }
                    // then create the filter instance and remove the params from the method invocation
                    pagination.getFilters().put(key, f.create(webRequest));
                    paramNames.removeAll(f.getQueryParamNames());
                }
            }
        }

        // remove known hardcoded params
        paramNames.remove("sort");
        paramNames.remove("limit");
        paramNames.remove("offset");

        // remove request params
        for (final Parameter param : parameter.getExecutable().getParameters()) {
            paramNames.remove(param.getName());
        }

        // if needed, error out here
        if (!paramNames.isEmpty()) {
            throw new HangarApiException(paramNames + " are invalid parameters/filters for this request");
        }

        // find sorters
        final Set<String> applicableSorters = Optional.ofNullable(parameter.getMethodAnnotation(ApplicableSorters.class)).map(ApplicableSorters::value).map(sorters -> Stream.of(sorters).map(SorterRegistry::getName).collect(Collectors.toUnmodifiableSet())).orElse(Collections.emptySet());
        final List<String> presentSorters = Optional.ofNullable(webRequest.getParameterValues("sort")).map(Arrays::asList).orElse(new ArrayList<>());
        for (final String sorter : presentSorters) {
            final String sortKey = sorter.startsWith("-") ? sorter.substring(1) : sorter;
            if (!applicableSorters.contains(sortKey)) {
                throw new HangarApiException(sortKey + " is an invalid sort type for this request");
            }
            pagination.getSorters().put(sorter, sorter.startsWith("-") ? SorterRegistry.getSorter(sortKey).descending() : SorterRegistry.getSorter(sortKey).ascending());
        }

        return pagination;
    }

    private long parseLong(final String s) {
        try {
            return Long.parseLong(s);
        } catch (final NumberFormatException e) {
            throw new HangarApiException(s + " is not a valid long");
        }
    }
}
