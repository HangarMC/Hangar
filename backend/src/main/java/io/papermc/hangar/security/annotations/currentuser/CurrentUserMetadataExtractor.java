package io.papermc.hangar.security.annotations.currentuser;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class CurrentUserMetadataExtractor implements AnnotationMetadataExtractor<CurrentUser> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final CurrentUser securityAnnotation) {
        return Set.of(new CurrentUserAttribute(this.expressionParser.parseExpression(securityAnnotation.userArgument())));
    }

    static class CurrentUserAttribute implements ConfigAttribute {

        private final Expression expression;

        public CurrentUserAttribute(final Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return this.expression;
        }

        @Override
        public String getAttribute() {
            return null;
        }
    }
}
