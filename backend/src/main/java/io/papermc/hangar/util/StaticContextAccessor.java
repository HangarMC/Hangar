package io.papermc.hangar.util;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

// https://stackoverflow.com/questions/12537851/accessing-spring-beans-in-static-method/42796662
@Component
public class StaticContextAccessor {

    private static final Map<Class, DynamicInvocationhandler> classHandlers = new HashMap<>();
    private static ApplicationContext context;

    @Autowired
    public StaticContextAccessor(final ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(final Class<T> clazz) {
        if (context == null) {
            return getProxy(clazz);
        }
        return context.getBean(clazz);
    }

    private static <T> T getProxy(final Class<T> clazz) {
        final DynamicInvocationhandler<T> invocationhandler = new DynamicInvocationhandler<>();
        classHandlers.put(clazz, invocationhandler);
        return (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[]{clazz},
            invocationhandler
        );
    }

    // Use the context to get the actual beans and feed them to the invocationhandlers
    @PostConstruct
    private void init() {
        classHandlers.forEach((clazz, invocationHandler) -> {
            final Object bean = context.getBean(clazz);
            invocationHandler.setActualBean(bean);
        });
    }

    static class DynamicInvocationhandler<T> implements InvocationHandler {

        private T actualBean;

        public void setActualBean(final T actualBean) {
            this.actualBean = actualBean;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (this.actualBean == null) {
                throw new RuntimeException("Not initialized yet! :(");
            }
            return method.invoke(this.actualBean, args);
        }
    }
}
