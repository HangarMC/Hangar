package io.papermc.hangar.controller.extras.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.papermc.hangar.model.api.requests.RequestPagination;

public class PaginationResolver implements HandlerMethodArgumentResolver {

    private final HandlerMethodArgumentResolver delegate;

    public PaginationResolver(HandlerMethodArgumentResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return delegate.supportsParameter(parameter);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object result = delegate.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        if (result instanceof RequestPagination) {
            RequestPagination pagination = (RequestPagination) result;
            Set<String> knownParams = new HashSet<>();
            knownParams.add("limit");
            knownParams.add("offset");
            knownParams.add("sort");
            for (Parameter param : parameter.getExecutable().getParameters()) {
                knownParams.add(param.getName());
            }

            Map<String, String> filters = new HashMap<>();
            for (String key : webRequest.getParameterMap().keySet()) {
                if (!knownParams.contains(key)) {
                    filters.put(key, String.join(",", webRequest.getParameterValues(key)));
                }
            }
            pagination.setFilters(filters);

            String[] sorts = webRequest.getParameterMap().get("sort");
            List<String> sort = new ArrayList<>();
            if (sorts != null && sorts.length > 0) {
                for (String s : sorts) {
                    if (s != null && s.length() > 0) {
                        sort.add(s);
                    }
                }
                pagination.setSorts(sort);
            }
        }

        return result;
    }
}
