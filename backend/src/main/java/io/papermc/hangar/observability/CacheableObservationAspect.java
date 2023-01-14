package io.papermc.hangar.observability;

import io.micrometer.common.lang.Nullable;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

@Aspect
@Order(0)
public class CacheableObservationAspect {

    private final ObservationRegistry registry;

    public CacheableObservationAspect(final ObservationRegistry registry) {
        this.registry = registry;
    }

    @Around("execution (@org.springframework.cache.annotation.Cacheable * *.*(..))")
    @Nullable
    public Object observeMethod(final ProceedingJoinPoint pjp) throws Throwable {
        final Method method = this.getMethod(pjp);
        final Cacheable observed = method.getAnnotation(Cacheable.class);
        return this.observe(pjp, method, observed);
    }

    private Object observe(final ProceedingJoinPoint pjp, final Method method, final Cacheable cacheable) throws Throwable {
        final Observation observation = Observation.createNotStarted(this.getName(cacheable, method), this.registry);
        observation.lowCardinalityKeyValue("hangar.cache.key", Arrays.toString(pjp.getArgs()));
        observation.highCardinalityKeyValue("hangar.cache.name", Arrays.toString(cacheable.value()));
        observation.highCardinalityKeyValue("hangar.type", "CACHE");
        if (CompletionStage.class.isAssignableFrom(method.getReturnType())) {
            observation.start();
            final Observation.Scope scope = observation.openScope();
            try {
                return ((CompletionStage<?>) pjp.proceed())
                    .whenComplete((result, error) -> this.stopObservation(observation, scope, error));
            } catch (final Throwable error) {
                this.stopObservation(observation, scope, error);
                throw error;
            } finally {
                scope.close();
            }
        } else {
            return observation.observeChecked(() -> pjp.proceed());
        }
    }

    private String getName(final Cacheable cacheable, final Method method) {
        final String name;
        if (cacheable.value().length == 0) {
            name = "";
        } else if (cacheable.value().length == 1) {
            name = "(" + cacheable.value()[0] + ")";
        } else {
            name = Arrays.toString(cacheable.value());
        }
        return "cacheable" + name + "-" + method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    private Method getMethod(final ProceedingJoinPoint pjp) throws NoSuchMethodException {
        final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        if (method.getAnnotation(Cacheable.class) == null) {
            return pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
        }

        return method;
    }

    private void stopObservation(final Observation observation, final Observation.Scope scope, @Nullable final Throwable error) {
        if (error != null) {
            observation.error(error);
        }
        scope.close();
        observation.stop();
    }
}
