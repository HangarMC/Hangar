package io.papermc.hangar.security.annotations;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
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

/**
 * Must override one of
 * <ul>
 *    <li>{@link #vote(Authentication, MethodInvocation, ConfigAttribute)}</li>
 *    <li>{@link #vote(Authentication, MethodInvocation, Set)}</li>
 * </ul>
 *
 * @param <A>
 */
public abstract class HangarDecisionVoter<A extends ConfigAttribute> implements AccessDecisionVoter<MethodInvocation> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<A> attributeClass;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private boolean allowMultipleAttributes = false;

    protected HangarDecisionVoter(final Class<A> attributeClass) {
        this.attributeClass = attributeClass;
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    public boolean isAllowMultipleAttributes() {
        return this.allowMultipleAttributes;
    }

    public void setAllowMultipleAttributes(final boolean allowMultipleAttributes) {
        this.allowMultipleAttributes = allowMultipleAttributes;
    }

    @Override
    public boolean supports(final ConfigAttribute attribute) {
        return this.attributeClass.isAssignableFrom(attribute.getClass());
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public final int vote(final Authentication authentication, final MethodInvocation methodInvocation, final Collection<ConfigAttribute> configAttributes) {
        final Set<A> attributes = this.findAttributes(configAttributes);
        if (attributes.isEmpty()) {
            return ACCESS_ABSTAIN;
        }
        if (!this.allowMultipleAttributes && attributes.size() > 1) {
            throw new IllegalArgumentException("Multiple " + this.attributeClass + " found where only 1 is allowed (or use setAllowMultipleAttributes(true))");
        } else if (this.allowMultipleAttributes) {
            return this.vote(authentication, methodInvocation, attributes);
        } else {
            return this.vote(authentication, methodInvocation, attributes.stream().findFirst().get());
        }
    }

    public int vote(final Authentication authentication, final MethodInvocation methodInvocation, final @NotNull A attribute) {
        return ACCESS_ABSTAIN;
    }

    public int vote(final Authentication authentication, final MethodInvocation methodInvocation, final Set<A> attributes) {
        return ACCESS_ABSTAIN;
    }

    protected final Set<A> findAttributes(final Collection<ConfigAttribute> attributes) {
        return attributes.stream().filter(a -> this.attributeClass.isAssignableFrom(a.getClass())).map(this.attributeClass::cast).collect(Collectors.toSet());
    }

    protected final @NotNull MethodBasedEvaluationContext getMethodEvaluationContext(final MethodInvocation invocation) {
        return new MethodBasedEvaluationContext(
            invocation.getMethod().getDeclaringClass(),
            invocation.getMethod(),
            invocation.getArguments(),
            this.parameterNameDiscoverer
        );
    }

    public void onAccessDenied() {
    }
}
