package io.papermc.hangar.controller.extras.resolver;

import io.papermc.hangar.controller.extras.pagination.filters.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.filters.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.Filter.FilterInstance;
import io.papermc.hangar.controller.extras.pagination.filters.Filters;
import io.papermc.hangar.controller.extras.pagination.sorters.ApplicableSorters;
import io.papermc.hangar.controller.extras.pagination.sorters.Sorters;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.requests.RequestPagination;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PaginationResolver implements HandlerMethodArgumentResolver {

    private final HandlerMethodArgumentResolver delegate;

    public PaginationResolver(HandlerMethodArgumentResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return delegate.supportsParameter(parameter);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object result = delegate.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        if (result instanceof RequestPagination) {
            RequestPagination pagination = (RequestPagination) result;

            Class<? extends Filter<? extends FilterInstance>>[] applicableFilters = Optional.ofNullable(parameter.getMethodAnnotation(ApplicableFilters.class)).map(ApplicableFilters::value).orElse(null);

            if (applicableFilters != null) {
                for (Class<? extends Filter<? extends FilterInstance>> filter : applicableFilters) {
                    if (Filters.getFilter(filter).supports(webRequest)) {
                        pagination.getFilters().add(Filters.getFilter(filter).create(webRequest));
                    }
                }
            }

            List<String> applicableSorters = Optional.ofNullable(parameter.getMethodAnnotation(ApplicableSorters.class)).map(ApplicableSorters::value).map(Arrays::asList).orElse(new ArrayList<>());
            List<String> presentSorters = Optional.ofNullable(webRequest.getParameterValues("sort")).map(Arrays::asList).orElse(new ArrayList<>());
            for (String sorter : presentSorters) {
                String sortKey = sorter.startsWith("-") ? sorter.substring(1) : sorter;
                if (!applicableSorters.contains(sortKey)) {
                    throw new HangarApiException(sortKey + " is an invalid sort type for this request");
                }
                pagination.getSorters().add(sorter.startsWith("-") ? Sorters.getSorter(sortKey).descending() : Sorters.getSorter(sortKey).ascending());
            }

//
//            if (applicableSorters != null) {
//                for (String sorter : applicableSorters) {
//                    if (webRequest.getParameterMap().containsKey(sorter)) {
//
//                    }
//                }
//            }
        }




//        if (result instanceof RequestPagination) {
//            RequestPagination pagination = (RequestPagination) result;
//            Set<String> knownParams = new HashSet<>();
//            knownParams.add("limit");
//            knownParams.add("offset");
//            knownParams.add("sort");
//            for (Parameter param : parameter.getExecutable().getParameters()) {
//                knownParams.add(param.getName());
//            }
//
//            Map<String, String> filters = new HashMap<>();
//            for (String key : webRequest.getParameterMap().keySet()) {
//                if (!knownParams.contains(key)) {
//                    filters.put(key, String.join(",", webRequest.getParameterValues(key)));
//                }
//            }
//            pagination.setFilters(filters);
//
//            String[] sorts = webRequest.getParameterMap().get("sort");
//            List<String> sort = new ArrayList<>();
//            if (sorts != null && sorts.length > 0) {
//                for (String s : sorts) {
//                    if (s != null && s.length() > 0) {
//                        sort.add(s);
//                    }
//                }
//                pagination.setSorts(sort);
//            }
//        }

        return result;
    }
}
