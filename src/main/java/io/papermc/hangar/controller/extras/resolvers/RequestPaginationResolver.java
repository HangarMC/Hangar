package io.papermc.hangar.controller.extras.resolvers;

import io.papermc.hangar.controller.extras.ApiUtils;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.Filter.FilterInstance;
import io.papermc.hangar.controller.extras.pagination.FilterRegistry;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.controller.extras.pagination.annotations.ConfigurePagination;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.requests.RequestPagination;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RequestPaginationResolver implements HandlerMethodArgumentResolver {

    private final FilterRegistry filterRegistry;

    // need lazy here to avoid circular dep issue
    @Autowired
    public RequestPaginationResolver(@Lazy FilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return RequestPagination.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public RequestPagination resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        long offset = ApiUtils.offsetOrZero(Optional.ofNullable(webRequest.getParameter("offset")).map(Long::parseLong).orElse(null));
        long limit;
        Optional<Long> limitParam = Optional.ofNullable(webRequest.getParameter("limit")).map(Long::parseLong);
        ConfigurePagination settings = parameter.getParameterAnnotation(ConfigurePagination.class);
        if (settings != null) {
            limit = ApiUtils.limitOrDefault(limitParam.orElse(null), settings.maxLimit());
        } else {
            limit = ApiUtils.limitOrDefault(limitParam.orElse(null));
        }
        RequestPagination pagination = new RequestPagination(limit, offset);

        // find filters
        Set<String> paramNames = new HashSet<>(webRequest.getParameterMap().keySet());
        Class<? extends Filter<? extends FilterInstance>>[] applicableFilters = Optional.ofNullable(parameter.getMethodAnnotation(ApplicableFilters.class)).map(ApplicableFilters::value).orElse(null);
        if (applicableFilters != null) {
            for (Class<? extends Filter<? extends FilterInstance>> filter : applicableFilters) {
                Filter<? extends FilterInstance> f = filterRegistry.get(filter);
                if (f.supports(webRequest)) {
                    pagination.getFilters().add(f.create(webRequest));
                    paramNames.removeAll(f.getQueryParamNames());
                }
            }
        }

        // remove known hardcoded params
        paramNames.remove("sort");
        paramNames.remove("limit");
        paramNames.remove("offset");
        // TODO remove these bellow eventually
        paramNames.remove("relevance");

        paramNames.remove("projectSort");

        // remove request params
        for (Parameter param : parameter.getExecutable().getParameters()) {
            paramNames.remove(param.getName());
        }

        // if needed, error out here
        if (!paramNames.isEmpty()) {
            throw new HangarApiException(paramNames + " are invalid parameters/filters for this request");
        }

        // find sorters
        Set<String> applicableSorters = Optional.ofNullable(parameter.getMethodAnnotation(ApplicableSorters.class)).map(ApplicableSorters::value).map(sorters -> Stream.of(sorters).map(SorterRegistry::getName).collect(Collectors.toUnmodifiableSet())).orElse(Collections.emptySet());
        List<String> presentSorters = Optional.ofNullable(webRequest.getParameterValues("sort")).map(Arrays::asList).orElse(new ArrayList<>());
        for (String sorter : presentSorters) {
            String sortKey = sorter.startsWith("-") ? sorter.substring(1) : sorter;
            if (!applicableSorters.contains(sortKey)) {
                throw new HangarApiException(sortKey + " is an invalid sort type for this request");
            }
            pagination.getSorters().put(sorter, sorter.startsWith("-") ? SorterRegistry.getSorter(sortKey).descending() : SorterRegistry.getSorter(sortKey).ascending());
        }

        pagination.setProjectSortBy(webRequest.getParameter("projectSort"));

        return pagination;
    }
}
