package io.papermc.hangar.security.voters;

import io.papermc.hangar.security.HangarAuthentication;
import io.papermc.hangar.security.UserLockException;
import io.papermc.hangar.security.attributes.UserLockAttribute;
import io.papermc.hangar.service.UserService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserLockVoter implements AccessDecisionVoter {

    private final UserService userService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public UserLockVoter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attribute instanceof UserLockAttribute;
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection collection) {
        if (!(object instanceof MethodInvocation)) return ACCESS_ABSTAIN;
        if (!(authentication instanceof HangarAuthentication) || authentication.getPrincipal().equals("anonymousUser")) {
            return ACCESS_ABSTAIN;
        }
        MethodInvocation methodInvocation = (MethodInvocation) object;
        HangarAuthentication hangarAuth = (HangarAuthentication) authentication;
        Collection<UserLockAttribute> attributes = ((Collection<ConfigAttribute>) collection).stream().filter(this::supports).map(UserLockAttribute.class::cast).collect(Collectors.toSet());
        if (attributes.size() > 1) {
            throw new IllegalStateException("Should have, at most, 1 user lock attribute");
        }
        if (attributes.isEmpty()) {
            return ACCESS_GRANTED;
        }
        UserLockAttribute userLockAttribute = attributes.stream().findAny().get();
        EvaluationContext context = new MethodBasedEvaluationContext(
                methodInvocation.getMethod().getDeclaringClass(),
                methodInvocation.getMethod(),
                methodInvocation.getArguments(),
                parameterNameDiscoverer
        );
        Expression exp = parser.parseExpression(userLockAttribute.getArgs());
        List<String> argList = (List<String>) exp.getValue(context);
        if (argList == null) {
            argList = List.of();
        }
        if (userService.getUsersTable(hangarAuth.getUserId()).isLocked()) {
            throw new UserLockException("error.user.locked", userLockAttribute.getRoute(), argList.toArray(new String[0]));
        }
        return ACCESS_GRANTED;
    }
}
