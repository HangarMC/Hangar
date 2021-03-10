package io.papermc.hangar.security.annotations.visibility;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class VisibilityRequiredMetadataExtractor implements AnnotationMetadataExtractor<VisibilityRequired> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(VisibilityRequired securityAnnotation) {
        return Set.of(new VisibilityRequiredAttribute(securityAnnotation.type(), expressionParser.parseExpression(securityAnnotation.args())));
    }

    static class VisibilityRequiredAttribute implements ConfigAttribute {

        private final VisibilityRequired.Type type;
        private final Expression expression;

        VisibilityRequiredAttribute(VisibilityRequired.Type type, Expression expression) {
            this.type = type;
            this.expression = expression;
        }

        public VisibilityRequired.Type getType() {
            return type;
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public String getAttribute() {
            return null;
        }
    }
}
