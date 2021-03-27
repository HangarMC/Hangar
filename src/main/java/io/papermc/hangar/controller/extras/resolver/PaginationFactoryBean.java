package io.papermc.hangar.controller.extras.resolver;

import io.papermc.hangar.controller.extras.pagination.FilterRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PaginationFactoryBean implements InitializingBean {

    private final RequestMappingHandlerAdapter adapter;
    private final FilterRegistry filterRegistry;

    @Autowired
    public PaginationFactoryBean(RequestMappingHandlerAdapter adapter, FilterRegistry filterRegistry) {
        this.adapter = adapter;
        this.filterRegistry = filterRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(Objects.requireNonNull(adapter.getArgumentResolvers()));
        decorateHandlers(argumentResolvers);
        adapter.setArgumentResolvers(argumentResolvers);
    }

    private void decorateHandlers(List<HandlerMethodArgumentResolver> handlers) {
        for (HandlerMethodArgumentResolver handler : handlers) {
            PaginationResolver decorator = new PaginationResolver(handler, filterRegistry);
            int index = handlers.indexOf(handler);
            handlers.set(index, decorator);
        }
    }
}
