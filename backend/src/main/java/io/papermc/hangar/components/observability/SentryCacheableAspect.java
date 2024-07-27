package io.papermc.hangar.components.observability;

import io.micrometer.common.lang.Nullable;
import io.sentry.ISpan;
import io.sentry.Sentry;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;

@Aspect
@Order(0)
public class SentryCacheableAspect {

    @Around("execution (@org.springframework.cache.annotation.Cacheable * *.*(..))")
    @Nullable
    public Object observeMethod(final ProceedingJoinPoint pjp) throws Throwable {
        final Method method = this.getMethod(pjp);
        final Cacheable observed = method.getAnnotation(Cacheable.class);
        return this.observe(pjp, method, observed);
    }

    private Object observe(final ProceedingJoinPoint pjp, final Method method, final Cacheable cacheable) throws Throwable {
        final ISpan parentSpan = Sentry.getSpan();
        if (parentSpan == null) {
            return pjp.proceed();
        } else {
            ISpan childSpan = parentSpan.startChild("cacheable", getName(cacheable, method));
            childSpan.setData("cache.name",Arrays.toString(cacheable.value()));
            childSpan.setData("cache.key",  Arrays.toString(pjp.getArgs()));
            if (CompletionStage.class.isAssignableFrom(method.getReturnType())) {
                try {
                return ((CompletionStage<?>) pjp.proceed())
                    .whenComplete((_, _) -> childSpan.finish());
                } finally {
                    childSpan.finish();
                }
            } else {
                try {
                    return pjp.proceed();
                } finally {
                    childSpan.finish();
                }
            }
        }
    }

    private String getName(final Cacheable cacheable, final Method method) {
        final String name;
        if (cacheable.value().length == 0) {
            name = "";
        } else if (cacheable.value().length == 1) {
            name = cacheable.value()[0];
        } else {
            name = Arrays.toString(cacheable.value());
        }
        return name + " " + method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    private Method getMethod(final ProceedingJoinPoint pjp) throws NoSuchMethodException {
        final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        if (method.getAnnotation(Cacheable.class) == null) {
            return pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
        }

        return method;
    }
}
