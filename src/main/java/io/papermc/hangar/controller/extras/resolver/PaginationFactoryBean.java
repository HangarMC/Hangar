package io.papermc.hangar.controller.extras.resolver;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PaginationFactoryBean implements InitializingBean {

    private final RequestMappingHandlerAdapter adapter;

    public PaginationFactoryBean(RequestMappingHandlerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(Objects.requireNonNull(adapter.getArgumentResolvers()));
        decorateHandlers(argumentResolvers);
        adapter.setArgumentResolvers(argumentResolvers);
    }

    private void decorateHandlers(List<HandlerMethodArgumentResolver> handlers) {
        for (HandlerMethodArgumentResolver handler : handlers) {
            PaginationResolver decorator = new PaginationResolver(handler);
            int index = handlers.indexOf(handler);
            handlers.set(index, decorator);
        }
    }
}
