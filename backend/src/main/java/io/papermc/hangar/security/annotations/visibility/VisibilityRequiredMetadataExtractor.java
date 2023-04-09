package io.papermc.hangar.security.annotations.visibility;

import java.util.Collection;
import java.util.Set;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

@Component
public class VisibilityRequiredMetadataExtractor implements AnnotationMetadataExtractor<VisibilityRequired> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final VisibilityRequired securityAnnotation) {
        return Set.of(new VisibilityRequiredAttribute(securityAnnotation.type(), this.expressionParser.parseExpression(securityAnnotation.args())));
    }

    record VisibilityRequiredAttribute(VisibilityRequired.Type type, Expression expression) implements ConfigAttribute {

        @Override
        public String getAttribute() {
            return null;
        }
    }
}
