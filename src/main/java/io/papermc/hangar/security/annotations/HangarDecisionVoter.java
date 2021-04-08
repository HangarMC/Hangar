package io.papermc.hangar.security.annotations;

import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Must override one of
 * <ul>
 *    <li>{@link #vote(Authentication, MethodInvocation, ConfigAttribute)}</li>
 *    <li>{@link #vote(Authentication, MethodInvocation, Set)}</li>
 * </ul>
 * @param <A>
 */
public abstract class HangarDecisionVoter<A extends ConfigAttribute> implements AccessDecisionVoter<MethodInvocation> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<A> attributeClass;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private boolean allowMultipleAttributes = false;

    protected HangarDecisionVoter(Class<A> attributeClass) {
        this.attributeClass = attributeClass;
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    public boolean isAllowMultipleAttributes() {
        return allowMultipleAttributes;
    }

    public void setAllowMultipleAttributes(boolean allowMultipleAttributes) {
        this.allowMultipleAttributes = allowMultipleAttributes;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attributeClass.isAssignableFrom(attribute.getClass());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public final int vote(Authentication authentication, MethodInvocation methodInvocation, Collection<ConfigAttribute> configAttributes) {
        Set<A> attributes = findAttributes(configAttributes);
        if (attributes.isEmpty()) {
            return ACCESS_ABSTAIN;
        }
        if (!allowMultipleAttributes && attributes.size() > 1) {
            throw new IllegalArgumentException("Multiple " + attributeClass + " found where only 1 is allowed (or use setAllowMultipleAttributes(true))");
        } else if (allowMultipleAttributes) {
            return vote(authentication, methodInvocation, attributes);
        } else {
            return vote(authentication, methodInvocation, attributes.stream().findFirst().get());
        }
    }

    public int vote(Authentication authentication, MethodInvocation methodInvocation, @NotNull A attribute) {
        return ACCESS_ABSTAIN;
    }

    public int vote(Authentication authentication, MethodInvocation methodInvocation, Set<A> attributes) {
        return ACCESS_ABSTAIN;
    }

    protected final Set<A> findAttributes(Collection<ConfigAttribute> attributes) {
        return attributes.stream().filter(a -> attributeClass.isAssignableFrom(a.getClass())).map(attributeClass::cast).collect(Collectors.toSet());
    }

    @NotNull
    protected final MethodBasedEvaluationContext getMethodEvaluationContext(MethodInvocation invocation) {
        return new MethodBasedEvaluationContext(
                invocation.getMethod().getDeclaringClass(),
                invocation.getMethod(),
                invocation.getArguments(),
                parameterNameDiscoverer
        );
    }
}
