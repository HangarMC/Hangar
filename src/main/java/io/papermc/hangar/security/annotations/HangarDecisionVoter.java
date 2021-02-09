package io.papermc.hangar.security.annotations;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;

import java.util.Collection;

public abstract class HangarDecisionVoter<A extends ConfigAttribute> implements AccessDecisionVoter<MethodInvocation> {

    private final Class<A> attributeClass;
    protected final ParameterNameDiscoverer parameterNameDiscoverer;

    protected HangarDecisionVoter(Class<A> attributeClass) {
        this.attributeClass = attributeClass;
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attributeClass.isAssignableFrom(attribute.getClass());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }


    @SuppressWarnings("unchecked")
    protected final A findAttribute(Collection<ConfigAttribute> attributes) {
        for (ConfigAttribute attribute : attributes) {
            if (attributeClass.isAssignableFrom(attribute.getClass())) {
                return (A) attribute;
            }
        }
        return null;
    }
}
